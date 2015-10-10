package com.duanze.gasst.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duanze.gasst.R;
import com.duanze.gasst.activity.Note;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.util.GNotebookUtil;
import com.duanze.gasst.util.ProviderUtil;
import com.duanze.gasst.util.TimeUtils;
import com.duanze.gasst.util.Util;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Duanze on 2015/10/4.
 */
public class GNoteRVAdapter extends RecyclerView.Adapter<GNoteRVAdapter.GNoteItemHolder>
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = GNoteRVAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private Cursor mCursor;
    private boolean mDataValid;
    private Context mContext;
    private boolean mCheckMode;
    private HashMap<Integer, GNote> mCheckedItems;
    private ItemLongPressedListener mItemLongPressedListener;
    private OnItemSelectListener mOnItemSelectListener;

    private SharedPreferences preferences;

    public interface ItemLongPressedListener {
        void startActionMode();
    }

    public interface OnItemSelectListener {
        void onSelect();

        void onCancelSelect();
    }

    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void setmItemLongPressedListener(ItemLongPressedListener mItemLongPressedListener) {
        this.mItemLongPressedListener = mItemLongPressedListener;
    }

    public void setmOnItemSelectListener(OnItemSelectListener mOnItemSelectListener) {
        this.mOnItemSelectListener = mOnItemSelectListener;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public GNoteRVAdapter(Context context, Cursor cursor) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mCursor = cursor;
        boolean cursorPresent = null != cursor;
        mDataValid = cursorPresent;
    }

    public GNoteRVAdapter(Context context, Cursor cursor, OnItemSelectListener
            onItemSelectListener, ItemLongPressedListener itemLongPressedListener) {
        this(context, cursor);
        mOnItemSelectListener = onItemSelectListener;
        mItemLongPressedListener = itemLongPressedListener;
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there wasa not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mDataValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public GNoteItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.gnote_rv_item, viewGroup, false);
        GNoteItemHolder gNoteItemHolder = new GNoteItemHolder(view);
        return gNoteItemHolder;
    }

    @Override
    public void onBindViewHolder(GNoteItemHolder gNoteItemHolder, int i) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(i)) {
            throw new IllegalStateException("couldn't move cursor to position " + i);
        }

        GNote gNote = new GNote(mCursor);
        gNoteItemHolder.itemLayout.setTag(R.string.gnote_data, gNote);
        gNoteItemHolder.itemLayout.setOnClickListener(this);
        gNoteItemHolder.itemLayout.setOnLongClickListener(this);
//        gNoteItemHolder.title.setText(gNote.getContentFromHtml().toString().trim());
        gNoteItemHolder.title.setText(gNote.getContent());
        gNoteItemHolder.editTime.setText(TimeUtils.getConciseTime(gNote.getEditTime(), mContext));
        if (!gNote.getIsPassed()) {
            gNoteItemHolder.alertTime.setText(Util.timeString(gNote));
            gNoteItemHolder.alertTime.setVisibility(View.VISIBLE);
        } else {
            gNoteItemHolder.alertTime.setVisibility(View.GONE);
        }

