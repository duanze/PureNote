package com.duanze.gasst.fragment;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.duanze.gasst.MainActivity;
import com.duanze.gasst.R;
import com.duanze.gasst.activity.NoteActivity;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.view.GridUnit;
import com.duanze.gasst.view.MyViewFlipper;

import java.util.Calendar;
import java.util.List;

/**
 * Created by duanze on 2015/4/23.
 */
public class ColorGrid extends Fragment {
    public static final String TAG = "ColorGrid";

    public static final int MODEL_ONE = 1;
    public static final int MODEL_TWO = 2;
    public static final int[] LAYOUT_ARRAY = {R.layout.sun, R.layout.mon, R.layout.tue,
            R.layout.wed, R.layout.thu, R.layout.fri, R.layout.sat};
    public static final int[] MODE_ARRAY = {1, 2, 1, 2, 1, 2, 1};
    public static final int[] LONG_GRID_ARRAY = {0, 0, 2, 2, 4, 4, 6};

    /**
     * 缓存视图
     */
    private View[] weekViews = new View[3];
    /**
     * 当前所在周标号
     */
    private int curWeekNo;
    private int nextWeekNo;
    private int previousWeekNo;

    /**
     * 缓存格子视图
     */
    private GridUnit[][] gridUnits = new GridUnit[3][7];
    private int model;
    private int longGrid;
    private int type;
    private int layout;

    private MyViewFlipper myViewFlipper;

