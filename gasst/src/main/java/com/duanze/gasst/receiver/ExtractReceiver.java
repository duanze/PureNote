package com.duanze.gasst.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.util.LogUtil;

public class ExtractReceiver extends BroadcastReceiver {
    public static final String TAG = "ExtractReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        int op = intent.getIntExtra("op", AlarmService.NOTHING);
        LogUtil.i(TAG, "op:" + op);

       if (AlarmService.NOTHING!=op){
           if (AlarmService.RAPID_START_EXTRACT == op){
               AlarmService.rapidStartExtractTask(context);
           }else if (AlarmService.RAPID_STOP_EXTRACT == op){
               AlarmService.rapidStopExtractTask(context);
           }
       }

    }

}
