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
import com.duanze.gasst.activity.Folder;
import com.duanze.gasst.activity.Note;
import com.duanze.gasst.adapter.NoteAdapter;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
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
//        List<GNote> tmpList = db.loadGNotes();
        List<GNote> tmpList = db.loadGNotesByBookId(((MainActivity) mContext).getgNotebookId());
        for (GNote g : tmpList) {
            gNoteList.add(g);
        }

        adapter.setValues((MainActivity) mContext);
        adapter.notifyDataSetChanged();
//        noteTitleListView.setSelection(0);
    }

    private void initValues() {
        mContext = getActivity();
        today = Calendar.getInstance();
        db = GNoteDB.getInstance(mContext);
//        gNoteList = db.loadGNotes();
        gNoteList = db.loadGNotesByBookId(((MainActivity) mContext).getgNotebookId());

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

        View view = inflater.inflate(R.layout.fragment_classic_list, container, false);
        noteTitleListView = (SwipeMenuListView) view.findViewById(R.id.swipe_lv);
        adapter = new NoteAdapter(mContext, R.layout.classic_list_item, gNoteList, noteTitleListView);
        adapter.setValues((MainActivity) mContext);
        noteTitleListView.setAdapter(adapter);
        noteTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GNote gNote = gNoteList.get(i);
                Note.activityStart(
                        mContext, gNote, Note.MODE_EDIT);
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
                Note.writeNewNote(mContext, today);
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

        int noteBookId = gNote.getGNotebookId();
        if (noteBookId < 0) noteBookId = 0;
        updateGNotebook(noteBookId, -1);
        if (noteBookId != 0) {
            updateGNotebook(0, -1);
        }
    }

    private void updateGNotebook(int id, int value) {
        if (id == 0) {
            int cnt = ((MainActivity) getActivity()).getPreferences().getInt(Folder.PURENOTE_NOTE_NUM,
                    3);
            ((MainActivity) getActivity()).getPreferences().edit().putInt(Folder.PURENOTE_NOTE_NUM, cnt + value).commit();
        } else {
            List<GNotebook> gNotebooks = db.loadGNotebooks();
            for (GNotebook gNotebook : gNotebooks) {
                if (gNotebook.getId() == id) {
                    int cnt = gNotebook.getNum();
                    gNotebook.setNum(cnt + value);

                    db.updateGNotebook(gNotebook);
                    break;
                }
            }
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
                remindItem.setBackground(new ColorDrawable(Color.argb(0x00, 0xEE,
                        0xF0, 0xED)));
                // set item width
                remindItem.setWidth(dp2px(60));
                // set a icon
                remindItem.setIcon(R.drawable.remind_blue);
                // add to menu
                menu.addMenuItem(remindItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        mContext.getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xE2,
                        0xE2, 0xE2)));
                // set item width
                deleteItem.setWidth(dp2px(60));
                // set a icon
                deleteItem.setIcon(R.drawable.trash_red);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    public void randomfabButtonColor() {
        Util.randomBackground(fabButton);
    }

}
