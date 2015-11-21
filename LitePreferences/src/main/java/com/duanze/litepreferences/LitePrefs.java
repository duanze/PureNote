/**
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
package com.duanze.litepreferences;

import android.content.Context;

import com.duanze.litepreferences.model.Pref;
import com.duanze.litepreferences.parser.ParsePrefsXml;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Duanze on 2015/11/20.
 */
public class LitePrefs {
    private Context mContext;
    private static LitePrefs sMe;
    private ActualUtil mUtil;
    private boolean valid = false;

    private LitePrefs() {

    }

    public static LitePrefs getInstance() {
        if (null == sMe) {
            sMe = new LitePrefs();
        }
        return sMe;
    }

    /**
     * Must call this method before use LitePrefs.
     * Pass the **Application Context**
     * as parameter to avoid your activity
     * cannot be recycled by GC.
     *
     * @param context
     * @param res
     */
    public void initFromXml(Context context, int res) throws IOException, XmlPullParserException {
        mContext = context;
        mUtil = ParsePrefsXml.parse(context.getResources().getXml(res));
        mUtil.init(context);

        valid = true;
    }

    /**
     * Must call this method before use LitePrefs.
     * Pass the **Application Context**
     * as parameter to avoid your activity
     * cannot be recycled by GC.
     *
     * @param context
     * @param name
     * @param map
     */
    public void initFromMap(Context context, String name, Map<String, Pref> map) {
        mContext = context;
        mUtil = new ActualUtil(name, map);
        mUtil.init(context);

        valid = true;
    }

    private void checkValid() {
        if (!valid) {
            throw new IllegalStateException("this should only be called when LitePrefs didn't initialize once");
        }
    }

    public int getInt(int keyRes) {
        return getInt(mContext.getString(keyRes));
    }

    public int getInt(String key) {
        checkValid();
        return mUtil.getInt(key);
    }

    public long getLong(int keyRes) {
        return getLong(mContext.getString(keyRes));
    }

    public long getLong(String key) {
        checkValid();
        return mUtil.getLong(key);
    }

    public float getFloat(int keyRes) {
        return getFloat(mContext.getString(keyRes));
    }

    public float getFloat(String key) {
        checkValid();
        return mUtil.getFloat(key);
    }

    public boolean getBoolean(int keyRes) {
        return getBoolean(mContext.getString(keyRes));
    }

    public boolean getBoolean(String key) {
        checkValid();
        return mUtil.getBoolean(key);
    }

    public String getString(int keyRes) {
        return getString(mContext.getString(keyRes));
    }

    public String getString(String key) {
        checkValid();
        return mUtil.getString(key);
    }

    public boolean putInt(int keyRes, int value) {
        return putInt(mContext.getString(keyRes), value);
    }

    public boolean putInt(String key, int value) {
        checkValid();
        return mUtil.putInt(key, value);
    }

    public boolean putLong(int keyRes, long value) {
        return putLong(mContext.getString(keyRes), value);
    }

    public boolean putLong(String key, long value) {
        checkValid();
        return mUtil.putLong(key, value);
    }

    public boolean putFloat(int keyRes, float value) {
        return putFloat(mContext.getString(keyRes), value);
    }

    public boolean putFloat(String key, float value) {
        checkValid();
        return mUtil.putFloat(key, value);
    }

    public boolean putBoolean(int keyRes, boolean value) {
        return putBoolean(mContext.getString(keyRes), value);
    }

    public boolean putBoolean(String key, boolean value) {
        checkValid();
        return mUtil.putBoolean(key, value);
    }

    public boolean putString(int keyRes, String value) {
        return putString(mContext.getString(keyRes), value);
    }

    public boolean putString(String key, String value) {
        checkValid();
        return mUtil.putString(key, value);
    }

    public boolean remove(int keyRes) {
        return remove(mContext.getString(keyRes));
    }

    public boolean remove(String key) {
        checkValid();
        return mUtil.remove(key);
    }

    public boolean clear() {
        checkValid();
        return mUtil.clear();
    }
}
