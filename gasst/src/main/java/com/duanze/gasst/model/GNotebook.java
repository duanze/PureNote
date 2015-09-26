package com.duanze.gasst.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class GNotebook implements Parcelable {
    public static final String TAG = "GNotebook";
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    public static final int NOTHING = 0;
    public static final int NEW = 1;
    public static final int UPDATE = 2;
    public static final int DELETE = 3;


    private int id = -1;
    private String name;
    private int synStatus = NOTHING;//同步状态，仅登录EverNote后有效
    private String notebookGuid = "";
    private int deleted = FALSE;
    private int notesNum = 0;

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    private int selected = FALSE;

    public int getNotesNum() {
        return notesNum;
    }

    public void setNotesNum(int notesNum) {
        this.notesNum = notesNum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }


    public String getNotebookGuid() {
        return notebookGuid;
    }

    public void setNotebookGuid(String notebookGuid) {
        this.notebookGuid = notebookGuid;
    }


    public int getSynStatus() {
        return synStatus;
    }

    public void setSynStatus(int synStatus) {
        this.synStatus = synStatus;
    }

    public boolean needUpdate() {
        return synStatus == UPDATE;
    }

    public boolean needDelete() {
        return synStatus == DELETE;
    }

    public boolean needCreate() {
        return synStatus == NEW;
    }

    public boolean isDeleted() {
        return deleted == TRUE;
    }

    public ContentValues toContentValues() {
        ContentValues values = toInsertContentValues();
        values.put(GNoteDB.ID, id);
        return values;
    }

    public ContentValues toInsertContentValues() {
        ContentValues values = new ContentValues();
        values.put(GNoteDB.NAME, name);
        values.put(GNoteDB.SYN_STATUS, synStatus);
        values.put(GNoteDB.NOTEBOOK_GUID, notebookGuid);
        values.put(GNoteDB.DELETED, deleted);
        values.put(GNoteDB.NOTES_NUM, notesNum);
        values.put(GNoteDB.SELECTED, selected);
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeInt(synStatus);
        parcel.writeString(notebookGuid);
        parcel.writeInt(deleted);
        parcel.writeInt(notesNum);
        parcel.writeInt(selected);
    }

    public static final Creator<GNotebook> CREATOR = new Creator<GNotebook>() {
        @Override
        public GNotebook createFromParcel(Parcel parcel) {
            GNotebook gNotebook = new GNotebook();
            gNotebook.id = parcel.readInt();
            gNotebook.name = parcel.readString();
            gNotebook.synStatus = parcel.readInt();
            gNotebook.notebookGuid = parcel.readString();
            gNotebook.deleted = parcel.readInt();
            gNotebook.notesNum = parcel.readInt();
            gNotebook.selected = parcel.readInt();
            return gNotebook;
        }

        @Override
        public GNotebook[] newArray(int i) {
            return new GNotebook[i];
        }
    };
}
