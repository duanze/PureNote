package com.faizmalkani.floatingactionbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class FloatingActionButton extends View {

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private final Paint mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mBitmap;
    private int mColor;

    private boolean mHidden = false;
    private Rect rect;
    private int mLeftDisplayed = -1;
    private int mRightDisplayed = -1;
    private int mTopDisplayed = -1;
    private int mBottomDisplayed = -1;
    /**
     * The FAB button's Y position when it is displayed.
     */
    private float mYDisplayed = -1;
    /**
     * The FAB button's Y position when it is hidden.
     */
    private float mYHidden = -1;

    // / Added by duanze
    /**
     * Just for PureNote by Duanze
     */
    private boolean lock = false;

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }
    // / End

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }


    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);
        mColor = a.getColor(R.styleable.FloatingActionButton_fab_color, Color.WHITE);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setColor(mColor);
        float radius, dx, dy;
        radius = a.getFloat(R.styleable.FloatingActionButton_shadowRadius, 10.0f);
        dx = a.getFloat(R.styleable.FloatingActionButton_shadowDx, 0.0f);
        dy = a.getFloat(R.styleable.FloatingActionButton_shadowDy, 3.5f);
        int color = a.getInteger(R.styleable.FloatingActionButton_shadowColor, Color.argb(100, 0,
                0, 0));
        mButtonPaint.setShadowLayer(radius, dx, dy, color);

        Drawable drawable = a.getDrawable(R.styleable.FloatingActionButton_drawable);
        if (null != drawable) {
            mBitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        setWillNotDraw(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context
                .WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            mYHidden = size.y;
        } else mYHidden = display.getHeight();
    }

    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public void setColor(int color) {
        mColor = color;
        mButtonPaint.setColor(mColor);
        invalidate();
    }

    public void setDrawable(Drawable drawable) {
        mBitmap = ((BitmapDrawable) drawable).getBitmap();
        invalidate();
    }

    public void setShadow(float radius, float dx, float dy, int color) {
        mButtonPaint.setShadowLayer(radius, dx, dy, color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6),
                mButtonPaint);
        if (null != mBitmap) {
            canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2, (getHeight() -
                    mBitmap.getHeight()) / 2, mDrawablePaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Perform the default behavior
        super.onLayout(changed, left, top, right, bottom);
        if (mLeftDisplayed == -1) {
            mLeftDisplayed = left;
            mRightDisplayed = right;
            mTopDisplayed = top;
            mBottomDisplayed = bottom;
        }

        // Store the FAB button's displayed Y position if we are not already aware of it
        if (mYDisplayed == -1) {
            mYDisplayed = ViewHelper.getY(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int color;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            color = mColor;
        } else {
            color = darkenColor(mColor);
            rect = new Rect(mLeftDisplayed, mTopDisplayed, mRightDisplayed, mBottomDisplayed);
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!rect.contains(mLeftDisplayed + (int) event.getX(), mTopDisplayed + (int) event
                    .getY())) {
                color = mColor;
            }
        }
        mButtonPaint.setColor(color);
        invalidate();
        return super.onTouchEvent(event);
    }

    public void hide(boolean hide) {
        // / Added by Duanze
        if (lock) return;
        // / End

        // If the hidden state is being updated
        if (mHidden != hide) {

            // Store the new hidden state
            mHidden = hide;

            // Animate the FAB to it's new Y position
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "y", mHidden ? mYHidden :
                    mYDisplayed).setDuration(500);
            animator.setInterpolator(mInterpolator);
            animator.start();
        }
    }

    public void listenTo(AbsListView listView) {
        if (null != listView) {
            listView.setOnScrollListener(new DirectionScrollListener(this));
        }
    }

    /**
     * Created by Duanze
     * @param recyclerView
     */
    public void listenTo(RecyclerView recyclerView) {
        if (null != recyclerView) {
            recyclerView.addOnScrollListener(new DirectionScrollListenerRV(this));
        }
    }
}
