package com.duanze.gasst.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duanze.gasst.MainActivity;
import com.duanze.gasst.R;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.util.Util;
import com.duanze.gasst.view.GridUnit;

import java.util.Calendar;
import java.util.List;

public class NoteAdapter extends ArrayAdapter<GNote> {
    private int resourceId;
    private boolean isFold;
    private int maxLines;
    private Calendar today;
    private boolean customizeColor;

    public NoteAdapter(Context context, int textViewResourceId, List<GNote> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;

    }

    public void setValues(MainActivity activity) {
        isFold = activity.getIsFold();
        today = activity.getToday();
        maxLines = activity.getMaxLines();
        customizeColor = activity.isCustomizeColor();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GNote gNote = getItem(position);
        View view;
        Holder holder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            holder = new Holder();
            holder.listItem = (RelativeLayout) view.findViewById(R.id.rl_list_item);
            holder.title = (TextView) view.findViewById(R.id.note_title);
            holder.time = (TextView) view.findViewById(R.id.note_time);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (Holder) view.getTag();
        }

        holder.title.setText(gNote.getNoteFromHtml());
        //如果设置了过期单行折叠并且该条note已过期
        if (isFold && gNote.compareToCalendar(today) < 0) {
            holder.title.setMaxLines(1);
        } else {
            holder.title.setMaxLines(maxLines);
        }
        holder.time.setText(Util.timeString(gNote));
        if (customizeColor) {
            holder.listItem.setBackgroundColor(gNote.getColor());
        }else {
            holder.listItem.setBackgroundColor(GridUnit.THRANSPARENT);
        }
        return view;
    }

    class Holder {
        RelativeLayout listItem;
        TextView title;
        TextView time;
    }
}
