package com.duanze.gasst.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.duanze.gasst.R;
import com.duanze.gasst.activity.Settings;

/**
 * Created by Duanze on 2015/10/14.
 */
public class PreferencesUtils {

    private float maxLengthRatio;
    private boolean useCreateOrder;
    private boolean concentrateWrite;
    private boolean oneColumn;
    private int gNotebookId;
    private ThemeUtils.Theme theme;
    private boolean passwordGuard;
    private int notesNum;

    private Context mContext;
    private SharedPreferences preferences;
    private boolean mValid = false;

    private static PreferencesUtils sMe;

    private PreferencesUtils(Context context) {
        mContext = context;
        preferences = context.getSharedPreferences(Settings.DATA, Context.MODE_PRIVATE);

        refreshData();
    }

    public static PreferencesUtils getInstance(Context mContext) {
        if (null == sMe) {
            sMe = new PreferencesUtils(mContext);
        }
        return sMe;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public void refreshData() {
        maxLengthRatio = preferences.getFloat(mContext.getString(R.string.note_max_length_key), (float) 0.418);
        useCreateOrder = preferences.getBoolean(mContext.getString(R.string.create_order_key), false);
        concentrateWrite = preferences.getBoolean(mContext.getString(R.string.concentrate_write_key), false);
        oneColumn = preferences.getBoolean(mContext.getString(R.string.one_column_key), false);
        theme = ThemeUtils.Theme.mapValueToTheme(preferences.getInt(mContext.getString(R.string.choose_theme_key), 0));

        passwordGuard = preferences.getBoolean(Settings.PASSWORD_GUARD, false);
        gNotebookId = preferences.getInt(Settings.GNOTEBOOK_ID, 0);
        notesNum = preferences.getInt(Settings.PURENOTE_NOTE_NUM, 3);
        mValid = true;
    }

    private void checkValid() {
        if (!mValid) {
            throw new IllegalStateException("this should only be called when the data you want are not fetched once");
        }
    }

    public float getMaxLengthRatio() {
        checkValid();
        return maxLengthRatio;
    }

    public void setMaxLengthRatio(float maxLengthRatio) {
        this.maxLengthRatio = maxLengthRatio;
        preferences.edit().putFloat(mContext.getString(R.string.note_max_length_key), maxLengthRatio).apply();
    }

    public boolean isUseCreateOrder() {
        checkValid();
        return useCreateOrder;
    }

    public void setUseCreateOrder(boolean useCreateOrder) {
        this.useCreateOrder = useCreateOrder;
        preferences.edit().putBoolean(mContext.getString(R.string.create_order_key), useCreateOrder).apply();
    }

    public boolean isConcentrateWrite() {
        checkValid();
        return concentrateWrite;
    }

    public boolean fetchIsConcentrate() {
        return concentrateWrite = preferences.getBoolean(mContext.getString(R.string.concentrate_write_key), true);
    }

    public void setConcentrateWrite(boolean concentrateWrite) {
        this.concentrateWrite = concentrateWrite;
        preferences.edit().putBoolean(mContext.getString(R.string.concentrate_write_key), concentrateWrite).apply();
    }

    public boolean isOneColumn() {
        checkValid();
        return oneColumn;
    }

    public void setOneColumn(boolean oneColumn) {
        this.oneColumn = oneColumn;
        preferences.edit().putBoolean(mContext.getString(R.string.one_column_key), oneColumn).apply();
    }

    public int getGNotebookId() {
        checkValid();
        return gNotebookId;
    }

    public int fetchGNotebookId() {
        return gNotebookId = preferences.getInt(Settings.GNOTEBOOK_ID, 0);
    }


    public void setGNotebookId(int gNotebookId) {
        this.gNotebookId = gNotebookId;
        preferences.edit().putInt(Settings.GNOTEBOOK_ID, gNotebookId).apply();
    }

    public ThemeUtils.Theme getTheme() {
        checkValid();
        return theme;
    }

    public ThemeUtils.Theme fetchTheme() {
        return theme = ThemeUtils.Theme.mapValueToTheme(preferences.getInt(mContext.getString(R.string.choose_theme_key), 0));
    }

    public void setTheme(int i) {
        theme = ThemeUtils.Theme.mapValueToTheme(i);
        preferences.edit().putInt(mContext.getString(R.string.choose_theme_key), i).apply();
    }

    public boolean isPasswordGuard() {
        checkValid();
        return passwordGuard;
    }

    public void setPasswordGuard(boolean passwordGuard) {
        this.passwordGuard = passwordGuard;
        preferences.edit().putBoolean(Settings.PASSWORD_GUARD, passwordGuard).apply();
    }

    public int getNotesNum() {
        checkValid();
        return notesNum;
    }

    public void setNotesNum(int notesNum) {
        this.notesNum = notesNum;
        preferences.edit().putInt(Settings.PURENOTE_NOTE_NUM, notesNum).apply();
    }
}
