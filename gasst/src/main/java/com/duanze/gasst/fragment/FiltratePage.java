package com.duanze.gasst.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.duanze.gasst.activity.Settings;
import com.duanze.gasst.activity.StartActivity;
import com.duanze.gasst.R;
import com.duanze.gasst.adapter.GNoteRVAdapter;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.provider.GNoteProvider;
import com.duanze.gasst.util.PreferencesUtils;
import com.faizmalkani.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Created by duanze on 2015/9/19.
 */
public class FiltratePage extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        GNoteRVAdapter.ItemLongPressedListener, GNoteRVAdapter.OnItemSelectListener {
    public static final String TAG = FiltratePage.class.getSimpleName();

    private static final int LOADER_ID = 113 + 1;//以示区分
    //MODE_LIST相关
    private Context mContext;
    private LoaderManager loaderManager;

    //nice FloatingButton
    private FloatingActionButton fabButton;
    private GNoteRVAdapter mAdapter;

    private void initValues() {
        mContext = getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initValues();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gnote_recycler, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_gnotes);

        int columnNum = 2;
        if (PreferencesUtils.getInstance(mContext).isOneColumn()) {
            columnNum = 1;
        }
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(columnNum,
                StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new GNoteRVAdapter(mContext, null, null, null);
        recyclerView.setAdapter(mAdapter);

        loaderManager = getLoaderManager();
//        loaderManager.initLoader(LOADER_ID, null, this);

        fabButton = (FloatingActionButton) view.findViewById(R.id.fabbutton);
        fabButton.setVisibility(View.GONE);

        // / In this fragment,we don't need it
        view.findViewById(R.id.refresher).setEnabled(false);
        return view;
    }

    public void clearResult() {
        if (null == mAdapter) return;
        mAdapter.swapCursor(null);
    }

    public static final String SELECTION = GNoteDB.CONTENT + " LIKE ?";
    private String selection;
    private String[] selectionArgs;

    public void startFiltrate(String selection, String[] selectionArgs) {
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        refreshUI();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = GNoteProvider.STANDARD_SORT_ORDER;
        if (PreferencesUtils.getInstance(mContext).isUseCreateOrder()) {
            sortOrder = GNoteProvider.STANDARD_SORT_ORDER2;
        }

        CursorLoader cursorLoader = new CursorLoader(mContext, GNoteProvider.BASE_URI,
                GNoteProvider.STANDARD_PROJECTION, selection, selectionArgs, sortOrder);
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

    @Override
    public void onDestroy() {
        if (null != mAdapter && null != mAdapter.getCursor()) {
            mAdapter.getCursor().close();
        }
        super.onDestroy();
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
            ((StartActivity) mContext).unlockDrawerLock();

            showFAB();

            mActionMode = null;
            mContextMenu = null;
            mAdapter.setCheckMode(false);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode arg0, Menu menu) {
            ((StartActivity) mContext).lockDrawerLock();

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
            builder.setMessage(R.string.delete_all_confirm)
                    .setPositiveButton(R.string.delete_sure, new
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
        mActionMode = ((StartActivity) mContext).startActionMode(mActionModeCallback);
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
