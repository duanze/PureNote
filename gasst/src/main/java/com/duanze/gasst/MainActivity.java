package com.duanze.gasst;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.duanze.gasst.activity.Folder;
import com.duanze.gasst.activity.NoteActivity;
import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.fragment.ClassicList;
import com.duanze.gasst.fragment.ColorGrid;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.syn.Evernote;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.view.GridUnit;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements Evernote.EvernoteLoginCallback {
    public static final String TAG = "MainActivity";

    public static final String ShownRate = "shown_rate";
    public static final String UPDATE_UI = "update_ui";

    public static final int MODE_LIST = 0;
    public static final int MODE_GRID = 1;
    private int mode;
    private Context mContext;

    /**
     * 获取当前屏幕密度
     */
    private DisplayMetrics dm;

    private DatePickerDialog datePickerDialog;
    public static final String DATEPICKER_TAG = "datepicker";

    private Calendar today;
    private GNoteDB db;

    //启动时是否开启新记事
    private boolean isOpenNewNote;

    //列表-过期记事是否折叠
    private boolean isFold;

    public boolean isRandomColor() {
        return randomColor;
    }

    //彩格-记事格子随机填色
    private boolean randomColor;

    //列表模式-最大显示行数
    private int maxLines;

    public boolean isCustomizeColor() {
        return customizeColor;
    }

    private boolean customizeColor;

    public int getgNotebookId() {
        return gNotebookId;
    }

    //当前所用的笔记本id
    private int gNotebookId;

    public SharedPreferences getPreferences() {
        return preferences;
    }

    SharedPreferences preferences;

    private MenuItem bindItem;
    private Evernote mEvernote;

    private Timer syncTimer;

    private final int DIALOG_PROGRESS = 101;
    private final int INITIAL = 102;

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

    private void loginNow() {
        bindItem.setTitle(R.string.syn_evernote);
//        bindItem.setIcon(R.drawable.flags);
    }

    private void logoutNow() {
        bindItem.setTitle(R.string.bind_evernote);
//        bindItem.setIcon(R.drawable.evernote);
    }

    @Override
    public void onLoginResult(Boolean result) {
        if (result) {
            loginNow();
            mEvernote.sync(true, true, new SyncHandler());
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
            gNote.setTimeFromDate(year, month, day);
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

        if (mEvernote.isLogin()) {
            mEvernote.sync(true, true, null);
        }
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
//        LogUtil.i(TAG, "onCreate before ... end.");
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                return new ProgressDialog(MainActivity.this);
            case INITIAL:
                return new ProgressDialog(MainActivity.this);
        }
        // TODO onCreateDialog(int) is deprecated
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_PROGRESS:
                ((ProgressDialog) dialog).setIndeterminate(true);
                dialog.setCancelable(false);
                ((ProgressDialog) dialog).setMessage(getString(com.evernote.androidsdk.R.string.esdk__loading));
                break;
            case INITIAL:
                ((ProgressDialog) dialog).setIndeterminate(true);
                dialog.setCancelable(false);
                ((ProgressDialog) dialog).setMessage(getString(R.string.initial));
                break;
        }
    }

    private void initValues() {
        mContext = this;
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
        readSetting();

        //        below_version_2.0.4
        if (preferences.getBoolean("below_version_2.0.4", true)) {
            showDialog(INITIAL);
            preferences.edit().putBoolean("below_version_2.0.4", false).commit();
            calNoteNum();
            removeDialog(INITIAL);
        }

        //evernote
        mEvernote = new Evernote(this, this);
    }

    private int calNoteNum() {
        int n = preferences.getInt(Folder.PURENOTE_NOTE_NUM, -1);
        if (n == -1) {
//            将所有原有数据转移至默认文件夹中
            List<GNote> list = db.loadGNotes();
            for (GNote gNote : list) {
                gNote.setGNotebookId(0);
                db.updateGNote(gNote);
            }
            n = list.size();
            preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, n).commit();
        }
        return n;
    }

    private void firstLaunch() {
        //如果是第一次启动应用，在数据库中添加note
        boolean first = preferences.getBoolean("first", true);
        if (first) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("first", false);
            editor.apply();

            GNote one = new GNote();
            one.setCalToTime(today);
            one.setNote(getResources().getString(R.string.tip1) + "(・8・)");
//            one.setSynStatus(GNote.NEW);
            one.setColor(GridUnit.PURPLE);
            db.saveGNote(one);

            GNote two = new GNote();
            two.setTimeFromDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH) - 1);
            two.setNote(getResources().getString(R.string.tip2));
