package com.duanze.gasst.util;

import android.graphics.Color;

import com.duanze.gasst.model.GNote;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class Util {
    public static final String[] MONTH_ARR = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
            "Aug", "Sep", "Oct", "Nov", "Dev"};
    public static final String[] DATE_ARR = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th",
            "8th", "9th", "10th", "11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th",
            "19th", "20th", "21th", "22th", "23th", "24th", "25th", "26th", "27th", "28th", "29th",
            "30th", "31th"};

    public static final int GREY = Color.parseColor("#808080");
    public static final int HOLO_ORANGE_LIGHT = Color.parseColor("#ffffbb33");
    public static final int HOLO_GREEN_LIGHT = Color.parseColor("#ff99cc00");
    public static final int HOLO_RED_LIGHT = Color.parseColor("#ffff4444");
    public static final int HOLO_PURPLE = Color.parseColor("#ffaa66cc");
    public static final int HOLO_BLUE_LIGHT = Color.parseColor("#ff33b5e5");
    public static final Random random = new Random();


    public static String twoDigit(int n) {
        java.text.DecimalFormat format = new java.text.DecimalFormat("00");
        return format.format(n);
    }

    public static String twoDigit(String str) {
        if (str.length() == 1) {
            return "0" + str;
        }
        return str;
    }

    /**
     * 得到一个GAsstNote的格式化表示时间
     *
     * @param gNote
     * @return
     */
    public static String timeString(GNote gNote) {
        if (gNote.getPassed() == GNote.FALSE) {
            return alertTimeStamp(gNote);
        } else {
           return timeStamp(gNote);
        }
    }

    public static String alertTimeStamp(GNote gNote){
        String tmp = "";
        String[] allInfo;
        allInfo = gNote.getAlertTime().split(",");
        if (allInfo.length == 5) {
            tmp = "Link"
                    + twoDigit(Integer.parseInt(allInfo[1]) + 1)
                    + allInfo[2]
                    + "At"
                    + allInfo[3]
                    + ":"
                    + allInfo[4];
        }
        return tmp;
    }

    public static String timeStamp(GNote gNote){
        String tmp = "";
        String[] allInfo;
        allInfo = gNote.getTime().split(",");
        if (allInfo.length == 3) {
            tmp = MONTH_ARR[Integer.parseInt(allInfo[1])]
                    + "."
                    + DATE_ARR[Integer.parseInt(allInfo[2]) - 1]
                    + "."
                    + allInfo[0];
        }
        return tmp;
    }

    /**
     * 随机设置背景色
     *
     */
    public static void randomBackground(FloatingActionButton b) {
        int color = GREY;
        int result = random.nextInt(21);
        if (result < 3) {
            color = HOLO_GREEN_LIGHT;
        } else if (result < 6) {
            color = HOLO_BLUE_LIGHT;
        } else if (result < 9) {
            color = HOLO_RED_LIGHT;
        } else if (result < 12) {
            color = HOLO_PURPLE;
        } else if (result < 15) {
            color = HOLO_ORANGE_LIGHT;
        }
        b.setColor(color);
    }

}
