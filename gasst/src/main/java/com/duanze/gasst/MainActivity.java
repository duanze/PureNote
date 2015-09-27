package com.duanze.gasst;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.duanze.gasst.activity.About;
import com.duanze.gasst.activity.Folder;
import com.duanze.gasst.activity.Note;
import com.duanze.gasst.activity.Password;
import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.adapter.DrawerNotebookAdapter;
import com.duanze.gasst.fragment.ColorGrid;
import com.duanze.gasst.fragment.FolderFooter;
import com.duanze.gasst.fragment.FolderFooterDelete;
import com.duanze.gasst.fragment.FooterInterface;
import com.duanze.gasst.fragment.GNoteList;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.syn.Evernote;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.ProviderUtil;
import com.duanze.gasst.util.Util;
import com.duanze.gasst.view.GridUnit;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements Evernote.EvernoteLoginCallback,
        FooterInterface, CompoundButton.OnCheckedChangeListener {
    public static final String TAG = "MainActivity";

    public static final boolean TINT_STATUS_BAR = false;

    // version code
    private int versionCode;

    public static final String ShownRate = "shown_rate";
    public static final String UPDATE_UI = "update_ui";

    public static final int MODE_LIST = 0;
    public static final int MODE_GRID = 1;
    private int mode;
    private Context mContext;

    /**
     * 是否正在显示回收站
     */
    private boolean isRecycleShown = false;

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

//    public boolean isCustomizeColor() {
//        return customizeColor;
//    }

//    private boolean customizeColor;

    private boolean passwordGuard;

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

    private CharSequence mDrawerTitle;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;


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
        if (null == bindItem) return;
        bindItem.setTitle(R.string.syn_evernote);
//        bindItem.setIcon(R.drawable.flags);
    }

    private void logoutNow() {
        if (null == bindItem) return;
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

    @Override
    public void changeFooter() {
        if (Folder.MODE_FOOTER == modeFooter) {
            modeFooter = Folder.MODE_FOOTER_DELETE;
            showCheckBox();
        } else if (Folder.MODE_FOOTER_DELETE == modeFooter) {
            modeFooter = Folder.MODE_FOOTER;
            hideCheckBox();
        }
        setFooter();
    }

    @Override
    public void actionClick() {
        if (Folder.MODE_FOOTER == modeFooter) {
            showCreateFolderDialog();
        } else if (Folder.MODE_FOOTER_DELETE == modeFooter) {
            if (deleteNum > 0) {
                trash();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            deleteNum++;
        } else {
            deleteNum--;
        }
        footerDelete.deleteNum(deleteNum);
    }

    private class MyDatePickerListener implements OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
            GNote gNote = new GNote();
            gNote.setTimeFromDate(year, month, day);
            Note.actionStart(MainActivity.this, gNote, Note.MODE_NEW);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        if (passwordGuard) {
            Password.activityStart(mContext, preferences.getString(Settings.PASSWORD_HINT, ""),
                    preferences.getString(Settings.PASSWORD, ""));
        }

        //如果设置了启动时进入新记事
//        if (isOpenNewNote) {
//            writeNewNote(today);
//        }

        //判断所需使用的布局
        mode = preferences.getInt("mode", MODE_LIST);
        changeContent();
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
//        fff3f3f3
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
                ((ProgressDialog) dialog).setMessage(getString(com.evernote.androidsdk.R.string
                        .esdk__loading));
                break;
            case INITIAL:
                ((ProgressDialog) dialog).setIndeterminate(true);
                dialog.setCancelable(false);
                ((ProgressDialog) dialog).setMessage(getString(R.string.initial));
                break;
        }
    }


    private void init() {

        preferences = getSharedPreferences(Settings.DATA, MODE_PRIVATE);
        readSetting();

        setContentView(R.layout.activity_main);

        if (TINT_STATUS_BAR) {
            //沉浸式时，对状态栏染色
            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);

            tintManager.setStatusBarTintColor(getResources().getColor(R.color.background_color));

            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
//        // enable navigation bar tint
//        tintManager.setNavigationBarTintEnabled(true);
        }

        mContext = this;
        dm = getResources().getDisplayMetrics();
        today = Calendar.getInstance();
        db = GNoteDB.getInstance(this);

        initDrawer();
        initDatePicker();

        //evernote
        mEvernote = new Evernote(this, this);

        //如果是第一次启动应用，在数据库中添加note
        versionCode = Util.getVersionCode(mContext);
        boolean first = preferences.getBoolean("first", true);
        if (first) {
            firstLaunch();
            insertProud();
            setVersionCode();
        } else {
            int v = preferences.getInt("version_code", 0);
//                below_version_2.0.4
            if (v < 15) {
                upgradeTo15();
                upgradeTo21();
                insertProud();
                setVersionCode();
            }

            if (v < 21) {
                upgradeTo21();
                insertProud();
                setVersionCode();
            }

            if (v < 23) {
                insertProud();
                setVersionCode();
            }
        }

        setActionBarTitle();
    }

    private void setActionBarTitle() {
        int bookId = preferences.getInt(Folder.GNOTEBOOK_ID, 0);
        String bookName;
        if (bookId == 0) {
            bookName = getResources().getString(R.string.all_notes);
        } else {
            bookName = db.getGNotebookById(bookId).getName();
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(bookName);
        }
    }


    private void upgradeTo21() {
        int n = db.loadGNotes().size();
        preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, n).putInt("mode", MODE_LIST).apply();

        readSetting();
    }


    //    左侧滑动抽屉相关

    private void createFolder(String name) {
        GNotebook gNotebook = new GNotebook();
        gNotebook.setName(name);
        db.saveGNotebook(gNotebook);
    }

    private void refreshFolderList() {
        gNotebookList.clear();
        List<GNotebook> tmpList = db.loadGNotebooks();
        for (GNotebook g : tmpList) {
            gNotebookList.add(g);
        }

        drawerNotebookAdapter.notifyDataSetChanged();
//        noteTitleListView.setSelection(0);
//        hideFlag(originalFolderId);
//        showFlag(folderId);
//        listview更新后似乎其数据源list与listview.getChildAt() 方法不再一一对应

    }

    /**
     * 删除按钮的方法
     */
    // TODO 待优化～～～～～
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
                        int extractId = preferences.getInt(Settings
                                .LIGHTNING_EXTRACT_SAVE_LOCATION, 0);

                        //快写存储位置
                        int quickId = preferences.getInt(Settings.QUICK_WRITE_SAVE_LOCATION, 0);

