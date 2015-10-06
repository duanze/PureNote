package com.duanze.gasst.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

public class MinusTextView extends TextView {

    private static int width;
    private static int height;
    private static final float GOLDEN_PROPORTION = (float) 0.618;
    private static final float ACTUAL_PROPORTION = (float) 0.418;

    {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        width = point.x;
        height = point.y;
    }

    public MinusTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = getMeasuredWidth();
        int heightSize = getMeasuredHeight();

        if (heightSize > height * ACTUAL_PROPORTION) {
            heightSize = (int) (height * ACTUAL_PROPORTION);
        }

        setMeasuredDimension(widthSize, heightSize);
        requestLayout();
    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}
