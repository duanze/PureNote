/*
 * Copyright 2015 Duanze
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.duanze.litepreferences.model;

/**
 * Created by Duanze on 2015/11/20.
 * <p>
 * Model to express a Preference.
 * It's easy,just go to see the source code.
 */
public class Pref {

    public String key;

    /**
     * use String store the default value
     */
    public String defValue;

    /**
     * use String store the current value
     */
    public String curValue;

    /**
     * flag to show the pref has queried its data from SharedPreferences or not
     */
    public boolean queried = false;

    public Pref() {
    }

    public Pref(String key, String defValue) {
        this.key = key;
        this.defValue = defValue;
    }

    public Pref(String key, int defValue) {
        this.key = key;
        this.defValue = String.valueOf(defValue);
    }

    public Pref(String key, long defValue) {
        this.key = key;
        this.defValue = String.valueOf(defValue);
    }

    public Pref(String key, float defValue) {
        this.key = key;
        this.defValue = String.valueOf(defValue);
    }

    public Pref(String key, boolean defValue) {
        this.key = key;
        this.defValue = String.valueOf(defValue);
    }

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
        return Integer.parseInt(curValue);
    }

    public long getCurLong() {
        return Long.parseLong(curValue);
    }

    public float getCurFloat() {
        return Float.parseFloat(curValue);
    }

    public boolean getCurBoolean() {
        return Boolean.parseBoolean(curValue);
    }

    public String getCurString() {
        return curValue;
    }

    public void setValue(int value) {
        curValue = String.valueOf(value);
    }

    public void setValue(long value) {
        curValue = String.valueOf(value);
    }

    public void setValue(float value) {
        curValue = String.valueOf(value);
    }

    public void setValue(boolean value) {
        curValue = String.valueOf(value);
    }

    public void setValue(String value) {
        curValue = value;
    }
}
