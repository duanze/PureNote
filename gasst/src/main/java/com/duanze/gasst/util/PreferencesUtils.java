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
    private Context mContext;
    private SharedPreferences preferences;
    private boolean mValid = false;

    private static PreferencesUtils sMe;

    private PreferencesUtils(Context context) {
        mContext = context;
        preferences = context.getSharedPreferences(Settings.DATA, Context.MODE_PRIVATE);
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
        concentrateWrite = preferences.getBoolean(mContext.getString(R.string.concentrate_write_key), true);
        oneColumn = preferences.getBoolean(mContext.getString(R.string.one_column_key), false);

        gNotebookId = preferences.getInt(Settings.GNOTEBOOK_ID, 0);
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

    public boolean isUseCreateOrder() {
        checkValid();
        return useCreateOrder;
    }

    public boolean isConcentrateWrite() {
        checkValid();
        return concentrateWrite;
    }

    public boolean isOneColumn() {
        checkValid();
        return oneColumn;
    }

    public int getGNotebookId() {
        checkValid();
        return gNotebookId;
    }

    public int fetchGNotebookId() {
        return gNotebookId = preferences.getInt(Settings.GNOTEBOOK_ID, 0);
    }
}
