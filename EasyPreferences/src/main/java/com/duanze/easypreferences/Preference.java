package com.duanze.easypreferences;

/**
 * Created by Duanze on 2015/11/20.
 */
public class Preference {

    public String key;
    public String defValue;

    public boolean queried = false;
    public int curInt;
    public long curLong;
    public float curFloat;
    public boolean curBoolean;
    public String curString;

    public int getDefInt() {
        return Integer.parseInt(defValue);
    }

    public long getDefLong() {
        return Long.parseLong(defValue);
    }

    public float getDefFloat() {
        return Float.parseFloat(defValue);
    }

    public boolean getDefBoolean() {
        return Boolean.parseBoolean(defValue);
    }

    public String getDefString() {
        return defValue;
    }

    public int getCurInt() {
        return curInt;
    }

    public long getCurLong() {
        return curLong;
    }

    public float getCurFloat() {
        return curFloat;
    }

    public boolean getCurBoolean() {
        return curBoolean;
    }

    public String getCurString() {
        return curString;
    }
}
