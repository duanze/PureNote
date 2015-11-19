package com.duanze.easypreferences;

import java.util.Set;

/**
 * Created by Duanze on 2015/11/19.
 */
public class Preference {
    public static final String TYPE_INT = "int";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_STRING = "String";
    public static final String TYPE_STRING_SET = "Set<String>";

    private String key;
    private String type;
    private String defValue;

    public Preference() {

    }

    public Preference(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public Preference(String key, String type, String defValue) {
        this(key, type);
        this.defValue = defValue;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getInt() {
        if (TYPE_INT.equals(type)) {
            return Integer.parseInt(defValue);
        } else {
            throw new IllegalStateException("mismatch type:" + type);
        }
    }

    public long getLong() {
        if (TYPE_LONG.equals(type)) {
            return Long.parseLong(defValue);
        } else {
            throw new IllegalStateException("mismatch type:" + type);
        }
    }

    public float getFloat() {
        if (TYPE_FLOAT.equals(type)) {
            return Float.parseFloat(defValue);
        } else {
            throw new IllegalStateException("mismatch type:" + type);
        }
    }

    public boolean getBoolean() {
        if (TYPE_BOOLEAN.equals(type)) {
            return Boolean.parseBoolean(defValue);
        } else {
            throw new IllegalStateException("mismatch type:" + type);
        }
    }

    public String getString() {
        if (TYPE_STRING.equals(type)) {
            return defValue;
        } else {
            throw new IllegalStateException("mismatch type:" + type);
        }
    }
}
