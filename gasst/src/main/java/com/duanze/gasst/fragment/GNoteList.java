package com.duanze.gasst.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.duanze.gasst.MainActivity;
import com.duanze.gasst.R;
import com.duanze.gasst.activity.Note;
import com.duanze.gasst.adapter.GNoteAdapter;
import com.duanze.gasst.provider.GNoteProvider;
import com.duanze.gasst.util.Util;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

/**
 * Created by duanze on 2015/9/19.
 */
public class GNoteList extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = GNoteList.class.getSimpleName();

    //MODE_LIST相关
    private Context mContext;
    //nice FloatingButton
    private FloatingActionButton fabButton;

    private GNoteAdapter mAdapter;

//    public static final String DATEPICKER_TAG = "datepicker";
//    private DatePickerDialog pickerDialog;
//    private DateTimePickerCallback listener = new DateTimePickerCallback() {
//        @Override
//        public void onFinish(String result) {
//            gNote.setAlertTime(result);
//            gNote.setIsPassed(GNote.FALSE);
//            db.updateGNote(gNote);
//            AlarmService.alarmTask(mContext);
//
//        }
//
//        @Override
//        public void onError(Exception e) {
//
//        }
//    };

    private void initValues() {
        mContext = getActivity();

//        pickerDialog = DatePickerDialog.newInstance(new MyPickerListener(mContext, today, listener),
//                today.get(Calendar.YEAR), today.get(Calendar.MONTH),
//                today.get(Calendar.DAY_OF_MONTH), false);
//        pickerDialog.setYearRange(today.get(Calendar.YEAR) - 10,
//                today.get(Calendar.YEAR) + 10);
//        pickerDialog.setCloseOnSingleTapDay(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initValues();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gnote_list, container, false);
        ListView gNoteList = (ListView) view.findViewById(R.id.lv_gnotes);
        mAdapter = new GNoteAdapter(mContext, null, 0);
        MainActivity mainActivity = ((MainActivity)mContext);
        mAdapter.setValues(mainActivity.getIsFold(), mainActivity.getToday(), mainActivity.getMaxLines());
        gNoteList.setAdapter(mAdapter);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(1,null,this);

        fabButton = (FloatingActionButton) view.findViewById(R.id.fabbutton);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note.todayNewNote(mContext);
            }
        });
        fabButton.listenTo(gNoteList);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        randomFABButtonColor();
    }

    public void randomFABButtonColor() {
        Util.randomBackground(fabButton);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(
                mContext
                , GNoteProvider.BASE_URI
                , GNoteProvider.STANDARD_PROJECTION
                , null
                , null
                , GNoteProvider.STANDARD_SORT_ORDER
        );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void refreshUI() {

    }
}
