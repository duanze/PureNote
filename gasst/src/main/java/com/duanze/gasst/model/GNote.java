package com.duanze.gasst.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.Util;
import com.duanze.gasst.view.GridUnit;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.edam.type.Note;

import java.util.Calendar;

public class GNote implements Parcelable {
    public static final String TAG = "GNote";

    public static final int TRUE = 1;
    public static final int FALSE = 0;

    public static final int NOTHING = 0;
    public static final int NEW = 1;
    public static final int UPDATE = 2;
    public static final int DELETE = 3;

    private int id = -1;
    private String time = "";
    private String alertTime = "";
    private int isPassed = TRUE;
    private String content = "";
    private int done = FALSE;
    private int color = GridUnit.colorArr[0];//初始透明
    private long editTime = 0;//最后编辑时间
    private long createdTime = 0;//创建时间
    private int synStatus = NOTHING;//同步状态，仅登录EverNote后有效
    private String guid = "";//evernote 服务器创建的标志符，惟一确定一条note
    private String bookGuid = "";

    //数据表中笔记本的id号，为0时使用默认笔记本PureNote
    private int gNoteBookId = 0;
    private int deleted = FALSE;

    public GNote() {

    }

    public GNote(Cursor cursor) {
//        因为不知道主键规定而产生的悲剧-_-||
        id = cursor.getInt(cursor.getColumnIndex("_id"));
        time = cursor.getString(cursor.getColumnIndex(GNoteDB.TIME));
        alertTime = cursor.getString(cursor.getColumnIndex(GNoteDB.ALERT_TIME));
        isPassed = cursor.getInt(cursor.getColumnIndex(GNoteDB.IS_PASSED));
        content = cursor.getString(cursor.getColumnIndex(GNoteDB.CONTENT));
        done = cursor.getInt(cursor.getColumnIndex(GNoteDB.IS_DONE));
        color = cursor.getInt(cursor.getColumnIndex(GNoteDB.COLOR));
        editTime = cursor.getLong(cursor.getColumnIndex(GNoteDB.EDIT_TIME));
        createdTime = cursor.getLong(cursor.getColumnIndex(GNoteDB.CREATED_TIME));
        synStatus = cursor.getInt(cursor.getColumnIndex(GNoteDB.SYN_STATUS));
        guid = cursor.getString(cursor.getColumnIndex(GNoteDB.GUID));
        bookGuid = cursor.getString(cursor.getColumnIndex(GNoteDB.BOOK_GUID));
        deleted = cursor.getInt(cursor.getColumnIndex(GNoteDB.DELETED));
        gNoteBookId = cursor.getInt(cursor.getColumnIndex(GNoteDB.GNOTEBOOK_ID));
    }

    public ContentValues toContentValues() {
        ContentValues values = toInsertContentValues();
        values.put(GNoteDB.ID, id);
        return values;
    }

    public ContentValues toInsertContentValues() {
        ContentValues values = new ContentValues();
        values.put(GNoteDB.TIME, time);
        values.put(GNoteDB.ALERT_TIME, alertTime);
        values.put(GNoteDB.IS_PASSED, isPassed);
        values.put(GNoteDB.CONTENT, content);
        values.put(GNoteDB.IS_DONE, done);
        values.put(GNoteDB.COLOR, color);
        values.put(GNoteDB.EDIT_TIME, editTime);
        values.put(GNoteDB.CREATED_TIME, createdTime);
        values.put(GNoteDB.SYN_STATUS, synStatus);
        values.put(GNoteDB.GUID, guid);
        values.put(GNoteDB.BOOK_GUID, bookGuid);
        values.put(GNoteDB.DELETED, deleted);
        values.put(GNoteDB.GNOTEBOOK_ID, gNoteBookId);
        return values;
    }


    public int getGNotebookId() {
        return gNoteBookId;
    }

