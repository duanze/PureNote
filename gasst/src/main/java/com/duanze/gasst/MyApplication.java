package com.duanze.gasst;

import android.app.Application;
import android.content.Context;

import com.duanze.gasst.ui.activity.StartActivity.SyncHandler;

public class MyApplication extends Application {
    private static Context mContext;
    private SyncHandler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }

    public SyncHandler getHandler() {
        return mHandler;
    }

    public void setHandler(SyncHandler handler) {
        mHandler = handler;
    }
}
