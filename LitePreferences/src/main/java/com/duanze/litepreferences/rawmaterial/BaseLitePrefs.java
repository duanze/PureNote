/*
 * Copyright 2015 Duanze
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.duanze.litepreferences.rawmaterial;

import android.content.Context;

import com.duanze.litepreferences.ActualUtil;
import com.duanze.litepreferences.LiteInterface;
import com.duanze.litepreferences.model.Pref;
import com.duanze.litepreferences.parser.ParsePrefsXml;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Duanze on 2015/11/20.
 */
public class BaseLitePrefs implements LiteInterface {
    protected static BaseLitePrefs sMe;

    protected Context mContext;
    protected ActualUtil mUtil;
    protected boolean valid = false;

    protected BaseLitePrefs() {

    }

    @Override
    public LiteInterface getImpl() {
        return this;
    }

    public static LiteInterface getLiteInterface() {
        if (null == sMe) {
            sMe = new BaseLitePrefs();
        }
        return sMe.getImpl();
    }

    /**
     * Must call this method before use LitePrefs.
     * Pass the **Application Context** or **Main Activity**
     * as parameter to avoid your activity
     * cannot be recycled by GC.
     *
     * @param context should pass a **Application Context** or **Main Activity**
     * @param res     the xml file resource id
     * @see #initFromMap(Context, String, Map)
     */
    public static void initFromXml(Context context, int res) throws IOException, XmlPullParserException {
        getLiteInterface().initFromXmlLite(context, res);
    }

    /**
     * Must call this method before use LitePrefs.
     * Pass the **Application Context** or **Main Activity**
     * as parameter to avoid your activity
     * cannot be recycled by GC.
     *
     * @param context should pass a **Application Context** or **Main Activity**
     * @param res     the xml file resource id
     * @see #initFromMapLite(Context, String, Map)
     */
    public void initFromXmlLite(Context context, int res) throws IOException, XmlPullParserException {
        mContext = context;
        mUtil = ParsePrefsXml.parse(context.getResources().getXml(res));
        mUtil.init(context);

        valid = true;
    }

    /**
     * Must call this method before use LitePrefs.
     * Pass the **Application Context** or **Main Activity**
     * as parameter to avoid your activity
     * cannot be recycled by GC.
     *
     * @param context should pass a **Application Context** or **Main Activity**
     * @param name    the name of the SharedPreferences you use
     * @param map     the core data map
     * @see #initFromXml(Context, int)
     */
    public static void initFromMap(Context context, String name, Map<String, Pref> map) {
        getLiteInterface().initFromMapLite(context, name, map);
    }

    /**
     * Must call this method before use LitePrefs.
     * Pass the **Application Context** or **Main Activity**
     * as parameter to avoid your activity
     * cannot be recycled by GC.
     *
     * @param context should pass a **Application Context** or **Main Activity**
     * @param name    the name of the SharedPreferences you use
     * @param map     the core data map
     * @see #initFromXmlLite(Context, int)
     */
    public void initFromMapLite(Context context, String name, Map<String, Pref> map) {
        mContext = context;
        mUtil = new ActualUtil(name, map);
        mUtil.init(context);

        valid = true;
    }

    /**
     * Return the core data map,
     * the **key** of the map is just **Pref.key**
     *
     * @return the core data map
     * @see Pref
     */
    public static Map<String, Pref> getPrefsMap() {
        return getLiteInterface().getPrefsMapLite();
    }

    /**
     * Return the core data map,
     * the **key** of the map is just **Pref.key**
     *
     * @return the core data map
     * @see Pref
     */
    public Map<String, Pref> getPrefsMapLite() {
        checkValid();
        return mUtil.getPrefsMap();
    }

    /**
     * Add one key-value pair to the core data map.
     * <p/>
     * Be sure call it after LitePrefs is initialized.
     *
     * @param key
     * @param pref
     */
    public static void putToMap(String key, Pref pref) {
        getLiteInterface().putToMapLite(key, pref);
    }

    /**
     * Add one key-value pair to the core data map.
     * <p/>
     * Be sure call it after LitePrefs is initialized.
     *
     * @param key
     * @param pref
     */
    public void putToMapLite(String key, Pref pref) {
        checkValid();
        mUtil.putToMap(key, pref);
    }

    private void checkValid() {
        if (!valid) {
            throw new IllegalStateException("this should only be called when LitePrefs didn't initialize once");
        }
    }

    public static int getInt(int keyRes) {
        return getLiteInterface().getIntLite(keyRes);
    }

    public static int getInt(String key) {
        return getLiteInterface().getIntLite(key);
    }

    public int getIntLite(String key) {
        checkValid();
        return mUtil.getInt(key);
    }

    public int getIntLite(int keyRes) {
        checkValid();
        return mUtil.getInt(mContext.getString(keyRes));
    }

