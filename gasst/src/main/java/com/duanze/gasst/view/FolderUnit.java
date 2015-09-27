package com.duanze.gasst.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.duanze.gasst.R;

public class FolderUnit extends LinearLayout {

    public FolderUnit(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.drawer_folder_unit, this);
    }

}
