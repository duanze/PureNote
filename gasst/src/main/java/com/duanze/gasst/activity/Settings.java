package com.duanze.gasst.activity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.duanze.gasst.R;
import com.duanze.gasst.adapter.ColorsListAdapter;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.syn.Evernote;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.PreferencesUtils;
import com.duanze.gasst.util.ThemeUtils;
import com.duanze.gasst.util.Util;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;

import java.util.Arrays;
import java.util.List;


public class Settings extends BaseActivity implements View.OnClickListener, Evernote
        .EvernoteLoginCallback {
    public static final String TAG = "Settings";

    public static final String GNOTEBOOK_ID = "gnotebook_id";
    public static final String PURENOTE_NOTE_NUM = "purenote_note_num";

    public static final String DATA = "gasst_pref";
    public static final String PASSWORD_GUARD = "password_guard";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_HINT = "password_hint";
    public static final String UNIVERSAL_PASSWORD = "-_-#";
    public static final String USE_UNIVERSAL_PASSWORD = "use_universal_password";
    public static final String SHOW_UNIVERSAL_SWITCH = "show_universal_switch";
    public static final String LIGHTNING_EXTRACT = "lightning_extract";
    public static final String LIGHTNING_EXTRACT_SAVE_LOCATION = "lightning_extract_save_location";
    public static final String QUICK_WRITE_SAVE_LOCATION = "quick_write_save_location";

    public static final String NOTIFICATION_ALWAYS_SHOW = "notification_always_show";

    public static final String SETTINGS_CHANGED = "is_changed";

    private SharedPreferences preferences;
    private boolean settingChanged = false;

    private Evernote mEvernote;
    private LinearLayout loginEvernote;
    private ImageView arrow;
    private TextView loginText;
    private Context mContext;
    private GNoteDB db;


    public static void activityStart(Context context) {
        Intent intent = new Intent(context, Settings.class);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(DATA, MODE_PRIVATE);
        mContext = this;
        db = GNoteDB.getInstance(mContext);
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
        passwordGuard = (TextView) findViewById(R.id.tv_password_guard);
        boolean b = preferences.getBoolean(PASSWORD_GUARD, false);
        setGuardText(b);
        passwordGuard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ans = preferences.getBoolean(PASSWORD_GUARD, false);
                if (!ans) {
                    showCreatePasswordDialog();
                } else {
                    showCancelPasswordDialog();
                }
            }
        });

        maxLengthRatio = (TextView) findViewById(R.id.tv_note_max_length);
        float ratio = preferences.getFloat(getString(R.string.note_max_length_key), (float) 0.418);
        maxLengthRatio.setText(String.valueOf(ratio));

        universal = (CheckBox) findViewById(R.id.cb_universal);
        boolean useUniversalPassword = preferences.getBoolean(USE_UNIVERSAL_PASSWORD, true);
        universal.setChecked(useUniversalPassword);
        universal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit().putBoolean(USE_UNIVERSAL_PASSWORD, b).apply();
            }
        });

        boolean showUniversalSwitch = preferences.getBoolean(SHOW_UNIVERSAL_SWITCH, false);
        if (showUniversalSwitch) {
//show the parent layout
            View view = findViewById(R.id.mr_universal_container);
            view.setVisibility(View.VISIBLE);
            view = findViewById(R.id.v_universal);
            view.setVisibility(View.VISIBLE);
        }

        lightningExtract = (Switch) findViewById(R.id.s_lightning_extract);
        b = preferences.getBoolean(LIGHTNING_EXTRACT, false);
        lightningExtract.setChecked(b);
        lightningExtract.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean(LIGHTNING_EXTRACT, isChecked).apply();
                if (isChecked) {
                    if (Util.isServiceWork(mContext, "com.duanze.gasst.service.AlarmService")) {
                        LogUtil.i(TAG, "服务已启动");
                        AlarmService.startExtractTask(mContext);
                    } else {
                        LogUtil.i(TAG, "服务未启动");
                        AlarmService.alarmTask(mContext);
                    }
                } else {
                    if (Util.isServiceWork(mContext, "com.duanze.gasst.service.AlarmService")) {
                        LogUtil.i(TAG, "服务已启动");
                        AlarmService.stopExtractTask(mContext);
                    } else {
                        LogUtil.i(TAG, "服务未启动");
                    }
                }
            }
        });

        alwaysShow = (Switch) findViewById(R.id.s_notification_always_show);
        b = preferences.getBoolean(NOTIFICATION_ALWAYS_SHOW, false);
        alwaysShow.setChecked(b);
        alwaysShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean(NOTIFICATION_ALWAYS_SHOW, isChecked).apply();
                AlarmService.showOrHide(mContext);
            }
        });

        concentrateWrite = (Switch) findViewById(R.id.s_concentrate_write);
        b = preferences.getBoolean(getString(R.string.concentrate_write_key), true);
        concentrateWrite.setChecked(b);
        concentrateWrite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferencesUtils.getInstance(mContext).setConcentrateWrite(isChecked);
            }
        });

        extractLocationSummary = (TextView) findViewById(R.id.tv_extract_location_summary);
        extractLocationSummary.setText(Util.readSaveLocation(Settings
                        .LIGHTNING_EXTRACT_SAVE_LOCATION, preferences, db,
                mContext));
        extractLocationSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectExtractLocationDialog(extractLocationSummary);
            }
        });

        quickLocationSummary = (TextView) findViewById(R.id.tv_quick_location_summary);
        quickLocationSummary.setText(Util.readSaveLocation(Settings.QUICK_WRITE_SAVE_LOCATION,
                preferences, db, mContext));
        quickLocationSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectQuickLocationDialog(quickLocationSummary);
            }
        });

        oneColumn = (CheckBox) findViewById(R.id.cb_one_column);
        boolean isOneColumn = preferences.getBoolean(getString(R.string.one_column_key), false);
        oneColumn.setChecked(isOneColumn);
        oneColumn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferencesUtils.getInstance(mContext).setOneColumn(isChecked);
                activityNeedRecreate();
            }
        });

        createOrder = (CheckBox) findViewById(R.id.cb_create_order);
        boolean isUseCreateOrder = preferences.getBoolean(getString(R.string.create_order_key), false);
        createOrder.setChecked(isUseCreateOrder);
        createOrder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferencesUtils.getInstance(mContext).setUseCreateOrder(isChecked);
                activityNeedRecreate();
            }
        });
    }

    private void activityNeedRecreate() {
        PreferencesUtils.getInstance(mContext).setActivityNeedRecreate(true);
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
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String _password = password.getText().toString();
                        String ans = preferences.getString(Settings.PASSWORD, "");
                        final boolean useUniversal = preferences.getBoolean(USE_UNIVERSAL_PASSWORD,
                                true);
                        if (ans.equals(_password) ||
                                useUniversal && Settings.UNIVERSAL_PASSWORD.equals(_password)) {
                            setGuardText(false);
                            Toast.makeText(mContext, R.string.password_guard_stop, Toast.LENGTH_SHORT).show();
                            PreferencesUtils.getInstance(mContext).setPasswordGuard(false);
                        } else {
                            Toast.makeText(mContext, R.string.password_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
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
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String _password = password.getText().toString();
                        String _confirm = confirm.getText().toString();
                        String _hint = hint.getText().toString().trim();
                        if (_password.equals(_confirm) && _password.length() > 0) {
                            preferences.edit()
                                    .putString(PASSWORD, _password)
                                    .putString(PASSWORD_HINT, _hint)
                                    .apply();

                            setGuardText(true);
                            Toast.makeText(mContext, R.string.password_guard_start, Toast.LENGTH_SHORT).show();
                            PreferencesUtils.getInstance(mContext).setPasswordGuard(true);
                        } else {
                            Toast.makeText(mContext, R.string.passwords_differ, Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private int tmpLocation;
    private String tmpLocationName;

    private void showSelectExtractLocationDialog(final TextView tv) {
        View view = getLayoutInflater().inflate(R.layout.dialog_radiogroup, (ViewGroup) getWindow
                ().getDecorView
                (), false);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rg_dialog);
        RadioButton purenote = (RadioButton) view.findViewById(R.id.rb_purenote);
        boolean radioChecked = false;
        List<GNotebook> list = db.loadGNotebooks();
        tmpLocation = preferences.getInt(LIGHTNING_EXTRACT_SAVE_LOCATION, 0);
        tmpLocationName = mContext.getResources().getString(R.string.all_notes);
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

        if (!radioChecked) {
            purenote.setChecked(true);
        }
        purenote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    tmpLocation = 0;
                    tmpLocationName = mContext.getResources().getString(R.string.all_notes);
                }
            }
        });
        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.lightning_extract_save_location)
                .setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferences.edit()
                                .putInt(LIGHTNING_EXTRACT_SAVE_LOCATION, tmpLocation)
                                .apply();
                        tv.setText(tmpLocationName);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void showSelectQuickLocationDialog(final TextView tv) {
        View view = getLayoutInflater().inflate(R.layout.dialog_radiogroup, (ViewGroup) getWindow
                ().getDecorView
                (), false);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.rg_dialog);
        RadioButton purenote = (RadioButton) view.findViewById(R.id.rb_purenote);
        boolean radioChecked = false;
        List<GNotebook> list = db.loadGNotebooks();
        tmpLocation = preferences.getInt(QUICK_WRITE_SAVE_LOCATION, 0);
        tmpLocationName = mContext.getResources().getString(R.string.all_notes);
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

        if (!radioChecked) {
            purenote.setChecked(true);
        }
        purenote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    tmpLocation = 0;
                    tmpLocationName = mContext.getResources().getString(R.string.all_notes);
                }
            }
        });

        final Dialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.lightning_extract_save_location)
                .setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferences.edit()
                                .putInt(QUICK_WRITE_SAVE_LOCATION, tmpLocation)
                                .apply();
                        tv.setText(tmpLocationName);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void bindSuccess() {
        arrow.setVisibility(View.GONE);

        if (mEvernote.getUsername() == null) {
            loginText.setText(R.string.logout_evernote_username_null);
            mEvernote.getUserInfo();
        } else {
            loginText.setText(getString(R.string.logout_evernote,
                    mEvernote.getUsername()));
        }
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

    @Override
    public void onBackPressed() {
        exitOperation();
    }

    private void exitOperation() {
        preferences.edit().putBoolean(SETTINGS_CHANGED, settingChanged).apply();
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_evernote:
                if (!mEvernote.isLogin()) {
                    LogUtil.i(TAG, "try to login");
                    mEvernote.auth();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                    builder.setMessage(R.string.logout_text)
                            .setPositiveButton(R.string.confirm,
                                    new DialogInterface.OnClickListener() {


                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            mEvernote.Logout();
                                        }
                                    })
                            .setNegativeButton(R.string.cancel, null)
                            .create().show();
                }
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

    private void chooseThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_theme_title);
        Integer[] res = new Integer[]{R.drawable.blue_round, R.drawable.yellow_round, R.drawable.pink_round,R.drawable.green_round};
        List<Integer> list = Arrays.asList(res);
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
        PreferencesUtils.getInstance(mContext).setTheme(position);
        PreferencesUtils.getInstance(mContext).setActivityNeedRecreate(true);
        recreate();
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
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ratio = editText.getText().toString();
                        float r = Float.valueOf(ratio);
                        float rMin = Float.valueOf(getString(R.string.note_max_length_min));
                        float rMax = Float.valueOf(getString(R.string.note_max_length_max));
                        if (r >= rMin && r <= rMax) {
                            maxLengthRatio.setText(ratio);
                            PreferencesUtils.getInstance(mContext).setMaxLengthRatio(r);
                            activityNeedRecreate();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onLoginResult(Boolean result) {
        if (result) {
            bindSuccess();
            preferences.edit().putBoolean("sync_now", true).apply();
        }
    }

    @Override
    public void onUserinfo(Boolean result, User user) {
        if (null == user) return;
        loginText.setText(getString(R.string.logout_evernote,
                user.getUsername()));
    }

    @Override
    public void onLogout(Boolean reuslt) {
        if (reuslt) {
            arrow.setVisibility(View.VISIBLE);
            loginText.setText(R.string.login_evernote);
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}