package com.duanze.gasst.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duanze.gasst.R;
import com.duanze.gasst.adapter.NotebookAdapter;
import com.duanze.gasst.fragment.FolderFooter;
import com.duanze.gasst.fragment.FolderFooterDelete;
import com.duanze.gasst.fragment.FooterInterface;
import com.duanze.gasst.model.GNote;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.view.FolderUnit;

import java.util.List;

/**
 * Created by Duanze on 2015/5/22.
 */
public class Folder extends Activity implements FooterInterface, CompoundButton.OnCheckedChangeListener {
    public static final String TAG = "Folder";

    public static final String GNOTEBOOK_ID = "gnotebook_id";
    public static final String BOOK_ID_FOR_NOTE = "book_id_for_note";
    public static final String PURENOTE_NOTE_NUM = "purenote_note_num";

    public static final int MODE_FOLDER = 0;
    public static final int MODE_MOVE = 1;
    public static final int MODE_FOOTER = 2;
    public static final int MODE_FOOTER_DELETE = 3;

    private int mode;
    private int modeFooter;
    private SharedPreferences preferences;
    private boolean settingChanged;

    @Override
    public int getDeleteNum() {
        return deleteNum;
    }

    private int deleteNum;

    private FolderFooter footer;
    private FolderFooterDelete footerDelete;
    private FolderUnit purenote;
    private ImageView purenoteFlag;

    private ListView folderListView;
    private List<GNotebook> gNotebookList;
    private NotebookAdapter notebookAdapter;
    private GNoteDB db;
    private Context mContext;

    private int folderId;
    private int originalFolderId;

    public static void activityStart(Context context, int mode) {
        Intent intent = new Intent(context, Folder.class);
        intent.putExtra("mode", mode);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.in_push_right_to_left,
                R.anim.in_stable);
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(Settings.DATA, MODE_PRIVATE);
//        boolean fScreen = preferences.getBoolean(Settings.FULL_SCREEN, false);
//        //如果设置了全屏
//        if (fScreen) {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }

