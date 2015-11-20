package com.duanze.easypreferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by Duanze on 15-11-19.
 */
public class ActualUtil {
    public static final int MODE_COMMIT = 111;
    public static final int MODE_APPLY = 112;
    public int mode = MODE_APPLY;

    private String name;
    private SharedPreferences mSharedPreferences;
    private Map<String, Preference> mMap;

    public ActualUtil(String name, Map<String, Preference> map) {
        this.name = name;
        this.mMap = map;
    }

    public void init(Context context) {
        mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    private void checkExist(Preference preference) {
        if (null == preference) {
            throw new NullPointerException("operate a preference that isn't contained in config xml");
        }
    }

    private Preference readyOperation(String key) {
        Preference preference = mMap.get(key);
        checkExist(preference);
        return preference;
    }

    public int getInt(String key) {
        Preference preference = readyOperation(key);
        if (preference.queried) {
            return preference.getCurInt();
        } else {
            preference.queried = true;
            return preference.curInt = mSharedPreferences.getInt(key, preference.getDefInt());
        }
    }

    public long getLong(String key) {
        Preference preference = readyOperation(key);
        if (preference.queried) {
            return preference.getCurLong();
        } else {
            preference.queried = true;
            return preference.curLong = mSharedPreferences.getLong(key, preference.getDefLong());
        }
    }

    public float getFloat(String key) {
        Preference preference = readyOperation(key);
        if (preference.queried) {
            return preference.getCurFloat();
        } else {
            preference.queried = true;
            return preference.curFloat = mSharedPreferences.getFloat(key, preference.getDefFloat());
        }
    }

    public boolean getBoolean(String key) {
        Preference preference = readyOperation(key);
        if (preference.queried) {
            return preference.getCurBoolean();
        } else {
            preference.queried = true;
            return preference.curBoolean = mSharedPreferences.getBoolean(key, preference.getDefBoolean());
        }
    }

    public String getString(String key) {
        Preference preference = readyOperation(key);
        if (preference.queried) {
            return preference.getCurString();
        } else {
            preference.queried = true;
            return preference.curString = mSharedPreferences.getString(key, preference.getDefString());
        }
    }

    public boolean setInt(String key, int value) {
        return setInt(key, value, mode);
    }

    public boolean setInt(String key, int value, int mode) {
        Preference preference = readyOperation(key);
        preference.queried = true;

        preference.curInt = value;
        if (MODE_APPLY == mode) {
            mSharedPreferences.edit().putInt(key, value).apply();
            return false;
        } else {
            return mSharedPreferences.edit().putInt(key, value).commit();
        }
    }

    public boolean setLong(String key, long value) {
        return setLong(key, value, mode);
    }

    public boolean setLong(String key, long value, int mode) {
        Preference preference = readyOperation(key);
        preference.queried = true;

        preference.curLong = value;
        if (MODE_APPLY == mode) {
            mSharedPreferences.edit().putLong(key, value).apply();
            return false;
        } else {
            return mSharedPreferences.edit().putLong(key, value).commit();
        }
    }

    public boolean setFloat(String key, float value) {
        return setFloat(key, value, mode);
    }

    public boolean setFloat(String key, float value, int mode) {
        Preference preference = readyOperation(key);
        preference.queried = true;

        preference.curFloat = value;
        if (MODE_APPLY == mode) {
            mSharedPreferences.edit().putFloat(key, value).apply();
            return false;
        } else {
            return mSharedPreferences.edit().putFloat(key, value).commit();
        }
    }

    public boolean setBoolean(String key, boolean value) {
        return setBoolean(key, value, mode);
    }

    public boolean setBoolean(String key, boolean value, int mode) {
        Preference preference = readyOperation(key);
        preference.queried = true;

        preference.curBoolean = value;
        if (MODE_APPLY == mode) {
            mSharedPreferences.edit().putBoolean(key, value).apply();
            return false;
        } else {
            return mSharedPreferences.edit().putBoolean(key, value).commit();
        }
    }

    public boolean setString(String key, String value) {
        return setString(key, value, mode);
    }

    public boolean setString(String key, String value, int mode) {
        Preference preference = readyOperation(key);
        preference.queried = true;

        preference.curString = value;
        if (MODE_APPLY == mode) {
            mSharedPreferences.edit().putString(key, value).apply();
            return false;
        } else {
            return mSharedPreferences.edit().putString(key, value).commit();
        }
    }


}
