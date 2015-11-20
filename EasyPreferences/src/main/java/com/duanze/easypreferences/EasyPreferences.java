package com.duanze.easypreferences;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Duanze on 2015/11/20.
 */
public class EasyPreferences {
    public static final int MODE_COMMIT = ActualUtil.MODE_COMMIT;
    public static final int MODE_APPLY = ActualUtil.MODE_APPLY;

    private Context mContext;
    private static EasyPreferences sMe;
    private ActualUtil mUtil;
    private boolean valid = false;

    private EasyPreferences() {

    }

    public static EasyPreferences getInstance() {
        if (null == sMe) {
            sMe = new EasyPreferences();
        }
        return sMe;
    }

    /**
     * Must call this method before use EasyPreferences.
     * Pass the **Application Context** to avoid your activity
     * cannot be recycled by GC.
     *
     * @param context
     * @param res
     */
    public void init(Context context, int res) throws IOException, XmlPullParserException {
        mContext = context;
        mUtil = ParsePreferencesXml.parse(context.getResources().getXml(res));
        mUtil.init(context);

        valid = true;
    }

    public void setMode(int mode) {
        mUtil.mode = mode;
    }

    private void checkValid() {
        if (!valid) {
            throw new IllegalStateException("this should only be called when the EasyPreferences you used didn't call init() once");
        }
    }

    public void getInt(String key) {
        checkValid();
        mUtil.getInt(key);
    }

    public void getLong(String key) {
        checkValid();
        mUtil.getLong(key);
    }

    public void getFloat(String key) {
        checkValid();
        mUtil.getFloat(key);
    }

    public boolean getBoolean(int keyRes) {
        return getBoolean(mContext.getString(keyRes));
    }

    public boolean getBoolean(String key) {
        checkValid();
        return mUtil.getBoolean(key);
    }

    public void getString(String key) {
        checkValid();
        mUtil.getString(key);
    }

    public boolean setInt(String key, int value) {
        return setInt(key, value, mUtil.mode);
    }

    public boolean setInt(String key, int value, int mode) {
        return mUtil.setInt(key, value, mode);
    }

    public boolean setLong(String key, long value) {
        return setLong(key, value, mUtil.mode);
    }

    public boolean setLong(String key, long value, int mode) {
        return mUtil.setLong(key, value, mode);
    }

    public boolean setFloat(String key, float value) {
        return setFloat(key, value, mUtil.mode);
    }

    public boolean setFloat(String key, float value, int mode) {
        return mUtil.setFloat(key, value, mode);
    }

    public boolean setBoolean(int keyRes, boolean value) {
        return setBoolean(mContext.getString(keyRes), value);
    }

    public boolean setBoolean(String key, boolean value) {
        return setBoolean(key, value, mUtil.mode);
    }

    public boolean setBoolean(String key, boolean value, int mode) {
        return mUtil.setBoolean(key, value, mode);
    }

    public boolean setString(String key, String value) {
        return setString(key, value, mUtil.mode);
    }

    public boolean setString(String key, String value, int mode) {
        return mUtil.setString(key, value, mode);
    }
}
