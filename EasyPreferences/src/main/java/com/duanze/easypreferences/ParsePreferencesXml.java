package com.duanze.easypreferences;

import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ParsePreferencesXml {

    private static final String TAG_ROOT = "preferences";
    private static final String TAG_CHILD = "preference";
    private static final String ATTR_NAME = "name";

    private static final String TAG_KEY = "key";
    private static final String TAG_DEFAULT_VALUE = "def-value";

    public static ActualUtil parse(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        Map<String, Preference> map = new HashMap<>();
        int event = parser.getEventType();

        Preference preference = null;
        String name = null;
        Stack<String> tagStack = new Stack<>();

        while (event != XmlResourceParser.END_DOCUMENT) {
            if (event == XmlResourceParser.START_TAG) {
                switch (parser.getName()) {
                    case TAG_ROOT:
                        name = parser.getAttributeValue(null, ATTR_NAME);
                        tagStack.push(TAG_ROOT);
                        if (null == name) {
                            throw new XmlPullParserException(
                                    "Error in xml: doesn't contain a 'name' at line:"
                                            + parser.getLineNumber());
                        }
                        break;
                    case TAG_CHILD:
                        preference = new Preference();
                        tagStack.push(TAG_CHILD);
                        break;
                    case TAG_KEY:
                        tagStack.push(TAG_KEY);
                        break;
                    case TAG_DEFAULT_VALUE:
                        tagStack.push(TAG_DEFAULT_VALUE);
                        break;
                    default:
                        throw new XmlPullParserException(
                                "Error in xml: tag isn't '"
                                        + TAG_ROOT
                                        + "' or '"
                                        + TAG_CHILD
                                        + "' or '"
                                        + TAG_KEY
                                        + "' or '"
                                        + TAG_DEFAULT_VALUE
                                        + "' at line:"
                                        + parser.getLineNumber());
                }

            } else if (event == XmlResourceParser.TEXT) {
                switch (tagStack.peek()) {
                    case TAG_KEY:
                        preference.key = parser.getText();
                        break;
                    case TAG_DEFAULT_VALUE:
                        preference.defValue = parser.getText();
                        break;
                }

            } else if (event == XmlResourceParser.END_TAG) {
                boolean mismatch = false;
                switch (parser.getName()) {
                    case TAG_ROOT:
                        if (!TAG_ROOT.equals(tagStack.pop())) {
                            mismatch = true;
                        }
                        break;
                    case TAG_CHILD:
                        if (!TAG_CHILD.equals(tagStack.pop())) {
                            mismatch = true;
                        }
                        map.put(preference.key, preference);
                        break;
                    case TAG_KEY:
                        if (!TAG_KEY.equals(tagStack.pop())) {
                            mismatch = true;
                        }
                        break;
                    case TAG_DEFAULT_VALUE:
                        if (!TAG_DEFAULT_VALUE.equals(tagStack.pop())) {
                            mismatch = true;
                        }
                        break;
                }

                if (mismatch) {
                    throw new XmlPullParserException(
                            "Error in xml: mismatch end tag at line:"
                                    + parser.getLineNumber());
                }

            }
            event = parser.next();
        }
        parser.close();
        return new ActualUtil(name, map);
    }
}