    private Calendar tmpCal;
    private Calendar today;
    private Context mContext;
    private GNoteDB db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initValues();
    }

    private void initValues() {
        today = Calendar.getInstance();
        mContext = getActivity();
        db = GNoteDB.getInstance(mContext);

        WindowManager window = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        window.getDefaultDisplay().getSize(point);
        screenWidth = point.x;
        LogUtil.i(TAG, "width: " + screenWidth);
        leftEdge = -screenWidth;
        rightEdge = screenWidth;
        defaultSpeed = screenWidth / 10;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        model = MODE_ARRAY[today.get(Calendar.DAY_OF_WEEK) - 1];
        longGrid = LONG_GRID_ARRAY[today.get(Calendar.DAY_OF_WEEK) - 1];
        type = today.get(Calendar.DAY_OF_WEEK) - 1;
        layout = LAYOUT_ARRAY[type];

        myViewFlipper = new MyViewFlipper(mContext);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams
                .MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        myViewFlipper.setLayoutParams(params);

        curWeekNo = 1;
        nextWeekNo = curWeekNo + 1;
        previousWeekNo = curWeekNo - 1;

        for (int i = 0; i < 3; i++) {
            weekViews[i] = inflateView(inflater, i);
            myViewFlipper.addView(weekViews[i]);
        }
        refreshUI();
        myViewFlipper.setDisplayedChild(curWeekNo);
        setTouchListener();
        return myViewFlipper;
    }

    /**
     * 仅用于MODE_GRID
     *
     * @param inflater
     * @param i        填充的二维数组unitMyViews行号
     * @return
     */
    private View inflateView(LayoutInflater inflater, int i) {
        View view = inflater.inflate(layout, null, false);
        // 以下情况为布局模式一，sun在第一位
        if (model == MODEL_ONE) {
            gridUnits[i][0] = (GridUnit) view.findViewById(R.id.sun);
            gridUnits[i][1] = (GridUnit) view.findViewById(R.id.mon);
            gridUnits[i][2] = (GridUnit) view.findViewById(R.id.tue);
            gridUnits[i][3] = (GridUnit) view.findViewById(R.id.wed);
            gridUnits[i][4] = (GridUnit) view.findViewById(R.id.thu);
            gridUnits[i][5] = (GridUnit) view.findViewById(R.id.fri);
            gridUnits[i][6] = (GridUnit) view.findViewById(R.id.sat);
        } else if (model == MODEL_TWO) {// 布局模式二,sun在最后
            gridUnits[i][0] = (GridUnit) view.findViewById(R.id.mon);
            gridUnits[i][1] = (GridUnit) view.findViewById(R.id.tue);
            gridUnits[i][2] = (GridUnit) view.findViewById(R.id.wed);
            gridUnits[i][3] = (GridUnit) view.findViewById(R.id.thu);
            gridUnits[i][4] = (GridUnit) view.findViewById(R.id.fri);
            gridUnits[i][5] = (GridUnit) view.findViewById(R.id.sat);
            gridUnits[i][6] = (GridUnit) view.findViewById(R.id.sun);
        }
        return view;
    }

    public void refreshUI() {
//        List<GNote> gNoteList = db.loadGNotes();
        List<GNote> gNoteList = db.loadGNotesByBookId(((MainActivity) mContext).getgNotebookId());
        int index = gNoteList.size() - 1;
        tmpCal = (Calendar) today.clone();
        tmpCal.add(Calendar.DAY_OF_MONTH, -7);
        if (model == MODEL_ONE) {
            // 先得到星期日
            tmpCal.add(Calendar.DAY_OF_MONTH, -type);
        } else if (model == MODEL_TWO) {
            // 先得到星期一
            tmpCal.add(Calendar.DAY_OF_MONTH, -(type - 1));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                if (j == longGrid) {
                    gridUnits[i][j].setViewUnit(tmpCal, true);
                } else {
                    gridUnits[i][j].setViewUnit(tmpCal, false);
                }
                gridUnits[i][j].setBackgroundColor(GridUnit.THRANSPARENT);

                final TextView button = gridUnits[i][j].getDoneButton();
                final GridUnit gridUnit = gridUnits[i][j];
                final Calendar cal = (Calendar) tmpCal.clone();

                boolean isSetClick = false;
                if (index >= 0) {
                    //对格子中是否应有note进行判定

                    //当日期比现在的日期小，持续加大
                    while (gNoteList.get(index).compareToCalendar(tmpCal) < 0 && index > 0) {
                        index--;
                    }
                    if (gNoteList.get(index).compareToCalendar(tmpCal) == 0) {
                        final GNote note = gNoteList.get(index);
                        gridUnit.setViewNote(note.getNoteFromHtml().toString().trim());

                        //判断格子色彩
                        if (randomColor()) {
                            gridUnit.randomSetBackground();
                        } else {
                            if (customizeColor()) {
                                gridUnit.setBackgroundColor(note.getColor());
                            } else {
                                gridUnits[i][j].setBackgroundColor(GridUnit.THRANSPARENT);
                            }
                        }

                        button.setVisibility(View.VISIBLE);
                        if (!note.isDone()) {
                            gridUnit.removeStrike();
                            button.setText(R.string.done);

                            gridUnitEdit(gridUnit,note);
                        } else if (note.isDone()) {
                            gridUnit.addStrike();
                            button.setText(R.string.undone);

                            gridUnitOpenNew(gridUnit, cal);
                        }
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!note.isDone()) {
                                    button.setTextColor(GridUnit.DIM_GREY);
                                    button.setText(R.string.undone);
                                    gridUnit.addStrike();
                                    note.setDone(GNote.TRUE);
                                    db.updateGNote(note);

                                    gridUnitOpenNew(gridUnit, cal);
                                } else if (note.isDone()) {
                                    button.setTextColor(GridUnit.GREY);
                                    button.setText(R.string.done);
                                    gridUnit.removeStrike();
                                    note.setDone(GNote.FALSE);
                                    db.updateGNote(note);

                                    gridUnitEdit(gridUnit,note);
                                }
                            }
                        });

                        isSetClick = true;
                    }
                }
                if (!isSetClick) {
                    button.setVisibility(View.GONE);
                    gridUnit.setViewNote("");
                    gridUnit.removeStrike();

                    gridUnitOpenNew(gridUnit, cal);
                }
                tmpCal.add(Calendar.DAY_OF_MONTH, +1);
            }
        }
    }

    private boolean randomColor(){
        return ((MainActivity)mContext).isRandomColor();
    }

    private boolean customizeColor(){
        return ((MainActivity)mContext).isCustomizeColor();
    }

    private void gridUnitOpenNew(GridUnit gridUnit,final Calendar cal) {
        gridUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteActivity.writeNewNote(mContext, cal);
            }
        });
    }

    private void gridUnitEdit(GridUnit gridUnit,final GNote note) {
        //设置可点击
        gridUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteActivity.activityStart(mContext, note,
                        NoteActivity.MODE_EDIT);
            }
        });
    }

    private void setTouchListener() {
        myViewFlipper.setOnTouchListener(touchListener);
//            myViewFlipper.setFocusable(true);
//            myViewFlipper.setClickable(true);
        myViewFlipper.setLongClickable(true);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                gridUnits[i][j].setOnTouchListener(touchListener);
            }
        }
    }

    //以下为滑动效果相关
    /**
     * VIEW滚动时的默认速度
     */
    private int defaultSpeed;

    /**
     * View滚动时，让线程休眠时间，目的让用户的眼睛可以看到图片滚动的效果
     */
    private final static int SLEEP_TIME = 20;

    /**
     * 记录手指按下时的X轴的坐标
     */
    private float xDown;

    /**
     * 记录手指移动的时候的X轴的坐标
     */
    private float xMove;

    /**
     * 记录手指抬起时的X轴的坐标
     */
    private float xUp;

    private int xDistance;
    /**
     * 滚动显示view时，手指滑动需要达到的速度。
     */
    public static final int SNAP_VELOCITY = 200;

    /**
     * 用于计算手指滑动的速度。
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 屏幕宽度值。
     */
    private int screenWidth;

    /**
     * 当前view最多可以滑动到的左边缘，为-screenWidth
     */
    private int leftEdge;

    /**
     * 当前view最多可以滑动到的右边缘，为screenWidth
     */
    private int rightEdge;

    private boolean interrupt = false;

    private void changeViewport(int xDistance) {
        weekViews[curWeekNo].scrollTo(xDistance, 0);

        //在ViewFlipper工作机制中，某一个时间点只有当前的子View是VISIBLE状态，其他的子View都是GONE状态。
        //所以要在滑动的时候看见他们，必须将他们设置为VISIBLE状态
        if (xDistance > 0 && curWeekNo != 2) {
            weekViews[nextWeekNo].setVisibility(View.VISIBLE);
            //将下一个ImageView移动到(xNext, 0)位置
            int xNext = -screenWidth + xDistance;
            weekViews[nextWeekNo].scrollTo(xNext, 0);

        } else if (xDistance < 0 && curWeekNo != 0) {
            weekViews[previousWeekNo].setVisibility(View.VISIBLE);
            //将上一个ImageView移动到(xLast, 0)位置
            int xLast = screenWidth + xDistance;
            weekViews[previousWeekNo].scrollTo(xLast, 0);
        }

    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            createVelocityTracker(motionEvent);
            switch (motionEvent.getAction()) {
                //手指按下时的逻辑
                case MotionEvent.ACTION_DOWN:
//                    LogUtil.i(TAG, "MotionEvent.ACTION_DOWN");
                    xDown = motionEvent.getRawX();
                    break;
                //手指移动时的逻辑
                case MotionEvent.ACTION_MOVE:
//                    LogUtil.i(TAG, "ACTION_MOVE");
                    xMove = motionEvent.getRawX();
                    //手指滑动的距离
                    xDistance = -(int) (xMove - xDown);
//                    LogUtil.i(TAG, "-----------------dist = " + xDistance);

                    changeViewport(xDistance);
                    //当有移动发生，截断点击
                    if (Math.abs(xDistance) > 10) {
                        interrupt = true;
                    }
                    break;
                //手指抬起时的逻辑
                case MotionEvent.ACTION_UP:
//                    LogUtil.i(TAG, "ACTION_UP");
                    xUp = motionEvent.getRawX();
                    //判断用户手指的意图，这块可以自己改写逻辑
                    if (wantToShowPrevious()) {
                        if (shouldScrollToPrevious()) {
                            scrollToPrevious();
                        } else {
                            reset();
                        }
                    } else if (wantToShowNext()) {
                        if (shouldScrollToNext()) {
                            scrollToNext();
                        } else {
                            reset();
                        }
                    }
                    recycleVelocityTracker();
                    if (interrupt) {
                        interrupt = false;
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private void reset() {
        if (curWeekNo != 0) {
            weekViews[previousWeekNo].setVisibility(View.GONE);
            weekViews[previousWeekNo].scrollTo(0, 0);
        }
        if (curWeekNo != 2) {
            weekViews[nextWeekNo].setVisibility(View.GONE);
            weekViews[nextWeekNo].scrollTo(0, 0);
        }
        weekViews[curWeekNo].scrollTo(0, 0);
    }

    static final int NEXT = 0;
    static final int PREVIOUS = 1;


    private void complete(int flag) {
        weekViews[curWeekNo].setVisibility(View.GONE);
        //将当前（移动发生之前）的ImageView移动到（0，0）位置因为在滑动时它的位置被改变
        weekViews[curWeekNo].scrollTo(0, 0);
        if (flag == NEXT) {
            curWeekNo = curWeekNo + 1;
        } else if (flag == PREVIOUS) {
            curWeekNo = curWeekNo - 1;
        }

        nextWeekNo = curWeekNo + 1;
        previousWeekNo = curWeekNo - 1;

        myViewFlipper.setDisplayedChild(curWeekNo);
        myViewFlipper.setClickable(true);
    }

    private boolean wantToShowNext() {
        return xUp - xDown < 0;
    }

    private boolean wantToShowPrevious() {
        return xUp - xDown > 0;
    }

    private boolean shouldScrollToNext() {
        return curWeekNo != 2 && (xDown - xUp > screenWidth / 5 || getScrollVelocity() >
                SNAP_VELOCITY);
    }

    private boolean shouldScrollToPrevious() {
        return curWeekNo != 0 && (xUp - xDown > screenWidth / 5 || getScrollVelocity() > SNAP_VELOCITY);
    }

    private void scrollToNext() {
        new ScrollTask().execute(defaultSpeed);
    }

    private void scrollToPrevious() {
        new ScrollTask().execute(-defaultSpeed);
    }

    /**
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
     *
     * @param event content界面的滑动事件
     */
    private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 获取手指在content界面滑动的速度。
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... speed) {
            int curLocation = xDistance;
//暂时去除点击效果
            myViewFlipper.setClickable(false);

            // 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
            while (true) {
                curLocation = curLocation + speed[0];
                if (curLocation > rightEdge) {
                    curLocation = rightEdge;
                    break;
                }
                if (curLocation < leftEdge) {
                    curLocation = leftEdge;
                    break;
                }
                publishProgress(curLocation);
                // 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
                sleep(SLEEP_TIME);
            }

            return curLocation;
        }

        @Override
        protected void onProgressUpdate(Integer... curLocation) {
            changeViewport(curLocation[0]);
        }

        @Override
        protected void onPostExecute(Integer curLocation) {
            changeViewport(curLocation);
            if (curLocation > 0) {
                complete(NEXT);
            } else if (curLocation < 0) {
                complete(PREVIOUS);
            }
        }
    }

    /**
     * 使当前线程睡眠指定的毫秒数。
     *
     * @param millis 指定当前线程睡眠多久，以毫秒为单位
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //滑动效果相关至此结束
}
