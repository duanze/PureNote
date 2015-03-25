package com.duanze.gasst.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.duanze.gasst.db.GNoteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class GNoteDB {
    /**
     * 数据库名
     */
    public static final String DB_NAME = "db_gnote";
    public static final String TABLE_NAME = "table_gnote";

    public static final String ID = "id";
    public static final String TIME = "time";
    public static final String ALERT_TIME = "alert_time";
    public static final String IS_PASSED = "is_passed";
    public static final String CONTENT = "content";
    public static final String IS_DONE = "done";
    public static final String COLOR = "color";
    public static final String EDIT_TIME = "edit_time";
    public static final String CREATED_TIME = "created_time";
    public static final String SYN_STATUS = "syn_status";
    public static final String GUID = "guid";

    /**
     * 数据库版本
     */
    public static final int VERSION = 1;

    private static GNoteDB gNoteDB;
    private SQLiteDatabase db;

    private GNoteDB(Context context) {
        GNoteOpenHelper openHelper = new GNoteOpenHelper(context, DB_NAME, null, VERSION);
        db = openHelper.getWritableDatabase();
    }

    /**
     * 获取GAsstNoteDB实例，注意 同步 限定
     */
    public synchronized static GNoteDB getInstance(Context context) {
        if (gNoteDB == null) {
            gNoteDB = new GNoteDB(context);
        }
        return gNoteDB;
    }

    /**
     * 将GAsstNote实例存入数据库
     */
    public void saveGNote(GNote gNote) {
        if (gNote != null) {
            ContentValues values = new ContentValues();
            values.put(TIME, gNote.getTime());
            values.put(ALERT_TIME, gNote.getAlertTime());
            values.put(IS_PASSED, gNote.getPassed());
            values.put(CONTENT, gNote.getNote());
            values.put(IS_DONE, gNote.getDone());
            values.put(COLOR, gNote.getColor());
            values.put(EDIT_TIME, gNote.getEditTime());
            values.put(CREATED_TIME, gNote.getCreatedTime());
            values.put(SYN_STATUS, gNote.getSynStatus());
            values.put(GUID, gNote.getGuid());

            db.insert(TABLE_NAME, null, values);
        }
    }

    /**
     * 从数据库中读取GAsstNote数据
     */
    public List<GNote> loadGNotes() {
        List<GNote> list = new ArrayList<GNote>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, "time desc");
        if (cursor.moveToFirst()) {
            do {
                GNote gNote = new GNote();
                gNote.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                gNote.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
                gNote.setAlertTime(cursor.getString(cursor.getColumnIndex(ALERT_TIME)));
                gNote.setPassed(cursor.getInt(cursor.getColumnIndex(IS_PASSED)));
                gNote.setNote(cursor.getString(cursor.getColumnIndex(CONTENT)));
                gNote.setDone(cursor.getInt(cursor.getColumnIndex(IS_DONE)));
                gNote.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
                gNote.setEditTime(cursor.getLong(cursor.getColumnIndex(EDIT_TIME)));
                gNote.setCreatedTime(cursor.getLong(cursor.getColumnIndex(CREATED_TIME)));
                gNote.setSynStatus(cursor.getInt(cursor.getColumnIndex(SYN_STATUS)));
                gNote.setGuid(cursor.getString(cursor.getColumnIndex(GUID)));

                list.add(gNote);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    /**
     * 得到最新一个GAsstNote的id
     * 失败返回-1
     */
    public int getNewestGNoteId() {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, "id desc");
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        if (cursor != null) {
            cursor.close();
        }
        return id;
    }

    /**
     * 通过id查找特定Note失败返回null
     *
     * @param id
     * @return
     */
    public GNote getGNoteById(int id) {
        Cursor cursor = db.query(TABLE_NAME, null, "id = ?", new String[]{"" + id}, null, null, null);
        if (cursor.moveToFirst()) {
            GNote gNote = new GNote();
            gNote.setId(cursor.getInt(cursor.getColumnIndex(ID)));
            gNote.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
            gNote.setAlertTime(cursor.getString(cursor.getColumnIndex(ALERT_TIME)));
            gNote.setPassed(cursor.getInt(cursor.getColumnIndex(IS_PASSED)));
            gNote.setNote(cursor.getString(cursor.getColumnIndex(CONTENT)));
            gNote.setDone(cursor.getInt(cursor.getColumnIndex(IS_DONE)));
            gNote.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
            gNote.setEditTime(cursor.getLong(cursor.getColumnIndex(EDIT_TIME)));
            gNote.setCreatedTime(cursor.getLong(cursor.getColumnIndex(CREATED_TIME)));
            gNote.setSynStatus(cursor.getInt(cursor.getColumnIndex(SYN_STATUS)));
            gNote.setGuid(cursor.getString(cursor.getColumnIndex(GUID)));

            return gNote;
        }
        return null;
    }

    /**
     * 根据主键删除GAsstNote数据
     */
    public boolean deleteGNote(int id) {
        if (db.delete(TABLE_NAME, "id = ?", new String[]{"" + id}) == 1) {
            return true;
        }
        return false;
    }

    /**
     * 根据主键更新GAsstNote数据
     */
    public boolean updateGNote(GNote gNote) {
        ContentValues values = new ContentValues();
        values.put(TIME, gNote.getTime());
        values.put(ALERT_TIME, gNote.getAlertTime());
        values.put(IS_PASSED, gNote.getPassed());
        values.put(CONTENT, gNote.getNote());
        values.put(IS_DONE, gNote.getDone());
        values.put(COLOR, gNote.getColor());
        values.put(EDIT_TIME, gNote.getEditTime());
        values.put(CREATED_TIME, gNote.getCreatedTime());
        values.put(SYN_STATUS, gNote.getSynStatus());
        values.put(GUID, gNote.getGuid());

        if (db.update(TABLE_NAME, values, "id = ?", new String[]{"" + gNote.getId()}) == 1) {
            return true;
        }
        return false;
    }
}
