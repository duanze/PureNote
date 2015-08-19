package com.duanze.gasst.service;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;


import com.duanze.gasst.MainActivity;
import com.duanze.gasst.R;
import com.duanze.gasst.activity.NoteActivity;
import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.receiver.AlarmReceiver;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.Util;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AlarmService extends Service {
    public static final String TAG = "MyService";


    public static final int NOTHING = -347;
    public static final int DELETE_ALARM = 0;
    public static final int ADD_ALARM = 1;
    public static final int REDUCE_ALARM = 2;
    public static final int START_EXTRACT = 3;
    public static final int STOP_EXTRACT = 4;
    public static final int RAPID_START_EXTRACT = 5;
    public static final int RAPID_STOP_EXTRACT = 6;
    public static final int SHOW_OR_HIDE = 7;


    private GNoteDB db;
    private int cnt;
    private Context mContext;
    private RemoteViews remoteViews;
    private Notification notification;
    private SharedPreferences preferences;


    private boolean isExtract = false;


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


    public static void startExtractTask(Context context) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("command", AlarmService.START_EXTRACT);
        context.startService(intent);
    }


    public static void stopExtractTask(Context context) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("command", AlarmService.STOP_EXTRACT);
        context.startService(intent);
    }


    public static void rapidStartExtractTask(Context context) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("command", AlarmService.RAPID_START_EXTRACT);
        context.startService(intent);
    }


    public static void rapidStopExtractTask(Context context) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("command", AlarmService.RAPID_STOP_EXTRACT);
        context.startService(intent);
    }


    public static void showOrHide(Context context) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("command", AlarmService.SHOW_OR_HIDE);
        context.startService(intent);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        db = GNoteDB.getInstance(mContext);
        preferences = getSharedPreferences(Settings.DATA, MODE_PRIVATE);
        isExtract = preferences.getBoolean(Settings.LIGHTNING_EXTRACT, false);


        final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (isExtract) {
                    ClipData data = cm.getPrimaryClip();
                    ClipData.Item item = data.getItemAt(0);
                    int extractLocation = preferences.getInt(Settings
                            .LIGHTNING_EXTRACT_SAVE_LOCATION, 0);


                    String extractGroup = Util.extractNote(preferences, db,
                            item.coerceToHtmlText(mContext),
                            extractLocation, mContext);
                    if (extractGroup != null) {
                        Toast.makeText(mContext, "文本摘录至 " + extractGroup, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, R.string.extract_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "start command");
        int command = intent.getIntExtra("command", -1);
        if (command == ADD_ALARM) {
            refreshAlarmTasks();
            refreshNotification();
        } else if (command == DELETE_ALARM) {
            int id = intent.getIntExtra("id", -1);
            if (id != -1) {
                deleteAlarm(id);
                maybeStop();
                updateNotification();
            }
        } else if (command == REDUCE_ALARM) {
            cnt--;
            maybeStop();
            updateNotification();
        } else if (START_EXTRACT == command) {
            startExtract();
        } else if (STOP_EXTRACT == command) {
            stopExtract();
            maybeStop();
        } else if (RAPID_START_EXTRACT == command) {
            LogUtil.i(TAG, "rapidStart");
            rapidStartExtract();
        } else if (RAPID_STOP_EXTRACT == command) {
            LogUtil.i(TAG, "rapidStop");
            rapidStopExtract();
            maybeStop();
        } else if (NOTHING == command) {
            LogUtil.i(TAG, "nothing");
        }
        else if (SHOW_OR_HIDE == command) {
            showOrHideService();
        } else {
            LogUtil.i(TAG, "command error");
            stopSelf();
        }


        return super.onStartCommand(intent, flags, startId);
    }


    private void showOrHideService() {
        boolean b = preferences.getBoolean(Settings.NOTIFICATION_ALWAYS_SHOW,false);
        if (b) {
            refreshAlarmTasks();
            refreshNotification();
        } else {
            refreshNotification();
        }
    }


    private void stopExtract() {
        if (null == remoteViews) return;
        isExtract = false;
        remoteViews.setImageViewResource(R.id.iv_bolt, R.drawable.bolt_off_big);
        updateBoltOnClick();
        startForeground(-1, notification);
        Toast.makeText(mContext, "停用闪电摘录", Toast.LENGTH_SHORT).show();
    }


    private void rapidStopExtract() {
        if (null == remoteViews) return;
        isExtract = false;
        remoteViews.setImageViewResource(R.id.iv_bolt, R.drawable.bolt_off_big);
        updateBoltOnClick();
        startForeground(-1, notification);



        preferences.edit().putBoolean(Settings.LIGHTNING_EXTRACT, false).apply();
        Toast.makeText(mContext, "停用闪电摘录", Toast.LENGTH_SHORT).show();
    }


    private void startExtract() {
        if (null == remoteViews) return;
        isExtract = true;


        remoteViews.setImageViewResource(R.id.iv_bolt, R.drawable.bolt_on_big);
        updateBoltOnClick();
        startForeground(-1, notification);
        Toast.makeText(mContext, "启动闪电摘录", Toast.LENGTH_SHORT).show();
    }


    private void rapidStartExtract() {
        if (null == remoteViews) return;
        isExtract = true;
        remoteViews.setImageViewResource(R.id.iv_bolt, R.drawable.bolt_on_big);
        updateBoltOnClick();
        startForeground(-1, notification);


        preferences.edit().putBoolean(Settings.LIGHTNING_EXTRACT, true).apply();
        Toast.makeText(mContext, "启动闪电摘录", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy() {
        LogUtil.i(TAG, "destroyed");
        super.onDestroy();
    }


    private void refreshNotification() {


        String str;
        if (cnt <= 0) {
            cnt = 0;
            str = "没有倒计时中的定时提醒";
        } else if (cnt == 1) {
            str = cnt + getResources().getString(R.string.one_reminder);
        } else {
            str = cnt + getResources().getString(R.string.more_reminder);
        }



        maybeStop();



        remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.tv_up, getResources().getString(R.string.app_name));
        remoteViews.setTextViewText(R.id.tv_down, str);


//write a new note when onClicked
        GNote gNote = new GNote();
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra("gAsstNote_data", gNote);
        intent.putExtra("mode", NoteActivity.MODE_TODAY);
        PendingIntent writeNotePendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.iv_new_note, writeNotePendingIntent);


        if (isExtract) {
            remoteViews.setImageViewResource(R.id.iv_bolt, R.drawable.bolt_on_big);
//特殊路径，初始启动
            Toast.makeText(mContext, "启动闪电摘录", Toast.LENGTH_SHORT).show();
        } else {
            remoteViews.setImageViewResource(R.id.iv_bolt, R.drawable.bolt_off_big);
        }
//switch lightning extract
        updateBoltOnClick();


        Intent myIntent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, myIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOngoing(false);
        builder.setAutoCancel(false);
        builder.setContent(remoteViews);
        builder.setSmallIcon(R.drawable.small_logo);


        notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        notification.contentIntent = pi;


        startForeground(-1, notification);
    }


    private void maybeStop() {
//检测是否常驻通知栏与是否开启闪电摘录
        boolean b = preferences.getBoolean(Settings.NOTIFICATION_ALWAYS_SHOW,false);
        boolean isExtract = preferences.getBoolean(Settings.LIGHTNING_EXTRACT,false);
        if (!b && cnt == 0 && !isExtract){
            stopSelf();
        }
    }


    private void updateBoltOnClick() {
        if (null == remoteViews) return;
        if (isExtract) {
            LogUtil.i(TAG, "Now isExtract,setRapidStop");
            Intent intent = new Intent(this, AlarmService.class);
            intent.putExtra("command", AlarmService.RAPID_STOP_EXTRACT);
            PendingIntent rapidStop = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.iv_bolt, rapidStop);
        } else {
            LogUtil.i(TAG, "Now !isExtract,setRapidStart");
            Intent intent = new Intent(this, AlarmService.class);
            intent.putExtra("command", AlarmService.RAPID_START_EXTRACT);
            PendingIntent rapidStart = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.iv_bolt, rapidStart);
        }
    }


    private void updateNotification() {
        String str;
        if (cnt <= 0) {
            cnt = 0;
            str = "没有倒计时中的定时提醒";
        } else if (cnt == 1) {
            str = cnt + getResources().getString(R.string.one_reminder);
        } else {
            str = cnt + getResources().getString(R.string.more_reminder);
        }




        if (null != remoteViews) {
            remoteViews.setTextViewText(R.id.tv_down, str);
        } else {
            LogUtil.e(TAG, "updateNotification()--remoteViews is null!!!");
        }
        startForeground(-1, notification);
    }



    private void refreshAlarmTasks() {
        List<GNote> list = db.loadGNotes();
        cnt = 0;
        for (int i = 0; i < list.size(); i++) {
            GNote note = list.get(i);
            if (note.getPassed() == GNote.FALSE) {
                alarmTask(note);
            }
        }
    }


    private void deleteAlarm(int id) {
        cancelTask(id);
        cnt--;
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