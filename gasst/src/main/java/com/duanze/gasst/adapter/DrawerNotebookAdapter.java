package com.duanze.gasst.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duanze.gasst.MainActivity;
import com.duanze.gasst.R;
import com.duanze.gasst.model.GNotebook;

import java.util.Calendar;
import java.util.List;

public class DrawerNotebookAdapter extends ArrayAdapter<GNotebook> {
    private int resourceId;
    private boolean isFold;
    private int maxLines;
    private Calendar today;
    private boolean customizeColor;
    private AbsListView mListView;
    private Context mContext;

    public DrawerNotebookAdapter(Context context, int textViewResourceId, List<GNotebook> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        mContext = context;
    }

    public DrawerNotebookAdapter(Context context, int textViewResourceId, List<GNotebook> objects,
                                 AbsListView listView) {
        this(context, textViewResourceId, objects);
        mListView = listView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GNotebook gNotebook = getItem(position);
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
            holder.folderUnit = (LinearLayout) view.findViewById(R.id.ll_folder_unit);
            holder.flag = (ImageView) view.findViewById(R.id.iv_folder_unit_flag);
            holder.name = (TextView) view.findViewById(R.id.tv_folder_unit_name);
            holder.num = (TextView) view.findViewById(R.id.tv_folder_unit_num);
            holder.chk = (CheckBox) view.findViewById(R.id.cb_folder_unit);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (Holder) view.getTag();
        }

//不是被选中的文件夹
        if (gNotebook.getSelected() == GNotebook.FALSE) {
            holder.flag.setVisibility(View.INVISIBLE);
        } else {
            holder.flag.setVisibility(View.VISIBLE);
        }
        holder.name.setText(gNotebook.getName());
//        下面这句注意，num为Int类型，运行时被当作resourceId报错
        holder.num.setText("" + gNotebook.getNum());
        holder.chk.setVisibility(View.INVISIBLE);
        holder.chk.setOnCheckedChangeListener((MainActivity) mContext);
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
        LinearLayout folderUnit;
        ImageView flag;
        TextView name;
        TextView num;
        CheckBox chk;
    }
}
