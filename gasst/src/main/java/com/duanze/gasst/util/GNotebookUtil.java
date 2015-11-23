package com.duanze.gasst.util;

import android.content.Context;

import com.duanze.gasst.data.model.GNoteDB;
import com.duanze.gasst.data.model.GNotebook;
import com.duanze.gasst.util.liteprefs.MyLitePrefs;

/**
 * Created by Duanze on 2015/9/26.
 */
public class GNotebookUtil {

    public static void updateGNotebook(Context mContext, int id, int diff) {
        if (id == 0) {
            int cnt = MyLitePrefs.getInt(MyLitePrefs.PURENOTE_NOTE_NUM);
            MyLitePrefs.putInt(MyLitePrefs.PURENOTE_NOTE_NUM, cnt + diff);
        } else {
            GNoteDB db = GNoteDB.getInstance(mContext);
            GNotebook gNotebook = db.getGNotebookById(id);
            int num = gNotebook.getNotesNum();
            gNotebook.setNotesNum(num + diff);
            ProviderUtil.updateGNotebook(mContext, gNotebook);
        }
    }
}
