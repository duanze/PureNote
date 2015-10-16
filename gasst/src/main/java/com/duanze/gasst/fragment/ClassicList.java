package com.duanze.gasst.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.duanze.gasst.activity.StartActivity;
import com.duanze.gasst.util.MyDatePickerListener;
import com.duanze.gasst.R;
import com.duanze.gasst.activity.Folder;
import com.duanze.gasst.activity.Note;
import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.adapter.NoteAdapter;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.util.DateTimePickerCallback;
import com.duanze.gasst.util.LogUtil;
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
    private NoteAdapter noteAdapter;
    private SwipeMenuListView noteListView;
    private SwipeMenuCreator creator;
    private List<GNote> gNoteList;
    private Context mContext;
    //nice FloatingButton
    private FloatingActionButton fabButton;
    private Calendar today;
    private GNote gNote;
    private GNoteDB db;
    private SharedPreferences preferences;

    public static final String DATEPICKER_TAG = "datepicker";
    private DatePickerDialog pickerDialog;
    private DateTimePickerCallback listener = new DateTimePickerCallback() {
        @Override
        public void onFinish(String result) {
            gNote.setAlertTime(result);
            gNote.setIsPassed(GNote.FALSE);
            db.updateGNote(gNote);
            AlarmService.alarmTask(mContext);

            refreshUI();
        }

        @Override
        public void onError(Exception e) {

        }
    };

    private boolean isRefreshing = false;
//    同步限定
    public synchronized void refreshUI() {
        if (isRefreshing) return;
        isRefreshing = true;

//      这种后台刷新的方式不够即时，在这里不适用
//        new RefreshTask(new RefreshHandler()).execute();

        StartActivity activity = (StartActivity) mContext;

//        List<GNote> tmpList = db.loadGNotes();
        List<GNote> tmpList = db.loadGNotesByBookId(activity.getGNotebookId());
        gNoteList.clear();
        for (GNote g : tmpList) {
            gNoteList.add(g);
        }

        noteAdapter.setValues(activity.getIsFold(), activity.getToday(), activity.getMaxLines());
        noteAdapter.notifyDataSetChanged();

        isRefreshing = false;
    }

    private void initValues() {
        mContext = getActivity();
        today = Calendar.getInstance();
        db = GNoteDB.getInstance(mContext);
//        gNoteList = db.loadGNotes();
        gNoteList = db.loadGNotesByBookId(((StartActivity) mContext).getGNotebookId());

        pickerDialog = DatePickerDialog.newInstance(new MyDatePickerListener(mContext, today, listener),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH), false);
        pickerDialog.setYearRange(today.get(Calendar.YEAR) - 10,
                today.get(Calendar.YEAR) + 10);
        pickerDialog.setCloseOnSingleTapDay(true);

        preferences = mContext.getSharedPreferences(Settings.DATA, Context.MODE_PRIVATE);
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
        noteListView = (SwipeMenuListView) view.findViewById(R.id.swipe_lv);
        noteAdapter = new NoteAdapter(mContext, R.layout.classic_list_item, gNoteList, noteListView);
        StartActivity activity = (StartActivity) mContext;
        noteAdapter.setValues(activity.getIsFold(), activity.getToday(), activity.getMaxLines());
        noteListView.setAdapter(noteAdapter);
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                operateNote(i);
            }
        });

        buildCreator();
        // set creator
        noteListView.setMenuCreator(creator);
        //listener item click event
        noteListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // remind
                        gNote = gNoteList.get(position);
                        pickerDialog.show(((StartActivity) mContext).getSupportFragmentManager(),
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
        fabButton.listenTo(noteListView);

        return view;
    }

    private void operateNote(int i) {
        GNote gNote = gNoteList.get(i);
        boolean prefNote = preferences.getBoolean(Settings.PREF_NOTE_KEY, false);
        if (prefNote) {
            Note.actionStart(
                    mContext, gNote, Note.MODE_SHOW);
        } else {
            Note.actionStart(
                    mContext, gNote, Note.MODE_EDIT);
        }
    }

    private void deleteNote() {
//        db.deleteGNote(gNote.getId());

        gNote.setDeleted(GNote.TRUE);
        if ("".equals(gNote.getGuid())) {
//            db.deleteGNote(gNote.getId());
        } else {
            gNote.setSynStatus(GNote.DELETE);
        }
        db.updateGNote(gNote);

        if (!gNote.getIsPassed()) {
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
            int cnt = preferences.getInt(Folder.PURENOTE_NOTE_NUM,
                    3);
            preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, cnt + value).commit();
        } else {
            List<GNotebook> gNotebooks = db.loadGNotebooks();
            for (GNotebook gNotebook : gNotebooks) {
                if (gNotebook.getId() == id) {
                    int cnt = gNotebook.getNotesNum();
                    gNotebook.setNotesNum(cnt + value);

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

    public static final int REFRESH_START = 3;
    public static final int REFRESH_END = 4;
    public static final int REFRESH_ERROR = 5;
    public static final int REFRESH_SUCCESS = 6;

    class RefreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_START:
                    LogUtil.i(TAG,"REFRESH_START");
                    ((StartActivity) mContext).showDialog();
                    break;
                case REFRESH_END:
                    LogUtil.i(TAG,"REFRESH_END");
                    ((StartActivity) mContext).removeDialog();
                    break;
                case REFRESH_SUCCESS:
                    LogUtil.i(TAG,"REFRESH_SUCCESS");
                    if (null != noteAdapter) {
                        noteAdapter.notifyDataSetChanged();
                    }
                    isRefreshing = false;
                    break;
                case REFRESH_ERROR:
                    LogUtil.e(TAG,"REFRESH_ERROR");
                    break;
                default:
                    break;
            }
        }
    }

    class RefreshTask extends AsyncTask<Void, Integer, Void> {

        Handler mHandler;

        public RefreshTask(Handler handler) {
            mHandler = handler;
        }

        @Override
        protected Void doInBackground(Void... params) {
            publishProgress(REFRESH_START);
            try {
                StartActivity activity = (StartActivity) mContext;
//        List<GNote> tmpList = db.loadGNotes();
                List<GNote> tmpList = db.loadGNotesByBookId(activity.getGNotebookId());
                gNoteList.clear();
                for (GNote g : tmpList) {
                    gNoteList.add(g);
                }

                noteAdapter.setValues(activity.getIsFold(), activity.getToday(), activity.getMaxLines());
//        noteListView.setSelection(0);

                publishProgress(REFRESH_SUCCESS);
            } catch (Exception e) {
                publishProgress(REFRESH_ERROR);
//                注意下面的语句，能让finally不再执行吗？.....
                return null;
            } finally {
                publishProgress(REFRESH_END);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mHandler == null) {
                return;
            }
            switch (values[0]) {
                case REFRESH_START:
                    mHandler.sendEmptyMessage(REFRESH_START);
                    break;
                case REFRESH_END:
                    mHandler.sendEmptyMessage(REFRESH_END);
                    break;
                case REFRESH_ERROR:
                    mHandler.sendEmptyMessage(REFRESH_ERROR);
                    break;
                case REFRESH_SUCCESS:
                    mHandler.sendEmptyMessage(REFRESH_SUCCESS);
                    break;
                default:
                    break;
            }
        }
    }
}
