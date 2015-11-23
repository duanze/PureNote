package com.duanze.gasst.util;

import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.duanze.gasst.R;
import com.duanze.gasst.data.model.GNote;
import com.duanze.gasst.data.model.GNoteDB;
import com.duanze.gasst.data.model.GNotebook;
import com.duanze.gasst.data.provider.GNoteProvider;
import com.duanze.gasst.util.liteprefs.MyLitePrefs;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Util {
    public static final String TAG = Util.class.getSimpleName();

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
                    + "-"
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
            tmp = allInfo[0]
                    + "."
                    + (Integer.parseInt(allInfo[1]) + 1)
                    + "."
                    + Integer.parseInt(allInfo[2]);
        }
        return tmp;
    }

    public static String parseTimeStamp(String[] info) {
        if (info.length != 3) {
            throw new IllegalStateException("the length of arg {String[] info} must be 3");
        }

        return info[0]
                + ","
                + Util.twoDigit(Integer.parseInt(info[1]) - 1)
                + ","
                + Util.twoDigit(info[2]);
    }

    public static int[] parseDateFromGNote(GNote gNote) {
        int[] date = new int[3];
        String[] allInfo = gNote.getTime().split(",");
        if (allInfo.length == 3) {
            date[0] = Integer.parseInt(allInfo[0]);
            date[1] = Integer.parseInt(allInfo[1]);
            date[2] = Integer.parseInt(allInfo[2]);
        }

        if (11 < date[1] || date[1] < 0) {
            date[1] = 0;
        }
        if (31 < date[2] || date[2] < 1) {
            date[2] = 1;
        }
        return date;
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
     */
    public static String extractNote(String str, int groupId, Context context) {
        boolean find = false;
        String groupName;
        if (groupId != 0) {
            Cursor cursor = context.getContentResolver().query(
                    ContentUris.withAppendedId(GNoteProvider.NOTEBOOK_URI, groupId),
                    GNoteProvider.NOTEBOOK_PROJECTION, null, null, null);
            GNotebook gNotebook = null;
            if (null != cursor && cursor.getCount() > 0) {
                cursor.moveToFirst();
                gNotebook = new GNotebook(cursor);
            }
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
            extractNoteToDB(context, str, groupId);
        } else {
            extractNoteToDB(context, str, 0);
        }
        return groupName;
    }

    /**
     * 闪电摘录，真实存入db与更新笔记本数量
     */
    public static void extractNoteToDB(Context mContext, String str, int groupId) {
        Calendar today = Calendar.getInstance();
        GNote gNote = new GNote();
        gNote.setCalToTime(today);
        gNote.setContent(str);
        //needCreate
        gNote.setSynStatus(GNote.NEW);
//        设置笔记本id
        gNote.setGNotebookId(groupId);

        gNote.setEditTime(TimeUtils.getCurrentTimeInLong());
        ProviderUtil.insertGNote(mContext, gNote);

//        更新笔记本
//        updateGNotebook(mContext, db, groupId, +1, false);
//        if (groupId != 0) {
//            updateGNotebook(mContext, db, 0, +1, false);
//        }

        GNotebookUtil.updateGNotebook(mContext, groupId, +1);
        if (0 != groupId) {
            GNotebookUtil.updateGNotebook(mContext, 0, +1);
        }
    }

    private static void updateGNotebook(Context mContext, GNoteDB db, int groupId, int value,
                                        boolean isMove) {
        if (groupId == 0) {
            //如果是移动文件，不加不减
            if (isMove) return;
            int num = MyLitePrefs.getInt(MyLitePrefs.PURENOTE_NOTE_NUM);
            MyLitePrefs.putInt(MyLitePrefs.PURENOTE_NOTE_NUM, num + value);
        } else {
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

    public static String readSaveLocation(String key, Context context) {
        int extractLocation = MyLitePrefs.getInt(key);
        List<GNotebook> list = GNoteDB.getInstance(context).loadGNotebooks();
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
                Toast.makeText(context, R.string.read_save_location_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            extractGroup = context.getResources().getString(R.string.all_notes);
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
                " - Device name: " + Build.MODEL + " - SDK Version: " + Build.VERSION.SDK_INT + "  "); // 正文
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

    public static void wordCount(String str, int[] res) {
        if (res.length < 3) {
            throw new IllegalStateException("the arg {int[] res} length must >= 3");
        }

//        计算 字数 = 英文单词 + 中文字
        boolean finding = false;//是否正在寻找单词结尾
        int engCnt = 0;
        int gbkCnt = 0;
        int spaceCnt = 0;//空白字符
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isDigit(c)
                    || 'a' <= (int) c && (int) c <= 'z'
                    || 'A' <= (int) c && (int) c <= 'Z') {
                if (finding) {
//                    连成一个单词
                } else {
                    finding = true;
                }
            } else if (Character.isSpaceChar(c)) {
                if (finding) {
                    finding = false;
                    engCnt++;
                }
                spaceCnt++;
            } else if (0x4e00 <= (int) c && (int) c <= 0x9fa5) {
                if (finding) {
                    finding = false;
                    engCnt++;
                }
                gbkCnt++;
            } else {
                if (finding) {
                    finding = false;
                    engCnt++;
                }
            }
        }
        //最后一个
        if (finding) {
            engCnt++;
        }

        res[2] = str.length();
        res[1] = res[2] - spaceCnt;
        res[0] = engCnt + gbkCnt;
    }
}
