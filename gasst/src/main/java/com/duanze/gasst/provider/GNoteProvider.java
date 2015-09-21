package com.duanze.gasst.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.duanze.gasst.db.GNoteOpenHelper;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;

public class GNoteProvider extends ContentProvider {
    public static final int NOTES_WITH_DELETED = 0;
    public static final int NOTE_DIR = 1;
    public static final int NOTE_ITEM = 2;

    public static final String AUTHORITY = "com.duanze.gasst.provider";
    public static final String TABLE_NAME = GNoteDB.TABLE_NAME;

    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static final String[] STANDARD_PROJECTION = {
            GNoteDB.ID + " AS _id"
            , GNoteDB.TIME
            , GNoteDB.ALERT_TIME
            , GNoteDB.IS_PASSED
            , GNoteDB.CONTENT
            , GNoteDB.IS_DONE
            , GNoteDB.COLOR
            , GNoteDB.EDIT_TIME
            , GNoteDB.CREATED_TIME
            , GNoteDB.SYN_STATUS
            , GNoteDB.GUID
            , GNoteDB.BOOK_GUID
            , GNoteDB.DELETED
            , GNoteDB.GNOTEBOOK_ID
    };
    public static final String STANDARD_SORT_ORDER = GNoteDB.TIME + " desc";

    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME, NOTE_DIR);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", NOTE_ITEM);
    }

    private GNoteOpenHelper dbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowAffected = 0;

        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                rowAffected = db.delete(TABLE_NAME, selection,
                        selectionArgs);
                break;
            case NOTE_ITEM:
//                通过update字段来标志“删除”
                String id = uri.getLastPathSegment();
                ContentValues values = new ContentValues();
                values.put(GNoteDB.DELETED, GNote.TRUE);
                values.put(GNoteDB.SYN_STATUS, GNote.DELETE);
                if (TextUtils.isEmpty(selection)) {
                    rowAffected = db.update(TABLE_NAME, values,
                            GNoteDB.ID + "=" + id, null);
                } else {
                    rowAffected = db.update(TABLE_NAME, values,
                            selection + " and " + GNoteDB.ID + "=" + id,
                            selectionArgs);
                }
            default:
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowAffected;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (values.containsKey(GNoteDB.ID)) {
            values.remove(GNoteDB.ID);
        }
        String content = values.getAsString(GNoteDB.CONTENT);
        if (null == content || content.trim().length() == 0) {
            return null;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri itemUri = null;
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                long newID = db.insert(TABLE_NAME, null, values);
                if (newID > 0) {
                    itemUri = ContentUris.withAppendedId(uri, newID);
                    getContext().getContentResolver().notifyChange(itemUri, null);
                }
            default:
                break;
        }

        return itemUri;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new GNoteOpenHelper(getContext(), GNoteDB.DB_NAME, null, GNoteDB.VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        /*
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTE_ITEM:
                String id = uri.getPathSegments().get(1);
                cursor = db.query(TABLE_NAME, projection, "id = ?", new String[]{id}, null, null, sortOrder);
                break;
            default:
                break;
        }
        */

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                queryBuilder.appendWhere(GNoteDB.DELETED + "!='" + GNote.TRUE
                        + "'");
                break;
            case NOTE_ITEM:
                queryBuilder
                        .appendWhere(GNoteDB.ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updateCount = 0;
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                updateCount = db.update(
                        TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTE_ITEM:
                String where = "";
                if (!TextUtils.isEmpty(selection)) {
                    where += " and " + selection;
                }
                updateCount = db.update(
                        TABLE_NAME, values,
                        GNoteDB.ID + "=" + uri.getLastPathSegment() + where,
                        selectionArgs);
                break;

            default:
                break;
        }
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }
}
