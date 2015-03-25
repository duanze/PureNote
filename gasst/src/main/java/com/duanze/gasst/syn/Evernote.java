package com.duanze.gasst.syn;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.util.LogUtil;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.User;
import com.evernote.thrift.transport.TTransportException;

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
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    public static boolean SyncingUp = false;
    public static boolean SyncingDown = false;

    private EvernoteSession mEvernoteSession;
    private SharedPreferences mSharedPreferences;

    private Context mContext;
    private EvernoteLoginCallback mEvernoteLoginCallback;

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

    public void isNotebookExsist(final String guid, final String name) throws Exception {
        try {
            mEvernoteSession.getClientFactory()
                    .createNoteStoreClient()
                    .getNotebook(guid, new OnClientCallback<Notebook>() {
                        @Override
                        public void onSuccess(Notebook data) {
                            if (data.getName().equals(name)) {
                                LogUtil.e(TAG, guid + "笔记本存在:" + name);
                                mSharedPreferences.edit()
                                        .putString(EVERNOTE_NOTEBOOK_GUID, data.getGuid())
                                        .apply();
                            }
                        }

                        @Override
                        public void onException(Exception exception) {
                            LogUtil.e(TAG, guid + "笔记本不存在");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * create a notebook by bookname
     *
     * @param bookname
     * @return
     * @throws Exception
     */
    public void createNotebook(final String bookname) throws Exception {
        Notebook notebook = new Notebook();
        notebook.setDefaultNotebook(false);
        notebook.setName(bookname);
        try {
            mEvernoteSession.getClientFactory()
                    .createNoteStoreClient()
                    .createNotebook(notebook, new OnClientCallback<Notebook>() {
                        @Override
                        public void onSuccess(Notebook data) {
                            mSharedPreferences
                                    .edit()
                                    .putString(EVERNOTE_NOTEBOOK_GUID, data.getGuid())
                                    .apply();
                            LogUtil.i(TAG, "Notebook:" + bookname + "不存在，创建成功");
                            LogUtil.i(TAG, "guid:" + data.getGuid());
                        }

                        @Override
                        public void onException(Exception exception) {
                            LogUtil.i(TAG, "创建notebook失败");
                        }
                    });

        } catch (Exception e) {
            LogUtil.i(TAG, "传输出现错误");
            throw e;
        }
    }

}
