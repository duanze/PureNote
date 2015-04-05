package com.duanze.gasst.syn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;

import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.util.LogUtil;
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
        mEvernoteSession = EvernoteSession.getInstance(mContext, CONSUMER_KEY,
                CONSUMER_SECRET, EVERNOTE_SERVICE);
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
            mSharedPreferences.edit()
                    .putString(EVERNOTE_TOKEN, mEvernoteSession.getAuthToken())
                    .putLong(EVERNOTE_TOKEN_TIME, System.currentTimeMillis())
                    .apply();
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
                mEvernoteSession.getClientFactory().createUserStoreClient()
                        .getUser(new OnClientCallback<User>() {

                            @Override
                            public void onSuccess(User user) {
                                mSharedPreferences
                                        .edit()
                                        .putString(EVERNOTE_USER_NAME,
                                                user.getUsername())
                                        .putString(EVERNOTE_USER_EMAIL,
                                                user.getEmail()).apply();
                                if (mEvernoteLoginCallback != null) {
                                    mEvernoteLoginCallback.onUserinfo(true,
                                            user);
                                }
                            }

                            @Override
                            public void onException(Exception exception) {
                                if (mEvernoteLoginCallback != null) {
                                    mEvernoteLoginCallback.onUserinfo(false,
                                            null);
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
            mSharedPreferences.edit().remove(EVERNOTE_USER_NAME)
                    .remove(EVERNOTE_NOTEBOOK_GUID).remove(EVERNOTE_USER_EMAIL)
                    .apply();
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
            Notebook notebook = mEvernoteSession.getClientFactory()
                    .createNoteStore()
                    .getNotebook(mEvernoteSession.getAuthToken(), guid);
            if (notebook.getName().equals(name)) {
                result = true;
                LogUtil.e(TAG, guid + "笔记本存在");
                mSharedPreferences.edit()
                        .putString(EVERNOTE_NOTEBOOK_GUID, notebook.getGuid())
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
            Notebook resultNotebook = mEvernoteSession.getClientFactory()
                    .createNoteStore()
                    .createNotebook(mEvernoteSession.getAuthToken(), notebook);
            result = true;
            LogUtil.e(TAG, "Notebook" + bookname + "不存在，创建成功");
            mSharedPreferences
                    .edit()
                    .putString(EVERNOTE_NOTEBOOK_GUID, resultNotebook.getGuid())
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

        db.updateGNote(gNote);
    }

    private void deleteGNote(GNote gNote) {
        db.deleteGNote(gNote.getId());
    }

    private Note createNote(GNote gNote) throws Exception {
        try {
            Note note = gNote.toNote();
            if ("".equals(gNote.getBookGuid())) {
                note.setNotebookGuid(mSharedPreferences.getString(
                        EVERNOTE_NOTEBOOK_GUID, null));
            }
            Note responseNote = mEvernoteSession.getClientFactory()
                    .createNoteStore()
                    .createNote(mEvernoteSession.getAuthToken(), note);
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
            return true;
        } else {
            try {
                mEvernoteSession
                        .getClientFactory()
                        .createNoteStore()
                        .deleteNote(mEvernoteSession.getAuthToken(),
                                note.getGuid());
                LogUtil.e(TAG, "Note删除成功");
                deleteGNote(gNote);
                return true;
            } catch (EDAMUserException e) {
                LogUtil.e(TAG, "Note早已被删除，说明删除成功");
                return true;
            } catch (EDAMNotFoundException e) {
                LogUtil.e(TAG, "Note未找到，说明无需删除");
                return true;
            } catch (Exception e) {
                LogUtil.e(TAG, "传输失败，说明删除失败");
                return false;
            }
        }
    }

    private Note updateNote(GNote gNote) throws Exception {
        try {
            Note responseNote = mEvernoteSession
                    .getClientFactory()
                    .createNoteStore()
                    .updateNote(mEvernoteSession.getAuthToken(),
                            gNote.toNote());

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

    private void makeSureNotebookExsits(String NotebookName) throws Exception {
        try {
            if (mSharedPreferences.contains(EVERNOTE_NOTEBOOK_GUID)) {
                if (!isNotebookExsist(mSharedPreferences.getString(
                        EVERNOTE_NOTEBOOK_GUID, ""), NOTEBOOK_NAME)) {
                    createNotebook(NOTEBOOK_NAME);
                }
            } else {
                List<Notebook> books = mEvernoteSession.getClientFactory()
                        .createNoteStore()
                        .listNotebooks(mEvernoteSession.getAuthToken());
                int count = books.size();
                for (int i = 0; i < count; i++) {
                    Notebook book = books.get(i);
                    if (book.getName().equals(NotebookName)) {
                        mSharedPreferences
                                .edit()
                                .putString(EVERNOTE_NOTEBOOK_GUID,
                                        book.getGuid()).commit();
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
        GNote gNote = GNote.parseGNote(note);
        db.saveGNote(gNote);
    }

    private void downloadNote(String guid) {
        LogUtil.e(TAG, "准备添加:" + guid);
        try {
            Note note = mEvernoteSession
                    .getClientFactory()
                    .createNoteStore()
                    .getNote(mEvernoteSession.getAuthToken(), guid, true,
                            false, false, false);
            LogUtil.e(TAG, "获取到的文本：" + note.getContent());

            saveNote(note);

        } catch (TTransportException e) {
        } catch (EDAMUserException e) {
        } catch (EDAMSystemException e) {
        } catch (EDAMNotFoundException e) {
        } catch (TException e) {
        }
    }

    private void updateLocalNote(String guid, int _id) {
        LogUtil.e(TAG, "准备更新:" + guid);
        try {
            Note note = mEvernoteSession
                    .getClientFactory()
                    .createNoteStore()
                    .getNote(mEvernoteSession.getAuthToken(), guid, true,
                            false, false, false);

            GNote gNote = GNote.parseGNote(note);
            gNote.setId(_id);
            updateGNote(gNote, note);
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
            NoteCollectionCounts noteCollectionCounts = mEvernoteSession
                    .getClientFactory()
                    .createNoteStore()
                    .findNoteCounts(mEvernoteSession.getAuthToken(),
                            noteFilter, false);
            Map<String, Integer> maps = noteCollectionCounts
                    .getNotebookCounts();
            if (maps == null || maps.size() == 0)
                return;
            int maxcount = maps.get(guid);
            NotesMetadataList list = mEvernoteSession
                    .getClientFactory()
                    .createNoteStore()
                    .findNotesMetadata(mEvernoteSession.getAuthToken(),
                            noteFilter, 0, maxcount, notesMetadataResultSpec);

            for (int i = 0; i < list.getNotes().size(); i++) {
                NoteMetadata note = list.getNotes().get(i);

                GNote gNote = db.getGNoteByGuid(note.getGuid());
                if (gNote != null) {
                    if (gNote.getEditTime() != note.getUpdated()) {

                        // 更新数据
                        updateLocalNote(note.getGuid(), gNote.getId());

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
                        LogUtil.e(TAG,
                                "尝试创建新的Note的时候出现错误:" + e.getCause());
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

    public synchronized void sync(final boolean syncUp, final boolean syncDown,
                                  Handler hanler) {
        if (hanler != null) {
            hanler.sendEmptyMessage(SYNC_START);
        }
        toSync(syncUp, syncDown, hanler);
    }

    private synchronized void toSync(final boolean syncUp,
                                     final boolean syncDown, Handler handler) {
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
                makeSureNotebookExsits(NOTEBOOK_NAME);
                if (mSyncUp)
                    syncUp(db.loadRawGNotes());
                if (mSyncDown)
                    syncDown();
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


//    public void createNotebook(final String bookname) {
//        Notebook notebook = new Notebook();
//        notebook.setDefaultNotebook(false);
//        notebook.setName(bookname);
//        try {
//            mEvernoteSession.getClientFactory()
//                    .createNoteStoreClient()
//                    .createNotebook(notebook, new OnClientCallback<Notebook>() {
//                        @Override
//                        public void onSuccess(Notebook data) {
//                            saveNotebookGuid(data.getGuid());
//
//                            LogUtil.i(TAG, "Notebook:" + bookname + "不存在，创建成功");
//                        }
//
//                        @Override
//                        public void onException(Exception exception) {
//                            LogUtil.i(TAG, "创建notebook失败，开始在已有列表中搜寻");
//                            searchNoteBookInList();
//                        }
//                    });
//
//        } catch (TTransportException e) {
//            Toast.makeText(mContext, "创建笔记本失败", Toast.LENGTH_SHORT).show();
//            LogUtil.i(TAG, "传输出现错误");
//            e.printStackTrace();
//        }
//    }
//
//    private void saveNotebookGuid(String guid) {
//        mSharedPreferences
//                .edit()
//                .putString(EVERNOTE_NOTEBOOK_GUID, guid)
//                .apply();
//
//        LogUtil.i(TAG, "Save guid:" + guid);
//    }
//
//    private void searchNoteBookInList() {
//        try {
//            mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>() {
//                @Override
//                public void onSuccess(List<Notebook> data) {
//                    for (Notebook notebook : data) {
//                        if (NOTEBOOK_NAME.equals(notebook.getName())) {
//                            LogUtil.i(TAG, "搜寻成功");
//                            saveNotebookGuid(notebook.getGuid());
//                            return;
//                        }
//                    }
//                    LogUtil.i(TAG, "莫名错误");
//                    Toast.makeText(mContext, "获取笔记本失败，请重新登录", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onException(Exception exception) {
//                    Toast.makeText(mContext, "获取笔记本失败，请重新登录", Toast.LENGTH_SHORT).show();
//                }
//            });
//        } catch (TTransportException t) {
//            LogUtil.i(TAG, t.getMessage());
//        }
//    }
//
//    private void actionFinish(GNote gNote, int code, Note note) {
//        if (code == GNote.DELETE) {
//            db.deleteGNote(gNote.getId());
//        } else {
//            gNote.setSynStatus(GNote.NOTHING);
//            gNote.setGuid(note.getGuid());
//            gNote.setEditTime(note.getUpdated());
//            gNote.setCreatedTime(note.getCreated());
//            gNote.setBookGuid(note.getNotebookGuid());
//
//            if (!db.updateGNote(gNote)) {
//                LogUtil.e(TAG, "actionFinish() updateNote error.");
//            } else {
//                LogUtil.i(TAG, "actionFinish() updateNote succeed.");
//            }
//        }
//    }
//
//    private String NoteBookGuid() {
//        return mSharedPreferences.getString(EVERNOTE_NOTEBOOK_GUID, "");
//    }
//
//    private void createNote(final GNote gNote) {
//        final Note note = gNote.toNote();
//        if ("".equals(gNote.getBookGuid())) {
//            note.setNotebookGuid(NoteBookGuid());
//        }
//        try {
//            mEvernoteSession.getClientFactory().createNoteStoreClient().createNote(note,
//                    new OnClientCallback<Note>() {
//                        @Override
//                        public void onSuccess(Note data) {
//                            actionFinish(gNote, GNote.NEW, data);
//                            LogUtil.i(TAG, "created note title:" + note.getTitle());
//                        }
//
//                        @Override
//                        public void onException(Exception exception) {
//                            LogUtil.e(TAG, "create note error:" + note.getTitle());
//                        }
//                    });
//        } catch (TTransportException t) {
//            LogUtil.i(TAG, t.getMessage());
//        }
//    }
//
//    private void deleteNote(final GNote gNote) {
//        final Note note = gNote.toNote();
//        try {
//            mEvernoteSession.getClientFactory().createNoteStoreClient().deleteNote(note.getGuid(),
//                    new OnClientCallback<Integer>() {
//                        @Override
//                        public void onSuccess(Integer data) {
//                            actionFinish(gNote, GNote.DELETE, null);
//                            LogUtil.i(TAG, "删除成功！guid:" + note.getGuid());
//                        }
//
//                        @Override
//                        public void onException(Exception exception) {
//                            LogUtil.i(TAG, "删除失败guid:" + note.getGuid());
//                        }
//                    });
//        } catch (TTransportException t) {
//            LogUtil.i(TAG, t.getMessage());
//        }
//    }
//
//    private void updateNote(final GNote gNote) {
//        final Note note = gNote.toNote();
//        try {
//            mEvernoteSession.getClientFactory().createNoteStoreClient().updateNote(note, new OnClientCallback<Note>() {
//                @Override
//                public void onSuccess(Note data) {
//                    LogUtil.i(TAG, "更新成功！guid:" + note.getGuid());
//                    actionFinish(gNote, GNote.UPDATE, data);
//                }
//
//                @Override
//                public void onException(Exception exception) {
//                    LogUtil.i(TAG, "更新失败guid:" + note.getGuid());
//                }
//            });
//        } catch (TTransportException t) {
//            LogUtil.i(TAG, t.getMessage());
//
//        }
//    }
//
//    private void getNote(final String guid) {
//        try {
//            mEvernoteSession.getClientFactory().createNoteStoreClient().getNote(guid, true, false,
//                    false, false, new OnClientCallback<Note>() {
//                        @Override
//                        public void onSuccess(Note data) {
//                            saveNote(data);
//                            LogUtil.i(TAG, "下载Note成功！ guid:" + guid);
//                        }
//
//                        @Override
//                        public void onException(Exception exception) {
//                            LogUtil.i(TAG, "下载Note失败 guid:" + guid);
//                        }
//                    });
//        } catch (TTransportException t) {
//            LogUtil.i(TAG, t.getMessage());
//        }
//    }
//
//    private void saveNote(Note note) {
//        GNote gNote = GNote.parseGNote(note);
//        db.saveGNote(gNote);
//    }
//
//    public void uploadData(List<GNote> list) {
//        if (uploading) {
//            return;
//        }
//        uploading = true;
//        for (GNote gNote : list) {
//            switch (gNote.getSynStatus()) {
//                case GNote.NEW:
//                    createNote(gNote);
//                    break;
//                case GNote.UPDATE:
//                    updateNote(gNote);
//                    break;
//                case GNote.DELETE:
//                    deleteNote(gNote);
//                    break;
//            }
//
//        }
//        uploading = false;
//    }
//
//    private void metaDataList(String guid, NoteFilter noteFilter,
//                              NotesMetadataResultSpec notesMetadataResultSpec,
//                              NoteCollectionCounts data) {
//        Map<String, Integer> maps = data
//                .getNotebookCounts();
//        if (maps == null || maps.size() == 0)
//            return;
//        int maxcount = maps.get(guid);
//        try {
//            mEvernoteSession
//                    .getClientFactory()
//                    .createNoteStoreClient()
//                    .findNotesMetadata(
//                            noteFilter, 0, maxcount, notesMetadataResultSpec, new OnClientCallback<NotesMetadataList>() {
//                                @Override
//                                public void onSuccess(NotesMetadataList data) {
//                                    updateOrDownload(data);
//                                }
//
//                                @Override
//                                public void onException(Exception exception) {
//
//                                }
//                            });
//        } catch (TTransportException t) {
//            LogUtil.i(TAG, t.getMessage());
//
//        }
//    }
//
//    private void updateOrDownload(NotesMetadataList list) {
//        for (int i = 0; i < list.getNotes().size(); i++) {
//            NoteMetadata note = list.getNotes().get(i);
//            final GNote gNote = db.getGNoteByGuid(note.getGuid());
//            if (gNote != null) {
//                if (gNote.getEditTime() != note.getUpdated()) {
//                    try {
//                        mEvernoteSession.getClientFactory().createNoteStoreClient().getNote(note.getGuid(),
//                                true, false,
//                                false, false, new OnClientCallback<Note>() {
//                                    @Override
//                                    public void onSuccess(Note data) {
//                                        gNote.setNote(data.getContent());
//                                        gNote.setEditTime(data.getUpdated());
//                                        // 更新数据
//                                        LogUtil.i(TAG, "从服务器更新Note成功！ guid:" + gNote.getGuid());
//                                        db.updateGNote(gNote);
//                                    }
//
//                                    @Override
//                                    public void onException(Exception exception) {
//                                        LogUtil.e(TAG, "从服务器更新Note失败 guid:" + gNote.getGuid());
//                                    }
//                                });
//                    } catch (TTransportException t) {
//                        LogUtil.i(TAG, t.getMessage());
//                    }
//
//                }
//            } else {
//                // 添加数据
//                getNote(note.getGuid());
//            }
//        }
//    }
//
//    public void downloadData() {
//        if (downloading) {
//            return;
//        }
//        downloading = true;
//        final NoteFilter noteFilter = new NoteFilter();
//        final String guid = NoteBookGuid();
//        noteFilter.setNotebookGuid(guid);
//        final NotesMetadataResultSpec notesMetadataResultSpec = new NotesMetadataResultSpec();
//        notesMetadataResultSpec.setIncludeUpdated(true);
//        try {
//            mEvernoteSession
//                    .getClientFactory()
//                    .createNoteStoreClient()
//                    .findNoteCounts(
//                            noteFilter, false, new OnClientCallback<NoteCollectionCounts>() {
//                                @Override
//                                public void onSuccess(NoteCollectionCounts data) {
//                                    metaDataList(guid, noteFilter, notesMetadataResultSpec, data);
//                                }
//
//                                @Override
//                                public void onException(Exception exception) {
//
//                                }
//                            });
//
//        } catch (TTransportException t) {
//            LogUtil.i(TAG, t.getMessage());
//        } finally {
//            downloading = false;
//        }
//
//    }
//
//    public synchronized void synchronizeData() {
//        if (synchronizing) {
//            return;
//        }
//        synchronizing = true;
//
//        if (!isLogin()) {
//            Toast.makeText(mContext, "请先登录", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        String guid = mSharedPreferences.getString(EVERNOTE_NOTEBOOK_GUID, "");
//        if ("".equals(guid)) {
//            Toast.makeText(mContext, "获取笔记本失败，请重新登录", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (callBack != null) {
//            callBack.start();
//        }
//
//        uploadData(db.loadRawGNotes());
//        downloadData();
//
//        if (callBack != null) {
//            callBack.end();
//        }
//
//        synchronizing = false;
//    }

}
