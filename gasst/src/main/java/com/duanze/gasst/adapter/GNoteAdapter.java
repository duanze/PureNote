package com.duanze.gasst.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duanze.gasst.R;
import com.duanze.gasst.activity.Note;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.util.GNotebookUtil;
import com.duanze.gasst.util.ProviderUtil;
import com.duanze.gasst.util.Util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

public class GNoteAdapter extends CursorAdapter implements View.OnClickListener, View
        .OnLongClickListener {
    private boolean isFold;
    private int maxLines;
    private Calendar today;
    private LayoutInflater mLayoutInflater;
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

    public GNoteAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
    }

    public GNoteAdapter(Context context, Cursor c, int flags, ItemLongPressedListener
            itemLongPressedListener, OnItemSelectListener onItemSelectListener) {
        this(context, c, flags);
        mItemLongPressedListener = itemLongPressedListener;
        mOnItemSelectListener = onItemSelectListener;
    }

    public void setValues(boolean fold, Calendar cal, int lines) {
        isFold = fold;
        today = cal;
        maxLines = lines;
//        customizeColor = activity.isCustomizeColor();
    }


    private View mView;
    private Holder mHolder;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        mDataValid 等疑似 父类 域,仅 v4 包中有效
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        if (convertView == null) {
            mView = newView(mContext, mCursor, parent);
            mHolder = new Holder();
            mHolder.itemLayout = (RelativeLayout) mView.findViewById(R.id.rl_list_item);
            mHolder.noteColor = (TextView) mView.findViewById(R.id.tv_note_color);
            mHolder.title = (TextView) mView.findViewById(R.id.note_content);
            mHolder.time = (TextView) mView.findViewById(R.id.edit_time);
            mView.setTag(mHolder);
        } else {
            mView = convertView;
            mHolder = (Holder) mView.getTag();
        }

        bindView(mView, mContext, mCursor);

        return mView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View commonView = mLayoutInflater.inflate(R.layout.gnote_list_item, parent, false);
        final View hover = commonView.findViewById(R.id.rl_list_item);
        hover.setOnClickListener(this);
        hover.setOnLongClickListener(this);
        return commonView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        GNote gNote = new GNote(cursor);
        mHolder.title.setText(gNote.getContent());
        //如果设置了过期单行折叠并且该条note已过期
        if (isFold && gNote.compareToCalendar(today) < 0) {
            mHolder.title.setMaxLines(1);
        } else {
            mHolder.title.setMaxLines(maxLines);
        }
        mHolder.time.setText(Util.timeString(gNote));


//        TODO 色彩标签考虑重做
        /*
                measureView(mHolder.itemLayout);
                int height = mHolder.itemLayout.getMeasuredHeight();

//            holder.noteColor.setVisibility(View.VISIBLE);
//        假如色彩为 “透明” 的话，就不需要设定可见性了
        mHolder.noteColor.setHeight(height * 12 / 20);
        mHolder.noteColor.setBackgroundColor(gNote.getColor());
*/

        mHolder.itemLayout.setTag(R.string.gnote_data, gNote);

//        主要用于批量操作时，notifyDataSetChanged()之后改变背景
        if (mCheckMode) {
            if (isChecked(gNote.getId())) {
                mHolder.itemLayout.setBackgroundResource(R.color.setting_press);
            } else {
                mHolder.itemLayout.setBackgroundResource(R.color.transparent);
            }
        } else {
            mHolder.itemLayout.setBackgroundResource(R.drawable.setting_item);
        }
    }

    private void measureView(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT);
        }
        //headerView的宽度信息
        int width = ViewGroup.getChildMeasureSpec(0, 0, lp.width);
        int height;
        if (lp.height > 0) {
            height = View.MeasureSpec.makeMeasureSpec(lp.height, View.MeasureSpec.EXACTLY);
            //最后一个参数表示：适合、匹配
        } else {
            height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);//未指定
        }
//System.out.println("childViewWidth"+childMeasureWidth);
//System.out.println("childViewHeight"+childMeasureHeight);
        //将宽和高设置给child
        view.measure(width, height);
    }

    class Holder {
        RelativeLayout itemLayout;
        TextView noteColor;
        TextView title;
        TextView time;
    }

    @Override
    public void onClick(View v) {
        if (R.id.rl_list_item == v.getId()) {
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
