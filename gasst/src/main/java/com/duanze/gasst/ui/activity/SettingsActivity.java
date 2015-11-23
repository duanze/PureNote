package com.duanze.gasst.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.duanze.gasst.MyApplication;
import com.duanze.gasst.R;
import com.duanze.gasst.data.model.GNoteDB;
import com.duanze.gasst.data.model.GNotebook;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.sync.Evernote;
import com.duanze.gasst.ui.activity.base.BaseActivity;
import com.duanze.gasst.ui.adapter.ColorsListAdapter;
import com.duanze.gasst.util.ThemeUtils;
import com.duanze.gasst.util.Util;
import com.duanze.gasst.util.liteprefs.MyLitePrefs;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Duanze on 15-11-21.
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener, Evernote
        .EvernoteLoginCallback {

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    private Evernote mEvernote;
    private LinearLayout loginEvernote;
    private ImageView arrow;
    private TextView loginText;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_settings);

        initButtons();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.action_setting);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        loginEvernote = (LinearLayout) findViewById(R.id.login_evernote);
        arrow = (ImageView) findViewById(R.id.bind_arrow);
        loginText = (TextView) findViewById(R.id.login_text);

        mEvernote = new Evernote(this, this);
        if (mEvernote.isLogin()) {
            bindSuccess();
        }
        loginEvernote.setOnClickListener(this);

