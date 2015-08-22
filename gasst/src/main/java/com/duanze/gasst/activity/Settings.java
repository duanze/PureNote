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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.duanze.gasst.R;
import com.duanze.gasst.model.GNoteDB;
import com.duanze.gasst.model.GNotebook;
import com.duanze.gasst.service.AlarmService;
import com.duanze.gasst.syn.Evernote;
import com.duanze.gasst.util.LogUtil;
import com.duanze.gasst.util.Util;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;
import com.readystatesoftware.systembartint.SystemBarTintManager;


import java.util.List;


public class Settings extends Activity implements View.OnClickListener, Evernote.EvernoteLoginCallback {
    public static final String TAG = "Settings";

    public static final String GNOTEBOOK_ID = "gnotebook_id";
    public static final String PURENOTE_NOTE_NUM = "purenote_note_num";

    public static final String DATA = "gasst_pref";
    public static final String NEW_NOTE = "is_open_new_note";
    public static final String FULL_SCREEN = "is_full_screen";
    public static final String FOLD = "is_fold";
    public static final String RANDOM_COLOR = "is_random_color";
    public static final String MAX_LINES = "max_lines";
    public static final String CUSTOMIZE_COLOR = "is_customize_color";
    public static final String COLOR_READ = "color_read";
    public static final String PASSWORD_GUARD = "password_guard";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_HINT = "password_hint";
    public static final String UNIVERSAL_PASSWORD = "-_-#";
    public static final String USE_UNIVERSAL_PASSWORD = "use_universal_password";
    public static final String SHOW_UNIVERSAL_SWITCH = "show_universal_switch";
    public static final String LIGHTNING_EXTRACT = "lightning_extract";
    public static final String LIGHTNING_EXTRACT_SAVE_LOCATION = "lightning_extract_save_location";
    public static final String QUICK_WRITE_SAVE_LOCATION = "quick_write_save_location";

    public static final String PREF_NOTE_KEY = "pref_note_key";
    public static final String NOTIFICATION_ALWAYS_SHOW = "notification_always_show";


    public static final String SETTINGS_CHANGED = "is_changed";


    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
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
        overridePendingTransition(R.anim.in_push_right_to_left,
                R.anim.out_stable);
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(DATA, MODE_PRIVATE);


        mContext = this;
        db = GNoteDB.getInstance(mContext);
        setContentView(R.layout.activity_settings);
//沉浸式时，对状态栏染色
// create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);


        tintManager.setStatusBarTintColor(getResources().getColor(R.color.background_color));


// enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
// // enable navigation bar tint
// tintManager.setNavigationBarTintEnabled(true);


        editor = preferences.edit();
        initButtons();
        ActionBar actionBar = getActionBar();
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
        findViewById(R.id.ll_universal).setOnClickListener(this);
        findViewById(R.id.ll_quick_write_location_container).setOnClickListener(this);
        findViewById(R.id.ll_lightning_container).setOnClickListener(this);
        findViewById(R.id.ll_extract_location_container).setOnClickListener(this);
        findViewById(R.id.ll_fold_container).setOnClickListener(this);
        findViewById(R.id.ll_maxlines_container).setOnClickListener(this);
        findViewById(R.id.ll_quick_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmService.alarmTask(mContext);
            }
        });
        findViewById(R.id.ll_notification_container).setOnClickListener(this);
        findViewById(R.id.ll_pref_note).setOnClickListener(this);
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


    private TextView passwordGurad;
    private Switch lightningExtract;
    private Switch alwaysShow;
    private TextView extractLocationSummary;
    private TextView quickLocationSummary;
    private CheckBox fold;
    private Spinner spinner;
    private CheckBox universal;
    private CheckBox prefNote;


    private void initButtons() {
// CheckBox newNote = (CheckBox) findViewById(R.id.new_note);
        fold = (CheckBox) findViewById(R.id.fold);


        spinner = (Spinner) findViewById(R.id.m_spinner);
        final String[] maxLinesArr = {"2", "3", "4", "5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, maxLinesArr);
        spinner.setAdapter(adapter);



        spinner.setSelection(preferences.getInt(MAX_LINES, 5) - 2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor.putInt(MAX_LINES, i + 2).apply();
                settingChanged = true;
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {


            }
        });


        fold.setChecked(preferences.getBoolean(FOLD, false));
        fold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(FOLD, b).apply();
                settingChanged = true;
            }
        });


        passwordGurad = (TextView) findViewById(R.id.tv_password_guard);
        boolean b = preferences.getBoolean(PASSWORD_GUARD, false);
        setGuardText(b);
        passwordGurad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!preferences.getBoolean(PASSWORD_GUARD, false)) {
                    showCreatePasswordDialog();
                } else {
                    showCancelPasswordDialog();
                }
            }
        });


        universal = (CheckBox) findViewById(R.id.cb_universal);
        boolean useUniversalPassword = preferences.getBoolean(USE_UNIVERSAL_PASSWORD, true);
        universal.setChecked(useUniversalPassword);
        universal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
