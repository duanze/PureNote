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

    public static void updateGNotebook(Context mContext, int id, int diff) {
        if (id == 0) {
            SharedPreferences preferences = PreferencesUtils.getInstance(mContext).getPreferences();
            int cnt = preferences.getInt(Folder.PURENOTE_NOTE_NUM, 3);
            PreferencesUtils.getInstance(mContext).setNotesNum(cnt + diff);
        } else {
            GNoteDB db = GNoteDB.getInstance(mContext);
            GNotebook gNotebook = db.getGNotebookById(id);
            int num = gNotebook.getNotesNum();
            gNotebook.setNotesNum(num + diff);
            ProviderUtil.updateGNotebook(mContext, gNotebook);
        }
    }
}
