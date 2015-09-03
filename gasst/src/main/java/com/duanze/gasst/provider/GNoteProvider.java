package com.duanze.gasst.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.duanze.gasst.db.GNoteOpenHelper;
import com.duanze.gasst.model.GNoteDB;

public class GNoteProvider extends ContentProvider {

    public static final int NOTES_WITH_DELETED = 0;
    public static final int NOTE_DIR = 1;
    public static final int NOTE_ITEM = 2;

    public static final String AUTHORITY = "com.duanze.gasst.provider";
    public static final String TABLE_NAME = GNoteDB.TABLE_NAME;

    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME, NOTE_DIR);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", NOTE_ITEM);
    }

    private GNoteOpenHelper dbHelper;

    public GNoteProvider() {
    }

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
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
