package com.duanze.gasst.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.duanze.gasst.R;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.util.DateTimePickerCallback;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.ProviderUtil;
import com.duanze.gasst.util.TimeUtils;
import com.duanze.gasst.view.GridUnit;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

public class Note extends BaseActivity {
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
    private TextView textView;
    private GNoteDB db;
    private android.support.v7.app.ActionBar actionBar;
    private Calendar today;

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

    private DatePickerDialog timePickerDialog;
    private DatePickerDialog datePickerDialog;
    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";


    /**
     * 退出时应对数据库进行操作的标志位
     */
    private int dbFlag;
    public static final int DB_SAVE = 1;
    public static final int DB_UPDATE = 2;
    public static final int DB_DELETE = 3;

    private Button[] chooseColorBtns = new Button[9];
    private boolean[] chooseColorBtnStates = new boolean[9];
    private GradientDrawable[] chooseColorBtnDrawables = new GradientDrawable[9];
    public static final int[] BTN_ARR = {R.id.transparent_btn, R.id.light_grey_btn, R.id
            .blue_btn, R.id.green_btn, R.id.yellow_btn, R.id.gold_btn, R.id.pink_btn, R.id
            .red_btn, R.id.purple_btn};

    private SharedPreferences preferences;

    /**
     * 原本是否有定时提醒
     */
    private int isPassed;
    private int bookId;

    private Context mContext;
    /**
     * 设定颜色值的一堆按钮
     */
    private HorizontalScrollView hsvColorBtns;

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

    /**
     * 内部类，监听时间选择器专用
     */
    private DateTimePickerCallback listener = new DateTimePickerCallback() {
        @Override
        public void onFinish(String result) {
            gNote.setAlertTime(result);
            gNote.setIsPassed(GNote.FALSE);
            checkDbFlag();

        }

        @Override
        public void onError(Exception e) {

        }
    };

    private class MyDatePickerListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
            gNote.setTimeFromDate(year, month, day);
            checkDbFlag();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(Settings.DATA, MODE_PRIVATE);

        setContentView(R.layout.activity_note);

        setOverflowShowingAlways();
        initValues();

        actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (MODE_NEW == mode) {
                actionBar.setTitle(R.string.mode_new);
            } else if (MODE_EDIT == mode) {
                actionBar.setTitle(R.string.mode_view);
            }
        }

        findViewById(R.id.transparent_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleActionBar();
            }
        });
    }

    private void initValues() {
        mContext = this;
        today = Calendar.getInstance();
        initDateTimePicker();
        gNote = getIntent().getParcelableExtra("gAsstNote_data");
        isPassed = gNote.getPassed();
        bookId = gNote.getGNotebookId();

        db = GNoteDB.getInstance(this);

        textView = (TextView) findViewById(R.id.tv_note_show);
        editText = (EditText) findViewById(R.id.et_note_edit);
        initMode();

        if (mode == MODE_NEW) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams
                    .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            editText.requestFocus();

        } else if (mode == MODE_EDIT) {
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        editText.setCursorVisible(true);
                        actionBar.setTitle(R.string.mode_edit);
                    }
                }
            });
        } else if (MODE_TODAY == mode) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams
                    .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            editText.requestFocus();

            gNote.setCalToTime(today);
            gNote.setGNotebookId(preferences.getInt(Settings.QUICK_WRITE_SAVE_LOCATION, 0));

        }
    }

    private void initDateTimePicker() {
        timePickerDialog = DatePickerDialog.newInstance(new com.duanze.gasst.util.MyDatePickerListener(this, today, listener),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar
                        .DAY_OF_MONTH), false);
        timePickerDialog.setYearRange(today.get(Calendar.YEAR) - 10, today.get(Calendar.YEAR) + 10);
        timePickerDialog.setCloseOnSingleTapDay(true);

        datePickerDialog = DatePickerDialog.newInstance(new MyDatePickerListener(), today.get
                        (Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar
                        .DAY_OF_MONTH),
                false);
        datePickerDialog.setYearRange(today.get(Calendar.YEAR) - 10, today.get(Calendar.YEAR) + 10);
        datePickerDialog.setCloseOnSingleTapDay(true);
    }

    private void checkColorRead() {
//        boolean colorRead = preferences.getBoolean(Settings.COLOR_READ, true);
//        if (colorRead && customizeColor) {
        if (mode == MODE_SHOW) {
            textView.setBackgroundColor(gNote.getColor());
        } else {
            editText.setBackgroundColor(gNote.getColor());
        }
//        }
    }

    private void initMode() {
        mode = getIntent().getIntExtra("mode", 0);
        if (mode == MODE_NEW || mode == MODE_EDIT || MODE_TODAY == mode) {
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            editText.setText(gNote.getContent());
            editText.setSelection(0);
        } else if (mode == MODE_SHOW) {
            textView.setText(gNote.getContent());
//            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        }

        int no = getIntent().getIntExtra("no", -1);
        //传入了no表明是从定时任务传来，先取消通知栏显示，[再表明需更新UI(使用Loader后，这一步不需要了:) ])
        if (no != -1) {
            LogUtil.i(TAG, "no:" + no);
            NotificationManager manager = (NotificationManager) getSystemService
                    (NOTIFICATION_SERVICE);
            manager.cancel(no);
//                uiShouldUpdate();
        }
    }

    private void initBtns() {
        for (int i = 0; i < 9; i++) {
            chooseColorBtns[i] = (Button) findViewById(BTN_ARR[i]);

            chooseColorBtnDrawables[i] = new GradientDrawable();
            chooseColorBtnDrawables[i].setShape(GradientDrawable.RECTANGLE); // 画框
            chooseColorBtnDrawables[i].setStroke(1, Color.BLACK); // 边框粗细及颜色
            chooseColorBtnDrawables[i].setColor(GridUnit.colorArr[i]); // 边框内部颜色

            final int tmp = i;
            chooseColorBtns[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!chooseColorBtnStates[tmp]) {
                        resetSelectColorBtns();
                        chosenBtn(tmp);

                        checkColorRead();
                        checkDbFlag();
                    }
                }
            });

            chooseColorBtns[i].setBackgroundColor(GridUnit.colorArr[i]);

            if (gNote.getColor() == GridUnit.colorArr[i]) {
                chosenBtn(i);
            }
        }
    }

    private void resetSelectColorBtns() {
        for (int i = 0; i < 9; i++) {
            if (chooseColorBtnStates[i]) {
                chooseColorBtnStates[i] = false;
                chooseColorBtns[i].setBackgroundColor(GridUnit.colorArr[i]);

                break;
            }
        }
    }

    private void chosenBtn(int i) {
        gNote.setColor(GridUnit.colorArr[i]);
        chooseColorBtnStates[i] = true;
        chooseColorBtns[i].setBackgroundDrawable(chooseColorBtnDrawables[i]); // 设置背景（效果就是有边框及底色）

    }

    private void checkDbFlag() {
        if (mode == MODE_EDIT || mode == MODE_SHOW || MODE_TODAY == mode) {
            dbFlag = DB_UPDATE;
        }
    }

    /**
     * 令Overflow菜单项能显示icon
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible",
                            Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
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
        if (mode == MODE_SHOW) {
            getMenuInflater().inflate(R.menu.show_note_menu, menu);
        } else if (mode == MODE_NEW || MODE_TODAY == mode) {
            getMenuInflater().inflate(R.menu.new_note_menu, menu);
        } else if (mode == MODE_EDIT) {
            getMenuInflater().inflate(R.menu.edit_note_menu, menu);
        }
        return true;
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
            case R.id.action_trash:
                trash();
                return true;
            case R.id.action_remind:
                timePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                return true;
            case R.id.action_cancel_remind:
                if (!gNote.getIsPassed()) {
                    gNote.setIsPassed(GNote.TRUE);
                    checkDbFlag();
                }
                return true;
            case R.id.action_edit:
                actionStart(this, gNote, MODE_EDIT);
                finish();
                return true;
            case R.id.action_move:
                showSelectFolderDialog();
                return true;
            default:
                return true;
        }
    }

    private boolean changedFolder = false;
    private int tmpGnoteBookId = -1;

    private void showSelectFolderDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_radiogroup, (ViewGroup) getWindow
                ().getDecorView(), false);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rg_dialog);
        RadioButton purenote = (RadioButton) view.findViewById(R.id.rb_purenote);
        boolean radioChecked = false;
        List<GNotebook> list = db.loadGNotebooks();
        for (final GNotebook gNotebook : list) {
            RadioButton tempButton = new RadioButton(mContext);
            tempButton.setText(gNotebook.getName());
            radioGroup.addView(tempButton, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout
                    .LayoutParams.WRAP_CONTENT);
            if (gNotebook.getId() == gNote.getGNotebookId()) {
                tempButton.setChecked(true);
                radioChecked = true;
            }

            tempButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        tmpGnoteBookId = gNotebook.getId();
                    }
                }
            });
        }

        if (!radioChecked) {
            purenote.setChecked(true);
        }
        purenote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    tmpGnoteBookId = 0;
                }
            }
        });

        final Dialog dialog = new AlertDialog.Builder(mContext).setTitle(R.string.action_move)
                .setView(view).setPositiveButton(R.string.confirm, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (tmpGnoteBookId != -1) {
                            dbFlag = DB_UPDATE;
                            changedFolder = true;
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        changedFolder = false;
                    }
                }).create();
        dialog.show();
    }

    /**
     * 分享按钮
     */
    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.action_share);
        String text;
        if (mode == MODE_NEW || mode == MODE_EDIT || MODE_TODAY == mode) {
            text = editText.getText().toString();
        } else {//mode == MODE_SHOW
            text = textView.getText().toString();
        }
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    /**
     * 删除按钮的方法
     */
    private void trash() {
        new AlertDialog.Builder(this).
                setMessage(R.string.delete_text).
                setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                }).
                setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        //需不是新建记事，方可从数据库中删除数据
                        if (mode != MODE_NEW && MODE_TODAY != mode) {
                            deleteNote();
                        }
                        finish();
                    }
                }).show();
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
        updateGNotebook(notebookId, -1, false);
        if (notebookId != 0) {
            updateGNotebook(0, -1, false);
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
            groupId = preferences.getInt(Folder.GNOTEBOOK_ID, 0);
            gNote.setGNotebookId(groupId);
        } else {
            //快写模式将存储至特定目录下
            groupId = gNote.getGNotebookId();
        }

        gNote.setEditTime(TimeUtils.getCurrentTimeInLong());
