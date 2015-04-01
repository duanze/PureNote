package com.duanze.gasst;

import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.duanze.gasst.activity.NoteActivity;
import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.adapter.NoteAdapter;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.syn.Evernote;
import com.duanze.gasst.util.CallBackListener;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.Util;
import com.duanze.gasst.view.GridUnit;
import com.duanze.gasst.view.MyViewFlipper;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends FragmentActivity implements Evernote.EvernoteLoginCallback {
    public static final String TAG = "MainActivity";

    public static final String UPDATE_UI = "update_ui";

    public static final int MODE_LIST = 0;
    public static final int MODE_GRID = 1;
    private int mode;

    //MODE_LIST相关
    private NoteAdapter adapter;
    private SwipeMenuListView noteTitleListView;
    private SwipeMenuCreator creator;

    //MODE_GRID相关
    public static final int MODEL_ONE = 1;
    public static final int MODEL_TWO = 2;
    public static final int[] LAYOUT_ARRAY = {R.layout.sun, R.layout.mon, R.layout.tue,
            R.layout.wed, R.layout.thu, R.layout.fri, R.layout.sat};
    public static final int[] MODE_ARRAY = {1, 2, 1, 2, 1, 2, 1};
    public static final int[] LONG_GRID_ARRAY = {0, 0, 2, 2, 4, 4, 6};

    /**
     * 缓存视图
     */
    private View[] weekViews = new View[3];
    /**
     * 当前所在周标号
     */
    private int curWeekNo;
    private int nextWeekNo;
    private int previousWeekNo;

    /**
     * 缓存格子视图
     */
    private GridUnit[][] gridUnits = new GridUnit[3][7];
    private int model;
    private int longGrid;
    private int type;
    private int layout;

    private LayoutInflater inflater;
    private MyViewFlipper myViewFlipper;

    private Calendar tmpCal;

    //GRID相关到此结束

    /**
     * 获取当前屏幕密度
     */
    private DisplayMetrics dm;

    private DatePickerDialog datePickerDialog;
    private DatePickerDialog pickerDialog;
    public static final String DATEPICKER_TAG = "datepicker";

    private GNote gNote;

    private GNoteDB db;
    private List<GNote> gNoteList;
    private int index;

    private Calendar today;

    //启动时是否开启新记事
    private boolean isOpenNewNote;

    //列表-过期记事是否折叠
    private boolean isFold;

    //彩格-记事格子随机填色
    private boolean randomColor;

    //列表模式-最大显示行数
    private int maxLines;

    public boolean isCustomizeColor() {
        return customizeColor;
    }

    private boolean customizeColor;

    SharedPreferences preferences;

    //nice FloatingButton
    private FloatingActionButton fabButton;

    private MenuItem bindItem;
    private Evernote mEvernote;

    public int getMaxLines() {
        return maxLines;
    }

    public boolean getIsFold() {
        return isFold;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public Calendar getToday() {
        return today;
    }

    private CallBackListener listener = new CallBackListener() {
        @Override
        public void onFinish(String result) {
            gNote.setAlertTime(result);
            gNote.setPassed(GNote.FALSE);
            db.updateGNote(gNote);
            AlarmService.alarmTask(MainActivity.this);

            refreshUI();
        }

        @Override
        public void onError(Exception e) {

        }
    };

    private void loginSuccess() {
        bindItem.setTitle(R.string.syn_evernote);
        bindItem.setIcon(R.drawable.flags);
    }

    private void logoutSuccess() {
        bindItem.setTitle(R.string.bind_evernote);
        bindItem.setIcon(R.drawable.evernote);
    }

    @Override
    public void onLoginResult(Boolean result) {
        if (result) {
            loginSuccess();
            //make sure notebook exists
            mEvernote.createNotebook(Evernote.NOTEBOOK_NAME);
        }
    }

    @Override
    public void onUserinfo(Boolean result, User user) {

    }

    @Override
    public void onLogout(Boolean reuslt) {

    }

    private class MyDatePickerListener implements OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
            GNote gNote = new GNote();
            gNote.setTime(year
                    + ","
                    + Util.twoDigit(month)
                    + ","
                    + Util.twoDigit(day));
            NoteActivity.activityStart(MainActivity.this, gNote, NoteActivity.MODE_NEW);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initValues();
        //如果设置了启动时进入新记事
        if (isOpenNewNote) {
            writeNewNote(today);
        }

        boolean fullScreen = preferences.getBoolean(Settings.FULL_SCREEN, false);
        //如果设置了全屏
        if (fullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        //判断所需使用的布局
        mode = preferences.getInt("mode", MODE_LIST);
        setContent();
        setOverflowShowingAlways();


        //test code
//        TypedArray array = getTheme().obtainStyledAttributes(new int[]{
//                android.R.attr.colorBackground,
//                android.R.attr.textColorPrimary,
//        });
//        int backgroundColor = array.getColor(0, 0xFF00FF);
//        int textColor = array.getColor(1, 0xFF00FF);
//        array.recycle();
//        LogUtil.i(TAG, "backgroundColor" + backgroundColor);
//        LogUtil.i(TAG, "backgroundColor:tohex" + Integer.toHexString(backgroundColor));
    }

    private void initValues() {
        preferences = getSharedPreferences(Settings.DATA, MODE_PRIVATE);
        dm = getResources().getDisplayMetrics();
        today = Calendar.getInstance();
        db = GNoteDB.getInstance(this);

        datePickerDialog = DatePickerDialog.newInstance(new MyDatePickerListener(),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH), false);
        datePickerDialog.setYearRange(today.get(Calendar.YEAR) - 10,
                today.get(Calendar.YEAR) + 10);
        datePickerDialog.setCloseOnSingleTapDay(true);

        //如果是第一次启动应用，在数据库中添加note
        firstLaunch();
        gNoteList = db.loadGNotes();

        readSetting();
        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        window.getDefaultDisplay().getSize(point);
        screenWidth = point.x;
        LogUtil.i(TAG, "width: " + screenWidth);
        leftEdge = -screenWidth;
        rightEdge = screenWidth;
        defaultSpeed = screenWidth / 12;

        //evernote
        mEvernote = new Evernote(this, this);
    }

    private void firstLaunch() {
        //如果是第一次启动应用，在数据库中添加note
        boolean first = preferences.getBoolean("first", true);
        if (first) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("first", false);
            editor.apply();

            GNote one = new GNote();
            one.setTime(today.get(Calendar.YEAR)
                    + ","
                    + Util.twoDigit(today.get(Calendar.MONTH))
                    + ","
                    + Util.twoDigit(today.get(Calendar.DAY_OF_MONTH)));
            one.setNote(getResources().getString(R.string.tip1) + "(・8・)");
            db.saveGNote(one);

            GNote two = new GNote();
            two.setTime(today.get(Calendar.YEAR)
                    + ","
                    + Util.twoDigit(today.get(Calendar.MONTH))
                    + ","
                    + Util.twoDigit(today.get(Calendar.DAY_OF_MONTH) - 1));
            two.setNote(getResources().getString(R.string.tip2));
            db.saveGNote(two);

            GNote three = new GNote();
            three.setTime(today.get(Calendar.YEAR)
                    + ","
                    + Util.twoDigit(today.get(Calendar.MONTH))
                    + ","
                    + Util.twoDigit(today.get(Calendar.DAY_OF_MONTH) - 2));
            three.setNote(getResources().getString(R.string.tip3));
            db.saveGNote(three);
        }
    }

    /**
     * 根据模式设置布局
     */
    public void setContent() {
        if (mode == MODE_LIST) {
            initModeList();
        } else if (mode == MODE_GRID) {
            initModeGrid();

        }
    }

    private void initModeList() {
        if (adapter == null) {
            adapter = new NoteAdapter(this, R.layout.list_item, gNoteList);
            adapter.setValues(this);
        }

        if (pickerDialog == null) {
            pickerDialog = DatePickerDialog.newInstance(new MyPickerListener(this, today, listener),
                    today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH), false);
            pickerDialog.setYearRange(today.get(Calendar.YEAR) - 10,
                    today.get(Calendar.YEAR) + 10);
            pickerDialog.setCloseOnSingleTapDay(true);
        }

        setContentView(R.layout.classic_list);
        noteTitleListView = (SwipeMenuListView) findViewById(R.id.list_view);
        noteTitleListView.setAdapter(adapter);
        noteTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GNote gNote = gNoteList.get(i);
                NoteActivity.activityStart(
                        MainActivity.this, gNote, NoteActivity.MODE_EDIT);
            }
        });

        if (creator == null) {
            creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    // create "remind" item
                    SwipeMenuItem remindItem = new SwipeMenuItem(
                            getApplicationContext());
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
                            getApplicationContext());
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
                        pickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                        break;
                    case 1:
                        // delete
                        gNote = gNoteList.get(position);
                        db.deleteGNote(gNoteList.get(position).getId());
                        refreshUI();
                        if (!gNote.isPassed()) {
                            AlarmService.cancelTask(MainActivity.this, gNote);
                        }
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        fabButton = (FloatingActionButton) findViewById(R.id.fabbutton);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeNewNote(today);
            }
        });
        fabButton.listenTo(noteTitleListView);
    }

    private void initModeGrid() {
        if (myViewFlipper != null) {
            setContentView(myViewFlipper);
            return;
        }
        inflater = getLayoutInflater();
        model = MODE_ARRAY[today.get(Calendar.DAY_OF_WEEK) - 1];
        longGrid = LONG_GRID_ARRAY[today.get(Calendar.DAY_OF_WEEK) - 1];
        type = today.get(Calendar.DAY_OF_WEEK) - 1;
        layout = LAYOUT_ARRAY[type];

        myViewFlipper = new MyViewFlipper(this);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams
                .MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        myViewFlipper.setLayoutParams(params);

        curWeekNo = 1;
        nextWeekNo = curWeekNo + 1;
        previousWeekNo = curWeekNo - 1;

        for (int i = 0; i < 3; i++) {
            weekViews[i] = inflateView(inflater, i);
            myViewFlipper.addView(weekViews[i]);
        }
        gridRefresh();
        myViewFlipper.setDisplayedChild(curWeekNo);
        setTouchListener();
        setContentView(myViewFlipper);
    }

    private void setTouchListener() {
        myViewFlipper.setOnTouchListener(touchListener);
//            myViewFlipper.setFocusable(true);
//            myViewFlipper.setClickable(true);
        myViewFlipper.setLongClickable(true);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                gridUnits[i][j].setOnTouchListener(touchListener);
            }
        }
    }

    /**
     * 仅用于MODE_GRID
     *
     * @param inflater
     * @param i        填充的二维数组unitMyViews行号
     * @return
     */
    private View inflateView(LayoutInflater inflater, int i) {
        View view = inflater.inflate(layout, null, false);
        // 以下情况为布局模式一，sun在第一位
        if (model == MODEL_ONE) {
            gridUnits[i][0] = (GridUnit) view.findViewById(R.id.sun);
            gridUnits[i][1] = (GridUnit) view.findViewById(R.id.mon);
            gridUnits[i][2] = (GridUnit) view.findViewById(R.id.tue);
            gridUnits[i][3] = (GridUnit) view.findViewById(R.id.wed);
            gridUnits[i][4] = (GridUnit) view.findViewById(R.id.thu);
            gridUnits[i][5] = (GridUnit) view.findViewById(R.id.fri);
            gridUnits[i][6] = (GridUnit) view.findViewById(R.id.sat);
        } else if (model == MODEL_TWO) {// 布局模式二,sun在最后
            gridUnits[i][0] = (GridUnit) view.findViewById(R.id.mon);
            gridUnits[i][1] = (GridUnit) view.findViewById(R.id.tue);
            gridUnits[i][2] = (GridUnit) view.findViewById(R.id.wed);
            gridUnits[i][3] = (GridUnit) view.findViewById(R.id.thu);
            gridUnits[i][4] = (GridUnit) view.findViewById(R.id.fri);
            gridUnits[i][5] = (GridUnit) view.findViewById(R.id.sat);
            gridUnits[i][6] = (GridUnit) view.findViewById(R.id.sun);
        }
        return view;
    }


    /**
     * 读取应用设置数据
     */
    private void readSetting() {
        //是否启动时开启新记事
        isOpenNewNote = preferences.getBoolean(Settings.NEW_NOTE, false);
        //是否过期记事折叠
        isFold = preferences.getBoolean(Settings.FOLD, false);
        //记事格子随机填色
        randomColor = preferences.getBoolean(Settings.RANDOM_COLOR, true);
        //最大显示行数
        maxLines = preferences.getInt(Settings.MAX_LINES, 5);

        customizeColor = preferences.getBoolean(Settings.CUSTOMIZE_COLOR, true);
    }


    /**
     * 以cal为日期写一篇新记事
     */
    private void writeNewNote(Calendar cal) {
        GNote note = new GNote();
        note.setCalToTime(cal);
        NoteActivity.activityStart(this, note, NoteActivity.MODE_NEW);
    }

    /**
     * 重写此方法，检查SharedPreferences,看是否需更新UI
     */
    @Override
    protected void onResume() {
        super.onResume();

        boolean settingsChanged = preferences.getBoolean(Settings.SETTINGS_CHANGED, false);
        //如果改变了设置
        if (settingsChanged) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Settings.SETTINGS_CHANGED, false).apply();

            readSetting();
        }

        boolean updateUI = preferences.getBoolean(UPDATE_UI, false);
        //是否因各种改变而需要进行UI刷新
        if (updateUI || settingsChanged) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(UPDATE_UI, false);
            editor.apply();

            refreshUI();
        }

        if (mode == MODE_LIST) {
            Util.randomBackground(fabButton);
        }

        //evernote
        if (bindItem != null) {
            if (mEvernote.isLogin()) {
                loginSuccess();
            } else {
                logoutSuccess();
            }
        }
    }

    private void refreshUI() {
        gNoteList.clear();
        List<GNote> tmpList = db.loadGNotes();
        for (GNote g : tmpList) {
            gNoteList.add(g);
        }

        if (mode == MODE_LIST) {
            listRefresh();
        } else if (mode == MODE_GRID) {
            gridRefresh();
        }
    }


    /**
     * 仅用于MODE_GRID
     */
    public void gridRefresh() {
        index = gNoteList.size() - 1;
        tmpCal = (Calendar) today.clone();
        tmpCal.add(Calendar.DAY_OF_MONTH, -7);
        if (model == MODEL_ONE) {
            // 先得到星期日
            tmpCal.add(Calendar.DAY_OF_MONTH, -type);
        } else if (model == MODEL_TWO) {
            // 先得到星期一
            tmpCal.add(Calendar.DAY_OF_MONTH, -(type - 1));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                if (j == longGrid) {
                    gridUnits[i][j].setViewUnit(tmpCal, true);
                } else {
                    gridUnits[i][j].setViewUnit(tmpCal, false);
                }
                gridUnits[i][j].setBackgroundColor(GridUnit.THRANSPARENT);

                final TextView button = gridUnits[i][j].getDoneButton();
                final GridUnit gridUnit = gridUnits[i][j];
                final Calendar cal = (Calendar) tmpCal.clone();

                boolean isSetClick = false;
                if (index >= 0) {
                    //对格子中是否应有note进行判定

                    //当日期比现在的日期小，持续加大
                    while (gNoteList.get(index).compareToCalendar(tmpCal) < 0 && index > 0) {
                        index--;
                    }
                    if (gNoteList.get(index).compareToCalendar(tmpCal) == 0) {
                        final GNote note = gNoteList.get(index);
                        gridUnit.setViewNote(note.getNote());

                        //判断格子色彩
                        if (randomColor) {
                            gridUnit.randomSetBackground();
                        } else {
                            if (customizeColor) {
                                gridUnit.setBackgroundColor(note.getColor());
                            } else {
                                gridUnits[i][j].setBackgroundColor(GridUnit.THRANSPARENT);
                            }
                        }

                        button.setVisibility(View.VISIBLE);
                        if (!note.isDone()) {
                            gridUnit.removeStrike();
                            button.setText(R.string.done);

                            //设置可点击
                            gridUnit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    NoteActivity.activityStart(MainActivity.this, note,
                                            NoteActivity.MODE_EDIT);
                                }
                            });
                        } else if (note.isDone()) {
                            gridUnit.addStrike();
                            button.setText(R.string.undone);

                            gridUnit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    writeNewNote(cal);
                                }
                            });
                        }
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!note.isDone()) {
                                    button.setTextColor(GridUnit.DIM_GREY);
                                    button.setText(R.string.undone);
                                    gridUnit.addStrike();
                                    note.setDone(GNote.TRUE);
                                    db.updateGNote(note);

                                    gridUnit.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            writeNewNote(cal);
                                        }
                                    });

                                } else if (note.isDone()) {
                                    button.setTextColor(GridUnit.GREY);
                                    button.setText(R.string.done);
                                    gridUnit.removeStrike();
                                    note.setDone(GNote.FALSE);
                                    db.updateGNote(note);

                                    //设置可点击
                                    gridUnit.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            NoteActivity.activityStart(MainActivity.this, note,
                                                    NoteActivity.MODE_EDIT);
                                        }
                                    });
                                }
                            }
                        });

                        isSetClick = true;
                    }
                }
                if (!isSetClick) {
                    button.setVisibility(View.GONE);
                    gridUnit.setViewNote("");
                    gridUnit.removeStrike();
                    gridUnit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            writeNewNote(cal);
                        }
                    });
                }
                tmpCal.add(Calendar.DAY_OF_MONTH, +1);
            }
        }
    }

    /**
     * 仅用于MODE_LIST
     * 列表模式下的更新
     */
    public void listRefresh() {
        adapter.setValues(this);
        adapter.notifyDataSetChanged();
        noteTitleListView.setSelection(0);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
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
    protected void onDestroy() {
        super.onDestroy();
    }

    //以下为滑动效果相关
    /**
     * VIEW滚动时的默认速度
     */
    private int defaultSpeed;

    /**
     * View滚动时，让线程休眠时间，目的让用户的眼睛可以看到图片滚动的效果
     */
    private final static int SLEEP_TIME = 20;

    /**
     * 记录手指按下时的X轴的坐标
     */
    private float xDown;

    /**
     * 记录手指移动的时候的X轴的坐标
     */
    private float xMove;

    /**
     * 记录手指抬起时的X轴的坐标
     */
    private float xUp;

    private int xDistance;
    /**
     * 滚动显示view时，手指滑动需要达到的速度。
     */
    public static final int SNAP_VELOCITY = 200;

    /**
     * 用于计算手指滑动的速度。
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 屏幕宽度值。
     */
    private int screenWidth;

    /**
     * 当前view最多可以滑动到的左边缘，为-screenWidth
     */
    private int leftEdge;

    /**
     * 当前view最多可以滑动到的右边缘，为screenWidth
     */
    private int rightEdge;

    private boolean interrupt = false;

    private void changeViewport(int xDistance) {
        weekViews[curWeekNo].scrollTo(xDistance, 0);

        //在ViewFlipper工作机制中，某一个时间点只有当前的子View是VISIBLE状态，其他的子View都是GONE状态。
        //所以要在滑动的时候看见他们，必须将他们设置为VISIBLE状态
        if (xDistance > 0 && curWeekNo != 2) {
            weekViews[nextWeekNo].setVisibility(View.VISIBLE);
            //将下一个ImageView移动到(xNext, 0)位置
            int xNext = -screenWidth + xDistance;
            weekViews[nextWeekNo].scrollTo(xNext, 0);

        } else if (xDistance < 0 && curWeekNo != 0) {
            weekViews[previousWeekNo].setVisibility(View.VISIBLE);
            //将上一个ImageView移动到(xLast, 0)位置
            int xLast = screenWidth + xDistance;
            weekViews[previousWeekNo].scrollTo(xLast, 0);
        }

    }

    private OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            createVelocityTracker(motionEvent);
            switch (motionEvent.getAction()) {
                //手指按下时的逻辑
                case MotionEvent.ACTION_DOWN:
                    LogUtil.i(TAG, "MotionEvent.ACTION_DOWN");
                    xDown = motionEvent.getRawX();
                    break;
                //手指移动时的逻辑
                case MotionEvent.ACTION_MOVE:
                    LogUtil.i(TAG, "ACTION_MOVE");
                    xMove = motionEvent.getRawX();
                    //手指滑动的距离
                    xDistance = -(int) (xMove - xDown);
                    LogUtil.i(TAG, "-----------------dist = " + xDistance);

                    changeViewport(xDistance);
                    //当有移动发生，截断点击
                    if (Math.abs(xDistance) > 10) {
                        interrupt = true;
                    }
                    break;
                //手指抬起时的逻辑
                case MotionEvent.ACTION_UP:
                    LogUtil.i(TAG, "ACTION_UP");
                    xUp = motionEvent.getRawX();
                    //判断用户手指的意图，这块可以自己改写逻辑
                    if (wantToShowPrevious()) {
                        if (shouldScrollToPrevious()) {
                            scrollToPrevious();
                        } else {
                            reset();
                        }
                    } else if (wantToShowNext()) {
                        if (shouldScrollToNext()) {
                            scrollToNext();
                        } else {
                            reset();
                        }
                    }
                    recycleVelocityTracker();
                    if (interrupt) {
                        interrupt = false;
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private void reset() {
        if (curWeekNo != 0) {
            weekViews[previousWeekNo].setVisibility(View.GONE);
            weekViews[previousWeekNo].scrollTo(0, 0);
        }
        if (curWeekNo != 2) {
            weekViews[nextWeekNo].setVisibility(View.GONE);
            weekViews[nextWeekNo].scrollTo(0, 0);
        }
        weekViews[curWeekNo].scrollTo(0, 0);
    }

    static final int NEXT = 0;
    static final int PREVIOUS = 1;


    private void complete(int flag) {
        weekViews[curWeekNo].setVisibility(View.GONE);
        //将当前（移动发生之前）的ImageView移动到（0，0）位置因为在滑动时它的位置被改变
        weekViews[curWeekNo].scrollTo(0, 0);
        if (flag == NEXT) {
            curWeekNo = curWeekNo + 1;
        } else if (flag == PREVIOUS) {
            curWeekNo = curWeekNo - 1;
        }

        nextWeekNo = curWeekNo + 1;
        previousWeekNo = curWeekNo - 1;
        myViewFlipper.setDisplayedChild(curWeekNo);
    }

    private boolean wantToShowNext() {
        return xUp - xDown < 0;
    }

    private boolean wantToShowPrevious() {
        return xUp - xDown > 0;
    }

    private boolean shouldScrollToNext() {
        return curWeekNo != 2 && (xDown - xUp > screenWidth / 5 || getScrollVelocity() >
                SNAP_VELOCITY);
    }

    private boolean shouldScrollToPrevious() {
        return curWeekNo != 0 && (xUp - xDown > screenWidth / 5 || getScrollVelocity() > SNAP_VELOCITY);
    }

    private void scrollToNext() {
        new ScrollTask().execute(defaultSpeed);
    }

    private void scrollToPrevious() {
        new ScrollTask().execute(-defaultSpeed);
    }

    /**
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
     *
     * @param event content界面的滑动事件
     */
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 获取手指在content界面滑动的速度。
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            int curLocation = xDistance;
            // 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
            while (true) {
                curLocation = curLocation + speed[0];
                if (curLocation > rightEdge) {
                    curLocation = rightEdge;
                    break;
                }
                if (curLocation < leftEdge) {
                    curLocation = leftEdge;
                    break;
                }
                publishProgress(curLocation);
                // 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
                sleep(SLEEP_TIME);
            }

            return curLocation;
        }

        @Override
        protected void onProgressUpdate(Integer... curLocation) {
            changeViewport(curLocation[0]);
        }

        @Override
        protected void onPostExecute(Integer curLocation) {
            changeViewport(curLocation);
            if (curLocation > 0) {
                complete(NEXT);
            } else if (curLocation < 0) {
                complete(PREVIOUS);
            }
        }
    }

    /**
     * 使当前线程睡眠指定的毫秒数。
     *
     * @param millis 指定当前线程睡眠多久，以毫秒为单位
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //滑动效果相关至此结束

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.getItem(1);
        Menu m = item.getSubMenu();

        //just for initialize layout mode submenu
        MenuItem[] items = new MenuItem[2];
        items[0] = m.getItem(0);
        items[1] = m.getItem(1);
        if (mode == MODE_GRID) {
            items[0].setChecked(true);
        } else if (mode == MODE_LIST) {
            items[1].setChecked(true);
        }

        //bind-evernote item
        bindItem = menu.getItem(2);
        //evernote
        if (mEvernote.isLogin()) {
            loginSuccess();
        } else {
            logoutSuccess();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
            case R.id.feedback:
                feedback();
                break;
            case R.id.evaluate:
                evaluate(MainActivity.this);
                break;
            case R.id.setting:
                Settings.activityStart(this);
                break;
            case R.id.action_plus:
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                break;

            case R.id.m_color_grid:
                if (!mi.isChecked()) {
                    mi.setChecked(true);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("mode", MainActivity.MODE_GRID).apply();
                    mode = MODE_GRID;
                    setContent();
                    gridRefresh();
                }
                break;
            case R.id.m_classic_list:
                if (!mi.isChecked()) {
                    mi.setChecked(true);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("mode", MainActivity.MODE_LIST).apply();
                    mode = MODE_LIST;
                    setContent();
                    listRefresh();
                }
                break;

            //evernote
            case R.id.bind_evernote:
                if (mi.getTitle() == getResources().getString(R.string.bind_evernote)) {
                    mEvernote.auth();
                } else {
//                    mEvernote.uploadData(gNoteList);
                }
            default:
                break;
        }
        return true;
    }

    /**
     * Called when the control returns from an activity that we launched.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                mEvernote.onAuthFinish(resultCode);
                break;
        }
    }

    private void feedback() {
        // 必须明确使用mailto前缀来修饰邮件地址
        Uri uri = Uri.parse("mailto:端泽<blue3434@qq.com>");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        // intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, "PureNote用户反馈"); // 主题
        intent.putExtra(Intent.EXTRA_TEXT, ""); // 正文
        startActivity(Intent.createChooser(intent, "Select email client"));
    }

    private void evaluate(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Couldn't launch the market!",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
