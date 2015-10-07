package com.duanze.gasst.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duanze.gasst.R;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.util.Util;

import java.util.Calendar;
import java.util.List;

public class NoteAdapter extends ArrayAdapter<GNote> {
    private int resourceId;
    private boolean isFold;
    private int maxLines;
    private Calendar today;
    //    private boolean customizeColor;
    private AbsListView mListView;

    public NoteAdapter(Context context, int textViewResourceId, List<GNote> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    public NoteAdapter(Context context, int textViewResourceId, List<GNote> objects,
                       AbsListView listView) {
        this(context, textViewResourceId, objects);
        mListView = listView;
    }

    public void setValues(boolean fold, Calendar cal, int lines) {
        isFold = fold;
        today = cal;
        maxLines = lines;
//        customizeColor = activity.isCustomizeColor();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GNote gNote = getItem(position);
        View view;
        Holder holder;
        if (convertView == null) {
//            view = LayoutInflater.from(getContext()).inflate(resourceId, null);

            // safety inflate case(### Solution 1 ###)
            // The second parameter "mListView" provides a set of
            // LayoutParams values for root of the returned hierarchy
            view = LayoutInflater.from(getContext()).inflate(
                    resourceId, mListView, false);

            holder = new Holder();
            holder.listItem = (RelativeLayout) view.findViewById(R.id.rl_list_item);
            holder.noteColor = (TextView) view.findViewById(R.id.tv_note_color);
            holder.title = (TextView) view.findViewById(R.id.note_content);
            holder.time = (TextView) view.findViewById(R.id.edit_time);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (Holder) view.getTag();
        }

        holder.title.setText(gNote.getContent());
        //如果设置了过期单行折叠并且该条note已过期
        if (isFold && gNote.compareToCalendar(today) < 0) {
            holder.title.setMaxLines(1);
        } else {
            holder.title.setMaxLines(maxLines);
        }
        holder.time.setText(Util.timeString(gNote));
//        if (customizeColor) {
        measureView(holder.listItem);

        int height = holder.listItem.getMeasuredHeight();
//            holder.noteColor.setVisibility(View.VISIBLE);
        holder.noteColor.setHeight(height * 12 / 20);
        holder.noteColor.setBackgroundColor(gNote.getColor());
//        } else {
//            holder.noteColor.setBackgroundColor(GridUnit.TRANSPARENT);
//            holder.noteColor.setVisibility(View.GONE);
//        }
        return view;
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

    class Holder {
        RelativeLayout listItem;
        TextView noteColor;
        TextView title;
        TextView time;
    }
}
