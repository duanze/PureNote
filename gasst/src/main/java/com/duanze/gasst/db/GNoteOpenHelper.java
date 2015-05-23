package com.duanze.gasst.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.duanze.gasst.model.GNoteDB;

public class GNoteOpenHelper extends SQLiteOpenHelper {

    /**
     * GNote建表语句
     */
    public static final String CREATE_TABLE =
            "create table table_gnote ("
                    + "id integer primary key autoincrement,"
                    + "time text,"
                    + "alert_time text,"
                    + "is_passed integer,"
                    + "content text,"
                    + "done integer,"
                    + "color integer,"
                    + "edit_time integer,"
                    + "created_time integer,"
                    + "syn_status integer,"
                    + "guid text,"
                    + "book_guid text,"
                    + "deleted integer,"
                    + "gnotebook_id integer"
                    + ")";

    public static final String CREATE_TABLE_NOTEBOOK =
            "create table " + GNoteDB.TABLE_NOTEBOOK + " ("
                    + "id integer primary key autoincrement,"
                    + "name text,"
                    + "syn_status integer,"
                    + "notebook_guid text,"
                    + "deleted integer,"
                    + "num integer,"
                    + "selected integer"
                    + ")";

    public GNoteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                           int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_TABLE_NOTEBOOK);
    }

    /**
     * 升级数据库
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL("alter table table_gnote add column gnotebook_id integer");
                db.execSQL(CREATE_TABLE_NOTEBOOK);
            default:
        }
    }
}