//        主要用于批量操作时，notifyDataSetChanged()之后改变背景
        if (mCheckMode) {
            if (isChecked(gNote.getId())) {
                gNoteItemHolder.itemLayout.setBackgroundResource(R.drawable.hover_multi_background_normal);
            } else {
                gNoteItemHolder.itemLayout.setBackgroundResource(R.drawable.hover_border_normal);
            }
        } else {
            gNoteItemHolder.itemLayout.setBackgroundResource(R.drawable.hover_background);
        }
    }


    @Override
    public int getItemCount() {
        if (mDataValid && null != mCursor) return mCursor.getCount();
        return 0;
    }


    class GNoteItemHolder extends RecyclerView.ViewHolder {
        View itemLayout;
        TextView title;
        TextView editTime;
        TextView alertTime;

        public GNoteItemHolder(View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.rv_item_container);
            title = (TextView) itemView.findViewById(R.id.note_content);
            editTime = (TextView) itemView.findViewById(R.id.edit_time);
            alertTime = (TextView) itemView.findViewById(R.id.alert_time);
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.rv_item_container == v.getId()) {
            if (!mCheckMode) {
                Note.actionStart(mContext, (GNote) v.getTag(R.string.gnote_data), Note.MODE_EDIT);
            } else {
                GNote gNote = (GNote) v.getTag(R.string.gnote_data);
                toggleCheckedId(gNote.getId(), gNote, v);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (!mCheckMode) {
            if (null != mItemLongPressedListener) {
                mItemLongPressedListener.startActionMode();
            }
            setCheckMode(true);
        }
        GNote gNote = (GNote) v.getTag(R.string.gnote_data);
        toggleCheckedId(gNote.getId(), gNote, v);
        return true;
    }

    private boolean isChecked(int id) {
        if (null == mCheckedItems) {
            return false;
        }
        return mCheckedItems.containsKey(id);
    }

    public int getSelectedCount() {
        if (mCheckedItems == null) {
            return 0;
        } else {
            return mCheckedItems.size();
        }
    }

    public void setCheckMode(boolean check) {
        if (!check) {
            mCheckedItems = null;
        }
        if (check != mCheckMode) {
            mCheckMode = check;
            notifyDataSetChanged();
        }
    }

    public void toggleCheckedId(int _id, GNote gNote, View v) {
        if (mCheckedItems == null) {
            mCheckedItems = new HashMap<Integer, GNote>();
        }
        if (!mCheckedItems.containsKey(_id)) {
            mCheckedItems.put(_id, gNote);

            if (null != mOnItemSelectListener) {
                mOnItemSelectListener.onSelect();
            }
        } else {
            mCheckedItems.remove(_id);

            if (null != mOnItemSelectListener) {
                mOnItemSelectListener.onCancelSelect();
            }
        }
        notifyDataSetChanged();
    }

    public void deleteSelectedNotes() {
        if (mCheckedItems == null || mCheckedItems.size() == 0) {
            return;
        } else {
            Set<Integer> keys = mCheckedItems.keySet();
            SparseIntArray affectedNotebooks = new SparseIntArray(mCheckedItems.size());
            for (Integer key : keys) {
                GNote gNote = mCheckedItems.get(key);
                gNote.setSynStatus(GNote.DELETE);
                gNote.setDeleted(GNote.TRUE);
                ProviderUtil.updateGNote(mContext, gNote);

//                更新受到影响的笔记本的应删除数值
                if (0 != gNote.getGNotebookId()) {
                    int num = affectedNotebooks.get(gNote.getGNotebookId());
                    affectedNotebooks.put(gNote.getGNotebookId(), num + 1);
                }
            }
            if (null != preferences) {
                GNotebookUtil.updateGNotebook(mContext, preferences, 0, -mCheckedItems.size());
                for (int i = 0; i < affectedNotebooks.size(); i++) {
                    int key = affectedNotebooks.keyAt(i);
                    int value = affectedNotebooks.valueAt(i);
                    GNotebookUtil.updateGNotebook(mContext, preferences, key, -value);
                }
            }

            mCheckedItems.clear();

            if (null != mOnItemSelectListener) {
                mOnItemSelectListener.onCancelSelect();
            }

//            new Evernote(mContext).sync(true, false, null);
        }
    }

    public void moveSelectedNotes(int toNotebookId) {
        if (mCheckedItems == null || mCheckedItems.size() == 0) {
            return;
        } else {
            Set<Integer> keys = mCheckedItems.keySet();
            SparseIntArray affectedNotebooks = new SparseIntArray(mCheckedItems.size());
            for (Integer key : keys) {
                GNote gNote = mCheckedItems.get(key);

                //                更新受到影响的笔记本中的数值
                if (0 != gNote.getGNotebookId()) {
                    int num = affectedNotebooks.get(gNote.getGNotebookId());
                    affectedNotebooks.put(gNote.getGNotebookId(), num + 1);
                }

                gNote.setGNotebookId(toNotebookId);
                ProviderUtil.updateGNote(mContext, gNote);

            }
            if (null != preferences) {
                if (0 != toNotebookId) {
                    GNotebookUtil.updateGNotebook(mContext, preferences, toNotebookId,
                            +mCheckedItems.size());
                }
                for (int i = 0; i < affectedNotebooks.size(); i++) {
                    int key = affectedNotebooks.keyAt(i);
                    int value = affectedNotebooks.valueAt(i);
                    GNotebookUtil.updateGNotebook(mContext, preferences, key, -value);
                }
            }

            mCheckedItems.clear();

            if (null != mOnItemSelectListener) {
                mOnItemSelectListener.onCancelSelect();
            }

        }
    }

    public void selectAllNotes() {
        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);
            GNote gNote = new GNote(mCursor);

            if (mCheckedItems == null) {
                mCheckedItems = new HashMap<Integer, GNote>();
            }
            int _id = gNote.getId();
            if (!mCheckedItems.containsKey(_id)) {
                mCheckedItems.put(_id, gNote);
            }

        }

        if (null != mOnItemSelectListener) {
            mOnItemSelectListener.onSelect();
        }

        notifyDataSetChanged();
    }
}
