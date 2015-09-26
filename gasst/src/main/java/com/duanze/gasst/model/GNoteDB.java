package com.duanze.gasst.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.duanze.gasst.db.GNoteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class GNoteDB {
    public static final String TAG = "GNoteDB";
    /**
     * 数据库名
     */
    public static final String DB_NAME = "db_gnote";

    public static final String TABLE_NAME = "table_gnote";
    public static final String TABLE_NOTEBOOK = "table_gnotebook";

    // common
    public static final String ID = "id";
    public static final String SYN_STATUS = "syn_status";
    public static final String DELETED = "deleted";

    //    table_gnote
    public static final String TIME = "time";
    public static final String ALERT_TIME = "alert_time";
    public static final String IS_PASSED = "is_passed";
    public static final String CONTENT = "content";
    public static final String IS_DONE = "done";
    public static final String COLOR = "color";
    public static final String EDIT_TIME = "edit_time";
    public static final String CREATED_TIME = "created_time";

    public static final String GUID = "guid";
    public static final String BOOK_GUID = "book_guid";
    public static final String GNOTEBOOK_ID = "gnotebook_id";

    //    table_gnotebook
    public static final String NAME = "name";
    public static final String NOTEBOOK_GUID = "notebook_guid";
    public static final String NOTES_NUM = "num";
    public static final String SELECTED = "selected";


    /**
     * 数据库版本
     */
    public static final int VERSION = 2;

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
            values.put(CONTENT, gNote.getContent());
            values.put(IS_DONE, gNote.getDone());
            values.put(COLOR, gNote.getColor());
            values.put(EDIT_TIME, gNote.getEditTime());
            values.put(CREATED_TIME, gNote.getCreatedTime());
            values.put(SYN_STATUS, gNote.getSynStatus());
            values.put(GUID, gNote.getGuid());
            values.put(BOOK_GUID, gNote.getBookGuid());
            values.put(DELETED, gNote.getDeleted());
            values.put(GNOTEBOOK_ID, gNote.getGNotebookId());

            db.insert(TABLE_NAME, null, values);
        }
    }

    /**
     * 从数据库中读取GAsstNote数据
     */
    public List<GNote> loadGNotes() {
        List<GNote> list = new ArrayList<GNote>();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                SYN_STATUS + " != ?" + " and " + DELETED + " != ?",
                new String[]{"" + GNote.DELETE, "" + GNote.TRUE},
                null,
                null,
                "time desc");

        if (cursor.moveToFirst()) {
            do {
                GNote gNote = new GNote();
                gNote.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                gNote.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
                gNote.setAlertTime(cursor.getString(cursor.getColumnIndex(ALERT_TIME)));
                gNote.setIsPassed(cursor.getInt(cursor.getColumnIndex(IS_PASSED)));
                gNote.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
                gNote.setDone(cursor.getInt(cursor.getColumnIndex(IS_DONE)));
                gNote.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
                gNote.setEditTime(cursor.getLong(cursor.getColumnIndex(EDIT_TIME)));
                gNote.setCreatedTime(cursor.getLong(cursor.getColumnIndex(CREATED_TIME)));
                gNote.setSynStatus(cursor.getInt(cursor.getColumnIndex(SYN_STATUS)));
                gNote.setGuid(cursor.getString(cursor.getColumnIndex(GUID)));
                gNote.setBookGuid(cursor.getString(cursor.getColumnIndex(BOOK_GUID)));
                gNote.setDeleted(cursor.getInt(cursor.getColumnIndex(DELETED)));
                gNote.setGNotebookId(cursor.getInt(cursor.getColumnIndex(GNOTEBOOK_ID)));

                list.add(gNote);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public List<GNote> loadGNotesByBookId(int id) {
        List<GNote> list = new ArrayList<GNote>();

        //当载入全部笔记时
        if (id == 0) {
            return loadGNotes();
        }

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                GNOTEBOOK_ID + " = ? and " + SYN_STATUS + " != ?" + " and " + DELETED + " != ?",
                new String[]{"" + id, "" + GNote.DELETE, "" + GNote.TRUE},
                null,
                null,
                "time desc");

        if (cursor.moveToFirst()) {
            do {
                GNote gNote = new GNote();
                gNote.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                gNote.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
                gNote.setAlertTime(cursor.getString(cursor.getColumnIndex(ALERT_TIME)));
                gNote.setIsPassed(cursor.getInt(cursor.getColumnIndex(IS_PASSED)));
                gNote.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
                gNote.setDone(cursor.getInt(cursor.getColumnIndex(IS_DONE)));
                gNote.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
                gNote.setEditTime(cursor.getLong(cursor.getColumnIndex(EDIT_TIME)));
                gNote.setCreatedTime(cursor.getLong(cursor.getColumnIndex(CREATED_TIME)));
                gNote.setSynStatus(cursor.getInt(cursor.getColumnIndex(SYN_STATUS)));
                gNote.setGuid(cursor.getString(cursor.getColumnIndex(GUID)));
                gNote.setBookGuid(cursor.getString(cursor.getColumnIndex(BOOK_GUID)));
                gNote.setDeleted(cursor.getInt(cursor.getColumnIndex(DELETED)));
                gNote.setGNotebookId(cursor.getInt(cursor.getColumnIndex(GNOTEBOOK_ID)));

                list.add(gNote);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public List<GNote> loadRawGNotes() {
        List<GNote> list = new ArrayList<GNote>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, "time desc");
        if (cursor.moveToFirst()) {
            do {
                GNote gNote = new GNote();
                gNote.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                gNote.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
                gNote.setAlertTime(cursor.getString(cursor.getColumnIndex(ALERT_TIME)));
                gNote.setIsPassed(cursor.getInt(cursor.getColumnIndex(IS_PASSED)));
                gNote.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
                gNote.setDone(cursor.getInt(cursor.getColumnIndex(IS_DONE)));
                gNote.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
                gNote.setEditTime(cursor.getLong(cursor.getColumnIndex(EDIT_TIME)));
                gNote.setCreatedTime(cursor.getLong(cursor.getColumnIndex(CREATED_TIME)));
                gNote.setSynStatus(cursor.getInt(cursor.getColumnIndex(SYN_STATUS)));
                gNote.setGuid(cursor.getString(cursor.getColumnIndex(GUID)));
                gNote.setBookGuid(cursor.getString(cursor.getColumnIndex(BOOK_GUID)));
                gNote.setDeleted(cursor.getInt(cursor.getColumnIndex(DELETED)));
                gNote.setGNotebookId(cursor.getInt(cursor.getColumnIndex(GNOTEBOOK_ID)));

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
            gNote.setIsPassed(cursor.getInt(cursor.getColumnIndex(IS_PASSED)));
            gNote.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
            gNote.setDone(cursor.getInt(cursor.getColumnIndex(IS_DONE)));
            gNote.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
            gNote.setEditTime(cursor.getLong(cursor.getColumnIndex(EDIT_TIME)));
            gNote.setCreatedTime(cursor.getLong(cursor.getColumnIndex(CREATED_TIME)));
            gNote.setSynStatus(cursor.getInt(cursor.getColumnIndex(SYN_STATUS)));
            gNote.setGuid(cursor.getString(cursor.getColumnIndex(GUID)));
            gNote.setBookGuid(cursor.getString(cursor.getColumnIndex(BOOK_GUID)));
            gNote.setDeleted(cursor.getInt(cursor.getColumnIndex(DELETED)));
            gNote.setGNotebookId(cursor.getInt(cursor.getColumnIndex(GNOTEBOOK_ID)));

            cursor.close();
            return gNote;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public GNote getGNoteByGuid(String guid) {
        Cursor cursor = db.query(TABLE_NAME, null, "guid = ?", new String[]{guid}, null, null,
                null);
        if (cursor.moveToFirst()) {
            GNote gNote = new GNote();
            gNote.setId(cursor.getInt(cursor.getColumnIndex(ID)));
            gNote.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
            gNote.setAlertTime(cursor.getString(cursor.getColumnIndex(ALERT_TIME)));
            gNote.setIsPassed(cursor.getInt(cursor.getColumnIndex(IS_PASSED)));
            gNote.setContent(cursor.getString(cursor.getColumnIndex(CONTENT)));
            gNote.setDone(cursor.getInt(cursor.getColumnIndex(IS_DONE)));
            gNote.setColor(cursor.getInt(cursor.getColumnIndex(COLOR)));
            gNote.setEditTime(cursor.getLong(cursor.getColumnIndex(EDIT_TIME)));
            gNote.setCreatedTime(cursor.getLong(cursor.getColumnIndex(CREATED_TIME)));
            gNote.setSynStatus(cursor.getInt(cursor.getColumnIndex(SYN_STATUS)));
            gNote.setGuid(cursor.getString(cursor.getColumnIndex(GUID)));
            gNote.setBookGuid(cursor.getString(cursor.getColumnIndex(BOOK_GUID)));
            gNote.setDeleted(cursor.getInt(cursor.getColumnIndex(DELETED)));
            gNote.setGNotebookId(cursor.getInt(cursor.getColumnIndex(GNOTEBOOK_ID)));

            cursor.close();
            return gNote;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    /**
     * 根据主键删除GAsstNote数据
     */
    public boolean deleteGNote(int id) {
        return db.delete(TABLE_NAME, "id = ?", new String[]{"" + id}) == 1;
    }

    /**
     * 根据主键更新GAsstNote数据
     */
    public boolean updateGNote(GNote gNote) {
        ContentValues values = new ContentValues();
        values.put(TIME, gNote.getTime());
        values.put(ALERT_TIME, gNote.getAlertTime());
        values.put(IS_PASSED, gNote.getPassed());
        values.put(CONTENT, gNote.getContent());
        values.put(IS_DONE, gNote.getDone());
        values.put(COLOR, gNote.getColor());
        values.put(EDIT_TIME, gNote.getEditTime());
        values.put(CREATED_TIME, gNote.getCreatedTime());
        values.put(SYN_STATUS, gNote.getSynStatus());
        values.put(GUID, gNote.getGuid());
        values.put(BOOK_GUID, gNote.getBookGuid());
        values.put(DELETED, gNote.getDeleted());
        values.put(GNOTEBOOK_ID, gNote.getGNotebookId());

        return db.update(TABLE_NAME, values, "id = ?", new String[]{"" + gNote.getId()}) == 1;
    }

    //    以下为数据表table_gnotebook相关
    public void saveGNotebook(GNotebook gNotebook) {
        if (gNotebook != null) {
            ContentValues values = new ContentValues();
            values.put(NAME, gNotebook.getName());
            values.put(SYN_STATUS, gNotebook.getSynStatus());
            values.put(NOTEBOOK_GUID, gNotebook.getNotebookGuid());
            values.put(DELETED, gNotebook.getDeleted());
            values.put(NOTES_NUM, gNotebook.getNotesNum());
            values.put(SELECTED, gNotebook.getSelected());

            db.insert(TABLE_NOTEBOOK, null, values);
        }
    }

    public boolean updateGNotebook(GNotebook gNotebook) {
        ContentValues values = new ContentValues();
        values.put(NAME, gNotebook.getName());
        values.put(SYN_STATUS, gNotebook.getSynStatus());
        values.put(NOTEBOOK_GUID, gNotebook.getNotebookGuid());
        values.put(DELETED, gNotebook.getDeleted());
        values.put(NOTES_NUM, gNotebook.getNotesNum());
        values.put(SELECTED, gNotebook.getSelected());

        return db.update(TABLE_NOTEBOOK, values, "id = ?", new String[]{"" + gNotebook.getId()}) == 1;
    }

    public boolean deleteGNotebook(GNotebook gNotebook) {
        return db.delete(TABLE_NOTEBOOK, "id = ?", new String[]{"" + gNotebook.getId()}) == 1;
    }

    public List<GNotebook> loadGNotebooks() {
        List<GNotebook> list = new ArrayList<GNotebook>();
        Cursor cursor = db.query(TABLE_NOTEBOOK, null, "syn_status != ?", new String[]{"" + GNotebook
                        .DELETE}, null,
                null,
                null);
        if (cursor.moveToFirst()) {
            do {
                GNotebook gNotebook = new GNotebook();
                gNotebook.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                gNotebook.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                gNotebook.setSynStatus(cursor.getInt(cursor.getColumnIndex(SYN_STATUS)));
                gNotebook.setNotebookGuid(cursor.getString(cursor.getColumnIndex(NOTEBOOK_GUID)));
                gNotebook.setDeleted(cursor.getInt(cursor.getColumnIndex(DELETED)));
                gNotebook.setNotesNum(cursor.getInt(cursor.getColumnIndex(NOTES_NUM)));
                gNotebook.setSelected(cursor.getInt(cursor.getColumnIndex(SELECTED)));

                list.add(gNotebook);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public GNotebook getGNotebookById(int id) {
        Cursor cursor = db.query(TABLE_NOTEBOOK, null, "id = ?", new String[]{"" + id}, null, null, null);
        if (cursor.moveToFirst()) {
            GNotebook gNotebook = new GNotebook();
            gNotebook.setId(cursor.getInt(cursor.getColumnIndex(ID)));
            gNotebook.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            gNotebook.setSynStatus(cursor.getInt(cursor.getColumnIndex(SYN_STATUS)));
            gNotebook.setNotebookGuid(cursor.getString(cursor.getColumnIndex(NOTEBOOK_GUID)));
            gNotebook.setDeleted(cursor.getInt(cursor.getColumnIndex(DELETED)));
            gNotebook.setNotesNum(cursor.getInt(cursor.getColumnIndex(NOTES_NUM)));
            gNotebook.setSelected(cursor.getInt(cursor.getColumnIndex(SELECTED)));

            cursor.close();
            return gNotebook;
        }

        cursor.close();
        return null;
    }
}
