package com.duanze.litepreferences;

import android.content.Context;

import com.duanze.litepreferences.model.Pref;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Duanze on 15-11-22.
 */
public interface LiteInterface {
    LiteInterface getImpl();

    void initFromXmlLite(Context context, int res) throws IOException, XmlPullParserException;

    void initFromMapLite(Context context, String name, Map<String, Pref> map);

    Map<String, Pref> getPrefsMapLite();

    void putToMapLite(String key, Pref pref);

    int getIntLite(String key);

    int getIntLite(int keyRes);

    long getLongLite(String key);

    long getLongLite(int keyRes);

    float getFloatLite(String key);

    float getFloatLite(int keyRes);

    boolean getBooleanLite(String key);

    boolean getBooleanLite(int keyRes);

    String getStringLite(String key);

    String getStringLite(int keyRes);

    boolean putIntLite(String key, int value);

    boolean putIntLite(int keyRes, int value);

    boolean putLongLite(String key, long value);

    boolean putLongLite(int keyRes, long value);

    boolean putFloatLite(String key, float value);

    boolean putFloatLite(int keyRes, float value);

    boolean putBooleanLite(String key, boolean value);

    boolean putBooleanLite(int keyRes, boolean value);

    boolean putStringLite(String key, String value);

    boolean putStringLite(int keyRes, String value);

    boolean removeLite(String key);

    boolean removeLite(int keyRes);

    boolean clearLite();
}
