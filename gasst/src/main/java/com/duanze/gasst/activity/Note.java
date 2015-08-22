package com.duanze.gasst.activity;

import android.app.ActionBar;
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
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.duanze.gasst.MainActivity;
import com.duanze.gasst.MyPickerListener;
import com.duanze.gasst.R;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.util.CallBackListener;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.Util;
import com.duanze.gasst.view.GridUnit;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

public class Note extends FragmentActivity {
    public static final String TAG = "NoteActivity";

    public static final String EditCount = "edit_count";

    public static final int MODE_NEW = 0;
    public static final int MODE_SHOW = 1;
    public static final int MODE_EDIT = 2;
    public static final int MODE_TODAY = 3;

    public static final int EDIT_COUNT = 13;

    private int mode;
    private GNote gNote;
    private EditText editText;
    private TextView textView;
    private GNoteDB db;
    private ActionBar actionBar;
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

    private DatePickerDialog pickerDialog;
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
    public static final int[] BTN_ARR = {
            R.id.transparent_btn, R.id.light_grey_btn, R.id.blue_btn,
            R.id.green_btn, R.id.yellow_btn, R.id.gold_btn,
            R.id.pink_btn, R.id.red_btn, R.id.purple_btn
    };

    private SharedPreferences preferences;

//    private boolean customizeColor;

    /**
     * 原本是否有定时提醒
     */
    private int isPassed;

    //    是否成功发动了activity Folder ，及是否将笔记移动至其他文件夹中
    private boolean launchFolder = false;
    private int bookId;

    private Context mContext;

    /**
     * 启动NoteActivity活动的静态方法，
     * 需给出GAsstNote实例及启动模式NoteActivity.MODE_SHOW or NoteActivity.MODE_NEW
     * or NoteActivity.MODE_EDIT
     *
     * @param context
     * @param gNote
     * @param mode
     */
    public static void activityStart(Context context, GNote gNote, int mode) {
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
        Note.activityStart(mContext, note, Note.MODE_NEW);
    }

    public static void todayNewNote(Context mContext) {
        GNote note = new GNote();
        Note.activityStart(mContext, note, Note.MODE_TODAY);
    }

    /**
     * 内部类，监听时间选择器专用
     */
    private CallBackListener listener = new CallBackListener() {
        @Override
        public void onFinish(String result) {
            gNote.setAlertTime(result);
            gNote.setPassed(GNote.FALSE);
            checkDbFlag();

            updateActionBarTitle();
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

            updateActionBarTitle();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.quick_load, R.anim.quick_load);
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(Settings.DATA, MODE_PRIVATE);

        setContentView(R.layout.activity_note);
        //沉浸式时，对状态栏染色
        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);

        tintManager.setStatusBarTintColor(getResources().getColor(R.color.background_color));

        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
//        // enable navigation bar tint
//        tintManager.setNavigationBarTintEnabled(true);

        setOverflowShowingAlways();

        initValues();

        actionBar = getActionBar();
        if (null != actionBar)
            actionBar.setDisplayHomeAsUpEnabled(true);

        updateActionBarTitle();
    }

    private void initValues() {
        mContext = this;

        today = Calendar.getInstance();
        pickerDialog = DatePickerDialog.newInstance(new MyPickerListener(this, today, listener),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH), false);
        pickerDialog.setYearRange(today.get(Calendar.YEAR) - 10,
                today.get(Calendar.YEAR) + 10);
        pickerDialog.setCloseOnSingleTapDay(true);

        datePickerDialog = DatePickerDialog.newInstance(new MyDatePickerListener(),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH), false);
        datePickerDialog.setYearRange(today.get(Calendar.YEAR) - 10,
                today.get(Calendar.YEAR) + 10);
        datePickerDialog.setCloseOnSingleTapDay(true);

        gNote = getIntent().getParcelableExtra("gAsstNote_data");
        isPassed = gNote.getPassed();
        bookId = gNote.getGNotebookId();

        db = GNoteDB.getInstance(this);

        textView = (TextView) findViewById(R.id.tv_note_show);
        editText = (EditText) findViewById(R.id.et_note_edit);
        initMode();

