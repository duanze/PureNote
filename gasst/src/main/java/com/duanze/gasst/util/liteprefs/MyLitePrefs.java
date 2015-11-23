package com.duanze.gasst.util.liteprefs;

import android.content.Context;

import com.duanze.gasst.R;
import com.duanze.gasst.util.ThemeUtils;
import com.duanze.litepreferences.model.StringPref;
import com.duanze.litepreferences.rawmaterial.BaseLitePrefs;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Duanze on 15-11-21.
 */
public class MyLitePrefs extends BaseLitePrefs {

    public static final String DATA = "gasst_pref";

    public static final String THEME = "choose_theme_key";
    public static final String CONCENTRATE_WRITE = "concentrate_write_key";
    public static final String ONE_COLUMN = "one_column_key";
    public static final String CREATE_ORDER = "create_order_key";
    public static final String NOTE_MAX_LENGTH_RATIO = "note_max_length_key";

    public static final String PASSWORD_GUARD = "password_guard";
    public static final String SHOW_UNIVERSAL_SWITCH = "show_universal_switch";
    public static final String USE_UNIVERSAL_PASSWORD = "use_universal_password";
    public static final String UNIVERSAL_PASSWORD = "-_-#";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_HINT = "password_hint";

    public static final String NOTIFICATION_ALWAYS_SHOW = "notification_always_show_key";

    public static final String LIGHTNING_EXTRACT = "lightning_extract";
    public static final String LIGHTNING_EXTRACT_SAVE_LOCATION = "lightning_extract_save_location";
    public static final String QUICK_WRITE_SAVE_LOCATION = "quick_write_save_location";

    public static final String GNOTEBOOK_ID = "gnotebook_id";
    public static final String PURENOTE_NOTE_NUM = "purenote_note_num";

    public static final String ALL_NOTES_STRING = "all_notes_string_key";

    public static void initFromXml(Context context) {
        try {
            initFromXml(context, R.xml.prefs);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        putToMap(ALL_NOTES_STRING, new StringPref(ALL_NOTES_STRING, R.string.all_notes, context.getApplicationContext()));
    }

    public static ThemeUtils.Theme getTheme() {
        return ThemeUtils.Theme.mapValueToTheme(getInt(THEME));
    }

    public static boolean setTheme(int value) {
        return putInt(THEME, value);
    }

}
