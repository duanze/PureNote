package com.duanze.gasst.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.duanze.gasst.activity.Folder;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;

/**
 * Created by Duanze on 2015/9/26.
 */
public class GNotebookUtil {

    public static void updateGNotebook(Context context, SharedPreferences preferences, int id,
                                        int diff) {
        if (id == 0) {
            int cnt = preferences.getInt(Folder.PURENOTE_NOTE_NUM, 3);
            preferences.edit().putInt(Folder.PURENOTE_NOTE_NUM, cnt + diff).apply();
        } else {
            GNoteDB db = GNoteDB.getInstance(context);
            GNotebook gNotebook = db.getGNotebookById(id);
            int num = gNotebook.getNotesNum();
            gNotebook.setNotesNum(num + diff);
            ProviderUtil.updateGNotebook(context, gNotebook);
        }
    }
}
