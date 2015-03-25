package com.duanze.gasst.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;

import com.duanze.gasst.MainActivity;
import com.duanze.gasst.MyApplication;
import com.duanze.gasst.R;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.receiver.AlarmReceiver;
import com.duanze.gasst.util.LogUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlarmService extends Service {
    public static final String TAG = "MyService";

    public static final int DELETE_ALARM = 0;
    public static final int ADD_ALARM = 1;
    public static final int REDUCE_ALARM = 2;

    private GNoteDB db;
    private int cnt;

    /**
     * 启动定时提醒
     *
     * @param context
     */
    public static void alarmTask(Context context) {
        //开启服务
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("command", AlarmService.ADD_ALARM);
        context.startService(intent);
    }

    /**
     * 根据id取消定时提醒
     */
    public static void cancelTask(Context context, GNote gNote) {
        //开启服务
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("command", AlarmService.DELETE_ALARM);
        intent.putExtra("id", gNote.getId());
        context.startService(intent);
    }

    public static void reduceTask(Context context) {
        //开启服务
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("command", AlarmService.REDUCE_ALARM);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = GNoteDB.getInstance(MyApplication.getContext());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "start command");
        int command = intent.getIntExtra("command", -1);
        if (command == ADD_ALARM) {
            addAlarm();
        } else if (command == DELETE_ALARM) {
            int id = intent.getIntExtra("id", -1);
            if (id != -1) {
                deleteAlarm(id);
            }
        } else if (command == REDUCE_ALARM) {
            cnt--;
            refreshNotification();
        } else {
            LogUtil.i(TAG, "command error");
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogUtil.i(TAG, "destroyed");
        super.onDestroy();
    }

    private void refreshNotification() {
        if (cnt > 0) {
            String str;
            if (cnt == 1) {
                str = cnt + getResources().getString(R.string.one_reminder);
            } else {
                str = cnt + getResources().getString(R.string.more_reminder);
            }
            Intent myIntent = new Intent(this, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, myIntent, 0);
            Notification notification = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(str)
                    .setSmallIcon(R.drawable.small_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                            R.drawable.logo))
                    .setContentIntent(pi)
                    .build();
            startForeground(-1, notification);
        } else {
            stopSelf();
        }
    }


    private void addAlarm() {
        List<GNote> list = db.loadGNotes();
        cnt = 0;
        for (int i = 0; i < list.size(); i++) {
            GNote note = list.get(i);
            if (note.getPassed() == GNote.FALSE) {
                alarmTask(note);
            }
        }

        refreshNotification();
    }

    private void deleteAlarm(int id) {
        cancelTask(id);
        cnt--;
        refreshNotification();
    }

    /**
     * 根据id设定一个定时提醒
     */
    private void alarmTask(GNote gNote) {
        int no = gNote.getId();
        Calendar tmp = Calendar.getInstance();
        String[] allInfo = gNote.getAlertTime().split(",");
        long diff = 10 * 1000;
        if (allInfo.length != 5) {
            return;
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d1 = df.parse(tmp.get(Calendar.YEAR) + "-"
                    + (tmp.get(Calendar.MONTH) + 1) + "-"
                    + tmp.get(Calendar.DAY_OF_MONTH) + " "
                    + tmp.get(Calendar.HOUR_OF_DAY) + ":"
                    + tmp.get(Calendar.MINUTE) + ":"
                    + tmp.get(Calendar.SECOND));

            Date d2 = df.parse(allInfo[0] + "-"
                    + (Integer.parseInt(allInfo[1]) + 1) + "-"
                    + allInfo[2] + " "
                    + allInfo[3] + ":"
                    + allInfo[4] + ":00");
            diff = d2.getTime() - d1.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (diff < 0) {
            return;
        }

        //有效任务数量加一
        cnt++;

        LogUtil.i(TAG, "diff:" + diff);
        LogUtil.i(TAG, "no:" + no);
        long triggerAtTime = SystemClock.elapsedRealtime() + diff;
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra("no", no);
        i.putExtra("gAsstNote_data", gNote);
        PendingIntent pi = PendingIntent.getBroadcast(this, no, i, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }

    /**
     * 根据id取消定时提醒
     */
    private void cancelTask(int no) {
        LogUtil.i(TAG, "cancel:no:" + no);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(AlarmService.this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(AlarmService.this, no, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        manager.cancel(pi);
    }
}