    public void setGNotebookId(int gnotebookId) {
        this.gNoteBookId = gnotebookId;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public String getBookGuid() {
        return bookGuid;
    }

    public void setBookGuid(String bookGuid) {
        this.bookGuid = bookGuid;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public boolean getIsPassed() {
        return isPassed == TRUE;
    }

    public boolean isDone() {
        return done == TRUE;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public long getEditTime() {
        return editTime;
    }

    public void setEditTime(long editTime) {
        this.editTime = editTime;
    }

    public int getSynStatus() {
        return synStatus;
    }

    public void setSynStatus(int synStatus) {
        this.synStatus = synStatus;
    }

    public int getDone() {
        return done;
    }

    public void setDone(int done) {
        this.done = done;
    }

    public int getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getAlertTime() {
        return alertTime;
    }

    public int getPassed() {
        return isPassed;
    }

    public Spanned getNoteFromHtml() {
        //莫名错误预防
        if (content == null) {
            content = "";
        }
        return Html.fromHtml(content);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setIsPassed(int isPassed) {
        this.isPassed = isPassed;
    }

    public void setAlertTime(String alertTime) {
        this.alertTime = alertTime;
    }

    public void setNoteToHtml(Spanned note) {
        this.content = Html.toHtml(note);
    }

    public void setContent(String tmp) {
        content = tmp;
    }

    public String getContent() {
        return content;
    }

    public boolean needUpdate() {
        return synStatus == UPDATE;
    }

    public boolean needDelete() {
        return synStatus == DELETE;
    }

    public boolean needCreate() {
        return synStatus == NEW;
    }

    public boolean isDeleted() {
        return deleted == TRUE;
    }

    public Note toNote() {
        Note note = new Note();
        note.setTitle("PureNote " + Util.timeStamp(this));
        note.setContent(convertContentToEvernote());
        if (!"".equals(bookGuid)) {
            note.setNotebookGuid(bookGuid);
        }
        if (!"".equals(guid)) {
            note.setGuid(guid);
        }
        return note;
    }

    public void setTimeFromDate(int year, int month, int day) {
        time = year
                + ","
                + Util.twoDigit(month)
                + ","
                + Util.twoDigit(day);
    }

    public static GNote parseGNote(Note n) {
        GNote gNote = new GNote();
        gNote.content = n.getContent();
        gNote.guid = n.getGuid();
        gNote.bookGuid = n.getNotebookGuid();
        gNote.createdTime = n.getCreated();
        gNote.editTime = n.getUpdated();

        boolean setTime = false;
        String title = n.getTitle();
        String[] tmp = title.split(" ");
        if (tmp.length == 2) {
            String[] allInfo = tmp[1].split("\\.");
            if (allInfo.length == 3) {
                gNote.setTime(Util.parseTimeStamp(allInfo));
                setTime = true;
            }
        }
        if (!setTime) {
            Calendar today = Calendar.getInstance();
            gNote.setCalToTime(today);
        }
        return gNote;
    }

    public Note toDeleteNote() {
        Note note = new Note();
        note.setGuid(guid);
        return note;
    }

    private String convertContentToEvernote() {
        String EvernoteContent = EvernoteUtil.NOTE_PREFIX
                + content.replace("<br>", "<br/>")
                + EvernoteUtil.NOTE_SUFFIX;
        LogUtil.i(TAG, "同步文字:" + EvernoteContent);
        return EvernoteContent;
    }

    public static GNote buildFromContentValues(ContentValues values) {
        GNote gNote = new GNote();
        if (values.containsKey(GNoteDB.ID)) {
            gNote.id = values.getAsInteger(GNoteDB.ID);
        }
        gNote.time = values.getAsString(GNoteDB.TIME);
        gNote.alertTime = values.getAsString(GNoteDB.ALERT_TIME);
        gNote.isPassed = values.getAsInteger(GNoteDB.IS_PASSED);
        gNote.content = values.getAsString(GNoteDB.CONTENT);
        gNote.done = values.getAsInteger(GNoteDB.IS_DONE);
        gNote.color = values.getAsInteger(GNoteDB.COLOR);
        gNote.editTime = values.getAsLong(GNoteDB.EDIT_TIME);
        gNote.createdTime = values.getAsLong(GNoteDB.CREATED_TIME);
        gNote.synStatus = values.getAsInteger(GNoteDB.SYN_STATUS);
        gNote.guid = values.getAsString(GNoteDB.GUID);
        gNote.bookGuid = values.getAsString(GNoteDB.BOOK_GUID);
        gNote.deleted = values.getAsInteger(GNoteDB.DELETED);
        gNote.gNoteBookId = values.getAsInteger(GNoteDB.GNOTEBOOK_ID);
        return gNote;
    }

    /**
     * 与一个Calendar进行日期（年月日）比较，小于返回-1，等于返回0，大于返回1
     *
     * @param calendar
     * @return 比较的结果
     */
    public int compareToCalendar(Calendar calendar) {
        String[] allInfo;
        allInfo = time.split(",");
        int mYear = Integer.parseInt(allInfo[0]);
        int mMonth = Integer.parseInt(allInfo[1]);
        int mDay = Integer.parseInt(allInfo[2]);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (mYear < year ||
                mYear == year && mMonth < month ||
                mYear == year && mMonth == month && mDay < day) {
            return -1;
        } else if (mYear == year && mMonth == month && mDay == day) {
            return 0;
        }
        return 1;
    }

    /**
     * 以calendar为日期给note的time赋值
     */
    public void setCalToTime(Calendar calendar) {
        setTime(calendar.get(Calendar.YEAR)
                + ","
                + Util.twoDigit(calendar.get(Calendar.MONTH))
                + ","
                + Util.twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(time);
        parcel.writeString(alertTime);
        parcel.writeInt(isPassed);
        parcel.writeString(content);
        parcel.writeInt(done);
        parcel.writeInt(color);
        parcel.writeLong(editTime);
        parcel.writeLong(createdTime);
        parcel.writeInt(synStatus);
        parcel.writeString(guid);
        parcel.writeString(bookGuid);
        parcel.writeInt(deleted);
        parcel.writeInt(gNoteBookId);
    }

    public static final Creator<GNote> CREATOR = new Creator<GNote>() {
        @Override
        public GNote createFromParcel(Parcel parcel) {
            GNote gNote = new GNote();
            gNote.id = parcel.readInt();
            gNote.time = parcel.readString();
            gNote.alertTime = parcel.readString();
            gNote.isPassed = parcel.readInt();
            gNote.content = parcel.readString();
            gNote.done = parcel.readInt();
            gNote.color = parcel.readInt();
            gNote.editTime = parcel.readLong();
            gNote.createdTime = parcel.readLong();
            gNote.synStatus = parcel.readInt();
            gNote.guid = parcel.readString();
            gNote.bookGuid = parcel.readString();
            gNote.deleted = parcel.readInt();
            gNote.gNoteBookId = parcel.readInt();
            return gNote;
        }

        @Override
        public GNote[] newArray(int i) {
            return new GNote[i];
        }
    };
}
