package com.duanze.gasst.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.duanze.gasst.R;
import com.duanze.gasst.adapter.DrawerNotebookAdapter;
import com.duanze.gasst.adapter.GNotebookAdapter;
import com.duanze.gasst.fragment.ColorGrid;
import com.duanze.gasst.fragment.FiltratePage;
import com.duanze.gasst.fragment.FolderFooter;
import com.duanze.gasst.fragment.FolderFooterDelete;
import com.duanze.gasst.fragment.FooterInterface;
import com.duanze.gasst.fragment.GNoteList;
import com.duanze.gasst.fragment.GNoteRecyclerView;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.provider.GNoteProvider;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.syn.Evernote;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.PreferencesUtils;
import com.duanze.gasst.util.ProviderUtil;
import com.duanze.gasst.util.TimeUtils;
import com.duanze.gasst.util.Util;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends BaseActivity implements Evernote.EvernoteLoginCallback,
        FooterInterface, CompoundButton.OnCheckedChangeListener, LoaderManager
                .LoaderCallbacks<Cursor>, GNotebookAdapter.ItemLongPressedListener,
        GNotebookAdapter.OnItemSelectListener, GNotebookAdapter.OnItemClickListener {

    public static final String TAG = "StartActivity";

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

    public int getGNotebookId() {
        return gNotebookId;
    }

    //当前所用的笔记本id
    private int gNotebookId;

    SharedPreferences preferences;

    private MenuItem bindItem;

    private Evernote mEvernote;

    private Timer syncTimer;

    private final int DIALOG_PROGRESS = 101;
    private final int INITIAL = 102;

    //    loader化
    private final int NOTEBOOK_LOADER_ID = 112;
    private GNotebookAdapter mAdapter;
    private LoaderManager loaderManager;

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
//            showCheckBox();
            if (null != mAdapter) {
                mAdapter.setCheckMode(true);
            }
        } else if (Folder.MODE_FOOTER_DELETE == modeFooter) {
            modeFooter = Folder.MODE_FOOTER;
//            hideCheckBox();
            if (null != mAdapter) {
                mAdapter.setCheckMode(false);
            }
        }
        setFooter();
    }

    @Override
    public void actionClick() {
        if (Folder.MODE_FOOTER == modeFooter) {
            showCreateFolderDialog();
        } else if (Folder.MODE_FOOTER_DELETE == modeFooter) {
            if (getDeleteNum() > 0) {
//                mAdapter.deleteItems();
                trash();
                updateDeleteNum();
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
        footerDelete.updateDeleteNum(deleteNum);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(mContext, GNoteProvider.NOTEBOOK_URI,
                GNoteProvider.NOTEBOOK_PROJECTION, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void startActionMode() {

    }

    @Override
    public void onLongPress(GNotebook gNotebook) {
        if (null != gNotebook) {
            showRenameFolderDialog(gNotebook);
        }
    }

    @Override
    public void onItemClick(View view) {
//进行删除操作时无视之
        if (Folder.MODE_FOOTER_DELETE == modeFooter) return;

//                特判回收站
//                if (drawerNotebookAdapter.getCount() - 1 == i) {
////                    RecycleBin.actionStart(mContext);
//                    isRecycleShown = true;
//                } else {

        int oldId = preferences.getInt(Settings.GNOTEBOOK_ID, 0);
        int newId = 0;
        GNotebook gNotebook = (GNotebook) view.getTag(R.string.gnotebook_data);
        if (null != gNotebook) {
            newId = gNotebook.getId();
        }
//                //由 id 解析一个 listview pos 出来
//                int from = parseBookIdToPos(folderId);
//                changeFlag(from, i, view);
        changeBookInDB(oldId, newId);

//                关闭抽屉
        mDrawerLayout.closeDrawer(drawerRoot);
        isRecycleShown = false;
//                }

        changeToBook(oldId, newId);
    }

    @Override
    public void onSelect() {
//        意义并不是很明确的复用
        updateDeleteNum();
    }

    private void updateDeleteNum() {
        footerDelete.updateDeleteNum(getDeleteNum());
    }

    @Override
    public void onCancelSelect() {
//        意义并不是很明确的复用
        updateDeleteNum();
    }

    private class MyDatePickerListener implements OnDateSetListener {
        @Override
        public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
            GNote gNote = new GNote();
            gNote.setTimeFromDate(year, month, day);
            Note.actionStart(StartActivity.this, gNote, Note.MODE_NEW);
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

    public static final int OPERATE = 103;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                return new ProgressDialog(StartActivity.this);
            case INITIAL:
                return new ProgressDialog(StartActivity.this);
            case OPERATE:
                return new ProgressDialog(StartActivity.this);
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
            case OPERATE:
                ((ProgressDialog) dialog).setIndeterminate(true);
                dialog.setCancelable(false);
                ((ProgressDialog) dialog).setMessage(getString(R.string.operate));
                break;
        }
    }


    private void init() {
        mContext = this;
        preferences = getSharedPreferences(Settings.DATA, MODE_PRIVATE);
//        已在父类 BaseActivity 中对Preferences进行初始化
//        PreferencesUtils.getInstance(mContext).refreshData();
        readSetting();
        setContentView(R.layout.activity_main);

        dm = getResources().getDisplayMetrics();
        today = Calendar.getInstance();
        db = GNoteDB.getInstance(this);

        initDrawer();
//        initDatePicker();

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
            int versionCode = preferences.getInt("version_code", 14);
//                below_version_2.0.4
            if (versionCode < 15) {
                upgradeTo15();
            }
            if (versionCode < 21) {
                upgradeTo21();
            }
            if (versionCode < 23) {
                insertProud();
            }
            if (versionCode < 27) {
                upgradeTo27();
            }
            if (versionCode < 28) {
                upgradeTo28();
            }
            if (versionCode < 29) {
                upgradeTo29();
            }
            setVersionCode();
        }
        setActionBarTitle();
    }

    private void upgradeTo29() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SyncHandler syncHandler = new SyncHandler();
                syncHandler.sendEmptyMessage(UPGRADE_START);
                List<GNote> list = db.loadGNotes();
                for (GNote gNote : list) {
                    String content = Html.fromHtml(gNote.getContent()).toString();
                    gNote.setContent(content);
                    db.updateGNote(gNote);
                }
                syncHandler.sendEmptyMessage(UPGRADE_END);
            }
        }).run();

        GNote zero = new GNote();
        zero.setContent(getResources().getString(R.string.tip2));
        zero.setEditTime(TimeUtils.getCurrentTimeInLong());
        db.saveGNote(zero);

        int n = preferences.getInt(Folder.PURENOTE_NOTE_NUM, 0);
        preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, n + 1).apply();
    }

    private void upgradeTo28() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SyncHandler syncHandler = new SyncHandler();
                syncHandler.sendEmptyMessage(UPGRADE_START);
                List<GNote> list = db.loadGNotes();
                for (GNote gNote : list) {
                    if (0 == gNote.getEditTime()) {
                        gNote.setEditTime(TimeUtils.getCurrentTimeInLong());
                        db.updateGNote(gNote);
                    }
                }
                syncHandler.sendEmptyMessage(UPGRADE_END);
            }
        }).run();
    }

    private void upgradeTo27() {
        GNote zero = new GNote();
        zero.setContent(getResources().getString(R.string.tip_optional));
//            two.setSynStatus(GNote.NEW);
        zero.setEditTime(TimeUtils.getCurrentTimeInLong());
        db.saveGNote(zero);

        int n = preferences.getInt(Folder.PURENOTE_NOTE_NUM, 0);
        preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, n + 1).apply();
    }

    private void setActionBarTitle() {
        int bookId = preferences.getInt(Folder.GNOTEBOOK_ID, 0);
        String bookName;
        if (bookId == 0) {
            bookName = getResources().getString(R.string.all_notes);
        } else {
            bookName = db.getGNotebookById(bookId).getName();
        }

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
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
//        db.saveGNotebook(gNotebook);
        ProviderUtil.insertGNotebook(mContext, gNotebook);
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
                setMessage(R.string.delete_folder_title).
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

                        HashMap<Integer, GNotebook> map = mAdapter.getCheckedItems();
                        if (null == map || 0 == map.size()) {
                            return;
                        } else {
                            int oldId = 0;
                            int newId = 0;

                            Set<Integer> keys = map.keySet();
                            for (Integer key : keys) {
                                if (key == extractId) {
                                    preferences.edit().putInt(Settings
                                            .LIGHTNING_EXTRACT_SAVE_LOCATION, 0).apply();
                                }
                                if (key == quickId) {
                                    preferences.edit().putInt(Settings.QUICK_WRITE_SAVE_LOCATION,
                                            0).apply();
                                }
                                GNotebook gNotebook = map.get(key);
                                if (gNotebook.isSelected()) {
                                    oldId = key;
                                    changeToBook(oldId, newId);
                                }

                                mAdapter.deleteGNotebook(gNotebook);
                            }

//这种做法是不对的，因为设计缺陷，处理变得极为复杂，今后还是要统一数据，统一处理
//                            map = null;
                            mAdapter.destroyCheckedItems();

                        }
                        changeFooter();

////                        从 ListView 中的各项开始遍历故起点需进行处理
//                        for (int i = 1; i < folderListView.getCount() && deleteNum > 0; i++) {
//                            CheckBox checkBox = (CheckBox) folderListView.getChildAt(i)
//                                    .findViewById(R.id.cb_folder_unit);
//                            if (checkBox.isChecked()) {
////                              注意-1 转换
//                                int pos = i - 1;
//
//                                db.deleteGNotebook(gNotebookList.get(pos));
//                                deleteNoteInBook(gNotebookList.get(pos).getId());
////                        清除所有checkBox状态防错位,此时会触发监听器
//                                checkBox.setChecked(false);
//
////                                如果selected笔记本在被删除之列，将笔记本还原为默认值
//                                if (gNotebookList.get(pos).getSelected() == GNotebook.TRUE) {
//                                    preferences.edit().putInt(Settings.GNOTEBOOK_ID, 0).commit();
//                                }
//
//                                if (gNotebookList.get(pos).getId() == extractId) {
//                                    preferences.edit().putInt(Settings
//                                            .LIGHTNING_EXTRACT_SAVE_LOCATION, 0).apply();
//                                }
//
//                                if (gNotebookList.get(pos).getId() == quickId) {
//                                    preferences.edit().putInt(Settings.QUICK_WRITE_SAVE_LOCATION,
//                                            0).apply();
//                                }
//                            }
//                        }
//                        refreshFolderList();

                        //情况：在A文件夹中删除文件了B文件夹中的文件，刷新
//                        changeToBook();
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

        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.create_folder_title)
                .setView(view)
                .setPositiveButton(R.string.create_folder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().toString().trim().length() == 0) {
                            Toast.makeText(mContext, R.string.create_folder_err, Toast.LENGTH_SHORT).show();
                        } else {
                            createFolder(editText.getText().toString().trim());
//                    refreshFolderList();
                        }
                    }
                })
                .setNegativeButton(R.string.folder_cancel, null)
                .create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void showRenameFolderDialog(final GNotebook gNotebook) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edittext, (ViewGroup) getWindow()
                .getDecorView(), false);
        final EditText editText = (EditText) view.findViewById(R.id.et_in_dialog);
        editText.setText(gNotebook.getName());
        editText.setSelection(gNotebook.getName().length());
        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.rename_folder_title)
                .setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().toString().trim().length() == 0) {
                            Toast.makeText(mContext, R.string.rename_folder_err, Toast.LENGTH_SHORT).show();
                        } else {
                            gNotebook.setName(editText.getText().toString().trim());
                            ProviderUtil.updateGNotebook(mContext, gNotebook);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private int modeFooter;
    private int deleteNum;

    @Override
    public int getDeleteNum() {
        if (null == mAdapter) return 0;
        return mAdapter.getSelectedCount();
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
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                if (!isRecycleShown) {
                    setActionBarTitle();
                } else {
                    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
                    if (null != actionBar) {
                        actionBar.setTitle(R.string.recycle_bin);
                    }
                }

                isDrawerOpened = false;
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                android.support.v7.app.ActionBar actionBar = getSupportActionBar();
                if (null != actionBar) {
                    actionBar.setTitle(mDrawerTitle);
                }

                if (null != mAdapter) {
                    mAdapter.setCheckMode(false);
                }
                loaderManager.restartLoader(NOTEBOOK_LOADER_ID, null, StartActivity.this);
//                refreshFolderList();
                modeFooter = Folder.MODE_FOOTER;
                setFooter();

                isDrawerOpened = true;
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setScrimColor(Color.parseColor("#66EEEEEE"));
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        folderListView = (ListView) findViewById(R.id.lv_folder);
        mAdapter = new GNotebookAdapter(mContext, null, 0, this, this, this);
        mAdapter.setPreferences(preferences);
        folderListView.setAdapter(mAdapter);

        loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(NOTEBOOK_LOADER_ID, null, this);

//        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                //进行删除操作时无视之
//                if (Folder.MODE_FOOTER_DELETE == modeFooter) return;
//
////                特判回收站
////                if (drawerNotebookAdapter.getCount() - 1 == i) {
//////                    RecycleBin.actionStart(mContext);
////                    isRecycleShown = true;
////                } else {
//
//                int oldId = preferences.getInt(Settings.GNOTEBOOK_ID, 0);
//                int newId = 0;
//                if (0 != i) {
//                    GNotebook gNotebook = (GNotebook) view.getTag(R.string.gnotebook_data);
//                    newId = gNotebook.getId();
//                }
////                //由 id 解析一个 listview pos 出来
////                int from = parseBookIdToPos(folderId);
////                changeFlag(from, i, view);
//                changeBookInDB(oldId, newId);
//
////                关闭抽屉
//                mDrawerLayout.closeDrawer(drawerRoot);
////                showDialog();
//
//                isRecycleShown = false;
////                }
//
//                changeToBook(oldId, newId);
//            }
//        });
    }

    public void changeToBook(int oldId, int newId) {
        if (oldId == newId) return;
        //刷新界面
        LogUtil.i(TAG, "gNotebook id oldId:" + oldId + " newId:" + newId);
        preferences.edit().putInt(Settings.GNOTEBOOK_ID, newId).commit();
        refreshUI();
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


    private void changeBookInDB(int fromId, int toId) {
//        当两者相同时，说明无需改变
        if (fromId == toId) {
            return;
        }
        cancelFolder(fromId);
        selectFolder(toId);

    }

    private void selectFolder(int bookId) {
//        if (bookId != 0) {
//            if (gNotebookList == null || bookId - 1 >= gNotebookList.size()) return 0;
//            GNotebook gNotebook = gNotebookList.get(bookId - 1);
//            gNotebook.setSelected(GNotebook.TRUE);
//            db.updateGNotebook(gNotebook);
//            return gNotebook.getId();
//        }
//        return 0;

        // / 此步需要使用 provider 来配合观察者模式
        if (0 != bookId && null != db) {
            try {
                GNotebook gNotebook = db.getGNotebookById(bookId);
                gNotebook.setSelected(GNotebook.TRUE);
//                ProviderUtil.updateGNotebook(mContext, gNotebook);
                db.updateGNotebook(gNotebook);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void cancelFolder(int bookId) {
//        if (bookId != 0) {
//            if (gNotebookList == null || bookId - 1 >= gNotebookList.size()) return 0;
//            GNotebook gNotebook = gNotebookList.get(bookId - 1);
//            gNotebook.setSelected(GNotebook.FALSE);
//            db.updateGNotebook(gNotebook);
//            return gNotebook.getId();
//        }
//        return 0;

        // / 仅当非 purenote 时需要更新GNotebook
        // 此步 不 需要使用 provider 来配合观察者模式
        // 但如果后一步 bookId 为0，那也会悲剧
        // 所以还是进行两次更新吧，汗- -||
        if (0 != bookId && null != db) {
            try {
                GNotebook gNotebook = db.getGNotebookById(bookId);
                gNotebook.setSelected(GNotebook.FALSE);
//                ProviderUtil.updateGNotebook(mContext, gNotebook);
                db.updateGNotebook(gNotebook);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
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
                        (Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar
                        .DAY_OF_MONTH),
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
        zero.setContent(getString(R.string.tip0));
//            two.setSynStatus(GNote.NEW);
//        zero.setColor(GridUnit.GOLD);
        zero.setEditTime(TimeUtils.getCurrentTimeInLong());
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
        one.setContent(getResources().getString(R.string.tip1));
//            one.setSynStatus(GNote.NEW);
//        one.setColor(GridUnit.PURPLE);
        one.setEditTime(TimeUtils.getCurrentTimeInLong());
        db.saveGNote(one);

        Calendar tmpCal = (Calendar) today.clone();
        tmpCal.add(Calendar.DAY_OF_MONTH, -1);

        GNote two = new GNote();
        two.setCalToTime(tmpCal);
        two.setContent(getResources().getString(R.string.tip2));
//            two.setSynStatus(GNote.NEW);
        two.setEditTime(TimeUtils.getCurrentTimeInLong());
        db.saveGNote(two);

//        tmpCal.add(Calendar.DAY_OF_MONTH, -1);
//        GNote three = new GNote();
//        three.setCalToTime(tmpCal);
//        three.setContent(getResources().getString(R.string.tip3));
////            three.setSynStatus(GNote.NEW);
//        three.setEditTime(TimeUtils.getCurrentTimeInLong());
//        db.saveGNote(three);

        preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, 2).commit();
//            GNotebook test = new GNotebook();
//            test.setName("Test");
//            db.saveGNotebook(test);
    }


    private GNoteList gNoteList;
    private ColorGrid colorGrid;
    private GNoteRecyclerView gNoteRecyclerView;
    private FiltratePage filtratePage;
    private boolean isInFiltrate = false;

    private void enterFiltratePage() {
        if (isInFiltrate) return;
        isInFiltrate = true;
//        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        lockDrawerLock();
//        gNoteRecyclerView.dismissFAB();
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (null == filtratePage) {
            transaction.add(R.id.fl_main_content, new FiltratePage());
        } else {
            transaction.show(filtratePage);
            filtratePage.clearResult();
        }
        transaction.hide(gNoteRecyclerView).commit();
        gNoteRecyclerView.setUserVisibleHint(false);
    }

    private void exitFiltratePage() {
        if (!isInFiltrate) return;
        isInFiltrate = false;
//        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        unlockDrawerLock();
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (null == filtratePage) {
            return;
        } else {
            transaction.hide(filtratePage);
        }
        transaction.show(gNoteRecyclerView).commit();
        gNoteRecyclerView.setUserVisibleHint(true);
//        gNoteRecyclerView.showFAB();
//        gNoteRecyclerView.refreshUI();
    }

    public void changeContent() {
        if (mode == MODE_LIST) {
//            if (null == gNoteList) {
//                gNoteList = new GNoteList();
//            }

            if (null == gNoteRecyclerView) {
                gNoteRecyclerView = new GNoteRecyclerView();
            }
            unlockDrawerLock();//打开手势滑动
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_content,
                    gNoteRecyclerView).commit();

//            getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_content,
//                    gNoteList).commit();
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
//        //是否启动时开启新记事
//        isOpenNewNote = preferences.getBoolean(Settings.NEW_NOTE, false);
//        //是否过期记事折叠
//        isFold = preferences.getBoolean(Settings.FOLD, false);
//        //记事格子随机填色
//        randomColor = preferences.getBoolean(Settings.RANDOM_COLOR, true);
//        //最大显示行数
//        maxLines = preferences.getInt(Settings.MAX_LINES, Settings.DEFAULT_MAX_LINES);
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

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof FiltratePage) {
            LogUtil.i(TAG, "filtratePage = (FiltratePage) fragment;");
            filtratePage = (FiltratePage) fragment;
        }
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
            }, 30 * 1000, 50 * 1000);
        }

        rateForPureNote();
        //        umeng
        MobclickAgent.onResume(this);

        boolean needRecreate = PreferencesUtils.getInstance(mContext).isActivityNeedRecreate();
        if (needRecreate){
            PreferencesUtils.getInstance(mContext).setActivityNeedRecreate(false);
            recreate();
        }
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
        if (!preferences.getBoolean(ShownRate, false)) {
            int editCnt = preferences.getInt(Note.EditCount, 0);
            if ((editCnt - Note.EDIT_COUNT) % Note.INTERVAL == 0) {
                new AlertDialog.Builder(mContext).setMessage(R.string.rate_for_purenote)
                        .setPositiveButton(R.string.rate_rate,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = Uri.parse("market://details?id=" + mContext
                                                .getPackageName());

                                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                        try {
                                            startActivity(goToMarket);
                                            preferences.edit().putBoolean(ShownRate, true).apply();
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(mContext, "Couldn't launch the " +
                                                    "market!", Toast
                                                    .LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .setNeutralButton(R.string.dont_care, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int count = preferences.getInt(Note.EditCount, 0);
                                count++;
                                preferences.edit().putInt(Note.EditCount, count).apply();
                            }
                        })
                        .setNegativeButton(R.string.rate_feedback, new DialogInterface
                                .OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Util.feedback(mContext);
                                preferences.edit().putBoolean(ShownRate, true).apply();
                            }
                        })
                        .setCancelable(false)
                        .create().show();
            }
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
//            if (null != gNoteList) {
//                gNoteList.refreshUI();
//            }

            if (null != gNoteRecyclerView) {
                gNoteRecyclerView.refreshUI();
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
            bindItem = menu.findItem(R.id.bind_evernote);
            //evernote
            if (mEvernote.isLogin()) {
                loginNow();
            } else {
                logoutNow();
            }

            MenuItem searchItem = menu.findItem(R.id.menu_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

//            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//            ComponentName componentName = getComponentName();
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

            searchView.setQueryHint(getString(R.string.search_note));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
//                    It's really weird
                    if (null != filtratePage) {
                        if (0 != s.length()) {
                            filtratePage.startFiltrate(FiltratePage.SELECTION, new String[]{"%" + s + "%"});
                        } else {
                            filtratePage.clearResult();
                        }
                    }
                    return true;
                }
            });
            MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    Log.d(TAG, "on expand");
                    if (null == gNoteRecyclerView) {
                        LogUtil.e(TAG, "onMenuItemActionExpand(MenuItem item),null==gNoteRecyclerView");
                        return true;
                    }
//                    Learn from Android Dialer source code
                    enterFiltratePage();
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    Log.d(TAG, "on collapse");
                    if (null == gNoteRecyclerView) {
                        LogUtil.e(TAG, "onMenuItemActionCollapse(MenuItem item),null==gNoteRecyclerView");
                        return true;
                    }
                    exitFiltratePage();
                    return true;
                }
            });

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
//            case R.id.action_plus:
//                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
//                break;

