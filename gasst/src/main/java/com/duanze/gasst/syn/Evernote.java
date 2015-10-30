package com.duanze.gasst.syn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;

import com.duanze.gasst.activity.Folder;
import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.PreferencesUtils;
import com.duanze.gasst.util.ProviderUtil;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.User;
import com.evernote.thrift.TException;
import com.evernote.thrift.transport.TTransportException;

import java.util.List;
import java.util.Map;


public class Evernote {
    public static final String TAG = "EverNote";

    private static final String CONSUMER_KEY = "guofeiyao";
    private static final String CONSUMER_SECRET = "1070d3ab7a287a11";

    public static final String NOTEBOOK_NAME = "PureNote";
    public static final String EVERNOTE_TOKEN = "PureNote_Token";
    public static final String EVERNOTE_TOKEN_TIME = "PureNote_Token_Time";
    public static final String EVERNOTE_USER_NAME = "User_Name";
    public static final String EVERNOTE_USER_EMAIL = "User_Email";
    public static final String EVERNOTE_NOTEBOOK_GUID = "Notebook_Guid";

    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession
            .EvernoteService.PRODUCTION;

    public static boolean downloading = false;
    public static boolean uploading = false;
    public static boolean synchronizing = false;

    private EvernoteSession mEvernoteSession;
    private SharedPreferences mSharedPreferences;

    private Context mContext;
    private EvernoteLoginCallback mEvernoteLoginCallback;
    private GNoteDB db;

    public interface EvernoteLoginCallback {
        public void onLoginResult(Boolean result);

        public void onUserinfo(Boolean result, User user);

        public void onLogout(Boolean reuslt);
    }

    public Evernote(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(Settings.DATA, Activity.MODE_PRIVATE);
        mEvernoteSession = EvernoteSession.getInstance(mContext, CONSUMER_KEY, CONSUMER_SECRET,
                EVERNOTE_SERVICE);
        db = GNoteDB.getInstance(mContext);
    }

    public Evernote(Context context, EvernoteLoginCallback l) {
        this(context);
        mEvernoteLoginCallback = l;
    }

    public boolean isLogin() {
        return mEvernoteSession.isLoggedIn();
    }

    public void auth() {
        mEvernoteSession.authenticate(mContext);
    }

