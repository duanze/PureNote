package com.duanze.gasst.util;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.provider.GNoteProvider;

/**
 * Created by Duanze on 2015/9/26.
 */
public class ProviderUtil {
    public static int updateGNote(Context context, GNote gNote) {
        ContentValues values = gNote.toContentValues();
        return context.getContentResolver().update(ContentUris.withAppendedId(GNoteProvider
                .BASE_URI, gNote.getId()), values, null, null);
    }

    public static Uri insertGNote(Context context, GNote gNote) {
        ContentValues values = gNote.toContentValues();
        return context.getContentResolver().insert(GNoteProvider.BASE_URI, values);
    }

    public static int updateGNotebook(Context context, GNotebook gNotebook) {
        ContentValues values = gNotebook.toContentValues();
        return context.getContentResolver().update(ContentUris.withAppendedId(GNoteProvider
                .NOTEBOOK_URI, gNotebook.getId()), values, null, null);
    }

    public static Uri insertGNotebook(Context context, GNotebook gNotebook) {
        ContentValues values = gNotebook.toContentValues();
        return context.getContentResolver().insert(GNoteProvider.NOTEBOOK_URI, values);
    }

    public static int deleteGNotebook(Context context, GNotebook gNotebook) {
        return context.getContentResolver().delete(ContentUris.withAppendedId(GNoteProvider
                .NOTEBOOK_URI, gNotebook.getId()), null, null);
    }

}
