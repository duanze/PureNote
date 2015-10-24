package com.duanze.gasst.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Duanze on 2015/10/24.
 */
public class SwipeRefreshLayoutEx extends SwipeRefreshLayout {
    private static final String TAG = SwipeRefreshLayoutEx.class.getSimpleName();

    public SwipeRefreshLayoutEx(Context context) {
        super(context);
    }

    public SwipeRefreshLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }
}