    public void onAuthFinish(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            mSharedPreferences.edit().putString(EVERNOTE_TOKEN, mEvernoteSession.getAuthToken())
                    .putLong(EVERNOTE_TOKEN_TIME, System.currentTimeMillis()).apply();
            getUserInfo();
            if (mEvernoteLoginCallback != null) {
                mEvernoteLoginCallback.onLoginResult(true);
            }
        } else {
            if (mEvernoteLoginCallback != null) {
                mEvernoteLoginCallback.onLoginResult(false);
            }
        }
    }


    public void getUserInfo() {
        if (mEvernoteSession.isLoggedIn()) {
            try {
                mEvernoteSession.getClientFactory().createUserStoreClient().getUser(new OnClientCallback<User>() {

                    @Override
                    public void onSuccess(User user) {
                        mSharedPreferences.edit().putString(EVERNOTE_USER_NAME, user.getUsername
                                ()).putString(EVERNOTE_USER_EMAIL, user.getEmail()).apply();
                        if (mEvernoteLoginCallback != null) {
                            mEvernoteLoginCallback.onUserinfo(true, user);
                        }
                    }

                    @Override
                    public void onException(Exception exception) {
                        if (mEvernoteLoginCallback != null) {
                            mEvernoteLoginCallback.onUserinfo(false, null);
                        }
                    }
                });
            } catch (IllegalStateException e) {
                e.printStackTrace();
                if (mEvernoteLoginCallback != null) {
                    mEvernoteLoginCallback.onUserinfo(false, null);
                }
            } catch (TTransportException e) {
                e.printStackTrace();
                if (mEvernoteLoginCallback != null) {
                    mEvernoteLoginCallback.onUserinfo(false, null);
                }
            }
        }
    }

    public String getUsername() {
        return mSharedPreferences.getString(EVERNOTE_USER_NAME, null);
    }

    public void Logout() {
        try {
            mEvernoteSession.logOut(mContext);
            mSharedPreferences.edit().remove(EVERNOTE_USER_NAME).remove(EVERNOTE_NOTEBOOK_GUID)
                    .remove(EVERNOTE_USER_EMAIL).apply();
            if (mEvernoteLoginCallback != null) {
                mEvernoteLoginCallback.onLogout(true);
            }

        } catch (InvalidAuthenticationException e) {
            if (mEvernoteLoginCallback != null) {
                mEvernoteLoginCallback.onLogout(false);
            }
        }
    }

    public boolean isNotebookExsist(String guid, String name) throws Exception {
        boolean result = false;
        try {
            Notebook notebook = mEvernoteSession.getClientFactory().createNoteStore().getNotebook
                    (mEvernoteSession.getAuthToken(), guid);
            if (notebook.getName().equals(name)) {
                result = true;
                LogUtil.e(TAG, guid + "笔记本存在");
                mSharedPreferences.edit().putString(EVERNOTE_NOTEBOOK_GUID, notebook.getGuid())
                        .commit();
            }
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
            if (e.getIdentifier().equals("Notebook.guid")) {
                result = false;
                LogUtil.e(TAG, guid + "笔记本不存在");
            }
        }
        return result;
    }

    /**
     * create a notebook by bookname
     *
     * @param bookname
     * @return
     * @throws Exception
     */
    public boolean createNotebook(String bookname) throws Exception {
        Notebook notebook = new Notebook();
        notebook.setDefaultNotebook(false);
        notebook.setName(bookname);
        boolean result = false;
        try {
            Notebook resultNotebook = mEvernoteSession.getClientFactory().createNoteStore()
                    .createNotebook(mEvernoteSession.getAuthToken(), notebook);
            result = true;
            LogUtil.e(TAG, "Notebook" + bookname + "不存在，创建成功");
            mSharedPreferences.edit().putString(EVERNOTE_NOTEBOOK_GUID, resultNotebook.getGuid())
                    .commit();
        } catch (EDAMUserException e) {
            if (e.getErrorCode() == EDAMErrorCode.DATA_CONFLICT) {
                result = true;
                LogUtil.e(TAG, "已经存在，无需创建");
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "传输出现错误");
            throw e;
        }
        return result;
    }

    private void updateGNote(GNote gNote, Note note) {
        gNote.setSynStatus(GNote.NOTHING);
        gNote.setGuid(note.getGuid());
        gNote.setEditTime(note.getUpdated());
        gNote.setCreatedTime(note.getCreated());
        gNote.setBookGuid(note.getNotebookGuid());

//        db.updateGNote(gNote);
        ProviderUtil.updateGNote(mContext, gNote);
    }

    private void deleteGNote(GNote gNote) {
//        db.deleteGNote(gNote.getId());
        gNote.setSynStatus(GNote.NOTHING);
        ProviderUtil.updateGNote(mContext, gNote);
    }

    private Note createNote(GNote gNote) throws Exception {
        try {
            Note note = gNote.toNote();
            if ("".equals(gNote.getBookGuid())) {
                note.setNotebookGuid(mSharedPreferences.getString(EVERNOTE_NOTEBOOK_GUID, null));
            }
            Note responseNote = mEvernoteSession.getClientFactory().createNoteStore().createNote
                    (mEvernoteSession.getAuthToken(), note);
            LogUtil.i(TAG, "Note创建成功");
            updateGNote(gNote, responseNote);
            return responseNote;
        } catch (EDAMUserException e) {
            throw new Exception("Note格式不合理");
        } catch (EDAMNotFoundException e) {
            throw new Exception("笔记本不存在");
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean deleteNote(GNote gNote) {
        Note note = gNote.toNote();

        if (note.getGuid() == null) {
            LogUtil.e(TAG, "GUID是空，无需删除");
            deleteGNote(gNote);
            return true;
        } else {
            try {
                mEvernoteSession.getClientFactory().createNoteStore().deleteNote(mEvernoteSession
                        .getAuthToken(), note.getGuid());
                LogUtil.e(TAG, "Note删除成功");
                deleteGNote(gNote);
                return true;
            } catch (EDAMUserException e) {
                LogUtil.e(TAG, "Note早已被删除，说明删除成功");
                deleteGNote(gNote);
                return true;
            } catch (EDAMNotFoundException e) {
                LogUtil.e(TAG, "Note未找到，说明无需删除");
                deleteGNote(gNote);
                return true;
            } catch (Exception e) {
                LogUtil.e(TAG, "传输失败，说明删除失败");
                return false;
            }
        }
    }

    private Note updateNote(GNote gNote) throws Exception {
        try {
            Note responseNote = mEvernoteSession.getClientFactory().createNoteStore().updateNote
                    (mEvernoteSession.getAuthToken(), gNote.toNote());
            updateGNote(gNote, responseNote);
            LogUtil.i(TAG, "Note更新成功");
            return responseNote;
        } catch (EDAMUserException e) {
            LogUtil.e(TAG, "数据格式有误");
            throw new Exception(e.getCause());
        } catch (EDAMNotFoundException e) {
            LogUtil.e(TAG, "Note根据GUID没有找到:" + e.getCause());
            throw new Exception("Note未找到");
        } catch (Exception e) {
            LogUtil.e(TAG, "传输出现错误:" + e.getCause());
            throw new Exception("传输出现错误:" + e.getCause());
        }
    }

    private void makeSureNotebookExists(String NotebookName) throws Exception {
        try {
            if (mSharedPreferences.contains(EVERNOTE_NOTEBOOK_GUID)) {
                if (!isNotebookExsist(mSharedPreferences.getString(EVERNOTE_NOTEBOOK_GUID, ""),
                        NOTEBOOK_NAME)) {
                    createNotebook(NOTEBOOK_NAME);
                }
            } else {
                List<Notebook> books = mEvernoteSession.getClientFactory().createNoteStore()
                        .listNotebooks(mEvernoteSession.getAuthToken());
                int count = books.size();
                for (int i = 0; i < count; i++) {
                    Notebook book = books.get(i);
                    if (book.getName().equals(NotebookName)) {
                        mSharedPreferences.edit().putString(EVERNOTE_NOTEBOOK_GUID, book.getGuid
                                ()).commit();
                        return;
                    }
                }
                createNotebook(NOTEBOOK_NAME);
            }

        } catch (Exception e) {
            LogUtil.e(TAG, "检查笔记本是否存和创建笔记本的时候出现异常");
            throw e;
        }
    }

    private void saveNote(Note note) {
        GNote gNote = GNote.parseFromNote(note);
//        db.saveGNote(gNote);
        ProviderUtil.insertGNote(mContext, gNote);
        updateGNotebook(0, +1);
    }

    private void updateGNotebook(int id, int diff) {
        if (id == 0) {
            int cnt = mSharedPreferences.getInt(Folder.PURENOTE_NOTE_NUM, 3);
            PreferencesUtils.getInstance(mContext).setNotesNum(cnt + diff);
        } else {
            List<GNotebook> gNotebooks = db.loadGNotebooks();
            for (GNotebook gNotebook : gNotebooks) {
                if (gNotebook.getId() == id) {
                    int cnt = gNotebook.getNotesNum();
                    gNotebook.setNotesNum(cnt + diff);
//                    db.updateGNotebook(gNotebook);
                    ProviderUtil.updateGNotebook(mContext, gNotebook);
                    break;
                }
            }
        }
    }

    private void downloadNote(String guid) {
        LogUtil.e(TAG, "准备添加:" + guid);
        try {
            Note note = mEvernoteSession.getClientFactory().createNoteStore().getNote
                    (mEvernoteSession.getAuthToken(), guid, true, false, false, false);
            LogUtil.e(TAG, "获取到的文本：" + note.getContent());
            saveNote(note);
        } catch (TTransportException e) {
        } catch (EDAMUserException e) {
        } catch (EDAMSystemException e) {
        } catch (EDAMNotFoundException e) {
        } catch (TException e) {
        }
    }

    private void updateLocalNote(String guid, GNote gNote) {
        LogUtil.e(TAG, "准备更新:" + guid);
        try {
            Note note = mEvernoteSession.getClientFactory().createNoteStore().getNote
                    (mEvernoteSession.getAuthToken(), guid, true, false, false, false);

            if (gNote != null) {
                gNote.setContentFromNote(note);
                gNote.setEditTime(note.getUpdated());
//                db.updateGNote(gNote);
                ProviderUtil.updateGNote(mContext, gNote);
            }
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (EDAMUserException e) {
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }

    }

    private void syncDown() {
        if (downloading) {
            return;
        }
        downloading = true;
        NoteFilter noteFilter = new NoteFilter();
        String guid = mSharedPreferences.getString(EVERNOTE_NOTEBOOK_GUID, "");
        noteFilter.setNotebookGuid(guid);
        NotesMetadataResultSpec notesMetadataResultSpec = new NotesMetadataResultSpec();
        notesMetadataResultSpec.setIncludeUpdated(true);
        try {
            NoteCollectionCounts noteCollectionCounts = mEvernoteSession.getClientFactory()
                    .createNoteStore().findNoteCounts(mEvernoteSession.getAuthToken(),
                            noteFilter, false);
            Map<String, Integer> maps = noteCollectionCounts.getNotebookCounts();
            if (maps == null || maps.size() == 0) return;
            int maxCount = maps.get(guid);
            LogUtil.e(TAG, "-------------服务器端笔记数量：" + maxCount);
            NotesMetadataList list = mEvernoteSession.getClientFactory().createNoteStore()
                    .findNotesMetadata(mEvernoteSession.getAuthToken(), noteFilter, 0, maxCount,
                            notesMetadataResultSpec);

            for (int i = 0; i < list.getNotes().size(); i++) {
                NoteMetadata note = list.getNotes().get(i);
                GNote gNote = db.getGNoteByGuid(note.getGuid());
                if (gNote != null) {
                    if (gNote.getEditTime() != note.getUpdated()) {
                        // 更新数据
                        updateLocalNote(note.getGuid(), gNote);
                    }
                } else {
                    // 添加数据
                    downloadNote(note.getGuid());
                }
            }
        } catch (TTransportException e) {
        } catch (EDAMUserException e) {
        } catch (EDAMSystemException e) {
        } catch (EDAMNotFoundException e) {
        } catch (TException e) {
        } finally {
            downloading = false;
        }
    }

    private void syncUp(List<GNote> list) {
        if (uploading) {
            LogUtil.e(TAG, "正在同步");
            return;
        }
        LogUtil.e(TAG, "开始同步");
        uploading = true;

        for (GNote gNote : list) {
            switch (gNote.getSynStatus()) {
                case GNote.NEW:
                    try {
                        createNote(gNote);
                    } catch (Exception e) {
                        LogUtil.e(TAG, "尝试创建新的Note的时候出现错误:" + e.getCause());
                        continue;
                    }
                    break;
                case GNote.UPDATE:
                    try {
                        updateNote(gNote);
                    } catch (Exception e) {
                        LogUtil.e(TAG, "尝试更新的时候出现错误:" + e.getCause());
                        continue;
                    }
                    break;
                case GNote.DELETE:
                    deleteNote(gNote);
                    break;
            }

        }

        uploading = false;
    }

    public synchronized void sync(final boolean syncUp, final boolean syncDown, Handler hanler) {
        if (hanler != null) {
            hanler.sendEmptyMessage(SYNC_START);
        }
        toSync(syncUp, syncDown, hanler);
    }

    private synchronized void toSync(final boolean syncUp, final boolean syncDown, Handler
            handler) {
        new SyncTask(syncUp, syncDown, handler).execute();
    }

    public static final int SYNC_START = 1;
    public static final int SYNC_END = 10;
    public static final int SYNC_ERROR = 100;
    public static final int SYNC_SUCCESS = 1000;

    class SyncTask extends AsyncTask<Void, Integer, Void> {
        Handler mHandler;
        boolean mSyncUp;
        boolean mSyncDown;

        public SyncTask(Boolean syncUp, Boolean syncDown, Handler handler) {
            this(syncUp, syncDown);
            mHandler = handler;
        }

        private SyncTask(Boolean syncUp, Boolean syncDown) {
            mSyncUp = syncUp;
            mSyncDown = syncDown;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mSyncUp == false && mSyncDown == false) {
                return null;
            }
            if (mEvernoteSession.isLoggedIn() == false) {
                LogUtil.e(TAG, "未登录");
                publishProgress(new Integer[]{SYNC_ERROR});
                return null;
            }
            publishProgress(new Integer[]{SYNC_START});
            try {
                makeSureNotebookExists(NOTEBOOK_NAME);
                if (mSyncUp) syncUp(db.loadRawGNotes());
//                    syncUp(db.loadGNotesByBookId(0));
                if (mSyncDown) syncDown();
                publishProgress(new Integer[]{SYNC_SUCCESS});
            } catch (Exception e) {
                publishProgress(new Integer[]{SYNC_ERROR});
                return null;
            } finally {
                publishProgress(new Integer[]{SYNC_END});
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
                case SYNC_START:
                    mHandler.sendEmptyMessage(SYNC_START);
                    break;
                case SYNC_END:
                    mHandler.sendEmptyMessage(SYNC_END);
                    break;
                case SYNC_ERROR:
                    mHandler.sendEmptyMessage(SYNC_ERROR);
                    break;
                case SYNC_SUCCESS:
                    mHandler.sendEmptyMessage(SYNC_SUCCESS);
                    break;
                default:
                    break;
            }
        }
    }

}