//        customizeColor = preferences.getBoolean(Settings.CUSTOMIZE_COLOR, true);
        if (mode == MODE_NEW || mode == MODE_EDIT) {
            HorizontalScrollView view = (HorizontalScrollView) findViewById(R.id.hsv_btns);
            view.setVisibility(View.VISIBLE);
            initBtns();
        } else if (MODE_TODAY == mode) {
            gNote.setCalToTime(today);
            gNote.setGNotebookId(preferences.getInt(Settings.QUICK_WRITE_SAVE_LOCATION, 0));

            HorizontalScrollView view = (HorizontalScrollView) findViewById(R.id.hsv_btns);
            view.setVisibility(View.VISIBLE);
            initBtns();
        } else {
            HorizontalScrollView view = (HorizontalScrollView) findViewById(R.id.hsv_btns);
            view.setVisibility(View.GONE);
        }

        checkColorRead();
    }

    private void checkColorRead() {
        boolean colorRead = preferences.getBoolean(Settings.COLOR_READ, true);
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
            editText.setText(gNote.getNoteFromHtml());
            editText.setSelection(0);
        } else if (mode == MODE_SHOW) {
            int no = getIntent().getIntExtra("no", -1);
            LogUtil.i(TAG, "no:" + no);
            //传入了no表明是从定时任务传来，先取消通知栏显示，再表明需更新UI
            if (no != -1) {
                NotificationManager manager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(no);
                uiShouldUpdate();
            }
            textView.setText(gNote.getNoteFromHtml());
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
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
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
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
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
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
                pickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                return true;
            case R.id.action_cancel_remind:
                if (!gNote.isPassed()) {
                    gNote.setPassed(GNote.TRUE);
                    updateActionBarTitle();
                    checkDbFlag();
                }
                return true;
            case R.id.action_edit:
                activityStart(this, gNote, MODE_EDIT);
                finish();
                return true;
            case R.id.edit_date:
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                return true;
//            case R.id.action_move_ac:
//                launchFolderForResult();
//                return true;
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
        View view = getLayoutInflater().inflate(R.layout.dialog_radiogroup, (ViewGroup) getWindow().getDecorView
                (), false);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rg_dialog);
        RadioButton purenote = (RadioButton) view.findViewById(R.id.rb_purenote);
        boolean radioChecked = false;
        List<GNotebook> list = db.loadGNotebooks();
        for (final GNotebook gNotebook : list) {
            RadioButton tempButton = new RadioButton(mContext);
            tempButton.setText(gNotebook.getName());
            radioGroup.addView(tempButton, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
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

        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.action_move)
                .setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (tmpGnoteBookId != -1) {
                            dbFlag = DB_UPDATE;
                            changedFolder = true;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        changedFolder = false;
                    }
                })
                .create();
        dialog.show();
    }

    private void launchFolderForResult() {
        Intent intent = new Intent(this, Folder.class);
        intent.putExtra("mode", Folder.MODE_MOVE);
        startActivityForResult(intent, 0);

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (resultCode) {
//            case RESULT_OK:
//                bookId = data.getExtras().getInt(Folder.BOOK_ID_FOR_NOTE, -1);
//                LogUtil.i(TAG, "get gNotebook id:" + bookId);
//                if (bookId != -1) {
//                    launchFolder = true;
//                    checkDbFlag();
//                }
//                break;
//            default:
//                break;
//        }
//    }

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
                setTitle(R.string.alert).
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
                            uiShouldUpdate();
                        }
                        finish();
                    }
                }).show();
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
            AlarmService.cancelTask(Note.this, gNote);
        }
//        更新笔记本状态
        int notebookId = preferences.getInt(Folder.GNOTEBOOK_ID, 0);
        updateGNotebook(notebookId, -1, false);
        if (notebookId != 0) {
            updateGNotebook(0, -1, false);
        }
    }

    private void createNote() {
        gNote.setNoteToHtml(editText.getText());

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

        db.saveGNote(gNote);
//        更新笔记本
        updateGNotebook(groupId, +1, false);
        if (groupId != 0) {
            updateGNotebook(0, +1, false);
        }
        //当有定时提醒
        if (!gNote.isPassed()) {
            //新建记事尚无id，需存储后从数据库中提取
            gNote.setId(db.getNewestGNoteId());
            AlarmService.alarmTask(this);
        }
    }

    private void updateGNotebook(int id, int value, boolean isMove) {
        if (id == 0) {
            //如果是移动文件，不加不减
            if (isMove) return;
            int cnt = preferences.getInt(Folder.PURENOTE_NOTE_NUM, 3);
            preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, cnt + value).commit();
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

    private void updateNote() {
        gNote.setNoteToHtml(editText.getText());
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

        db.updateGNote(gNote);
        //当有定时提醒
        if (!gNote.isPassed()) {
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
     * 修改参数，表明返回MainActivity时需要更新UI
     */
    private void uiShouldUpdate() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MainActivity.UPDATE_UI, true).apply();
    }

    /**
     * 仅用于更新ActionBarTitle文字
     */
    private void updateActionBarTitle() {
        actionBar.setTitle(Util.timeString(gNote));
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
        String tmp = editText.getText().toString().trim();

        if (mode == MODE_NEW || MODE_TODAY == mode) {
            if (tmp.length() > 0) {
                dbFlag = DB_SAVE;
            }
        } else if (mode == MODE_EDIT) {
            if (tmp.length() > 0) {
                if (!tmp.equals(gNote.getNoteFromHtml().toString().trim())) {
                    dbFlag = DB_UPDATE;
                }
            } else {
                dbFlag = DB_DELETE;
            }
        }

        if (dbFlag == DB_SAVE) {
            createNote();
            uiShouldUpdate();

            addEditCount();
        } else if (dbFlag == DB_UPDATE) {
            updateNote();
            uiShouldUpdate();

            addEditCount();
        } else if (dbFlag == DB_DELETE) {
            deleteNote();
            uiShouldUpdate();

            addEditCount();
        }

        finish();
        overridePendingTransition(R.anim.out_push_up, R.anim.out_push_down);
    }

    private void addEditCount() {
        int count = preferences.getInt(EditCount, 0);
        if (count < EDIT_COUNT) {
            count++;
            preferences.edit().putInt(EditCount, count).apply();
        }
    }
}
