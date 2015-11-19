package com.duanze.easypreferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by Duanze on 2015/11/19.
 */
public class EasyPreferences {
    private static EasyPreferences sMe;
    private Context mContext;

    private PreferencesUtil mPreferencesUtil;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private boolean valid = false;

    private EasyPreferences(Context context) {
        mContext = context;
    }

    /**
     * Note that:
     * To avoid activity cannot be recycle,
     * pass the **application context** as parameter in the first time you call it.
     *
     * @param context
     * @return
     */
    public static EasyPreferences getInstance(Context context) {
        if (null == sMe) {
            sMe = new EasyPreferences(context);
        }
        return sMe;
    }

    /**
     * This method should be called as soon as possible.
     * You must do it before EasyPreferences can be used.
     *
     * @param res
     */
    public void init(int res) {

        valid = true;
    }
}
