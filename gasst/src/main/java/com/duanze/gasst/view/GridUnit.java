package com.duanze.gasst.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duanze.gasst.R;
import com.duanze.gasst.util.Util;

import java.util.Calendar;
import java.util.Random;

public class GridUnit extends RelativeLayout {
    public static final int THRANSPARENT = Color.parseColor("#00000000");
    public static final int LIGHT_GREY = Color.parseColor("#6AC0C0C0");

    public static final int YELLOW = Color.parseColor("#52FFFF00");
    public static final int GREEN = Color.parseColor("#5A90EE90");
    public static final int PINK = Color.parseColor("#6AFFC0CB");
    public static final int PURPLE = Color.parseColor("#4ADA70D6");
    public static final int BLUE = Color.parseColor("#7AADD8E6");
    public static final int RED = Color.parseColor("#32FF0000");
    public static final int GOLD = Color.parseColor("#6AFFD700");

    public static final int[] colorArr = {
            THRANSPARENT, LIGHT_GREY, BLUE,
            GREEN, YELLOW, GOLD,
            PINK, RED, PURPLE
    };

    public static final Random random = new Random();

    // text color
    public static final int GREY = Color.parseColor("#808080");
    public static final int DIM_GREY = Color.parseColor("#696969");

    public static final int[] weekArr = {R.string.sunday, R.string.monday, R.string.tuesday,
            R.string.wednesday, R.string.thursday, R.string.friday, R.string.saturday};

//    public static final String[] monthArr = {"壹月", "贰月", "叁月", "肆月", "伍月", "陆月", "柒月", "捌月", "玖月", "拾月 ", "拾壹月", "拾贰月"};
//    public static final String[] dateArr = {"壹日", "贰日", "叁日", "肆日", "伍日", "陆日", "柒日", "捌日", "玖日", "拾日 ", "拾壹", "拾贰", "拾叁", "拾肆", "拾伍", "拾陆", "拾柒", "拾捌", "拾玖", "贰拾", "贰拾壹", "贰拾贰", "贰拾叁", "贰拾肆", "贰拾伍", "贰拾陆", "贰拾柒", "贰拾捌", "贰拾玖", "叁拾", "叁拾壹"};

    public static final int[] monthArr = {R.string.month1, R.string.month2, R.string.month3,
            R.string.month4, R.string.month5, R.string.month6, R.string.month7, R.string.month8,
            R.string.month9, R.string.month10, R.string.month11, R.string.month12};
    public static final int[] dayArr = {R.string.day1, R.string.day2, R.string.day3, R.string.day4,
            R.string.day5, R.string.day6, R.string.day7, R.string.day8, R.string.day9,
            R.string.day10, R.string.day11, R.string.day12, R.string.day13, R.string.day14,
            R.string.day15, R.string.day16, R.string.day17, R.string.day18, R.string.day19,
            R.string.day20, R.string.day21, R.string.day22, R.string.day23, R.string.day24,
            R.string.day25, R.string.day26, R.string.day27, R.string.day28, R.string.day29,
            R.string.day30, R.string.day31};

    private TextView date_month, date_week, note;

    public TextView getDoneButton() {
        return doneButton;
    }

    private TextView doneButton;

    public GridUnit(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.grid_unit, this);
        date_month = (TextView) this.findViewById(R.id.date_month);
        date_week = (TextView) this.findViewById(R.id.date_week);
        note = (TextView) this.findViewById(R.id.note);
        doneButton = (TextView) this.findViewById(R.id.is_done);

    }

    @Override
    public void setBackgroundColor(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE); // 画框
        drawable.setStroke(1, GridUnit.LIGHT_GREY); // 边框粗细及颜色
        drawable.setColor(color); // 边框内部颜色

        this.setBackground(drawable);
    }

    /**
     * 随机设置背景色（七种预定义透明度90颜色）
     */
    public void randomSetBackground() {
        int color = GOLD;
        int result = random.nextInt(24);
        if (result < 3) {
            color = YELLOW;
        } else if (result < 6) {
            color = GREEN;
        } else if (result < 9) {
            color = PINK;
        } else if (result < 12) {
            color = PURPLE;
        } else if (result < 15) {
            color = BLUE;
        } else if (result < 18) {
            color = RED;
        } else if (result < 21) {
            color = LIGHT_GREY;
        }
        this.setBackgroundColor(color);
    }

    /**
     * 对viewUnit填充日期date
     *
     * @param date
     * @param setMonth 是否设置月份（繁中）
     */
    public void setViewUnit(Calendar date, boolean setMonth) {
        String tmp = Util.twoDigit(date.get(Calendar.DAY_OF_MONTH));
        if (setMonth) {
            tmp = getResources().getString(monthArr[date.get(Calendar.MONTH)])
                    + getResources().getString(dayArr[date.get(Calendar.DAY_OF_MONTH) - 1]);
        }
        date_month.setText(tmp);
        date_week.setText(weekArr[date.get(Calendar.DAY_OF_WEEK) - 1]);
    }

    /**
     * 对viewUnit的note(TextView)写入数据
     *
     * @param text String
     */
    public void setViewNote(String text) {
        note.setText("        " + text);
    }

    public void addStrike() {
        note.setTextColor(GREY);
        note.getPaint().setStrikeThruText(true);

    }

    public void removeStrike() {
        note.setTextColor(DIM_GREY);
        note.getPaint().setStrikeThruText(false);

    }
}
