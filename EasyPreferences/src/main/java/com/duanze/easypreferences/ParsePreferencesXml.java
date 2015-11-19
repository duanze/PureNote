package com.duanze.easypreferences;

import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duanze on 2015/11/19.
 */
public class ParsePreferencesXml {
    private static final String TAG_ROOT = "preferences";
    private static final String ATTR_NAME = "name";

    private static final String TAG_CHILD = "preference";

    private static final String PREFERENCE_KEY = "key";
    private static final String PREFERENCE_TYPE = "type";
    private static final String PREFERENCE_DEFAULT_VALUE = "def-value";

    public static PreferencesUtil parse(XmlResourceParser parser)
            throws XmlPullParserException, IOException {

        PreferencesUtil preferencesUtil = new PreferencesUtil();

        String name = null;
        ArrayList<Preference> list = new ArrayList<>();
        Preference preference = null;
        String curTag = "";
        int event = parser.getEventType();

        while (event != XmlResourceParser.END_DOCUMENT) {
            if (event == XmlResourceParser.START_TAG) {
                if (parser.getName().equals(TAG_ROOT)) {
                    name = parser.getAttributeValue(null, ATTR_NAME);
                } else if (parser.getName().equals(TAG_CHILD)) {
                    preference = new Preference();
                } else if (parser.getName().equals(PREFERENCE_KEY)) {
                    curTag = PREFERENCE_KEY;
                } else if (parser.getName().equals(PREFERENCE_TYPE)) {
                    curTag = PREFERENCE_TYPE;
                } else if (parser.getName().equals(PREFERENCE_DEFAULT_VALUE)) {
                    curTag = PREFERENCE_DEFAULT_VALUE;
                } else {
                    throw new XmlPullParserException(
                            "Error in xml: tag isn't '"
                                    + TAG_ROOT
                                    + "' or '"
                                    + TAG_CHILD
                                    + "' or '"
                                    + PREFERENCE_KEY
                                    + "' or '"
                                    + PREFERENCE_TYPE
                                    + "' or '"
                                    + PREFERENCE_DEFAULT_VALUE
                                    + "' at line:"
                                    + parser.getLineNumber());
                }

            } else if (event == XmlResourceParser.TEXT) {
                if (PREFERENCE_KEY.equals(curTag)) {
                    String key = parser.getText();
                    if (null != preference) {
                        preference.setKey(key);
                    }
                } else if (PREFERENCE_TYPE.equals(curTag)) {
                    String type = parser.getText();
                    if (null != preference) {
                        preference.setKey(type);
                    }
                } else if (PREFERENCE_DEFAULT_VALUE.equals(curTag)) {
                    String defValue = parser.getText();
                    if (null != preference) {
                        preference.setDefValue(defValue);
                    }
                }
            } else if (event == XmlResourceParser.END_TAG) {
                if (name != null && type != null && license != null
                        && !parser.getName().equals(TAG_ROOT)) {
                    if (type.equals(VALUE_FILE)) {
                        list.add(new Preference(name, License.TYPE_FILE,
                                license));
                        System.out.println(name);
                    } else if (type.equals(VALUE_LIBRARY)) {
                        list.add(new Preference(name, License.TYPE_LIBRARY,
                                license));
                        System.out.println(name);
                    } else {
                        throw new XmlPullParserException(
                                "Error in xml: 'type' isn't valid at line:"
                                        + parser.getLineNumber());
                    }
                } else if (name == null) {
                    throw new XmlPullParserException(
                            "Error in xml: doesn't contain a 'name' at line:"
                                    + parser.getLineNumber());
                } else if (type == null) {
                    throw new XmlPullParserException(
                            "Error in xml: doesn't contain a 'type' at line:"
                                    + parser.getLineNumber());
                } else if (license == null) {
                    throw new XmlPullParserException(
                            "Error in xml: doesn't contain a 'license text' at line:"
                                    + parser.getLineNumber());
                }
            }
            event = parser.next();
        }
        parser.close();

        preferencesUtil.setName(name);
        preferencesUtil.setPreferences(list);
        return preferencesUtil;
    }
}
