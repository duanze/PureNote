package com.duanze.gasst.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.duanze.gasst.MyApplication;
import com.duanze.gasst.R;
import com.duanze.gasst.data.model.GNote;
import com.duanze.gasst.data.model.GNoteDB;
import com.duanze.gasst.data.model.GNotebook;
import com.duanze.gasst.data.provider.GNoteProvider;
import com.duanze.gasst.sync.Evernote;
import com.duanze.gasst.ui.activity.base.BaseActivity;
import com.duanze.gasst.ui.adapter.GNotebookAdapter;
import com.duanze.gasst.ui.fragment.FiltratePage;
import com.duanze.gasst.ui.fragment.FolderFooter;
import com.duanze.gasst.ui.fragment.FolderFooterDelete;
import com.duanze.gasst.ui.fragment.FooterInterface;
import com.duanze.gasst.ui.fragment.GNoteRecyclerView;
import com.duanze.gasst.ui.view.SwipeRefreshLayoutEx;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.ProviderUtil;
import com.duanze.gasst.util.TimeUtils;
import com.duanze.gasst.util.Util;
import com.duanze.gasst.util.liteprefs.MyLitePrefs;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;
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
        FooterInterface, LoaderManager.LoaderCallbacks<Cursor>, GNotebookAdapter.ItemLongPressedListener,
        GNotebookAdapter.OnItemSelectListener, GNotebookAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "StartActivity";

    // version code
    private int versionCode;
    public static final String ShownRate = "shown_rate";

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
    private Calendar today;
    private SharedPreferences preferences;
    private boolean isUnLocked = false;

    private MenuItem bindItem;

    private Evernote mEvernote;
    private Timer syncTimer;

    private final int DIALOG_PROGRESS = 101;
    private final int INITIAL = 102;

    //    loader化
    private final int NOTEBOOK_LOADER_ID = 112;
    private GNotebookAdapter gNotebookAdapter;
    private LoaderManager loaderManager;

    private CharSequence mDrawerTitle;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private void loginNow() {
        if (null == bindItem) return;
        bindItem.setTitle(R.string.syn_evernote);
        gNoteRecyclerView.setRefresherEnabled(true);
    }

    private void logoutNow() {
        if (null == bindItem) return;
        bindItem.setTitle(R.string.bind_evernote);
        gNoteRecyclerView.setRefresherEnabled(false);
    }

    @Override
    public void onLoginResult(Boolean result) {
        if (result) {
            loginNow();
            mEvernote.sync(true, true, new SyncHandler());
        }
    }

    @Override
    public void onUserInfo(Boolean result, User user) {

    }

    @Override
    public void onLogout(Boolean reuslt) {

    }

    @Override
    public void changeFooter() {
        if (FolderFooter.FLAG == modeFooter) {
            modeFooter = FolderFooterDelete.FLAG;
            if (null != gNotebookAdapter) {
                gNotebookAdapter.setCheckMode(true);
            }
        } else if (FolderFooterDelete.FLAG == modeFooter) {
            modeFooter = FolderFooter.FLAG;
            if (null != gNotebookAdapter) {
                gNotebookAdapter.setCheckMode(false);
            }
        }
        setFooter();
    }

    @Override
    public void actionClick() {
        if (FolderFooter.FLAG == modeFooter) {
            showCreateFolderDialog();
        } else if (FolderFooterDelete.FLAG == modeFooter) {
            if (getDeleteNum() > 0) {
                trash();
                updateDeleteNum();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(mContext, GNoteProvider.NOTEBOOK_URI,
                GNoteProvider.NOTEBOOK_PROJECTION, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        gNotebookAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        gNotebookAdapter.swapCursor(null);
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
        if (FolderFooterDelete.FLAG == modeFooter) return;

        int oldId = MyLitePrefs.getInt(MyLitePrefs.GNOTEBOOK_ID);
        int newId = 0;
        GNotebook gNotebook = (GNotebook) view.getTag(R.string.gnotebook_data);
        if (null != gNotebook) {
            newId = gNotebook.getId();
        }
        changeBookInDB(oldId, newId);

//                关闭抽屉
        mDrawerLayout.closeDrawer(drawerRoot);
        isRecycleShown = false;
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

    @Override
    public void onRefresh() {
        mEvernote.sync(true, true, new SyncHandler());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

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

        if (MyLitePrefs.getBoolean(MyLitePrefs.PASSWORD_GUARD)) {
            Intent intent = new Intent(this, Password.class);
            intent.putExtra(MyLitePrefs.PASSWORD_HINT, MyLitePrefs.getString(MyLitePrefs.PASSWORD_HINT));
            intent.putExtra(MyLitePrefs.PASSWORD, MyLitePrefs.getString(MyLitePrefs.PASSWORD));
            this.startActivityForResult(intent, Password.REQUEST_VALIDATE_PWD);
        }
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
        preferences = getSharedPreferences(MyLitePrefs.DATA, MODE_PRIVATE);
        setContentView(R.layout.activity_start);

        dm = getResources().getDisplayMetrics();
        today = Calendar.getInstance();

        initDrawer();

        //evernote
        mEvernote = new Evernote(this, this);

        // / 配置全局Handler
        MyApplication application = (MyApplication) getApplication();
        application.setHandler(new SyncHandler());

        //如果是第一次启动应用，在数据库中添加note
        versionCode = Util.getVersionCode(mContext);
        boolean first = preferences.getBoolean("first", true);
        if (first) {
            firstLaunch();
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
                List<GNote> list = GNoteDB.getInstance(mContext).loadGNotes();
                for (GNote gNote : list) {
                    String content = Html.fromHtml(gNote.getContent()).toString();
                    gNote.setContent(content);
                    ProviderUtil.updateGNote(mContext, gNote);
                }
                syncHandler.sendEmptyMessage(UPGRADE_END);
            }
        }).run();

        GNote zero = new GNote();
        zero.setCalToTime(today);
        zero.setContent(getResources().getString(R.string.tip2));
        zero.setEditTime(TimeUtils.getCurrentTimeInLong());
        ProviderUtil.insertGNote(mContext, zero);

        notePlusOne();
    }

    private void notePlusOne() {
        int n = MyLitePrefs.getInt(MyLitePrefs.PURENOTE_NOTE_NUM);
        MyLitePrefs.putInt(MyLitePrefs.PURENOTE_NOTE_NUM, n + 1);
    }

    private void upgradeTo28() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SyncHandler syncHandler = new SyncHandler();
                syncHandler.sendEmptyMessage(UPGRADE_START);
                List<GNote> list = GNoteDB.getInstance(mContext).loadGNotes();
                for (GNote gNote : list) {
                    if (0 == gNote.getEditTime()) {
                        gNote.setEditTime(TimeUtils.getCurrentTimeInLong());
                        ProviderUtil.updateGNote(mContext, gNote);
                    }
                }
                syncHandler.sendEmptyMessage(UPGRADE_END);
            }
        }).run();
    }

    private void upgradeTo27() {
        GNote zero = new GNote();
        zero.setCalToTime(today);
        zero.setContent(getResources().getString(R.string.tip_optional));
//            two.setSynStatus(GNote.NEW);
        zero.setEditTime(TimeUtils.getCurrentTimeInLong());
        ProviderUtil.insertGNote(mContext, zero);

        notePlusOne();
    }

    private void setActionBarTitle() {
        int bookId = MyLitePrefs.getInt(MyLitePrefs.GNOTEBOOK_ID);
        String bookName;
        if (bookId == 0) {
            bookName = getString(R.string.all_notes);
        } else {
            bookName = GNoteDB.getInstance(mContext).getGNotebookById(bookId).getName();
        }

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(bookName);
        }
    }


    private void upgradeTo21() {
        // / Set our notes num
        int n = GNoteDB.getInstance(mContext).loadGNotes().size();
        preferences.edit().putInt(MyLitePrefs.PURENOTE_NOTE_NUM, n).putInt("mode", MODE_LIST).apply();
    }


    // / 左侧滑动抽屉相关
    private void createFolder(String name) {
        GNotebook gNotebook = new GNotebook();
        gNotebook.setName(name);
        ProviderUtil.insertGNotebook(mContext, gNotebook);
    }

    /**
     * 删除按钮的方法
     */
    // TODO 待优化～～～～～
    private void trash() {
        new AlertDialog.Builder(this).
                setMessage(R.string.delete_folder_title).
                setNegativeButton(android.R.string.cancel, null).
                setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int extractId = MyLitePrefs.getInt(MyLitePrefs.LIGHTNING_EXTRACT_SAVE_LOCATION);
                        //快写存储位置
                        int quickId = MyLitePrefs.getInt(MyLitePrefs.QUICK_WRITE_SAVE_LOCATION);

                        HashMap<Integer, GNotebook> map = gNotebookAdapter.getCheckedItems();
                        if (null == map || 0 == map.size()) {
                            return;
                        } else {
                            int oldId = 0;
                            int newId = 0;

                            Set<Integer> keys = map.keySet();
                            for (Integer key : keys) {
                                if (key == extractId) {
                                    MyLitePrefs.putInt(MyLitePrefs.LIGHTNING_EXTRACT_SAVE_LOCATION, 0);
                                }
                                if (key == quickId) {
                                    MyLitePrefs.putInt(MyLitePrefs.QUICK_WRITE_SAVE_LOCATION, 0);
                                }
                                GNotebook gNotebook = map.get(key);
                                if (gNotebook.isSelected()) {
                                    oldId = key;
                                    changeToBook(oldId, newId);
                                }
                                gNotebookAdapter.deleteGNotebook(gNotebook);
                            }
//这种做法是不对的，因为设计缺陷，处理变得极为复杂，今后还是要统一数据，统一处理
                            gNotebookAdapter.destroyCheckedItems();
                        }
                        changeFooter();

                    }
                }).show();
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
                        }
                    }
                })
                .setNegativeButton(R.string.folder_cancel, null)
                .create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private int modeFooter;

    @Override
    public int getDeleteNum() {
        if (null == gNotebookAdapter) return 0;
        return gNotebookAdapter.getSelectedCount();
    }

    private FolderFooter footer;
    private FolderFooterDelete footerDelete;
    private ListView folderListView;
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
                if (null != gNotebookAdapter) {
                    gNotebookAdapter.setCheckMode(false);
                }
                loaderManager.restartLoader(NOTEBOOK_LOADER_ID, null, StartActivity.this);
                modeFooter = FolderFooter.FLAG;
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
        gNotebookAdapter = new GNotebookAdapter(mContext, null, 0, this, this, this);
        folderListView.setAdapter(gNotebookAdapter);

        loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(NOTEBOOK_LOADER_ID, null, this);
    }

    public void changeToBook(int oldId, int newId) {
        if (oldId == newId) return;
        //刷新界面
        LogUtil.i(TAG, "gNotebook id oldId:" + oldId + " newId:" + newId);
        MyLitePrefs.putInt(MyLitePrefs.GNOTEBOOK_ID, newId);
        refreshUI();
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
        // / 此步需要使用 provider 来配合观察者模式
        if (0 != bookId) {
            try {
                GNotebook gNotebook = GNoteDB.getInstance(mContext).getGNotebookById(bookId);
                gNotebook.setSelected(GNotebook.TRUE);
                ProviderUtil.updateGNotebook(mContext, gNotebook);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void cancelFolder(int bookId) {
        // / 仅当非 purenote 时需要更新GNotebook
        // 此步 不 需要使用 provider 来配合观察者模式
        // 但如果后一步 bookId 为0，那也会悲剧
        // 所以还是进行两次更新吧，汗- -||
        if (0 != bookId) {
            try {
                GNotebook gNotebook = GNoteDB.getInstance(mContext).getGNotebookById(bookId);
                gNotebook.setSelected(GNotebook.FALSE);
                ProviderUtil.updateGNotebook(mContext, gNotebook);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void setFooter() {
        if (modeFooter == FolderFooter.FLAG) {
            if (footer == null) {
                footer = new FolderFooter();
            }
            getFragmentManager().beginTransaction().replace(R.id.fl_folder_footer, footer).commit();
        } else if (modeFooter == FolderFooterDelete.FLAG) {
            if (footerDelete == null) {
                footerDelete = new FolderFooterDelete();
            }
            getFragmentManager().beginTransaction().replace(R.id.fl_folder_footer, footerDelete)
                    .commit();
        }
    }

//    滑动抽屉至此结束

    private int upgradeTo15() {
//      将所有原有数据转移至默认文件夹中
        List<GNote> list = GNoteDB.getInstance(mContext).loadGNotes();
        for (GNote gNote : list) {
            gNote.setGNotebookId(0);
            GNoteDB.getInstance(mContext).updateGNote(gNote);
        }
        int n = list.size();
        MyLitePrefs.putInt(MyLitePrefs.PURENOTE_NOTE_NUM, n);
        return n;
    }

    /*
    标注版本号
     */
    private void setVersionCode() {
        preferences.edit().putInt("version_code", versionCode).apply();
    }

    private void firstLaunch() {
        //如果是第一次启动应用，在数据库中添加note
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("first", false);
        editor.apply();

        GNote one = new GNote();
        one.setCalToTime(today);
        one.setContent(getString(R.string.tip1));
        one.setEditTime(TimeUtils.getCurrentTimeInLong());
        GNoteDB.getInstance(mContext).saveGNote(one);

        Calendar tmpCal = (Calendar) today.clone();
        tmpCal.add(Calendar.DAY_OF_MONTH, -1);

        GNote two = new GNote();
        two.setCalToTime(tmpCal);
        two.setContent(getString(R.string.tip2));
        two.setEditTime(TimeUtils.getCurrentTimeInLong());
        GNoteDB.getInstance(mContext).saveGNote(two);

        MyLitePrefs.putInt(MyLitePrefs.PURENOTE_NOTE_NUM, 2);
    }


    private GNoteRecyclerView gNoteRecyclerView;
    private FiltratePage filtratePage;
    private boolean isInFiltrate = false;

    private void enterFiltratePage() {
        if (isInFiltrate) return;
        isInFiltrate = true;
        lockDrawerLock();
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
        unlockDrawerLock();
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (null == filtratePage) {
            return;
        } else {
            transaction.hide(filtratePage);
        }
        transaction.show(gNoteRecyclerView).commit();
        gNoteRecyclerView.setUserVisibleHint(true);
    }

    public void changeContent() {
        // / If recreate ...
        if (null != filtratePage) {
            getSupportFragmentManager().beginTransaction().hide(filtratePage).commit();
        }

        if (mode == MODE_LIST) {
            if (null == gNoteRecyclerView) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_content, new GNoteRecyclerView()).commit();
            }
            unlockDrawerLock();//打开手势滑动
        } else if (mode == MODE_GRID) {//暂时弃用

        }
    }

    public void lockDrawerLock() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//关闭手势滑动
    }

    public void unlockDrawerLock() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);//关闭手势滑动
    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof FiltratePage) {
            LogUtil.i(TAG, "filtratePage = (FiltratePage) fragment;");
            filtratePage = (FiltratePage) fragment;
        } else if (fragment instanceof GNoteRecyclerView) {
            gNoteRecyclerView = (GNoteRecyclerView) fragment;
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
        // / umeng
        MobclickAgent.onResume(this);

//        if (MyLitePrefs.getBoolean(MyLitePrefs.PASSWORD_GUARD) && !isUnLocked) {
//            Intent intent = new Intent(this, Password.class);
//            intent.putExtra(MyLitePrefs.PASSWORD_HINT, MyLitePrefs.getString(MyLitePrefs.PASSWORD_HINT));
//            intent.putExtra(MyLitePrefs.PASSWORD, MyLitePrefs.getString(MyLitePrefs.PASSWORD));
//            this.startActivityForResult(intent, Password.REQUEST_VALIDATE_PWD);
//        }
    }

    public void uiOperation() {
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
                        .setNegativeButton(R.string.dont_care, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int count = preferences.getInt(Note.EditCount, 0);
                                count++;
                                preferences.edit().putInt(Note.EditCount, count).apply();
                            }
                        })
                        .setNeutralButton(R.string.rate_feedback, new DialogInterface
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
            if (null != gNoteRecyclerView) {
                gNoteRecyclerView.refreshUI();
            }
        } else if (mode == MODE_GRID) {

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
//        if (null != gNotebookAdapter && null != gNotebookAdapter.getCursor()) {
//            gNotebookAdapter.getCursor().close();
//        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isRecycleShown || isDrawerOpened) {
            getMenuInflater().inflate(R.menu.empty_menu, menu);
        } else {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu, menu);

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
//                    It's really weird when we enter SearchView ,this function is called.
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
                SettingsActivity.actionStart(mContext);
                break;
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
    public static final int NEED_RECREATE = 0x0010;
    public static final int NEED_CONFIG_LAYOUT = 0x0011;
    public static final int NEED_NOTIFY = 0x0012;

    public class SyncHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Evernote.SYNC_START:
                    setRefreshing(true);
                    break;
                case Evernote.SYNC_END:
                    setRefreshing(false);
                    break;
                case UPGRADE_START:
                    showDialog(OPERATE);
                    break;
                case UPGRADE_END:
                    dismissDialog(OPERATE);
                    break;

                case NEED_RECREATE:
                    recreate();
                    break;
                case NEED_CONFIG_LAYOUT:
                    gNoteRecyclerView.configLayoutManager();
                    break;
                case NEED_NOTIFY:
                    gNoteRecyclerView.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }

    private void setRefreshing(boolean b) {
        if (null == gNoteRecyclerView || !gNoteRecyclerView.isVisible()) return;
        SwipeRefreshLayoutEx refreshLayout = gNoteRecyclerView.getRefreshLayout();
        refreshLayout.setRefreshing(b);

        if (b) {
            refreshLayout.setEnabled(false);
        } else {
            refreshLayout.setEnabled(true);
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
            case Password.REQUEST_VALIDATE_PWD:
                if (RESULT_OK == resultCode) {
//                    isUnLocked = true;
                }
                break;
        }
    }
}
