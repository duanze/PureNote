package com.duanze.gasst.ui.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.duanze.gasst.R;
import com.duanze.gasst.memento.Memo;
import com.duanze.gasst.memento.Originator;
import com.duanze.gasst.data.model.GNote;
import com.duanze.gasst.data.model.GNoteDB;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.ui.activity.base.BaseActivity;
import com.duanze.gasst.util.GNotebookUtil;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.ProviderUtil;
import com.duanze.gasst.util.TimeUtils;
import com.duanze.gasst.util.Util;
import com.duanze.gasst.util.liteprefs.MyLitePrefs;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.util.Calendar;

public class Note extends BaseActivity implements TextWatcher {
    public static final String TAG = Note.class.getSimpleName();

    public static final int MODE_NEW = 0;
    public static final int MODE_SHOW = 1;
    public static final int MODE_EDIT = 2;
    public static final int MODE_TODAY = 3;

    public static final String EditCount = "edit_count";
    public static final int EDIT_COUNT = 13;
    public static final int INTERVAL = 8;

    private int mode;
    private GNote gNote;
    private EditText editText;
    private GNoteDB db;
    private android.support.v7.app.ActionBar actionBar;


    /**
     * 退出时应对数据库进行操作的标志位
     */
    private int dbFlag;
    public static final int DB_SAVE = 1;
    public static final int DB_UPDATE = 2;
    public static final int DB_DELETE = 3;

    /**
     * 原本是否有定时提醒
     */
    private int isPassed;
    private Context mContext;

    /**
     * 数据解析器，备忘录模式，轻松实现撤消、重做
     */
    private Originator mDataParser;

    /**
     * TextWatcher 中使用，标注是否是使用undo或redo引起的内容改变
     */
    private boolean isUndoOrRedo;

    private MenuItem undoItem;
    private MenuItem redoItem;

    /**
     * 启动NoteActivity活动的静态方法，
     * 需给出GAsstNote实例及启动模式NoteActivity.MODE_SHOW or NoteActivity.MODE_NEW
     * or NoteActivity.MODE_EDIT
     *
     * @param context
     * @param gNote
     * @param mode
     */
    public static void actionStart(Context context, GNote gNote, int mode) {
        Intent intent = new Intent(context, Note.class);
        intent.putExtra("gAsstNote_data", gNote);
        intent.putExtra("mode", mode);
        context.startActivity(intent);
    }

    /**
     * 以cal为日期写一篇新记事
     */
    public static void writeNewNote(Context mContext, Calendar cal) {
        GNote note = new GNote();
        note.setCalToTime(cal);
        Note.actionStart(mContext, note, Note.MODE_NEW);
    }

    public static void writeTodayNewNote(Context mContext) {
        GNote gNote = new GNote();
        Calendar cal = Calendar.getInstance();
        gNote.setCalToTime(cal);

        Intent intent = new Intent(mContext, Note.class);
        intent.putExtra("gAsstNote_data", gNote);
        intent.putExtra("mode", MODE_NEW);
        mContext.startActivity(intent);
        ((Activity) mContext).overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setOverflowShowingAlways();
        initValues();

        actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (MODE_NEW == mode) {
                actionBar.setTitle(R.string.mode_new);
            } else if (MODE_EDIT == mode) {
                updateAppBar();
            }
        }