//                        从 ListView 中的各项开始遍历故起点需进行处理
                        for (int i = 1; i < folderListView.getCount() && deleteNum > 0; i++) {
                            CheckBox checkBox = (CheckBox) folderListView.getChildAt(i)
                                    .findViewById(R.id.cb_folder_unit);
                            if (checkBox.isChecked()) {
//                              注意-1 转换
                                int pos = i - 1;

                                db.deleteGNotebook(gNotebookList.get(pos));
                                deleteNoteInBook(gNotebookList.get(pos).getId());
//                        清除所有checkBox状态防错位,此时会触发监听器
                                checkBox.setChecked(false);

//                                如果selected笔记本在被删除之列，将笔记本还原为默认值
                                if (gNotebookList.get(pos).getSelected() == GNotebook.TRUE) {
                                    preferences.edit().putInt(Settings.GNOTEBOOK_ID, 0).commit();
                                }

                                if (gNotebookList.get(pos).getId() == extractId) {
                                    preferences.edit().putInt(Settings
                                            .LIGHTNING_EXTRACT_SAVE_LOCATION, 0).apply();
                                }

                                if (gNotebookList.get(pos).getId() == quickId) {
                                    preferences.edit().putInt(Settings.QUICK_WRITE_SAVE_LOCATION,
                                            0).apply();
                                }
                            }
                        }
                        refreshFolderList();
                        changeFooter();

                        //情况：在A文件夹中删除文件了B文件夹中的文件，刷新
                        changeToBook();

                    }
                }).show();
    }

    private void deleteNoteInBook(final int id) {
        new Runnable() {
            @Override
            public void run() {
                List<GNote> list = db.loadGNotesByBookId(id);
                for (GNote gNote : list) {
                    gNote.setDeleted(GNote.TRUE);
                    if (!"".equals(gNote.getGuid())) {
                        gNote.setSynStatus(GNote.DELETE);
                    }
                    ProviderUtil.updateGNote(mContext, gNote);

                    if (!gNote.getIsPassed()) {
                        AlarmService.cancelTask(mContext, gNote);
                    }

                    //在AllNotes中进行删除
                    int cnt = preferences.getInt(Folder.PURENOTE_NOTE_NUM, 3);
                    preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, cnt - 1).apply();
                }
            }
        }.run();

    }


    public void showCheckBox() {
        for (int i = 1; i < folderListView.getCount(); i++) {
            folderListView.getChildAt(i).findViewById(R.id.cb_folder_unit).setVisibility(View
                    .VISIBLE);
        }
    }

    public void hideCheckBox() {
        for (int i = 1; i < folderListView.getCount(); i++) {
            folderListView.getChildAt(i).findViewById(R.id.cb_folder_unit).setVisibility(View
                    .INVISIBLE);
        }
    }

    private void showCreateFolderDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edittext, (ViewGroup) getWindow()
                .getDecorView(), false);
        final EditText editText = (EditText) view.findViewById(R.id.et_in_dialog);

        final Dialog dialog = new AlertDialog.Builder(mContext).setTitle(R.string
                .create_folder_title).setView(view).setPositiveButton(R.string.create_folder, new
                DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().length() == 0) {
                            Toast.makeText(mContext, R.string.create_folder_err, Toast.LENGTH_SHORT).show();
                        } else {
                            createFolder(editText.getText().toString());
                            refreshFolderList();
                        }
                    }
                }).setNegativeButton(R.string.folder_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private int modeFooter;
    private int deleteNum;

    @Override
    public int getDeleteNum() {
        return deleteNum;
    }

    private FolderFooter footer;
    private FolderFooterDelete footerDelete;

    private ListView folderListView;
    private List<GNotebook> gNotebookList;
    private DrawerNotebookAdapter drawerNotebookAdapter;

    private boolean isDrawerOpened;
    private View drawerRoot;

    private void initDrawer() {
        mDrawerTitle = getResources().getString(R.string.app_name);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerRoot = findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R
                .string.action_folder, R.string.app_name) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                if (!isRecycleShown) {
                    setActionBarTitle();
                } else {
                    ActionBar actionBar = getActionBar();
                    if (null != actionBar) {
                        actionBar.setTitle(R.string.recycle_bin);
                    }
                }

                isDrawerOpened = false;
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                ActionBar actionBar = getActionBar();
                if (null != actionBar) {
                    actionBar.setTitle(mDrawerTitle);
                }

                refreshFolderList();
                modeFooter = Folder.MODE_FOOTER;
                setFooter();

                isDrawerOpened = true;
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setScrimColor(Color.parseColor("#66EEEEEE"));
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        folderListView = (ListView) findViewById(R.id.lv_folder);

        gNotebookList = db.loadGNotebooks();
        drawerNotebookAdapter = new DrawerNotebookAdapter(mContext, R.layout.folder_unit,
                gNotebookList, folderListView, preferences);
        folderListView.setAdapter(drawerNotebookAdapter);

        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //进行删除操作时无视之
                if (Folder.MODE_FOOTER_DELETE == modeFooter) return;

//                特判回收站
//                if (drawerNotebookAdapter.getCount() - 1 == i) {
////                    RecycleBin.actionStart(mContext);
//                    isRecycleShown = true;
//                } else {

                int folderId = preferences.getInt(Settings.GNOTEBOOK_ID, 0);

                //由 id 解析一个 listview pos 出来
                int from = parseBookIdToPos(folderId);
                changeFlag(from, i, view);

//                关闭抽屉
                mDrawerLayout.closeDrawer(drawerRoot);
//                showDialog();

                changeBookInDB(from, i);

                isRecycleShown = false;

//                }

                changeToBook();
//                removeDialog();

            }
        });

    }

    public void changeToBook() {
        //刷新界面
        int tmp = preferences.getInt(Settings.GNOTEBOOK_ID, 0);
        if (gNotebookId != tmp) {
            gNotebookId = tmp;
            refreshUI();
        }
    }

    /**
     * 类型转换方法
     *
     * @param id
     * @return
     */
    private int parseBookIdToPos(int id) {
        if (0 == id) return 0;
        //空指针保护-_-||
        if (null == gNotebookList) return 0;
        for (int j = 0; j < gNotebookList.size(); j++) {
            if (id == gNotebookList.get(j).getId()) {
                //因为有一项特殊值，所以在列表中的位置位+1
                return j + 1;
            }
        }
        return 0;
    }

    /**
     * 类型转换方法
     *
     * @param pos
     * @return
     */
    private int parsePosToBookId(int pos) {
        if (0 == pos) return 0;

        //空指针保护-_-||
        if (null == gNotebookList || pos - 1 >= gNotebookList.size()) return 0;

        return gNotebookList.get(pos - 1).getId();
    }


    private void changeBookInDB(int from, int to) {
//        当两者相同时，说明无需改变
        if (from == to) {
            return;
        }

        int oldId = cancelFolder(from);
        int newId = selectFolder(to);

        preferences.edit().putInt(Settings.GNOTEBOOK_ID, newId).commit();
        LogUtil.i(TAG, "gNotebook id fromId:" + oldId + " toId:" + newId);
    }

    private int selectFolder(int pos) {
        if (pos != 0) {
            if (gNotebookList == null || pos - 1 >= gNotebookList.size()) return 0;
            GNotebook gNotebook = gNotebookList.get(pos - 1);
            gNotebook.setSelected(GNotebook.TRUE);
            db.updateGNotebook(gNotebook);
            return gNotebook.getId();
        }
        return 0;
    }

    private int cancelFolder(int pos) {
        if (pos != 0) {
            if (gNotebookList == null || pos - 1 >= gNotebookList.size()) return 0;
            GNotebook gNotebook = gNotebookList.get(pos - 1);
            gNotebook.setSelected(GNotebook.FALSE);
            db.updateGNotebook(gNotebook);
            return gNotebook.getId();
        }
        return 0;
    }

    private void changeFlag(int from, int to, View v) {
        if (from == to) {
            return;
        }

        //注意这里的处理差异性，两者的数据类型不同导致
        setVisibleByListPos(View.INVISIBLE, from);
        setVisibleByView(View.VISIBLE, v);
    }

    private void setVisibleByListPos(int visible, int pos) {
        if (null == folderListView) return;

        ImageView flag = (ImageView) folderListView.getChildAt(pos).findViewById(R.id
                .iv_folder_unit_flag);
        flag.setVisibility(visible);
    }

    private void setVisibleByView(int visible, View v) {
        if (null == v) return;

        ImageView flag = (ImageView) v.findViewById(R.id.iv_folder_unit_flag);
        if (null != flag) {
            flag.setVisibility(visible);
        }
    }


    public void setFooter() {
        if (modeFooter == Folder.MODE_FOOTER) {
            if (footer == null) {
                footer = new FolderFooter();
            }
            getFragmentManager().beginTransaction().replace(R.id.fl_folder_footer, footer).commit();
        } else if (modeFooter == Folder.MODE_FOOTER_DELETE) {
            if (footerDelete == null) {
                footerDelete = new FolderFooterDelete();
            }
            getFragmentManager().beginTransaction().replace(R.id.fl_folder_footer, footerDelete)
                    .commit();
        }
    }

