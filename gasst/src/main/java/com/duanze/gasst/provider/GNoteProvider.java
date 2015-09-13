package com.duanze.gasst.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

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
    public static final String STANDARD_PROJECTION =
            GNoteDB.ID + " AS _id"
                    + "," + GNoteDB.TIME
                    + "," + GNoteDB.ALERT_TIME
                    + "," + GNoteDB.IS_PASSED
                    + "," + GNoteDB.CONTENT
                    + "," + GNoteDB.IS_DONE
                    + "," + GNoteDB.COLOR
                    + "," + GNoteDB.EDIT_TIME
                    + "," + GNoteDB.CREATED_TIME
                    + "," + GNoteDB.SYN_STATUS
                    + "," + GNoteDB.GUID
                    + "," + GNoteDB.BOOK_GUID
                    + "," + GNoteDB.DELETED
                    + "," + GNoteDB.GNOTEBOOK_ID;
    public static final String STANDARD_SORTORDER = GNoteDB.TIME + " desc";

    private static UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME, NOTE_DIR);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", NOTE_ITEM);
    }

    private GNoteOpenHelper dbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
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
                queryBuilder.appendWhere(GNoteDB.SYN_STATUS + "!='" + GNote.DELETE
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
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