//container perform click
        findViewById(R.id.ll_password_container).setOnClickListener(this);
        findViewById(R.id.ll_note_max_length).setOnClickListener(this);
        findViewById(R.id.ll_universal).setOnClickListener(this);
        findViewById(R.id.ll_quick_write_location_container).setOnClickListener(this);
        findViewById(R.id.ll_lightning_container).setOnClickListener(this);
        findViewById(R.id.ll_extract_location_container).setOnClickListener(this);
        findViewById(R.id.ll_quick_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmService.alarmTask(mContext);
            }
        });
        findViewById(R.id.ll_notification_container).setOnClickListener(this);
        findViewById(R.id.ll_one_column).setOnClickListener(this);
        findViewById(R.id.ll_create_order).setOnClickListener(this);
        findViewById(R.id.ll_concentrate_write).setOnClickListener(this);
        findViewById(R.id.ll_choose_theme).setOnClickListener(this);
    }

    private void bindSuccess() {
        arrow.setVisibility(View.GONE);
        if (mEvernote.getUsername() == null) {
            loginText.setText(R.string.logout_evernote_username_null);
            mEvernote.getUserInfo();
        } else {
            loginText.setText(getString(R.string.logout_evernote, mEvernote.getUsername()));
        }
    }

    /**
     * Called when the control returns from an activity that we launched.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                mEvernote.onAuthFinish(resultCode);
                break;
        }
    }

    private TextView passwordGuard;
    private TextView maxLengthRatio;
    private Switch lightningExtract;
    private Switch alwaysShow;
    private Switch concentrateWrite;
    private TextView extractLocationSummary;
    private TextView quickLocationSummary;
    private CheckBox universal;
    private CheckBox oneColumn;
    private CheckBox createOrder;

    private void initButtons() {
        concentrateWrite = (Switch) findViewById(R.id.s_concentrate_write);
        concentrateWrite.setChecked(MyLitePrefs.getBoolean(MyLitePrefs.CONCENTRATE_WRITE));
        concentrateWrite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyLitePrefs.putBoolean(MyLitePrefs.CONCENTRATE_WRITE, isChecked);
            }
        });

        oneColumn = (CheckBox) findViewById(R.id.cb_one_column);
        oneColumn.setChecked(MyLitePrefs.getBoolean(MyLitePrefs.ONE_COLUMN));
        oneColumn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyLitePrefs.putBoolean(MyLitePrefs.ONE_COLUMN, isChecked);
                refreshStartActivity(StartActivity.NEED_CONFIG_LAYOUT);
            }
        });

        createOrder = (CheckBox) findViewById(R.id.cb_create_order);
        createOrder.setChecked(MyLitePrefs.getBoolean(MyLitePrefs.CREATE_ORDER));
        createOrder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyLitePrefs.putBoolean(MyLitePrefs.CREATE_ORDER, isChecked);
                refreshStartActivity(StartActivity.NEED_NOTIFY);
            }
        });

        maxLengthRatio = (TextView) findViewById(R.id.tv_note_max_length);
        float ratio = MyLitePrefs.getFloat(MyLitePrefs.NOTE_MAX_LENGTH_RATIO);
        maxLengthRatio.setText(String.valueOf(ratio));

        passwordGuard = (TextView) findViewById(R.id.tv_password_guard);
        setGuardText(MyLitePrefs.getBoolean(MyLitePrefs.PASSWORD_GUARD));
        passwordGuard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MyLitePrefs.getBoolean(MyLitePrefs.PASSWORD_GUARD)) {
                    showCreatePasswordDialog();
                } else {
                    showCancelPasswordDialog();
                }
            }
        });


        if (MyLitePrefs.getBoolean(MyLitePrefs.SHOW_UNIVERSAL_SWITCH)) {
            findViewById(R.id.mr_universal_container).setVisibility(View.VISIBLE);
            findViewById(R.id.v_universal).setVisibility(View.VISIBLE);
        }
        universal = (CheckBox) findViewById(R.id.cb_universal);
        universal.setChecked(MyLitePrefs.getBoolean(MyLitePrefs.USE_UNIVERSAL_PASSWORD));
        universal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyLitePrefs.putBoolean(MyLitePrefs.USE_UNIVERSAL_PASSWORD, b);
            }
        });

        alwaysShow = (Switch) findViewById(R.id.s_notification_always_show);
        alwaysShow.setChecked(MyLitePrefs.getBoolean(MyLitePrefs.NOTIFICATION_ALWAYS_SHOW));
        alwaysShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyLitePrefs.putBoolean(MyLitePrefs.NOTIFICATION_ALWAYS_SHOW, isChecked);
                AlarmService.showOrHide(mContext);
            }
        });

        lightningExtract = (Switch) findViewById(R.id.s_lightning_extract);
        lightningExtract.setChecked(MyLitePrefs.getBoolean(MyLitePrefs.LIGHTNING_EXTRACT));
        lightningExtract.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyLitePrefs.putBoolean(MyLitePrefs.LIGHTNING_EXTRACT, isChecked);
                if (isChecked) {
                    if (Util.isServiceWork(mContext, "com.duanze.gasst.service.AlarmService")) {
                        AlarmService.startExtractTask(mContext);
                    } else {
                        AlarmService.alarmTask(mContext);
                    }
                } else {
                    if (Util.isServiceWork(mContext, "com.duanze.gasst.service.AlarmService")) {
                        AlarmService.stopExtractTask(mContext);
                    }
                }
            }
        });

        quickLocationSummary = (TextView) findViewById(R.id.tv_quick_location_summary);
        quickLocationSummary.setText(Util.readSaveLocation(MyLitePrefs.QUICK_WRITE_SAVE_LOCATION, mContext));
        quickLocationSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectSaveLocationDialog(MyLitePrefs.QUICK_WRITE_SAVE_LOCATION, quickLocationSummary);
            }
        });

        extractLocationSummary = (TextView) findViewById(R.id.tv_extract_location_summary);
        extractLocationSummary.setText(Util.readSaveLocation(MyLitePrefs.LIGHTNING_EXTRACT_SAVE_LOCATION, mContext));
        extractLocationSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectSaveLocationDialog(MyLitePrefs.LIGHTNING_EXTRACT_SAVE_LOCATION, extractLocationSummary);
            }
        });

    }

    private void refreshStartActivity(int message) {
        MyApplication application = (MyApplication) getApplication();
        StartActivity.SyncHandler handler = application.getHandler();
        handler.sendEmptyMessage(message);
    }

    private void setGuardText(boolean b) {
        if (b) {
            passwordGuard.setText(R.string.password_guard_on);
        } else {
            passwordGuard.setText(R.string.password_guard_off);
        }
    }

    private void showCancelPasswordDialog() {
        View view = getLayoutInflater().inflate(R.layout.stop_password_dialog,
                (ViewGroup) getWindow().getDecorView
                        (), false);
        final EditText password = (EditText) view.findViewById(R.id.et_password);
        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.stop_password_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String _password = password.getText().toString();
                        String ans = MyLitePrefs.getString(MyLitePrefs.PASSWORD);
                        final boolean useUniversal = MyLitePrefs.getBoolean(MyLitePrefs.USE_UNIVERSAL_PASSWORD);
                        if (ans.equals(_password) ||
                                useUniversal && MyLitePrefs.UNIVERSAL_PASSWORD.equals(_password)) {
                            setGuardText(false);
                            Toast.makeText(mContext, R.string.password_guard_stop, Toast.LENGTH_SHORT).show();

                            MyLitePrefs.putBoolean(MyLitePrefs.PASSWORD_GUARD, false);
                        } else {
                            Toast.makeText(mContext, R.string.password_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void showCreatePasswordDialog() {
        View view = getLayoutInflater().inflate(R.layout.password_dialog, (ViewGroup) getWindow()
                .getDecorView(), false);
        final EditText password = (EditText) view.findViewById(R.id.et_password);
        final EditText confirm = (EditText) view.findViewById(R.id.et_confirm);
        final EditText hint = (EditText) view.findViewById(R.id.et_hint);
        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.password_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String _password = password.getText().toString();
                        String _confirm = confirm.getText().toString();
                        String _hint = hint.getText().toString().trim();
                        if (_password.equals(_confirm) && _password.length() > 0) {
                            MyLitePrefs.putString(MyLitePrefs.PASSWORD, _password);
                            MyLitePrefs.putString(MyLitePrefs.PASSWORD_HINT, _hint);

                            setGuardText(true);
                            Toast.makeText(mContext, R.string.password_guard_start, Toast.LENGTH_SHORT).show();
                            MyLitePrefs.putBoolean(MyLitePrefs.PASSWORD_GUARD, true);
                        } else {
                            Toast.makeText(mContext, R.string.passwords_differ, Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private int tmpLocation;
    private String tmpLocationName;

    private void showSelectSaveLocationDialog(final String key, final TextView tv) {
        View view = getLayoutInflater().inflate(R.layout.dialog_radiogroup, (ViewGroup) getWindow().getDecorView(), false);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rg_dialog);

        boolean radioChecked = false;
        List<GNotebook> list = GNoteDB.getInstance(mContext).loadGNotebooks();

        tmpLocation = MyLitePrefs.getInt(key);
        tmpLocationName = mContext.getString(R.string.all_notes);

        for (final GNotebook gNotebook : list) {
            RadioButton tempButton = new RadioButton(mContext);
            tempButton.setText(gNotebook.getName());
            radioGroup.addView(tempButton, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            if (gNotebook.getId() == tmpLocation) {
                tempButton.setChecked(true);
                radioChecked = true;
                tmpLocationName = gNotebook.getName();
            }

            tempButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        tmpLocation = gNotebook.getId();
                        tmpLocationName = gNotebook.getName();
                    }
                }
            });
        }

        RadioButton purenote = (RadioButton) view.findViewById(R.id.rb_purenote);
        if (!radioChecked) {
            purenote.setChecked(true);
        }
        purenote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    tmpLocation = 0;
                    tmpLocationName = mContext.getString(R.string.all_notes);
                }
            }
        });

        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.lightning_extract_save_location)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyLitePrefs.putInt(key, tmpLocation);
                        tv.setText(tmpLocationName);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return true;
        }
    }

    //    ------------------------------------------- implements
    @Override
    public void onLoginResult(Boolean result) {
        if (result) {
            bindSuccess();
//            preferences.edit().putBooleanLite("sync_now", true).apply();
        }
    }

    @Override
    public void onUserInfo(Boolean result, User user) {
        if (null == user) return;
        loginText.setText(getString(R.string.logout_evernote, user.getUsername()));
    }

    @Override
    public void onLogout(Boolean result) {
        if (result) {
            arrow.setVisibility(View.VISIBLE);
            loginText.setText(R.string.login_evernote);
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_evernote:
                loginEverNote();
                break;
            case R.id.ll_password_container:
                passwordGuard.performClick();
                break;
            case R.id.ll_note_max_length:
                inputMaxLengthRatio();
                break;
            case R.id.ll_universal:
                if (universal != null) {
                    universal.performClick();
                }
                break;
            case R.id.ll_quick_write_location_container:
                quickLocationSummary.performClick();
                break;
            case R.id.ll_lightning_container:
                lightningExtract.toggle();
                break;
            case R.id.ll_extract_location_container:
                extractLocationSummary.performClick();
                break;
            case R.id.ll_notification_container:
                alwaysShow.toggle();
                break;
            case R.id.ll_one_column:
                oneColumn.performClick();
                break;
            case R.id.ll_create_order:
                createOrder.performClick();
                break;
            case R.id.ll_concentrate_write:
                concentrateWrite.performClick();
                break;
            case R.id.ll_choose_theme:
                chooseThemeDialog();
                break;
            default:
                break;
        }
    }

    private void loginEverNote() {
        if (!mEvernote.isLogin()) {
            mEvernote.auth();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.logout_text)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {


                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    mEvernote.Logout();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show();
        }
    }

    private void chooseThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_theme_title);
        List<Integer> list = Arrays.asList(ThemeUtils.THEME_RES_ARR);
        ColorsListAdapter adapter = new ColorsListAdapter(this, list);
        adapter.setCheckItem(ThemeUtils.getCurrentTheme(this).getIntValue());
        GridView gridView = (GridView) LayoutInflater.from(this).inflate(R.layout.colors_panel_layout, null);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setCacheColorHint(0);
        gridView.setAdapter(adapter);
        builder.setView(gridView);
        final AlertDialog dialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                onThemeChosen(position);
            }
        });
    }

    private void onThemeChosen(int position) {
        MyLitePrefs.setTheme(position);
        refreshStartActivity(StartActivity.NEED_RECREATE);
        finish();
    }

    private void inputMaxLengthRatio() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edittext, (ViewGroup) getWindow().getDecorView(), false);
        final EditText editText = (EditText) view.findViewById(R.id.et_in_dialog);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        String originalRatio = maxLengthRatio.getText().toString();
        editText.setText(originalRatio);
        editText.setSelection(originalRatio.length());

        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.note_max_length_dialog_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ratio = editText.getText().toString();
                        float r = Float.valueOf(ratio);
                        float rMin = Float.valueOf(getString(R.string.note_max_length_min));
                        float rMax = Float.valueOf(getString(R.string.note_max_length_max));
                        if (r >= rMin && r <= rMax) {
                            maxLengthRatio.setText(ratio);
                            MyLitePrefs.putFloat(MyLitePrefs.NOTE_MAX_LENGTH_RATIO, r);
                            refreshStartActivity(StartActivity.NEED_RECREATE);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}