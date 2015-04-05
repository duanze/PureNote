package com.duanze.gasst.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.duanze.gasst.MainActivity;
import com.duanze.gasst.MyPickerListener;
import com.duanze.gasst.R;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.util.CallBackListener;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.Util;
import com.duanze.gasst.view.GridUnit;
import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;

public class NoteActivity extends FragmentActivity {
    public static final String TAG = "NoteActivity";

    public static final int MODE_NEW = 0;
    public static final int MODE_SHOW = 1;
    public static final int MODE_EDIT = 2;

    private int mode;
    private GNote gNote;
    private EditText editText;
    private TextView textView;
    private GNoteDB db;
    private ActionBar actionBar;
    private Calendar today;

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

    private boolean customizeColor;

    /**
     * 原本是否有定时提醒
     */
    private int isPassed;

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
        Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra("gAsstNote_data", gNote);
        intent.putExtra("mode", mode);
        context.startActivity(intent);
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
        boolean fullScreen = preferences.getBoolean(Settings.FULL_SCREEN, false);
        //如果设置了全屏
        if (fullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.note);
        setOverflowShowingAlways();

        initValues();
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        updateActionBarTitle();
    }

    private void initValues() {
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

        db = GNoteDB.getInstance(this);

        textView = (TextView) findViewById(R.id.tv_note_show);
        editText = (EditText) findViewById(R.id.et_note_edit);
        initMode();

        customizeColor = preferences.getBoolean(Settings.CUSTOMIZE_COLOR, true);
        if ((mode == MODE_NEW || mode == MODE_EDIT) && customizeColor) {
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
        if (colorRead && customizeColor) {
            if (mode == MODE_SHOW) {
                textView.setBackgroundColor(gNote.getColor());
            } else {
                editText.setBackgroundColor(gNote.getColor());
            }
        }
    }

    private void initMode() {
        mode = getIntent().getIntExtra("mode", 0);
        if (mode == MODE_NEW || mode == MODE_EDIT) {
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
        chooseColorBtns[i].setBackground(chooseColorBtnDrawables[i]); // 设置背景（效果就是有边框及底色）

    }

    private void checkDbFlag() {
        if (mode == MODE_EDIT || mode == MODE_SHOW) {
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
            getMenuInflater().inflate(R.menu.note_menu, menu);
        } else {//if (mode == MODE_NEW || mode == MODE_EDIT)
            getMenuInflater().inflate(R.menu.new_note_menu, menu);
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
            default:
                return true;
        }
    }

    /**
     * 分享按钮
     */
    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.action_share);
        String text;
        if (mode == MODE_NEW || mode == MODE_EDIT) {
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
                        if (mode != MODE_NEW) {
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
            AlarmService.cancelTask(NoteActivity.this, gNote);
        }
    }

    private void createNote() {
        gNote.setNoteToHtml(editText.getText());

        //needCreate
        gNote.setSynStatus(GNote.NEW);

        db.saveGNote(gNote);
        //当有定时提醒
        if (!gNote.isPassed()) {
            //新建记事尚无id，需存储后从数据库中提取
            gNote.setId(db.getNewestGNoteId());
            AlarmService.alarmTask(this);
        }
    }

    private void updateNote() {
        gNote.setNoteToHtml(editText.getText());
        if (gNote.getSynStatus() == GNote.NOTHING) {
            //needUpdate
            gNote.setSynStatus(GNote.UPDATE);
        }

        db.updateGNote(gNote);
        //当有定时提醒
        if (!gNote.isPassed()) {
            AlarmService.alarmTask(this);
        } else if (isPassed == GNote.FALSE) {//手动取消定时提醒
            AlarmService.cancelTask(this, gNote);
        }
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

        if (mode == MODE_NEW) {
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
        } else if (dbFlag == DB_UPDATE) {
            updateNote();
            uiShouldUpdate();
        } else if (dbFlag == DB_DELETE) {
            deleteNote();
            uiShouldUpdate();
        }

        finish();
        overridePendingTransition(R.anim.out_push_up, R.anim.out_push_down);
    }
}
