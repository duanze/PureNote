package com.duanze.gasst.util;

import android.app.Activity;
import android.content.Context;

import com.duanze.gasst.R;
import com.duanze.gasst.util.liteprefs.MyLitePrefs;


/**
 * Created by lgp on 2015/6/7.
 */
public class ThemeUtils {
    public static final Integer[] THEME_RES_ARR = new Integer[]{R.drawable.blue_round, R.drawable.yellow_round, R.drawable.pink_round, R.drawable.green_round
            , R.drawable.deep_purple_round, R.drawable.gray_round, R.drawable.light_pink_round, R.drawable.brown_round};

    public static void changTheme(Activity activity, Theme theme) {
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
            case DEEP_PURPLE:
                style = R.style.DeepPurpleTheme;
                break;
            case GRAY:
                style = R.style.GrayTheme;
                break;
            case LIGHT_PINK:
                style = R.style.LightPinkTheme;
                break;
            case BROWN:
                style = R.style.BrownTheme;
                break;
            default:
                break;
        }
        activity.setTheme(style);
    }

    public static Theme getCurrentTheme(Context context) {
        return MyLitePrefs.getTheme();
    }

    public enum Theme {
        BLUE(0),
        YELLOW(1),
        PINK(2),
        GREEN(3),
        DEEP_PURPLE(4),
        GRAY(5),
        LIGHT_PINK(6),
        BROWN(7);

        private int intValue;

        Theme(int value) {
            this.intValue = value;
        }

        public static Theme mapValueToTheme(final int value) {
            for (Theme theme : Theme.values()) {
                if (value == theme.getIntValue()) {
                    return theme;
                }
            }
            // If run here, return default
            return BLUE;
        }

        public int getIntValue() {
            return intValue;
        }
    }
}
