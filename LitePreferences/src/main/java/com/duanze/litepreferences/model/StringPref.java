package com.duanze.litepreferences.model;

import android.content.Context;

/**
 * Created by Duanze on 15-11-21.
 */
public class StringPref extends Pref {
    private int defRes;
    private Context mContext;

    /**
     * Special Pref to support that Preference whose default String value
     * want to be expressed by a resId.
     * <p/>
     * Pass the **Application Context**
     * as parameter to avoid your activity
     * cannot be recycled by GC.
     *
     * @param key
     * @param defRes
     * @param context
     */
    public StringPref(String key, int defRes, Context context) {
        this.key = key;
        this.defRes = defRes;
        mContext = context;
    }

    @Override
    public String getDefString() {
        return mContext.getString(defRes);
    }
}