//    滑动抽屉至此结束

    private void initDatePicker() {
        datePickerDialog = DatePickerDialog.newInstance(new MyDatePickerListener(), today.get
                        (Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH),
                false);
        datePickerDialog.setYearRange(today.get(Calendar.YEAR) - 10, today.get(Calendar.YEAR) + 10);
        datePickerDialog.setCloseOnSingleTapDay(true);
    }

    private int upgradeTo15() {
//      将所有原有数据转移至默认文件夹中
        List<GNote> list = db.loadGNotes();
        for (GNote gNote : list) {
            gNote.setGNotebookId(0);
            db.updateGNote(gNote);
        }
        int n = list.size();
        preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, n).apply();
        return n;
    }

    /*
    标注版本号
     */
    private void setVersionCode() {
        LogUtil.i(TAG, "setVersionCode():" + versionCode);
        preferences.edit().putInt("version_code", versionCode).apply();
    }

    private void insertProud() {
        GNote zero = new GNote();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, +1);

        zero.setCalToTime(tomorrow);
        zero.setContent(getResources().getString(R.string.tip0));
//            two.setSynStatus(GNote.NEW);
        zero.setColor(GridUnit.GOLD);
        db.saveGNote(zero);

        int n = preferences.getInt(Folder.PURENOTE_NOTE_NUM, 0);
        preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, n + 1).apply();
    }

    private void firstLaunch() {
        //如果是第一次启动应用，在数据库中添加note
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("first", false);
        editor.apply();

        GNote one = new GNote();
        one.setCalToTime(today);
        one.setContent(getResources().getString(R.string.tip1) + "(・8・)");
//            one.setSynStatus(GNote.NEW);
        one.setColor(GridUnit.PURPLE);
        db.saveGNote(one);

        Calendar tmpCal = (Calendar) today.clone();
        tmpCal.add(Calendar.DAY_OF_MONTH, -1);

        GNote two = new GNote();
        two.setCalToTime(tmpCal);
        two.setContent(getResources().getString(R.string.tip2));
//            two.setSynStatus(GNote.NEW);
        db.saveGNote(two);

        tmpCal.add(Calendar.DAY_OF_MONTH, -1);
        GNote three = new GNote();
        three.setCalToTime(tmpCal);
        three.setContent(getResources().getString(R.string.tip3));
//            three.setSynStatus(GNote.NEW);
        db.saveGNote(three);

        preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, 3).apply();
