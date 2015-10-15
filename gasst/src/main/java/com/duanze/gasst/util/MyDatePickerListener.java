package com.duanze.gasst.util;

/**
 * Created by Duanze on 2015/3/19.
 */

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.duanze.gasst.R;
import com.duanze.gasst.util.DateTimePickerCallback;
import com.duanze.gasst.util.Util;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.Calendar;

/**
 * 监听时间选择器专用
 */
public class MyDatePickerListener implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
    public static final String TIMEPICKER_TAG = "timepicker";

    private int year;
    private int month;
    private int day;

    private Calendar today;
    private DateTimePickerCallback dateTimePickerCallback;
    private Context context;

    public MyDatePickerListener(Context context, Calendar today, DateTimePickerCallback dateTimePickerCallback) {
        this.context = context;
        this.today = today;
        this.dateTimePickerCallback = dateTimePickerCallback;
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        if (year < today.get(Calendar.YEAR)
                || year == today.get(Calendar.YEAR) && month < today.get(Calendar.MONTH)
                || (year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH)
                && day < today.get(Calendar.DAY_OF_MONTH))) {
            Toast.makeText(context, R.string.date_error, Toast.LENGTH_SHORT).show();
            return;
        }
        this.year = year;
        this.month = month;
        this.day = day;
        //以当前时间新建picker
        Calendar tmp = Calendar.getInstance();
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this,
                tmp.get(Calendar.HOUR_OF_DAY), tmp.get(Calendar.MINUTE), false, false);
        timePickerDialog.setCloseOnSingleTapMinute(false);
        timePickerDialog.show(((FragmentActivity) context).getSupportFragmentManager(),
                TIMEPICKER_TAG);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        String result = this.year
                + ","
                + Util.twoDigit(this.month)
                + ","
                + Util.twoDigit(this.day)
                + ","
                + Util.twoDigit(hourOfDay)
                + ","
                + Util.twoDigit(minute);

        //定时完成执行回调
        if (dateTimePickerCallback != null) {
            dateTimePickerCallback.onFinish(result);
        }

    }

}
