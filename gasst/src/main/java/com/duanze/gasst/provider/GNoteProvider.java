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
    public static final int NOTE_DIR = 1;
    public static final int NOTE_ITEM = 2;
    public static final int NOTEBOOK_DIR = 3;
    public static final int NOTEBOOK_ITEM = 4;

    public static final String AUTHORITY = "com.duanze.gasst.provider";
    public static final String TABLE_NOTE = GNoteDB.TABLE_NAME;
    public static final String TABLE_NOTEBOOK = GNoteDB.TABLE_NOTEBOOK;

    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NOTE);
    public static final Uri NOTEBOOK_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NOTEBOOK);

    public static final String[] STANDARD_PROJECTION = {GNoteDB.ID + " AS _id", GNoteDB.TIME,
            GNoteDB.ALERT_TIME, GNoteDB.IS_PASSED, GNoteDB.CONTENT, GNoteDB.IS_DONE, GNoteDB
            .COLOR, GNoteDB.EDIT_TIME, GNoteDB.CREATED_TIME, GNoteDB.SYN_STATUS, GNoteDB.GUID,
            GNoteDB.BOOK_GUID, GNoteDB.DELETED, GNoteDB.GNOTEBOOK_ID};
    public static final String STANDARD_SORT_ORDER = GNoteDB.EDIT_TIME + " desc";
    public static final String STANDARD_SORT_ORDER2 = GNoteDB.TIME + " desc";

    public static final String STANDARD_SELECTION = GNoteDB.DELETED + " != ?";
    public static final String[] STANDARD_SELECTION_ARGS = new String[]{"" + GNote.TRUE};

    public static final String[] NOTEBOOK_PROJECTION = {GNoteDB.ID + " AS _id", GNoteDB.NAME,
            GNoteDB.SYN_STATUS, GNoteDB.NOTEBOOK_GUID, GNoteDB.DELETED, GNoteDB.NOTES_NUM,
            GNoteDB.SELECTED};

    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, TABLE_NOTE, NOTE_DIR);
        uriMatcher.addURI(AUTHORITY, TABLE_NOTE + "/#", NOTE_ITEM);
        uriMatcher.addURI(AUTHORITY, TABLE_NOTEBOOK, NOTEBOOK_DIR);
        uriMatcher.addURI(AUTHORITY, TABLE_NOTEBOOK + "/#", NOTEBOOK_ITEM);
    }

    private GNoteOpenHelper dbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowAffected = 0;

        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                rowAffected = db.delete(TABLE_NOTE, selection, selectionArgs);
                break;
            case NOTE_ITEM:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowAffected = db.delete(TABLE_NOTE, GNoteDB.ID + " = ?", new String[]{"" + id});
                } else {
                    rowAffected = db.delete(TABLE_NOTE, selection + " and " + GNoteDB.ID + "=" +
                            id, selectionArgs);
                }
                break;
            case NOTEBOOK_DIR:
                rowAffected = db.delete(TABLE_NOTEBOOK, selection, selectionArgs);
                break;
            case NOTEBOOK_ITEM:
                String bookId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowAffected = db.delete(TABLE_NOTEBOOK, GNoteDB.ID + " = ?", new String[]{""
                            + bookId});
                } else {
                    rowAffected = db.delete(TABLE_NOTEBOOK, selection + " and " + GNoteDB.ID +
                            "=" + bookId, selectionArgs);
                }
                break;
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

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri itemUri = null;
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                // / Begin 此部分代码仅适用于 对 Note 判定
                String content = values.getAsString(GNoteDB.CONTENT);
                if (null == content || content.trim().length() == 0) {
                    return null;
                }
                // / End
                long newID = db.insert(TABLE_NOTE, null, values);
                if (newID > 0) {
                    itemUri = ContentUris.withAppendedId(uri, newID);
                    getContext().getContentResolver().notifyChange(itemUri, null);
                }
                break;
            case NOTEBOOK_DIR:
                long newBookID = db.insert(TABLE_NOTEBOOK, null, values);
                if (newBookID > 0) {
                    itemUri = ContentUris.withAppendedId(uri, newBookID);
                    getContext().getContentResolver().notifyChange(itemUri, null);
                }
                break;
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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
//        这种格式似乎可读性更好
        /*
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                cursor = db.query(TABLE_NOTE, projection, selection, selectionArgs, null, null,
                sortOrder);
                break;
            case NOTE_ITEM:
                String id = uri.getPathSegments().get(1);
                cursor = db.query(TABLE_NOTE, projection, "id = ?", new String[]{id}, null, null,
                 sortOrder);
                break;
            default:
                break;
        }
        */

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                queryBuilder.setTables(TABLE_NOTE);
                queryBuilder.appendWhere(GNoteDB.DELETED + "!='" + GNote.TRUE + "'");
                break;
            case NOTE_ITEM:
                queryBuilder.setTables(TABLE_NOTE);
                queryBuilder.appendWhere(GNoteDB.ID + "=" + uri.getLastPathSegment());
                break;
            case NOTEBOOK_DIR:
                queryBuilder.setTables(TABLE_NOTEBOOK);
                queryBuilder.appendWhere(GNoteDB.DELETED + "!='" + GNote.TRUE + "'");
                break;
            case NOTEBOOK_ITEM:
                queryBuilder.setTables(TABLE_NOTEBOOK);
                queryBuilder.appendWhere(GNoteDB.ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(), projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updateCount = 0;
        String where = "";

        switch (uriMatcher.match(uri)) {
            case NOTE_DIR:
                updateCount = db.update(TABLE_NOTE, values, selection, selectionArgs);
                break;
            case NOTE_ITEM:
                if (!TextUtils.isEmpty(selection)) {
                    where += " and " + selection;
                }
                updateCount = db.update(TABLE_NOTE, values, GNoteDB.ID + "=" + uri
                        .getLastPathSegment() + where, selectionArgs);
                break;
            case NOTEBOOK_DIR:
                updateCount = db.update(TABLE_NOTEBOOK, values, selection, selectionArgs);
                break;
            case NOTEBOOK_ITEM:
                if (!TextUtils.isEmpty(selection)) {
                    where += " and " + selection;
                }
                updateCount = db.update(TABLE_NOTEBOOK, values, GNoteDB.ID + "=" + uri
                        .getLastPathSegment() + where, selectionArgs);
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