        if (MyLitePrefs.getBoolean(MyLitePrefs.CONCENTRATE_WRITE)) {
            listenSoftInput();
        }
    }

    private void updateAppBar() {
        String stamp;
        if (MyLitePrefs.getBoolean(MyLitePrefs.CREATE_ORDER)) {
            stamp = Util.timeStamp(gNote);
        } else {
            stamp = TimeUtils.getConciseTime(gNote.getEditTime(), mContext);
        }
        actionBar.setTitle(stamp);
    }

    private int lastHeight;

    private void listenSoftInput() {
        final View scroll = findViewById(R.id.sv_just);
        scroll.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int rootHeight = scroll.getRootView().getHeight();
                        int heightDiff = rootHeight - scroll.getHeight();
//                        Log.e(TAG, "getRootView().getHeight() = " + rootHeight);
//                        Log.e(TAG, "rootLayout.getHeight() = " + scroll.getHeight());

                        if (heightDiff > 0.33 * rootHeight) { // 说明键盘是弹出状态
                            lastHeight = scroll.getHeight();
                            hideToolbar();
                        } else if (0 != lastHeight && lastHeight < scroll.getHeight()) {
                            showToolbar();
                        }
                    }
                });
    }

    private void initValues() {
        mContext = this;
        Calendar today = Calendar.getInstance();
        gNote = getIntent().getParcelableExtra("gAsstNote_data");
        isPassed = gNote.getPassed();
        mDataParser = new Originator(new Memo(gNote.getContent(), 0));
        isUndoOrRedo = false;
        db = GNoteDB.getInstance(this);

        editText = (EditText) findViewById(R.id.et_note_edit);
        editText.setHint(R.string.write_something);
        initMode(mDataParser.getState().getContent());
        editText.addTextChangedListener(this);

        if (mode == MODE_NEW) {
            editText.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else if (mode == MODE_EDIT) {
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        editText.setCursorVisible(true);
//                        actionBar.setTitle(R.string.mode_edit);
                    }
                }
            });
        } else if (MODE_TODAY == mode) {
            gNote.setCalToTime(today);
            gNote.setGNotebookId(MyLitePrefs.getInt(MyLitePrefs.QUICK_WRITE_SAVE_LOCATION));
            editText.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void initMode(String content) {
        mode = getIntent().getIntExtra("mode", 0);
        if (mode == MODE_NEW || mode == MODE_EDIT || MODE_TODAY == mode) {
            setNoteContent(content, content.length());
        }

        int no = getIntent().getIntExtra("no", -1);
        //传入了no表明是从定时任务传来，先取消通知栏显示，[再表明需更新UI(使用Loader后，这一步不需要了:) ])
        if (no != -1) {
            LogUtil.i(TAG, "no:" + no);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(no);
        }
    }

    // / 历史代码，仅供参考
    //    private void initBtns() {
//        for (int i = 0; i < 9; i++) {
//            chooseColorBtns[i] = (Button) findViewById(BTN_ARR[i]);
//
//            chooseColorBtnDrawables[i] = new GradientDrawable();
//            chooseColorBtnDrawables[i].setShape(GradientDrawable.RECTANGLE); // 画框
//            chooseColorBtnDrawables[i].setStroke(1, Color.BLACK); // 边框粗细及颜色
//            chooseColorBtnDrawables[i].setColor(GridUnit.colorArr[i]); // 边框内部颜色
//
//            final int tmp = i;
//            chooseColorBtns[i].setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (!chooseColorBtnStates[tmp]) {
//                        resetSelectColorBtns();
//                        chosenBtn(tmp);
//
//                        checkColorRead();
//                        checkDbFlag();
//                    }
//                }
//            });
//
//            chooseColorBtns[i].setBackgroundColor(GridUnit.colorArr[i]);
//
//            if (gNote.getColor() == GridUnit.colorArr[i]) {
//                chosenBtn(i);
//            }
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        //        umeng
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //        umeng
        MobclickAgent.onResume(this);
    }

    private void checkDbFlag() {
        if (mode == MODE_EDIT || mode == MODE_SHOW || MODE_TODAY == mode) {
            dbFlag = DB_UPDATE;
        }
    }

    /**
     * 令Overflow菜单永远显示
     */
    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mode == MODE_NEW || MODE_TODAY == mode) {
            getMenuInflater().inflate(R.menu.new_note_menu, menu);
        } else if (mode == MODE_EDIT) {
            getMenuInflater().inflate(R.menu.edit_note_menu, menu);
            if (!MyLitePrefs.getBoolean(MyLitePrefs.CREATE_ORDER)) {
                menu.findItem(R.id.edit_date).setVisible(false);
            }
        }
        undoItem = menu.findItem(R.id.action_undo);
        redoItem = menu.findItem(R.id.action_redo);
        updateUndoAndRedo();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (gNote.getIsPassed()) {
            menu.findItem(R.id.action_cancel_remind).setVisible(false);
            menu.findItem(R.id.action_remind).setVisible(true);
        } else {
            menu.findItem(R.id.action_cancel_remind).setVisible(true);
            menu.findItem(R.id.action_remind).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exitOperation();
                return true;
            case R.id.action_share:
                share();
                return true;
            case R.id.action_remind:
                showDateTimePicker();
                return true;
            case R.id.action_cancel_remind:
                if (!gNote.getIsPassed()) {
                    gNote.setIsPassed(GNote.TRUE);
                    checkDbFlag();
                }
                return true;
            case R.id.word_count:
                showWordCount();
                return true;
            case R.id.edit_date:
                showDatePicker();
                return true;
            case R.id.action_redo:
                redo();
                return true;
            case R.id.action_undo:
                undo();
                return true;
            default:
                return true;
        }
    }

    private void undo() {
        isUndoOrRedo = true;
        mDataParser.undo();
        setDataToNoteContent();
    }

    private void setDataToNoteContent() {
        String content = mDataParser.getState().getContent();
        int selectionEnd = mDataParser.getState().getSelectionEnd();
        setNoteContent(content, selectionEnd);
    }

    private void setNoteContent(String content, int length) {
        editText.setText(content);
        editText.setSelection(length);
    }

    private void redo() {
        isUndoOrRedo = true;
        mDataParser.redo();
        setDataToNoteContent();
    }

    private void showDatePicker() {
        View v = getLayoutInflater().inflate(R.layout.date_picker, null);
        final DatePicker datePicker = (DatePicker) v.findViewById(R.id.date_picker);
        int[] date = Util.parseDateFromGNote(gNote);
        datePicker.init(date[0], date[1], date[2], null);
        new AlertDialog.Builder(mContext)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = datePicker.getYear();
                        int month = datePicker.getMonth();
                        int day = datePicker.getDayOfMonth();
                        gNote.setTimeFromDate(year, month, day);
                        checkDbFlag();
                        updateAppBar();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private void showDateTimePicker() {
        View v = getLayoutInflater().inflate(R.layout.date_picker, null);
        final DatePicker datePicker = (DatePicker) v.findViewById(R.id.date_picker);
        new AlertDialog.Builder(mContext)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] date = new int[3];
                        date[0] = datePicker.getYear();
                        date[1] = datePicker.getMonth();
                        date[2] = datePicker.getDayOfMonth();
                        showTimePicker(date);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private void showTimePicker(final int[] date) {
        View v = getLayoutInflater().inflate(R.layout.time_picker, null);
        final TimePicker timePicker = (TimePicker) v.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        new AlertDialog.Builder(mContext)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hour = timePicker.getCurrentHour();
                        int minute = timePicker.getCurrentMinute();

                        String result = date[0]
                                + ","
                                + Util.twoDigit(date[1])
                                + ","
                                + Util.twoDigit(date[2])
                                + ","
                                + Util.twoDigit(hour)
                                + ","
                                + Util.twoDigit(minute);

                        gNote.setAlertTime(result);
                        gNote.setIsPassed(GNote.FALSE);
                        checkDbFlag();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private void showWordCount() {
        int[] res = new int[3];
        Util.wordCount(editText.getText().toString(), res);
        String msg = getString(R.string.words) + "\n" + res[0] + "\n\n"
                + getString(R.string.characters_no_spaces) + "\n" + res[1] + "\n\n"
                + getString(R.string.characters_without_spaces) + "\n" + res[2] + "\n";
        new AlertDialog.Builder(mContext).setTitle(R.string.word_count)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .create().show();
    }

    /**
     * 分享按钮
     */
    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.action_share);
        String text = editText.getText().toString();
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    private void deleteNote() {
        //        物理数据存储，以改代删
        gNote.setDeleted(GNote.TRUE);
        if (!"".equals(gNote.getGuid())) {
            gNote.setSynStatus(GNote.DELETE);
        }
        ProviderUtil.updateGNote(mContext, gNote);

        if (!gNote.getIsPassed()) {
            AlarmService.cancelTask(Note.this, gNote);
        }
//        更新笔记本状态
        int notebookId = gNote.getGNotebookId();
        GNotebookUtil.updateGNotebook(mContext, notebookId, -1);
        if (notebookId != 0) {
            GNotebookUtil.updateGNotebook(mContext, 0, -1);
        }
    }

    private void createNote() {
//        不允许新建一条空内空的笔记
        if (0 == editText.getText().toString().trim().length()) {
            return;
        }

        gNote.setContent(editText.getText().toString());
        //needCreate
        gNote.setSynStatus(GNote.NEW);

//        设置笔记本id
        int groupId;
        if (MODE_TODAY != mode) {
            groupId = MyLitePrefs.getInt(MyLitePrefs.GNOTEBOOK_ID);
            gNote.setGNotebookId(groupId);
        } else {
            //快写模式将存储至特定目录下
            groupId = gNote.getGNotebookId();
        }

        gNote.setEditTime(TimeUtils.getCurrentTimeInLong());
//        物理数据存储
        ProviderUtil.insertGNote(mContext, gNote);

//        更新笔记本
        GNotebookUtil.updateGNotebook(mContext, groupId, +1);
        if (groupId != 0) {
            GNotebookUtil.updateGNotebook(mContext, 0, +1);
        }
        //当有定时提醒
        if (!gNote.getIsPassed()) {
            //新建记事尚无id，需存储后从数据库中提取
            gNote.setId(db.getNewestGNoteId());
            AlarmService.alarmTask(this);
        }
    }

    private void updateNote() {
        gNote.setContent(editText.getText().toString());
        if (gNote.getSynStatus() == GNote.NOTHING) {
            //needUpdate
            gNote.setSynStatus(GNote.UPDATE);
        }

        gNote.setEditTime(TimeUtils.getCurrentTimeInLong());
        //        物理数据存储
        ProviderUtil.updateGNote(mContext, gNote);

        //当有定时提醒
        if (!gNote.getIsPassed()) {
            AlarmService.alarmTask(this);
        } else if (isPassed == GNote.FALSE) {//手动取消定时提醒
            AlarmService.cancelTask(this, gNote);
        }
    }

    /**
     * 捕获Back按键
     */
    @Override
    public void onBackPressed() {
        exitOperation();
    }

    /**
     * 退出时的操作，用于 重写Back键 与 导航键
     */
    private void exitOperation() {
        String tmp = editText.getText().toString();

        if (mode == MODE_NEW || MODE_TODAY == mode) {
            if (tmp.length() > 0) {
                dbFlag = DB_SAVE;
            }
        } else if (mode == MODE_EDIT) {
            if (tmp.length() > 0) {
                if (!tmp.equals(gNote.getContent())) {
                    dbFlag = DB_UPDATE;
                }
            } else {
                dbFlag = DB_DELETE;
            }
        }

        if (dbFlag == DB_SAVE) {
            createNote();
            addEditCount();
        } else if (dbFlag == DB_UPDATE) {
            updateNote();
            addEditCount();
        } else if (dbFlag == DB_DELETE) {
            deleteNote();
            addEditCount();
        }
        finish();
    }

    private void addEditCount() {
        SharedPreferences preferences = getSharedPreferences(MyLitePrefs.DATA, MODE_PRIVATE);
        if (!preferences.getBoolean(StartActivity.ShownRate, false)) {
            int count = preferences.getInt(EditCount, 0);
            count++;
            preferences.edit().putInt(EditCount, count).apply();
        }
    }


    private boolean mIsActionBarSlidUp = false;

    private void slideAppBar(boolean slideUp) {
        final LinearLayout rootLayout = getRootLayout();
        final ActionBar actionBar = getSupportActionBar();
        if (null == actionBar) return;

        final View shadow = rootLayout.findViewById(R.id.toolbar_shadow);
        final int height = actionBar.getHeight() + shadow.getHeight();

        ValueAnimator animator =
                slideUp ? ValueAnimator.ofFloat(0, 1) : ValueAnimator.ofFloat(1, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();
//                    actionBar.setHideOffset((int) (actionBar.getHeight() * value));
                LinearLayout.MarginLayoutParams params = (LinearLayout.MarginLayoutParams) rootLayout.getLayoutParams();
                params.setMargins(0, -(int) (height * value), 0, 0);
                rootLayout.setLayoutParams(params);
                rootLayout.requestLayout();
            }
        });
        animator.start();
        mIsActionBarSlidUp = slideUp;
    }

    private void hideToolbar() {
        if (mIsActionBarSlidUp) return;
        slideAppBar(true);
    }

    private void showToolbar() {
        if (!mIsActionBarSlidUp) return;
        slideAppBar(false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!isUndoOrRedo) {
            int selectionLocation = editText.getSelectionEnd();
            if (-1 == selectionLocation) {
                selectionLocation = 0;
            }
            mDataParser.newState(new Memo(s.toString(), selectionLocation));
        } else {
            isUndoOrRedo = false;
        }

        updateUndoAndRedo();
    }

    private void updateUndoAndRedo() {
        undoItem.setEnabled(mDataParser.lastSize() > 0);
        redoItem.setEnabled(mDataParser.nextSize() > 0);
    }
}
