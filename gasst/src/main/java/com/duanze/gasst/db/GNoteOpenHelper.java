package com.duanze.gasst.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
                    + "deleted integer"
                    + ")";

    public GNoteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                           int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
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
            default:
        }
    }
}
