package com.duanze.gasst.util;

import android.app.Activity;
import android.content.Context;

import com.duanze.gasst.R;


/**
 * Created by lgp on 2015/6/7.
 */
public class ThemeUtils {

    public static void changTheme(Activity activity, PreferencesUtils.Theme theme) {
        if (activity == null)
            return;
        int style = R.style.DeepPurpleTheme;
        switch (theme) {
            case YELLOW:
                style = R.style.YellowTheme;
                break;
            case BLUE:
                style = R.style.BlueTheme;
                break;
            case PINK:
                style = R.style.PinkTheme;
                break;
            case GREEN:
                style = R.style.GreenTheme;
                break;
            default:
                break;
        }
        activity.setTheme(style);
    }

    public static PreferencesUtils.Theme getCurrentTheme(Context context) {
        return PreferencesUtils.getInstance(context).getTheme();
    }

}