//        物理数据存储
        ProviderUtil.insertGNote(mContext, gNote);

//        更新笔记本
        updateGNotebook(groupId, +1, false);
        if (groupId != 0) {
            updateGNotebook(0, +1, false);
        }
        //当有定时提醒
        if (!gNote.getIsPassed()) {
            //新建记事尚无id，需存储后从数据库中提取
            gNote.setId(db.getNewestGNoteId());
            AlarmService.alarmTask(this);
        }
    }

    private void updateGNotebook(int id, int diff, boolean isMove) {
        if (id == 0) {
            //如果是移动文件，不加不减
            if (isMove) return;
            int cnt = preferences.getInt(Folder.PURENOTE_NOTE_NUM, 3);
            preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, cnt + diff).apply();
        } else {
            GNotebook gNotebook = db.getGNotebookById(id);
            int num = gNotebook.getNotesNum();
            gNotebook.setNotesNum(num + diff);
            ProviderUtil.updateGNotebook(mContext, gNotebook);
        }
    }

    private void updateNote() {
        gNote.setContent(editText.getText().toString());
        if (gNote.getSynStatus() == GNote.NOTHING) {
            //needUpdate
            gNote.setSynStatus(GNote.UPDATE);
        }

        if (changedFolder) {
            LogUtil.i(TAG, "original bookid" + bookId);
            gNote.setGNotebookId(tmpGnoteBookId);
            updateGNotebook(bookId, -1, true);
            updateGNotebook(tmpGnoteBookId, +1, true);
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

    @Override
    protected void onPostResume() {
        super.onPostResume();
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
        if (!preferences.getBoolean(StartActivity.ShownRate, false)) {
            int count = preferences.getInt(EditCount, 0);
            count++;
            preferences.edit().putInt(EditCount, count).apply();
        }
    }


    private boolean mIsActionBarSlidUp = false;

    private void slideActionBar(boolean slideUp, boolean animate) {
        final ActionBar actionBar = getSupportActionBar();
        if (null == actionBar) return;

        if (animate) {
            ValueAnimator animator =
                    slideUp ? ValueAnimator.ofFloat(0, 1) : ValueAnimator.ofFloat(1, 0);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float value = (float) animation.getAnimatedValue();
                    actionBar.setHideOffset((int) (actionBar.getHeight() * value));
                }
            });
            animator.start();
        } else {
            actionBar.setHideOffset(slideUp ? actionBar.getHeight() : 0);
        }
        mIsActionBarSlidUp = slideUp;
    }

    private void toggleActionBar() {
        if (mIsActionBarSlidUp) {
            slideActionBar(false, true);
        } else {
            slideActionBar(true, true);
        }
    }
}
