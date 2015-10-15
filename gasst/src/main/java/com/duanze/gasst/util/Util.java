package com.duanze.gasst.util;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.duanze.gasst.R;
import com.duanze.gasst.activity.Folder;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.provider.GNoteProvider;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Util {
    public static final String[] MONTH_ARR = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
            "Aug", "Sep", "Oct", "Nov", "Dec"};
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

    public static String alertTimeStamp(GNote gNote) {
        String tmp = "";
        String[] allInfo;
        allInfo = gNote.getAlertTime().split(",");
        if (allInfo.length == 5) {
            tmp = twoDigit(Integer.parseInt(allInfo[1]) + 1)
                    + "."
                    + allInfo[2]
                    + " - "
                    + allInfo[3]
                    + ":"
                    + allInfo[4];
        }
        return tmp;
    }

    public static String timeStamp(GNote gNote) {
        String tmp = "";
        String[] allInfo;
        allInfo = gNote.getTime().split(",");
        //不知原因的数组越界，故暂时在此进行检测
        if (allInfo.length == 3 && (Integer.parseInt(allInfo[2]) >= 1 && Integer.parseInt
                (allInfo[2]) <= 31)) {
            tmp = MONTH_ARR[Integer.parseInt(allInfo[1])]
                    + "."
                    + DATE_ARR[Integer.parseInt(allInfo[2]) - 1]
                    + "."
                    + allInfo[0];
        }
        return tmp;
    }

    public static String parseTimeStamp(String[] info) {
        String year = info[2];
        int month = 0;
        int day = 1;

        for (int i = 0; i < MONTH_ARR.length; i++) {
            String m = MONTH_ARR[i];
            if (m.equals(info[0])) {
                month = i;
                break;
            }
        }

        for (int i = 0; i < DATE_ARR.length; i++) {
            String d = DATE_ARR[i];
            if (d.equals(info[1])) {
                day = i + 1;
                break;
            }
        }

        return year
                + ","
                + Util.twoDigit(month)
                + ","
                + Util.twoDigit(day);
    }

    /**
     * 随机设置fab背景色
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

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 闪电摘录
     *
     * @param preferences
     * @param db
     * @param str
     * @param groupId
     * @param context
     * @return
     */
    public static String extractNote(SharedPreferences preferences, GNoteDB db, String str,
                                     int groupId, Context context) {
        boolean find = false;
        String groupName;
        if (groupId != 0) {
            GNotebook gNotebook = db.getGNotebookById(groupId);
            if (gNotebook != null) {
                groupName = gNotebook.getName();
                find = true;
            } else {
                groupName = context.getResources().getString(R.string.all_notes);
            }
        } else {
            groupName = context.getResources().getString(R.string.all_notes);
        }
        if (find) {
            extractNoteToDB(context, preferences, db, str, groupId);
        } else {
            extractNoteToDB(context, preferences, db, str, 0);
        }
        return groupName;
    }

    /**
     * 闪电摘录，真实存入db与更新笔记本数量
     *
     * @param preferences
     * @param db
     * @param str
     * @param groupId
     */
    public static void extractNoteToDB(Context context, SharedPreferences preferences, GNoteDB db, String str, int groupId) {
        Calendar cal = Calendar.getInstance();
        GNote gNote = new GNote();
        gNote.setCalToTime(cal);
        gNote.setContent(str);
        //needCreate
        gNote.setSynStatus(GNote.NEW);
//        设置笔记本id
        gNote.setGNotebookId(groupId);

        gNote.setEditTime(TimeUtils.getCurrentTimeInLong());
//        db.saveGNote(gNote);
        ProviderUtil.insertGNote(context, gNote);

//        更新笔记本
        updateGNotebook(preferences, db, groupId, +1, false);
        if (groupId != 0) {
            updateGNotebook(preferences, db, 0, +1, false);
        }
    }

    private static void updateGNotebook(SharedPreferences preferences, GNoteDB db, int groupId, int value,
                                        boolean isMove) {
        if (groupId == 0) {
            //如果是移动文件，不加不减
            if (isMove) return;
            int cnt = preferences.getInt(Folder.PURENOTE_NOTE_NUM, 3);
            preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, cnt + value).apply();
        } else {

//            List<GNotebook> gNotebooks = db.loadGNotebooks();
//            for (GNotebook gNotebook : gNotebooks) {
//                if (gNotebook.getId() == groupId) {
//                    int cnt = gNotebook.getNotesNum();
//                    gNotebook.setNotesNum(cnt + value);
//
//                    db.updateGNotebook(gNotebook);
//                    break;
//                }
//            }

            GNotebook gNotebook = db.getGNotebookById(groupId);
            if (null != gNotebook) {
                int cnt = gNotebook.getNotesNum();
                gNotebook.setNotesNum(cnt + value);
                db.updateGNotebook(gNotebook);
            } else {
                throw new IllegalArgumentException("Invalid groupId!");
            }
        }
    }

    public static String readSaveLocation(String lo, SharedPreferences preferences, GNoteDB db,
                                          Context mContext) {
        int extractLocation = preferences.getInt(lo, 0);
        List<GNotebook> list = db.loadGNotebooks();
        String extractGroup = "";

        if (0 != extractLocation) {
            boolean find = false;
            for (GNotebook gNotebook : list) {
                if (extractLocation == gNotebook.getId()) {
                    extractGroup = gNotebook.getName();
                    find = true;
                    break;
                }
            }
            if (!find) {
                Toast.makeText(mContext, R.string.read_save_location_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            extractGroup = mContext.getResources().getString(R.string.all_notes);
        }

        return extractGroup;
    }

    public static void feedback(Context mContext) {
        // 必须明确使用mailto前缀来修饰邮件地址
        Uri uri = Uri.parse("mailto:端泽<blue3434@qq.com>");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        // intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, "PureNote用户反馈" + " Version:" + getVersionName(mContext));
        // 主题
        intent.putExtra(Intent.EXTRA_TEXT, "Manufacturer:" + Build.MANUFACTURER +
                " - Device name: " + Build.MODEL + " - SDK Version: " + Build.VERSION.SDK_INT+"  "); // 正文
        mContext.startActivity(Intent.createChooser(intent, "Select email client"));
    }

    public static String getVersionName(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "1.0.0";
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