//            case R.id.m_color_grid:
//                if (!mi.isChecked()) {
//                    mi.setChecked(true);
//                    preferences.edit().putInt("mode", StartActivity.MODE_GRID).apply();
//                    mode = MODE_GRID;
//                    changeContent();
//                }
//                break;
//            case R.id.m_classic_list:
//                if (!mi.isChecked()) {
//                    mi.setChecked(true);
//                    preferences.edit().putInt("mode", StartActivity.MODE_LIST).apply();
//                    mode = MODE_LIST;
//                    changeContent();
//                }
//                break;

            case R.id.menu_search:
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

    private static final int UPGRADE_START = 280;
    private static final int UPGRADE_END = 281;

    class SyncHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Evernote.SYNC_START:
                    showProgressBar();
                    break;
                case Evernote.SYNC_END:
                    hideProgressBar();
                    break;
                case UPGRADE_START:
                    showDialog(OPERATE);
                    break;
                case UPGRADE_END:
                    dismissDialog(OPERATE);
                    break;
                default:
                    break;
            }
        }
    }

    public void hideProgressBar() {
        findViewById(R.id.pb_blue).setVisibility(View.GONE);
    }

    public void showProgressBar() {
        findViewById(R.id.pb_blue).setVisibility(View.VISIBLE);
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