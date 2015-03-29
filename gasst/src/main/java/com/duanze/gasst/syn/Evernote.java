package com.duanze.gasst.syn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.util.LogUtil;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.User;
import com.evernote.thrift.transport.TTransportException;

import java.util.List;
import java.util.Map;

/**
 * Created by Duanze on 2015/3/22.
 */
public class Evernote {
    public static final String TAG = "EverNote";

    private static final String CONSUMER_KEY = "duanze";
    private static final String CONSUMER_SECRET = "1cfaabc066c64097";
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
                CONSUMER_SECRET, EVERNOTE_SERVICE, true);
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

    /**
     * create a notebook by bookname
     *
     * @param bookname
     */
    public void createNotebook(final String bookname) {
        Notebook notebook = new Notebook();
        notebook.setDefaultNotebook(false);
        notebook.setName(bookname);
        try {
            mEvernoteSession.getClientFactory()
                    .createNoteStoreClient()
                    .createNotebook(notebook, new OnClientCallback<Notebook>() {
                        @Override
                        public void onSuccess(Notebook data) {
                            saveNotebookGuid(data.getGuid());

                            LogUtil.i(TAG, "Notebook:" + bookname + "不存在，创建成功");
                        }

                        @Override
                        public void onException(Exception exception) {
                            LogUtil.i(TAG, "创建notebook失败，开始在已有列表中搜寻");
                            searchNoteBookInList();
                        }
                    });

        } catch (TTransportException e) {
            Toast.makeText(mContext, "创建笔记本失败", Toast.LENGTH_SHORT).show();
            LogUtil.i(TAG, "传输出现错误");
            e.printStackTrace();
        }
    }

    private void saveNotebookGuid(String guid) {
        mSharedPreferences
                .edit()
                .putString(EVERNOTE_NOTEBOOK_GUID, guid)
                .apply();

        LogUtil.i(TAG, "Save guid:" + guid);
    }

    private void searchNoteBookInList() {
        try {
            mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>() {
                @Override
                public void onSuccess(List<Notebook> data) {
                    for (Notebook notebook : data) {
                        if (NOTEBOOK_NAME.equals(notebook.getName())) {
                            LogUtil.i(TAG, "搜寻成功");
                            saveNotebookGuid(notebook.getGuid());
                            return;
                        }
                    }
                    LogUtil.i(TAG, "莫名错误");
                    Toast.makeText(mContext, "获取笔记本失败，请重新登录", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onException(Exception exception) {
                    Toast.makeText(mContext, "获取笔记本失败，请重新登录", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (TTransportException t) {
            LogUtil.i(TAG, t.getMessage());
        }
    }

    private void actionFinish(GNote gNote, int code) {
        gNote.setSynStatus(GNote.NOTHING);
        if (code == GNote.DELETE) {
            db.deleteGNote(gNote.getId());
        } else {
            db.updateGNote(gNote);
        }
    }

    private void createNote(final GNote gNote) {
        final Note note = gNote.toNote();
        try {
            mEvernoteSession.getClientFactory().createNoteStoreClient().createNote(note,
                    new OnClientCallback<Note>() {
                        @Override
                        public void onSuccess(Note data) {
                            actionFinish(gNote, GNote.NEW);
                            LogUtil.i(TAG, "created note title:" + note.getTitle());
                        }

                        @Override
                        public void onException(Exception exception) {
                            LogUtil.e(TAG, "create note error:" + note.getTitle());
                        }
                    });
        } catch (TTransportException t) {
            LogUtil.i(TAG, t.getMessage());
        }
    }

    private void deleteNote(final GNote gNote) {
        final Note note = gNote.toNote();
        try {
            mEvernoteSession.getClientFactory().createNoteStoreClient().deleteNote(note.getGuid(),
                    new OnClientCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer data) {
                            actionFinish(gNote, GNote.DELETE);
                        }

                        @Override
                        public void onException(Exception exception) {
                            LogUtil.i(TAG, "删除失败guid:" + note.getGuid());
                        }
                    });
        } catch (TTransportException t) {
            LogUtil.i(TAG, t.getMessage());
        }
    }

    private void updateNote(final GNote gNote) {
        final Note note = gNote.toNote();
        try {
            mEvernoteSession.getClientFactory().createNoteStoreClient().updateNote(note, new OnClientCallback<Note>() {
                @Override
                public void onSuccess(Note data) {
                    actionFinish(gNote, GNote.UPDATE);
                }

                @Override
                public void onException(Exception exception) {
                    LogUtil.i(TAG, "更新失败guid:" + note.getGuid());
                }
            });
        } catch (TTransportException t) {
            LogUtil.i(TAG, t.getMessage());

        }
    }

    private void getNote(final String guid) {
        try {
            mEvernoteSession.getClientFactory().createNoteStoreClient().getNote(guid, true, false,
                    false, false, new OnClientCallback<Note>() {
                        @Override
                        public void onSuccess(Note data) {
                            saveNote(data);
                        }

                        @Override
                        public void onException(Exception exception) {
                            LogUtil.i(TAG, "下载Note失败 guid:" + guid);
                        }
                    });
        } catch (TTransportException t) {
            LogUtil.i(TAG, t.getMessage());
        }
    }

    private void saveNote(Note note) {
        GNote gNote = GNote.parseGNote(note);
        db.saveGNote(gNote);
    }

    public void uploadData(List<GNote> list) {
        if (uploading) {
            return;
        }
        uploading = true;
        for (GNote gNote : list) {
            switch (gNote.getSynStatus()) {
                case GNote.NEW:
                    createNote(gNote);
                    break;
                case GNote.UPDATE:
                    updateNote(gNote);
                    break;
                case GNote.DELETE:
                    deleteNote(gNote);
                    break;
            }

        }
        uploading = false;
    }

    private void metaDataList(String guid, NoteFilter noteFilter,
                              NotesMetadataResultSpec notesMetadataResultSpec,
                              NoteCollectionCounts data) {
        Map<String, Integer> maps = data
                .getNotebookCounts();
        if (maps == null || maps.size() == 0)
            return;
        int maxcount = maps.get(guid);
        try {
            mEvernoteSession
                    .getClientFactory()
                    .createNoteStoreClient()
                    .findNotesMetadata(
                            noteFilter, 0, maxcount, notesMetadataResultSpec, new OnClientCallback<NotesMetadataList>() {
                                @Override
                                public void onSuccess(NotesMetadataList data) {
                                    updateOrDownload(data);
                                }

                                @Override
                                public void onException(Exception exception) {

                                }
                            });
        } catch (TTransportException t) {
            LogUtil.i(TAG, t.getMessage());

        }
    }

    private void updateOrDownload(NotesMetadataList list) {
        for (int i = 0; i < list.getNotes().size(); i++) {
            NoteMetadata note = list.getNotes().get(i);
            final GNote gNote = db.getGNoteByGuid(note.getGuid());
            if (gNote != null) {
                if (gNote.getEditTime() != note.getUpdated()) {
                    try {
                        mEvernoteSession.getClientFactory().createNoteStoreClient().getNote(note.getGuid(),
                                true, false,
                                false, false, new OnClientCallback<Note>() {
                                    @Override
                                    public void onSuccess(Note data) {
                                        gNote.setNote(data.getContent());
                                        gNote.setEditTime(data.getUpdated());
                                        // 更新数据
                                        db.updateGNote(gNote);
                                    }

                                    @Override
                                    public void onException(Exception exception) {
                                        LogUtil.i(TAG, "更新Note失败 guid:" + gNote.getGuid());
                                    }
                                });
                    } catch (TTransportException t) {
                        LogUtil.i(TAG, t.getMessage());
                    }

                }
            } else {
                // 添加数据
                getNote(note.getGuid());
            }
        }
    }

    public void downloadData() {
        if (downloading) {
            return;
        }
        downloading = true;
        final NoteFilter noteFilter = new NoteFilter();
        final String guid = mSharedPreferences.getString(EVERNOTE_NOTEBOOK_GUID, "");
        noteFilter.setNotebookGuid(guid);
        final NotesMetadataResultSpec notesMetadataResultSpec = new NotesMetadataResultSpec();
        notesMetadataResultSpec.setIncludeUpdated(true);
        try {
            mEvernoteSession
                    .getClientFactory()
                    .createNoteStoreClient()
                    .findNoteCounts(
                            noteFilter, false, new OnClientCallback<NoteCollectionCounts>() {
                                @Override
                                public void onSuccess(NoteCollectionCounts data) {
                                    metaDataList(guid, noteFilter, notesMetadataResultSpec, data);
                                }

                                @Override
                                public void onException(Exception exception) {

                                }
                            });

        } catch (TTransportException t) {
            LogUtil.i(TAG, t.getMessage());
        } finally {
            downloading = false;
        }

    }

    public void synchronizeData() {
        if (!isLogin()) {
            Toast.makeText(mContext, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        String guid = mSharedPreferences.getString(EVERNOTE_NOTEBOOK_GUID, "");
        if ("".equals(guid)) {
            Toast.makeText(mContext, "获取笔记本失败，请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadData(db.loadGNotes());
        downloadData();
    }
}
