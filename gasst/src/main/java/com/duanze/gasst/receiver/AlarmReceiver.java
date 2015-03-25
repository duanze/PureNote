package com.duanze.gasst.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.duanze.gasst.R;
import com.duanze.gasst.activity.NoteActivity;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.Util;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);
        GNote gNote = intent.getParcelableExtra("gAsstNote_data");
        //更新状态并存入数据库
        gNote.setPassed(GNote.TRUE);
        GNoteDB db = GNoteDB.getInstance(context);
        db.updateGNote(gNote);

        int no = intent.getIntExtra("no", 0);
        LogUtil.i("receiver", "no:" + no);

        Intent mIntent = new Intent(context, NoteActivity.class);
        mIntent.putExtra("gAsstNote_data", gNote);
        mIntent.putExtra("no", no);
        mIntent.putExtra("mode", NoteActivity.MODE_SHOW);
        PendingIntent pi = PendingIntent.getActivity(context, no, mIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new Notification.Builder(context)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentTitle(Util.timeString(gNote))
                .setContentText(gNote.getNote())
                .setSmallIcon(R.drawable.small_logo)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.logo))
                .setTicker(gNote.getNote())
                .setContentIntent(pi)
                .build();
        notification.flags |= Notification.FLAG_INSISTENT; // 声音一直响到用户相应，就是通知会一直响起，直到你触碰通知栏的时间就会停止

        manager.notify(no, notification);

        AlarmService.reduceTask(context);
    }

}
