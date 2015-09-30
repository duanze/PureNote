package com.duanze.gasst.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duanze.gasst.R;
import com.duanze.gasst.activity.Note;
import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.Util;

import java.util.HashMap;

public class GNotebookAdapter extends CursorAdapter implements View.OnClickListener, View
        .OnLongClickListener {
    private static final String TAG = GNotebookAdapter.class.getSimpleName();

    public static final int SPECIAL_ITEM_NUM = 1;
    private LayoutInflater mLayoutInflater;
    private boolean mCheckMode;
    private HashMap<Integer, GNotebook> mCheckedItems;
    private ItemLongPressedListener mItemLongPressedListener;
    private OnItemSelectListener mOnItemSelectListener;
    private OnItemClickListener mOnItemClickListener;

    private SharedPreferences preferences;

    public interface ItemLongPressedListener {
        void startActionMode();
    }

    public interface OnItemSelectListener {
        void onSelect();

        void onCancelSelect();
    }

    public interface OnItemClickListener {
        void onItemClick(View view);
    }

    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void setItemLongPressedListener(ItemLongPressedListener mItemLongPressedListener) {
        this.mItemLongPressedListener = mItemLongPressedListener;
    }

    public void setOnItemSelectListener(OnItemSelectListener mOnItemSelectListener) {
        this.mOnItemSelectListener = mOnItemSelectListener;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public GNotebookAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
    }

    public GNotebookAdapter(Context context, Cursor c, int flags, ItemLongPressedListener
            itemLongPressedListener, OnItemSelectListener onItemSelectListener) {
        this(context, c, flags);
        mItemLongPressedListener = itemLongPressedListener;
        mOnItemSelectListener = onItemSelectListener;
    }

    public GNotebookAdapter(Context context, Cursor c, int flags, ItemLongPressedListener
            itemLongPressedListener, OnItemSelectListener onItemSelectListener,
                            OnItemClickListener onItemClickListener) {
        this(context, c, flags, itemLongPressedListener, onItemSelectListener);
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getCount() {
//        注意当 未配置 cursor 时，需要返回0
        if (null != mCursor) {
            return super.getCount() + SPECIAL_ITEM_NUM;
        }
        return 0;
    }

    private View mView;
    private Holder mHolder;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        mDataValid 等疑似 父类 域,仅 v4 包中有效
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        if (convertView == null) {
//            newView()中实际并未使用 mCursor，所以没有问题
            mView = newView(mContext, mCursor, parent);
            mHolder = new Holder();
            mHolder.itemLayout = (LinearLayout) mView.findViewById(R.id.ll_folder_unit);
            mHolder.flag = (ImageView) mView.findViewById(R.id.iv_folder_unit_flag);
            mHolder.name = (TextView) mView.findViewById(R.id.tv_folder_unit_name);
            mHolder.num = (TextView) mView.findViewById(R.id.tv_folder_unit_num);
            mHolder.checkBox = (CheckBox) mView.findViewById(R.id.cb_folder_unit);
            mHolder.divider = mView.findViewById(R.id.v_divider);
            mView.setTag(mHolder);
        } else {
            mView = convertView;
            mHolder = (Holder) mView.getTag();
        }

        if (position == 0) {
            bindFirstView(mView);
        } else {
            int newPosition = position - 1;
            if (!mCursor.moveToPosition(newPosition)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            bindView(mView, mContext, mCursor);
        }
        return mView;
    }

    private void bindFirstView(View mView) {
        if (null == preferences) {
            LogUtil.e(TAG, "Error in bindFirstView,null==preferences!!!");
            return;
        }
        int bookId = preferences.getInt(Settings.GNOTEBOOK_ID, 0);
        int notesNum = preferences.getInt(Settings.PURENOTE_NOTE_NUM, 0);
        if (0 == bookId) {
            mHolder.flag.setVisibility(View.VISIBLE);
        } else {
            mHolder.flag.setVisibility(View.INVISIBLE);
        }
        mHolder.num.setText("" + notesNum);
        mHolder.checkBox.setVisibility(View.INVISIBLE);
        mHolder.divider.setVisibility(View.VISIBLE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View commonView = mLayoutInflater.inflate(R.layout.drawer_folder_item, parent, false);
        final View hover = commonView.findViewById(R.id.ll_folder_unit);
        hover.setOnClickListener(this);
        hover.setOnLongClickListener(this);
        return commonView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        GNotebook gNotebook = new GNotebook(cursor);
        if (gNotebook.isSelected()) {
            mHolder.flag.setVisibility(View.VISIBLE);
        } else {
            mHolder.flag.setVisibility(View.INVISIBLE);
        }
        mHolder.name.setText(gNotebook.getName());
        mHolder.num.setText("" + gNotebook.getNotesNum());
        mHolder.divider.setVisibility(View.INVISIBLE);
        mHolder.itemLayout.setTag(R.string.gnotebook_data, gNotebook);

        if (mCheckMode) {
            mHolder.checkBox.setVisibility(View.VISIBLE);
        } else {
            mHolder.checkBox.setVisibility(View.INVISIBLE);
        }
    }

    class Holder {
        LinearLayout itemLayout;
        ImageView flag;
        TextView name;
        TextView num;
        CheckBox checkBox;
        View divider;
    }

    @Override
    public void onClick(View v) {
        if (R.id.ll_folder_unit == v.getId()) {
            if (!mCheckMode) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(v);
                }
            } else {

            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (!mCheckMode) {

        }

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

}
