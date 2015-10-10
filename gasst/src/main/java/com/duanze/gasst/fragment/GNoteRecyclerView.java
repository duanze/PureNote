package com.duanze.gasst.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.duanze.gasst.MainActivity;
import com.duanze.gasst.R;
import com.duanze.gasst.activity.Note;
import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.adapter.GNoteRVAdapter;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.provider.GNoteProvider;
import com.duanze.gasst.util.LogUtil;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanze on 2015/9/19.
 */
public class GNoteRecyclerView extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        GNoteRVAdapter.ItemLongPressedListener, GNoteRVAdapter.OnItemSelectListener {
    public static final String TAG = GNoteRecyclerView.class.getSimpleName();

    private static final int LOADER_ID = 113;
    //MODE_LIST相关
    private Context mContext;
    private SharedPreferences preferences;
    private LoaderManager loaderManager;

    //nice FloatingButton
    private FloatingActionButton fabButton;
    private GNoteRVAdapter mAdapter;
    private Filter filter;

    private void initValues() {
        mContext = getActivity();
        preferences = ((MainActivity) mContext).getPreferences();
    }

    public GNoteRVAdapter getGNoteRVAdapter() {
        return mAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initValues();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gnote_recycler, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_gnotes);

        boolean isOneColumn = preferences.getBoolean(getString(R.string.one_column_key), false);
        int columnNum = 2;
        if (isOneColumn) {
            columnNum = 1;
        }
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(columnNum,
                StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new GNoteRVAdapter(mContext, null, this, this);
        mAdapter.setPreferences(preferences);
        recyclerView.setAdapter(mAdapter);

        loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_ID, null, this);

        fabButton = (FloatingActionButton) view.findViewById(R.id.fabbutton);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note.writeTodayNewNote(mContext);
            }
        });
        fabButton.listenTo(recyclerView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        randomFABButtonColor();
    }

//    public void randomFABButtonColor() {
//        Util.randomBackground(fabButton);
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int bookId = preferences.getInt(Settings.GNOTEBOOK_ID, 0);
        String selection = GNoteDB.GNOTEBOOK_ID + " = ?";
        String[] selectionArgs = {"" + bookId};
        if (0 == bookId) {
            selection = null;
            selectionArgs = null;
        }

        CursorLoader cursorLoader = new CursorLoader(mContext, GNoteProvider.BASE_URI,
                GNoteProvider.STANDARD_PROJECTION, selection, selectionArgs, GNoteProvider
                .STANDARD_SORT_ORDER);
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
        loaderManager.restartLoader(LOADER_ID, null, this);
    }

    //    The followings are about ActionMode
    private Menu mContextMenu;
    private int tmpGNoteBookId;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onActionItemClicked(ActionMode arg0, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.delete:
                    deleteNotes();
                    break;
                case R.id.i_move:
                    moveNotes();
                    break;
                case R.id.i_select_all:
                    selectAll();
                    break;
                default:
                    break;
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            ((MainActivity) mContext).unlockDrawerLock();

            showFAB();

            mActionMode = null;
            mContextMenu = null;
            mAdapter.setCheckMode(false);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode arg0, Menu menu) {
            ((MainActivity) mContext).lockDrawerLock();

            dismissFAB();

            mContextMenu = menu;
            updateActionMode();
            return false;
        }

    };

    public void dismissFAB() {
        if (fabButton.isLock()) return;
        fabButton.hide(true);
        fabButton.setLock(true);
    }

    public void showFAB() {
        if (!fabButton.isLock()) return;
        fabButton.setLock(false);
        fabButton.hide(false);
    }

    private void selectAll() {
        mAdapter.selectAllNotes();
    }

    private void moveNotes() {
        if (mAdapter.getSelectedCount() == 0) {
            Toast.makeText(mContext, R.string.delete_select_nothing, Toast
                    .LENGTH_SHORT).show();
        } else {
            View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout
                    .dialog_radiogroup, (ViewGroup) ((Activity) mContext).getWindow()
                    .getDecorView(), false);

            final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id
                    .rg_dialog);
            RadioButton purenote = (RadioButton) view.findViewById(R.id.rb_purenote);
            GNoteDB db = GNoteDB.getInstance(mContext);
            List<GNotebook> list = db.loadGNotebooks();
            for (final GNotebook gNotebook : list) {
                RadioButton tempButton = new RadioButton(mContext);
                tempButton.setText(gNotebook.getName());
                radioGroup.addView(tempButton, LinearLayout.LayoutParams
                        .MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                tempButton.setOnCheckedChangeListener(new CompoundButton
                        .OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton,
                                                 boolean b) {
                        if (b) {
                            tmpGNoteBookId = gNotebook.getId();
                        }
                    }
                });
            }

            purenote.setChecked(true);
            tmpGNoteBookId = 0;
            purenote.setOnCheckedChangeListener(new CompoundButton
                    .OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        tmpGNoteBookId = 0;
                    }
                }
            });

            final Dialog dialog = new AlertDialog.Builder(mContext).setTitle(R.string
                    .action_move).setView(view).setPositiveButton(R.string.confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAdapter.moveSelectedNotes(tmpGNoteBookId);
                            if (mActionMode != null) {
                                mActionMode.finish();
                            }

                        }
                    }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
        }
    }

    private void deleteNotes() {
        if (mAdapter.getSelectedCount() == 0) {
            Toast.makeText(mContext, R.string.delete_select_nothing, Toast
                    .LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.delete_all_confirm).setTitle(R.string
                    .delete_title).setPositiveButton(R.string.delete_sure, new
                    DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAdapter.deleteSelectedNotes();
                            if (mActionMode != null) {
                                mActionMode.finish();
                            }

                        }
                    }).setNegativeButton(R.string.delete_cancel, null).create().show();
        }
    }

    private ActionMode mActionMode;

    @Override
    public void startActionMode() {
        if (mActionMode != null) {
            return;
        }
        mActionMode = ((MainActivity) mContext).startActionMode(mActionModeCallback);
    }

    public void updateActionMode() {
        if (mAdapter.getSelectedCount() <= 1) {
            mContextMenu.findItem(R.id.selected_counts).setTitle(mContext.getString(R.string
                    .selected_one_count, mAdapter.getSelectedCount()));
        } else {
            mContextMenu.findItem(R.id.selected_counts).setTitle(mContext.getString(R.string
                    .selected_more_count, mAdapter.getSelectedCount()));
        }
    }

    @Override
    public void onSelect() {
        updateActionMode();
    }

    @Override
    public void onCancelSelect() {
        updateActionMode();
    }
}