        init();

    }

    private void init() {
        setContentView(R.layout.folder_activity);
        purenote = (FolderUnit) findViewById(R.id.fu_purenote);
        purenoteFlag = (ImageView) purenote.findViewById(R.id.iv_folder_unit_flag);
        TextView purenoteNum = (TextView) purenote.findViewById(R.id.tv_folder_unit_num);
        purenoteNum.setText(getString(R.string.folder_note_num,
                preferences.getInt(PURENOTE_NOTE_NUM, 3)));
        folderListView = (ListView) findViewById(R.id.lv_folder);

        settingChanged = false;
        mode = getIntent().getIntExtra("mode", MODE_FOLDER);
        ActionBar actionBar = getActionBar();
        if (mode == MODE_FOLDER) {
            actionBar.setTitle(R.string.action_folder);
        } else if (mode == MODE_MOVE) {
            actionBar.setTitle(R.string.action_move);
            purenoteFlag.setImageResource(R.drawable.map_pin_angle_3);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);

        modeFooter = MODE_FOOTER;
        readFolderId();
        setFooter();

        mContext = this;
        db = GNoteDB.getInstance(mContext);
        gNotebookList = db.loadGNotebooks();
        if (MODE_FOLDER == mode) {
            notebookAdapter = new NotebookAdapter(mContext, R.layout.folder_unit, gNotebookList, folderListView);
        } else if (MODE_MOVE == mode) {
            notebookAdapter = new NotebookAdapter(mContext, R.layout.folder_unit_move, gNotebookList,
                    folderListView);
        }
        folderListView.setAdapter(notebookAdapter);
        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (folderId != gNotebookList.get(i).getId()) {
                    hideFlag(folderId);
                    folderId = gNotebookList.get(i).getId();
                    ImageView flag = (ImageView) view.findViewById(R.id.iv_folder_unit_flag);
                    flag.setVisibility(View.VISIBLE);
                }
            }
        });

        if (folderId != 0) {
            purenoteFlag.setVisibility(View.INVISIBLE);
        }

        purenote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (folderId != 0) {
                    hideFlag(folderId);
                    purenoteFlag.setVisibility(View.VISIBLE);
                    folderId = 0;
                }
            }
        });
    }

    private void readFolderId() {
        folderId = preferences.getInt(GNOTEBOOK_ID, 0);
        originalFolderId = folderId;

        if (folderId == 0) {
            showFlag(0);
        }
    }

    private void hideFlag(int id) {
        if (id == 0) {
            purenoteFlag.setVisibility(View.INVISIBLE);
        } else {
            for (int j = 0; j < gNotebookList.size(); j++) {
                if (id == gNotebookList.get(j).getId()) {
                    ImageView flag = (ImageView) folderListView.getChildAt(j)
                            .findViewById(R.id.iv_folder_unit_flag);
                    flag.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        }
    }

    private void showFlag(int id) {
        if (id == 0) {
            purenoteFlag.setVisibility(View.VISIBLE);
        } else {
            for (int j = 0; j < gNotebookList.size(); j++) {
                if (id == gNotebookList.get(j).getId()) {
                    ImageView flag = (ImageView) folderListView.getChildAt(j)
                            .findViewById(R.id.iv_folder_unit_flag);
                    flag.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
    }

    public void setFooter() {
        if (modeFooter == MODE_FOOTER) {
            if (footer == null) {
                footer = new FolderFooter();
            }
            getFragmentManager().beginTransaction().replace(R.id.fl_folder_footer, footer)
                    .commit();
        } else if (modeFooter == MODE_FOOTER_DELETE) {
            if (footerDelete == null) {
                footerDelete = new FolderFooterDelete();
            }
            getFragmentManager().beginTransaction().replace(R.id.fl_folder_footer, footerDelete)
                    .commit();
        }
    }

    /**
     * 捕获Back按键
     */
    @Override
    public void onBackPressed() {
        exitOperation();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exitOperation();
                return true;
            default:
                return true;
        }
    }

    /**
     * 退出时的操作，用于 重写Back键 与 导航键
     */
    private void exitOperation() {
        if (originalFolderId != folderId) {

            if (mode == MODE_FOLDER) {
                settingChanged = true;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(Settings.SETTINGS_CHANGED, settingChanged).apply();

                selectFolder(folderId);
                cancelFolder(originalFolderId);
                editor.putInt(GNOTEBOOK_ID, folderId).apply();
                LogUtil.i(TAG, "gNotebook id:" + folderId);
            } else if (mode == MODE_MOVE) {
                Intent intent = new Intent();
                intent.putExtra(BOOK_ID_FOR_NOTE, folderId);
                setResult(RESULT_OK, intent);
                LogUtil.i(TAG, "Move Mode :gNotebook id:" + folderId);
            }
        }
        finish();
        overridePendingTransition(R.anim.in_stable,
                R.anim.out_push_left_to_right);
    }

    private void selectFolder(int id) {
        if (id == 0) {
            return;
        } else {
            for (GNotebook gNotebook : gNotebookList) {
                if (id == gNotebook.getId()) {
                    gNotebook.setSelected(GNotebook.TRUE);
                    db.updateGNotebook(gNotebook);
                    break;
                }
            }
        }
    }

    private void cancelFolder(int id) {
        if (id == 0) {
            return;
        } else {
            for (GNotebook gNotebook : gNotebookList) {
                if (id == gNotebook.getId()) {
                    gNotebook.setSelected(GNotebook.FALSE);
                    db.updateGNotebook(gNotebook);
                    break;
                }
            }
        }
    }


    /**
     * 删除按钮的方法
     */
    private void trash() {
        new AlertDialog.Builder(this).
                setTitle(R.string.alert).
                setMessage(R.string.delete_text).
                setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                }).
                setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        for (int i = 0; i < folderListView.getCount() && deleteNum > 0; i++) {
                            CheckBox checkBox = (CheckBox) folderListView.getChildAt(i).findViewById(R.id
                                    .cb_folder_unit);
                            if (checkBox.isChecked()) {
                                db.deleteGNotebook(gNotebookList.get(i));
                                deleteNoteInBook(gNotebookList.get(i).getId());
//                        清除所有checkBox状态防错位,此时会触发监听器
                                checkBox.setChecked(false);

//                                如果selected笔记本在被删除之列，将笔记本还原为默认值
                                if (gNotebookList.get(i).getSelected() == GNotebook.TRUE) {
                                    restoreDefault();
                                }
                            }
                        }
                        refreshFolderList();
                        changeFooter();
                    }
                }).show();
    }

    private void deleteNoteInBook(int id) {
        List<GNote> list = db.loadGNotesByBookId(id);
        for (GNote gNote : list) {
            gNote.setDeleted(GNote.TRUE);
            if ("".equals(gNote.getGuid())) {
                db.deleteGNote(gNote.getId());
            } else {
                gNote.setSynStatus(GNote.DELETE);
                db.updateGNote(gNote);
            }

            if (!gNote.isPassed()) {
                AlarmService.cancelTask(mContext, gNote);
            }
        }
    }

    private void restoreDefault() {
        preferences.edit().putInt(GNOTEBOOK_ID, 0).commit();
        showFlag(0);
        settingChanged = true;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Settings.SETTINGS_CHANGED, settingChanged).apply();
    }


    public void showCheckBox() {
        for (int i = 0; i < folderListView.getCount(); i++) {
            folderListView.getChildAt(i).findViewById(R.id.cb_folder_unit).setVisibility(View.VISIBLE);
        }
    }

    public void hideCheckBox() {
        for (int i = 0; i < folderListView.getCount(); i++) {
            folderListView.getChildAt(i).findViewById(R.id.cb_folder_unit).setVisibility(View.INVISIBLE);
        }
    }

    private void showCreateFolderDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edittext, (ViewGroup) getWindow().getDecorView(), false);
        final EditText editText = (EditText) view.findViewById(R.id.et_in_dialog);

        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.create_folder_title)
                .setView(view)
                .setPositiveButton(R.string.create_folder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().length() == 0) {
                            Toast.makeText(mContext, R.string.create_folder_err, Toast.LENGTH_SHORT).show();
                        } else {
                            createFolder(editText.getText().toString());
                            refreshFolderList();
                        }