    public static long getLong(int keyRes) {
        return getLiteInterface().getLongLite(keyRes);
    }

    public static long getLong(String key) {
        return getLiteInterface().getLongLite(key);
    }

    public long getLongLite(String key) {
        checkValid();
        return mUtil.getLong(key);
    }

    public long getLongLite(int keyRes) {
        checkValid();
        return mUtil.getLong(mContext.getString(keyRes));
    }

    public static float getFloat(int keyRes) {
        return getLiteInterface().getFloatLite(keyRes);
    }

    public static float getFloat(String key) {
        return getLiteInterface().getFloatLite(key);
    }

    public float getFloatLite(String key) {
        checkValid();
        return mUtil.getFloat(key);
    }

    public float getFloatLite(int keyRes) {
        checkValid();
        return mUtil.getFloat(mContext.getString(keyRes));
    }

    public static boolean getBoolean(int keyRes) {
        return getLiteInterface().getBooleanLite(keyRes);
    }

    public static boolean getBoolean(String key) {
        return getLiteInterface().getBooleanLite(key);
    }

    public boolean getBooleanLite(String key) {
        checkValid();
        return mUtil.getBoolean(key);
    }

    public boolean getBooleanLite(int keyRes) {
        checkValid();
        return mUtil.getBoolean(mContext.getString(keyRes));
    }

    public static String getString(int keyRes) {
        return getLiteInterface().getStringLite(keyRes);
    }

    public static String getString(String key) {
        return getLiteInterface().getStringLite(key);
    }

    public String getStringLite(String key) {
        checkValid();
        return mUtil.getString(key);
    }

    public String getStringLite(int keyRes) {
        checkValid();
        return mUtil.getString(mContext.getString(keyRes));
    }

    public static boolean putInt(int keyRes, int value) {
        return getLiteInterface().putIntLite(keyRes, value);
    }

    public static boolean putInt(String key, int value) {
        return getLiteInterface().putIntLite(key, value);
    }

    public boolean putIntLite(String key, int value) {
        checkValid();
        return mUtil.putInt(key, value);
    }

    public boolean putIntLite(int keyRes, int value) {
        checkValid();
        return mUtil.putInt(mContext.getString(keyRes), value);
    }

    public static boolean putLong(int keyRes, long value) {
        return getLiteInterface().putLongLite(keyRes, value);
    }

    public static boolean putLong(String key, long value) {
        return getLiteInterface().putLongLite(key, value);
    }

    public boolean putLongLite(String key, long value) {
        checkValid();
        return mUtil.putLong(key, value);
    }

    public boolean putLongLite(int keyRes, long value) {
        checkValid();
        return mUtil.putLong(mContext.getString(keyRes), value);
    }

    public static boolean putFloat(int keyRes, float value) {
        return getLiteInterface().putFloatLite(keyRes, value);
    }

    public static boolean putFloat(String key, float value) {
        return getLiteInterface().putFloatLite(key, value);
    }

    public boolean putFloatLite(String key, float value) {
        checkValid();
        return mUtil.putFloat(key, value);
    }

    public boolean putFloatLite(int keyRes, float value) {
        checkValid();
        return mUtil.putFloat(mContext.getString(keyRes), value);
    }

    public static boolean putBoolean(int keyRes, boolean value) {
        return getLiteInterface().putBooleanLite(keyRes, value);
    }

    public static boolean putBoolean(String key, boolean value) {
        return getLiteInterface().putBooleanLite(key, value);
    }

    public boolean putBooleanLite(String key, boolean value) {
        checkValid();
        return mUtil.putBoolean(key, value);
    }

    public boolean putBooleanLite(int keyRes, boolean value) {
        checkValid();
        return mUtil.putBoolean(mContext.getString(keyRes), value);
    }

    public static boolean putString(int keyRes, String value) {
        return getLiteInterface().putStringLite(keyRes, value);
    }

    public static boolean putString(String key, String value) {
        return getLiteInterface().putStringLite(key, value);
    }

    public boolean putStringLite(String key, String value) {
        checkValid();
        return mUtil.putString(key, value);
    }

    public boolean putStringLite(int keyRes, String value) {
        checkValid();
        return mUtil.putString(mContext.getString(keyRes), value);
    }

    public static boolean remove(int keyRes) {
        return getLiteInterface().removeLite(keyRes);
    }

    public static boolean remove(String key) {
        return getLiteInterface().removeLite(key);
    }

    public boolean removeLite(String key) {
        checkValid();
        return mUtil.remove(key);
    }

    public boolean removeLite(int keyRes) {
        checkValid();
        return mUtil.remove(mContext.getString(keyRes));
    }

    public static boolean clear() {
        return getLiteInterface().clearLite();
    }

    public boolean clearLite() {
        checkValid();
        return mUtil.clear();
    }
}
