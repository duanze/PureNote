package com.duanze.gasst.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.duanze.gasst.MainActivity;
import com.duanze.gasst.MyPickerListener;
import com.duanze.gasst.R;
import com.duanze.gasst.activity.NoteActivity;
import com.duanze.gasst.adapter.NoteAdapter;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.util.CallBackListener;
import com.duanze.gasst.util.Util;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;
import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.List;

/**
 * Created by duanze on 2015/4/23.
 */
public class ClassicList extends Fragment {
    public static final String TAG = "ClassicList";

    //MODE_LIST相关
    private NoteAdapter adapter;
    private SwipeMenuListView noteTitleListView;
    private SwipeMenuCreator creator;
    private List<GNote> gNoteList;
    private Context mContext;
    //nice FloatingButton
    private FloatingActionButton fabButton;
    private Calendar today;
    private GNote gNote;
    private GNoteDB db;

    public static final String DATEPICKER_TAG = "datepicker";
    private DatePickerDialog pickerDialog;
    private CallBackListener listener = new CallBackListener() {
        @Override
        public void onFinish(String result) {
            gNote.setAlertTime(result);
            gNote.setPassed(GNote.FALSE);
            db.updateGNote(gNote);
            AlarmService.alarmTask(mContext);

            refreshUI();
        }

        @Override
        public void onError(Exception e) {

        }
    };

    public void refreshUI() {
        gNoteList.clear();
        List<GNote> tmpList = db.loadGNotes();
        for (GNote g : tmpList) {
            gNoteList.add(g);
        }

        adapter.setValues((MainActivity) mContext);
        adapter.notifyDataSetChanged();
        noteTitleListView.setSelection(0);
    }

    private void initValues() {
        mContext = getActivity();
        today = Calendar.getInstance();
        db = GNoteDB.getInstance(mContext);
        gNoteList = db.loadGNotes();

        pickerDialog = DatePickerDialog.newInstance(new MyPickerListener(mContext, today, listener),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH), false);
        pickerDialog.setYearRange(today.get(Calendar.YEAR) - 10,
                today.get(Calendar.YEAR) + 10);
        pickerDialog.setCloseOnSingleTapDay(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initValues();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        View view = inflater.inflate(R.layout.classic_list, container, false);
        noteTitleListView = (SwipeMenuListView) view.findViewById(R.id.swipe_lv);
        adapter = new NoteAdapter(mContext, R.layout.list_item, gNoteList);
        adapter.setValues((MainActivity) mContext);
        noteTitleListView.setAdapter(adapter);
        noteTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GNote gNote = gNoteList.get(i);
                NoteActivity.activityStart(
                        mContext, gNote, NoteActivity.MODE_EDIT);
            }
        });

        buildCreator();
        // set creator
        noteTitleListView.setMenuCreator(creator);
        //listener item click event
        noteTitleListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // remind
                        gNote = gNoteList.get(position);
                        pickerDialog.show(((MainActivity) mContext).getSupportFragmentManager(),
                                DATEPICKER_TAG);
                        break;
                    case 1:
                        // delete
                        gNote = gNoteList.get(position);
//                        db.deleteGNote(gNoteList.get(position).getId());
                        deleteNote();
                        refreshUI();
                        if (!gNote.isPassed()) {
                            AlarmService.cancelTask(mContext, gNote);
                        }
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        fabButton = (FloatingActionButton) view.findViewById(R.id.fabbutton);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteActivity.writeNewNote(mContext,today);
            }
        });
        fabButton.listenTo(noteTitleListView);

        return view;
    }

    private void deleteNote() {
//        db.deleteGNote(gNote.getId());

        gNote.setDeleted(GNote.TRUE);
        if ("".equals(gNote.getGuid())) {
            db.deleteGNote(gNote.getId());
        } else {
            gNote.setSynStatus(GNote.DELETE);
            db.updateGNote(gNote);
        }

        if (!gNote.isPassed()) {
            AlarmService.cancelTask(mContext, gNote);
        }
    }

    private void buildCreator() {
        creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "remind" item
                SwipeMenuItem remindItem = new SwipeMenuItem(
                        mContext.getApplicationContext());
                // set item background
                remindItem.setBackground(new ColorDrawable(Color.rgb(0x33,
                        0xB5, 0xE5)));
                // set item width
                remindItem.setWidth(dp2px(66));
                // set item title
                remindItem.setTitle(R.string.remind);
                // set item title fontsize
                remindItem.setTitleSize(17);
                // set item title font color
                remindItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(remindItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        mContext.getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(66));
                // set item title
                deleteItem.setTitle(R.string.delete);
                // set item title fontsize
                deleteItem.setTitleSize(17);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    public void randomfabButtonColor(){
        Util.randomBackground(fabButton);
    }
}