//            GNotebook test = new GNotebook();
//            test.setName("Test");
//            db.saveGNotebook(test);
    }

    private GNoteList gNoteList;
    private ColorGrid colorGrid;

    public void changeContent() {

        if (mode == MODE_LIST) {
            if (null == gNoteList) {
                gNoteList = new GNoteList();
            }
            unlockDrawerLock();//打开手势滑动
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_content,
                    gNoteList).commit();
        } else if (mode == MODE_GRID) {
            if (colorGrid == null) {
                colorGrid = new ColorGrid();
            }
            lockDrawerLock();
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_content,
                    colorGrid).commit();
        }
    }

    public void lockDrawerLock() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//关闭手势滑动
    }

    public void unlockDrawerLock() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);//关闭手势滑动
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
        maxLines = preferences.getInt(Settings.MAX_LINES, Settings.DEFAULT_MAX_LINES);

//        customizeColor = preferences.getBoolean(Settings.CUSTOMIZE_COLOR, true);

        gNotebookId = preferences.getInt(Folder.GNOTEBOOK_ID, 0);

        passwordGuard = preferences.getBoolean(Settings.PASSWORD_GUARD, false);
    }


    /**
     * 以cal为日期写一篇新记事
     */
    private void writeNewNote(Calendar cal) {
        GNote note = new GNote();
        note.setCalToTime(cal);
        Note.actionStart(this, note, Note.MODE_NEW);
    }

    /**
     * 重写此方法，检查SharedPreferences,看是否需更新UI
     */
    @Override
    protected void onResume() {
        super.onResume();

        uiOperation();

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

    private void uiOperation() {
        boolean settingsChanged = preferences.getBoolean(Settings.SETTINGS_CHANGED, false);
//        boolean updateUI = preferences.getBoolean(UPDATE_UI, false);

        //是否因各种改变而需要进行UI刷新
//        if (updateUI || settingsChanged) {
        if (settingsChanged) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(UPDATE_UI, false);
            editor.putBoolean(Settings.SETTINGS_CHANGED, false);
            editor.apply();

            readSetting();
            refreshUI();

            LogUtil.i(TAG, "readingSetting & updateUI in onResume.");
        }


        //evernote
        if (bindItem != null) {
            if (mEvernote.isLogin()) {
                loginNow();
            } else {
                logoutNow();
            }
        }
    }

    //    一次性评分弹窗
    private void rateForPureNote() {
        if (preferences.getInt(Note.EditCount, 0) >= Note.EDIT_COUNT && !preferences.getBoolean
                (ShownRate, false)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.rate_for_purenote).setPositiveButton(R.string.rate_rate,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            try {
                                startActivity(goToMarket);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(mContext, "Couldn't launch the market!", Toast
                                        .LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.rate_feedback, new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Util.feedback(mContext);
                }
            }).create().show();

            preferences.edit().putBoolean(ShownRate, true).apply();
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
            if (null != gNoteList) {
                gNoteList.refreshUI();
            }
        } else if (mode == MODE_GRID) {
            if (null != colorGrid) {
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isRecycleShown || isDrawerOpened) {
            getMenuInflater().inflate(R.menu.empty_menu, menu);
        } else {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu, menu);
//        MenuItem item = menu.getItem(1);
//        Menu m = item.getSubMenu();

            //just for initialize layout mode submenu
//        MenuItem[] items = new MenuItem[2];
//        items[0] = m.getItem(0);
//        items[1] = m.getItem(1);
//        if (mode == MODE_GRID) {
//            items[0].setChecked(true);
//        } else if (mode == MODE_LIST) {
//            items[1].setChecked(true);
//        }

            //bind-evernote item
            bindItem = menu.getItem(1);
            //evernote
            if (mEvernote.isLogin()) {
                loginNow();
            } else {
                logoutNow();
            }
        }

        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(mi)) {
            return true;
        }
        // Handle your other action bar items...

        switch (mi.getItemId()) {
            case R.id.about:
                About.activityStart(mContext);
                break;
            case R.id.setting:
                Settings.activityStart(this);
                break;
            case R.id.action_plus:
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                break;

//            case R.id.m_color_grid:
//                if (!mi.isChecked()) {
//                    mi.setChecked(true);
//                    preferences.edit().putInt("mode", MainActivity.MODE_GRID).apply();
//                    mode = MODE_GRID;
//                    changeContent();
//                }
//                break;
//            case R.id.m_classic_list:
//                if (!mi.isChecked()) {
//                    mi.setChecked(true);
//                    preferences.edit().putInt("mode", MainActivity.MODE_LIST).apply();
//                    mode = MODE_LIST;
//                    changeContent();
//                }
//                break;

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
                    findViewById(R.id.pb_blue).setVisibility(View.VISIBLE);
                    break;
                case Evernote.SYNC_END:
                    findViewById(R.id.pb_blue).setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }

    public void removeDialog() {
        removeDialog(DIALOG_PROGRESS);
    }

    public void showDialog() {
        showDialog(DIALOG_PROGRESS);
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

}