//                        closeKeyboard();
                    }
                })
                .setNegativeButton(R.string.folder_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        closeKeyboard();
                    }
                })
                .create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /**
     * 关闭软键盘
     */
    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void createFolder(String name) {
        GNotebook gNotebook = new GNotebook();
        gNotebook.setName(name);
        db.saveGNotebook(gNotebook);
    }

    private void refreshFolderList() {
        gNotebookList.clear();
        List<GNotebook> tmpList = db.loadGNotebooks();
        for (GNotebook g : tmpList) {
            gNotebookList.add(g);
        }

        notebookAdapter.notifyDataSetChanged();
//        noteTitleListView.setSelection(0);
//        hideFlag(originalFolderId);
//        showFlag(folderId);
//        listview的更新后似乎其数据源list与listview.getchildat 方法不再一一对应
        readFolderId();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            deleteNum++;
        } else {
            deleteNum--;
        }
        footerDelete.deleteNum(deleteNum);
    }

    @Override
    public void changeFooter() {
        if (modeFooter == MODE_FOOTER) {
            modeFooter = MODE_FOOTER_DELETE;
            showCheckBox();
        } else {
            modeFooter = MODE_FOOTER;
            hideCheckBox();
        }
        setFooter();
    }

    @Override
    public void actionClick() {
        if (MODE_FOOTER == modeFooter) {
            showCreateFolderDialog();
        } else if (MODE_FOOTER_DELETE == modeFooter) {
            if (deleteNum <= 0) {
                return;
            } else {
                trash();
            }
        }
    }
}
