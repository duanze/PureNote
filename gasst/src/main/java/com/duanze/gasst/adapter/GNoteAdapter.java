package com.duanze.gasst.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duanze.gasst.R;
import com.duanze.gasst.activity.Note;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.util.Util;

import java.util.Calendar;

public class GNoteAdapter extends CursorAdapter implements View.OnClickListener {
    private boolean isFold;
    private int maxLines;
    private Calendar today;
    private LayoutInflater mLayoutInflater;

    public GNoteAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            throw new IllegalStateException(
                    "this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position "
                    + position);
        }

        if (convertView == null) {
            mView = newView(mContext, mCursor, parent);
            mHolder = new Holder();
            mHolder.listItem = (RelativeLayout) mView.findViewById(R.id.rl_list_item);
            mHolder.noteColor = (TextView) mView.findViewById(R.id.tv_note_color);
            mHolder.title = (TextView) mView.findViewById(R.id.note_title);
            mHolder.time = (TextView) mView.findViewById(R.id.note_time);
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
        View commonView = mLayoutInflater.inflate(R.layout.gnote_list_item,
                parent, false);
        final View hover = commonView.findViewById(R.id.rl_list_item);
        hover.setOnClickListener(this);
//            hover.setOnLongClickListener(this);
        return commonView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        GNote gNote = new GNote(cursor);
        mHolder.title.setText(gNote.getNoteFromHtml().toString().trim());
        //如果设置了过期单行折叠并且该条note已过期
        if (isFold && gNote.compareToCalendar(today) < 0) {
            mHolder.title.setMaxLines(1);
        } else {
            mHolder.title.setMaxLines(maxLines);
        }
        mHolder.time.setText(Util.timeString(gNote));
        measureView(mHolder.listItem);

        int height = mHolder.listItem.getMeasuredHeight();
//            holder.noteColor.setVisibility(View.VISIBLE);
//        假如色彩为 “透明” 的话，就不需要设定可见性了
        mHolder.noteColor.setHeight(height * 12 / 20);
        mHolder.noteColor.setBackgroundColor(gNote.getColor());

        mHolder.listItem.setTag(R.string.gnote_data, gNote);
    }

    private void measureView(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
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

    @Override
    public void onClick(View v) {
        if (R.id.rl_list_item == v.getId()) {
            Note.actionStart(mContext, (GNote) v.getTag(R.string.gnote_data),Note.MODE_EDIT);
        }
    }

    class Holder {
        RelativeLayout listItem;
        TextView noteColor;
        TextView title;
        TextView time;
    }
}
