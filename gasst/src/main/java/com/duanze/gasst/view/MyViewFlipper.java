package com.duanze.gasst.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

public class MyViewFlipper extends ViewFlipper {
    public MyViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyViewFlipper(Context context){
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return false;
            case MotionEvent.ACTION_MOVE:
                //如果你觉得需要拦截
                return false;
            case MotionEvent.ACTION_UP:
                return false;
        }
        return false;
    }
}
