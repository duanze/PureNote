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
package com.duanze.litepreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.duanze.litepreferences.model.Pref;

import java.util.Map;
import java.util.Set;

/**
 * Created by Duanze on 15-11-19.
 */
public class ActualUtil {

    private String name;
    private SharedPreferences mSharedPreferences;
    private Map<String, Pref> mMap;

    public ActualUtil(String name, Map<String, Pref> map) {
        this.name = name;
        this.mMap = map;
    }

    public void init(Context context) {
        mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void putToMap(String key, Pref pref) {
        mMap.put(key, pref);
    }

    private void checkExist(Pref pref) {
        if (null == pref) {
            throw new NullPointerException("operate a pref that isn't contained in data set,maybe there are some wrong in initialization of LitePrefs");
        }
    }

    private Pref readyOperation(String key) {
        Pref pref = mMap.get(key);
        checkExist(pref);
        return pref;
    }

    public int getInt(String key) {
        Pref pref = readyOperation(key);
        if (pref.queried) {
            return pref.getCurInt();
        } else {
            pref.queried = true;
            int ans = mSharedPreferences.getInt(key, pref.getDefInt());
            pref.setValue(ans);
            return ans;
        }
    }

    public long getLong(String key) {
        Pref pref = readyOperation(key);
        if (pref.queried) {
            return pref.getCurLong();
        } else {
            pref.queried = true;
            long ans = mSharedPreferences.getLong(key, pref.getDefLong());
            pref.setValue(ans);
            return ans;
        }
    }

    public float getFloat(String key) {
        Pref pref = readyOperation(key);
        if (pref.queried) {
            return pref.getCurFloat();
        } else {
            pref.queried = true;
            float ans = mSharedPreferences.getFloat(key, pref.getDefFloat());
            pref.setValue(ans);
            return ans;
        }
    }

    public boolean getBoolean(String key) {
        Pref pref = readyOperation(key);
        if (pref.queried) {
            return pref.getCurBoolean();
        } else {
            pref.queried = true;
            boolean ans = mSharedPreferences.getBoolean(key, pref.getDefBoolean());
            pref.setValue(ans);
            return ans;
        }
    }

    public String getString(String key) {
        Pref pref = readyOperation(key);
        if (pref.queried) {
            return pref.getCurString();
        } else {
            pref.queried = true;
            String ans = mSharedPreferences.getString(key, pref.getDefString());
            pref.setValue(ans);
            return ans;
        }
    }


    public boolean putInt(String key, int value) {
        Pref pref = readyOperation(key);
        pref.queried = true;
        pref.setValue(value);
        return mSharedPreferences.edit().putInt(key, value).commit();
    }


    public boolean putLong(String key, long value) {
        Pref pref = readyOperation(key);
        pref.queried = true;
        pref.setValue(value);
        return mSharedPreferences.edit().putLong(key, value).commit();
    }


    public boolean putFloat(String key, float value) {
        Pref pref = readyOperation(key);
        pref.queried = true;
        pref.setValue(value);

        return mSharedPreferences.edit().putFloat(key, value).commit();
    }

    public boolean putBoolean(String key, boolean value) {
        Pref pref = readyOperation(key);
        pref.queried = true;
        pref.setValue(value);

        return mSharedPreferences.edit().putBoolean(key, value).commit();
    }

    public boolean putString(String key, String value) {
        Pref pref = readyOperation(key);
        pref.queried = true;
        pref.setValue(value);

        return mSharedPreferences.edit().putString(key, value).commit();
    }

    public boolean remove(String key) {
        Pref pref = readyOperation(key);
        pref.queried = false;

        return mSharedPreferences.edit().remove(key).commit();
    }

    public boolean clear() {
        Set<String> keySet = mMap.keySet();
        for (String key : keySet) {
            Pref pref = mMap.get(key);
            pref.queried = false;
        }

        return mSharedPreferences.edit().clear().commit();
    }

    public Map<String, Pref> getPrefsMap() {
        return mMap;
    }
}
