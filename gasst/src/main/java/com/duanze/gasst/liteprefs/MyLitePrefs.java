package com.duanze.gasst.liteprefs;

import android.content.Context;

import com.duanze.gasst.R;
import com.duanze.litepreferences.rawmaterial.BaseLitePrefs;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Duanze on 15-11-21.
 */
public class MyLitePrefs extends BaseLitePrefs {

    private static MyLitePrefs sMe;

    private MyLitePrefs() {

    }

    public static MyLitePrefs getInstance() {
        if (null == sMe) {
            sMe = new MyLitePrefs();
        }
        return sMe;
    }

    public void initFromXml(Context context) {
        try {
            super.initFromXml(context, R.xml.prefs);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }


}