// if (b){
// Toast.makeText(mContext,"打开万能密码:-_-#",Toast.LENGTH_SHORT).show();
// }else {
// Toast.makeText(mContext,"关闭万能密码:-_-#",Toast.LENGTH_SHORT).show();
// }
                preferences.edit().putBoolean(USE_UNIVERSAL_PASSWORD, b).apply();
            }
        });

        prefNote = (CheckBox) findViewById(R.id.cb_pref_note);
        boolean usePrefNote = preferences.getBoolean(PREF_NOTE_KEY, false);
        prefNote.setChecked(usePrefNote);
        prefNote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit().putBoolean(PREF_NOTE_KEY, b).apply();
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
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit().putBoolean(LIGHTNING_EXTRACT, b).apply();
                if (b) {
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
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.edit().putBoolean(NOTIFICATION_ALWAYS_SHOW, b).apply();
                AlarmService.showOrHide(mContext);
            }
        });


        extractLocationSummary = (TextView) findViewById(R.id.tv_extract_location_summary);
        extractLocationSummary.setText(Util.readSaveLocation(Settings.LIGHTNING_EXTRACT_SAVE_LOCATION, preferences, db,
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
    }


    private void setGuardText(boolean b) {
        if (b) {
            passwordGurad.setText(R.string.password_guard_on);
        } else {
            passwordGurad.setText(R.string.password_guard_off);
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
                            preferences.edit()
                                    .putBoolean(PASSWORD_GUARD, false)
                                    .apply();


                            setGuardText(false);
                            Toast.makeText(mContext, "密码保护已停用", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "密码错误", Toast.LENGTH_SHORT).show();
                        }


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();


        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }


    private void showCreatePasswordDialog() {
        View view = getLayoutInflater().inflate(R.layout.password_dialog, (ViewGroup) getWindow().getDecorView
                (), false);
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
                                    .putBoolean(PASSWORD_GUARD, true)
                                    .apply();


                            setGuardText(true);
                            Toast.makeText(mContext, "密码保护已启用", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                        }


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();


        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }


    private int tmpLocation;
    private String tmpLocationName;


    private void showSelectExtractLocationDialog(final TextView tv) {
        View view = getLayoutInflater().inflate(R.layout.dialog_radiogroup, (ViewGroup) getWindow().getDecorView
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
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                })
                .create();
        dialog.show();
    }


    private void showSelectQuickLocationDialog(final TextView tv) {
        View view = getLayoutInflater().inflate(R.layout.dialog_radiogroup, (ViewGroup) getWindow().getDecorView
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
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                })
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
        editor.putBoolean(SETTINGS_CHANGED, settingChanged).apply();
        finish();
        overridePendingTransition(R.anim.in_stable,
                R.anim.out_push_left_to_right);
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
                            .setTitle(R.string.alert)
                            .setPositiveButton(R.string.confirm,
                                    new DialogInterface.OnClickListener() {


                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            mEvernote.Logout();
                                        }
                                    })
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {


                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                }
                break;
            case R.id.ll_password_container:
                passwordGurad.performClick();
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
                lightningExtract.performClick();
                break;
            case R.id.ll_extract_location_container:
                extractLocationSummary.performClick();
                break;


            case R.id.ll_fold_container:
                fold.performClick();
                break;


            case R.id.ll_maxlines_container:
                spinner.performClick();
                break;


            case R.id.ll_notification_container:
                alwaysShow.performClick();
                break;

            case R.id.ll_pref_note:
                prefNote.performClick();
                break;
            default:
                break;
        }
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