//            two.setSynStatus(GNote.NEW);
            db.saveGNote(two);

            GNote three = new GNote();
            three.setTimeFromDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH) - 2);
            three.setNote(getResources().getString(R.string.tip3));
//            three.setSynStatus(GNote.NEW);
            db.saveGNote(three);

//            GNotebook test = new GNotebook();
//            test.setName("Test");
//            db.saveGNotebook(test);
        }
    }

    private ClassicList classicList;
    private ColorGrid colorGrid;

    public void setContent() {
        setContentView(R.layout.activity_main);

        if (mode == MODE_LIST) {
            if (classicList == null) {
                classicList = new ClassicList();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_content, classicList)
                    .commit();
        } else if (mode == MODE_GRID) {
            if (colorGrid == null) {
                colorGrid = new ColorGrid();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_content, colorGrid)
                    .commit();
        }
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

        gNotebookId = preferences.getInt(Folder.GNOTEBOOK_ID, 0);
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
        boolean updateUI = preferences.getBoolean(UPDATE_UI, false);
        //是否因各种改变而需要进行UI刷新
        if (updateUI || settingsChanged) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(UPDATE_UI, false);
            editor.putBoolean(Settings.SETTINGS_CHANGED, false).apply();
            editor.apply();

            readSetting();
            refreshUI();

            LogUtil.i(TAG, "readingSetting & updateUI in onResume.");
        }

        if (mode == MODE_LIST) {
            classicList.randomfabButtonColor();
        }


        //evernote
        if (bindItem != null) {
            if (mEvernote.isLogin()) {
                loginNow();
            } else {
                logoutNow();
            }
        }

        if (mEvernote.isLogin()) {
            syncTimer = new Timer();
            LogUtil.i(TAG, "启动自动更新任务");
            syncTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    mEvernote.sync(true, true, null);
                }
            }, 30000, 50000);
        }

        rateForPureNote();
        //        umeng
        MobclickAgent.onResume(this);
    }

    //    一次性评分弹窗
    private void rateForPureNote() {
        if (preferences.getInt(NoteActivity.EditCount, 0) >= 5
                && preferences.getBoolean(ShownRate, false) == false) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.rate_for_purenote)
                    .setPositiveButton(R.string.rate_rate,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Uri uri = Uri.parse("market://details?id="
                                            + mContext.getPackageName());
                                    Intent goToMarket = new Intent(
                                            Intent.ACTION_VIEW, uri);
                                    try {
                                        startActivity(goToMarket);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(mContext,
                                                "Couldn't launch the market!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            })
                    .setNegativeButton(R.string.rate_feedback,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    feedback();
                                }
                            }).create().show();

            preferences.edit().putBoolean(ShownRate, true).commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (syncTimer != null) {
            LogUtil.i(TAG, "结束定时同步任务");
            syncTimer.cancel();
        }

        //        umeng
        MobclickAgent.onPause(this);
    }

    private void refreshUI() {
        if (mode == MODE_LIST) {
            if (classicList != null) {
                classicList.refreshUI();
            }
        } else if (mode == MODE_GRID) {
            if (colorGrid != null) {
                colorGrid.refreshUI();
            }
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
    protected void onDestroy() {
        super.onDestroy();
    }

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
            loginNow();
        } else {
            logoutNow();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
            case R.id.feedback:
                feedback();
                break;
//            case R.id.evaluate:
//                evaluate(MainActivity.this);
//                break;
            case R.id.folder:
                Folder.activityStart(mContext, Folder.MODE_FOLDER);
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
                    preferences.edit().putInt("mode", MainActivity.MODE_GRID).apply();
                    mode = MODE_GRID;
                    setContent();
                }
                break;
            case R.id.m_classic_list:
                if (!mi.isChecked()) {
                    mi.setChecked(true);
                    preferences.edit().putInt("mode", MainActivity.MODE_LIST).apply();
                    mode = MODE_LIST;
                    setContent();
                }
                break;

            //evernote
            case R.id.bind_evernote:
                if (!mEvernote.isLogin()) {
                    mEvernote.auth();
                } else {
                    mEvernote.sync(true, true, new SyncHandler());
                }
            default:
                break;
        }
        return true;
    }

    class SyncHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Evernote.SYNC_START:
//                    findViewById(R.id.sync_progress).setVisibility(View.VISIBLE);
                    showDialog(DIALOG_PROGRESS);
                    break;
                case Evernote.SYNC_END:
//                    findViewById(R.id.sync_progress).setVisibility(View.GONE);
                    removeDialog(DIALOG_PROGRESS);
                    refreshUI();
                    break;
                default:
                    break;
            }
        }
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
