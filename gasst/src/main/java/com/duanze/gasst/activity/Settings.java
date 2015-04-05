package com.duanze.gasst.activity;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.duanze.gasst.R;
import com.duanze.gasst.syn.Evernote;
import com.duanze.gasst.util.LogUtil;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;

public class Settings extends Activity implements View.OnClickListener, Evernote.EvernoteLoginCallback {
    public static final String TAG = "Settings";

    public static final String DATA = "gasst_pref";
    public static final String NEW_NOTE = "is_open_new_note";
    public static final String FULL_SCREEN = "is_full_screen";
    public static final String FOLD = "is_fold";
    public static final String RANDOM_COLOR = "is_random_color";
    public static final String MAX_LINES = "max_lines";
    public static final String CUSTOMIZE_COLOR = "is_customize_color";
    public static final String COLOR_READ = "color_read";

    public static final String SETTINGS_CHANGED = "is_changed";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean settingChanged = false;


    private Evernote mEvernote;
    private LinearLayout loginEvernote;
    private ImageView arrow;
    private TextView loginText;


    public static void activityStart(Context context) {
        Intent intent = new Intent(context, Settings.class);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.in_push_right_to_left,
                R.anim.in_stable);
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(DATA, MODE_PRIVATE);
        boolean fScreen = preferences.getBoolean(FULL_SCREEN, false);
        //如果设置了全屏
        if (fScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.custom);
        editor = preferences.edit();
        initButtons();
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.action_setting);
        actionBar.setDisplayHomeAsUpEnabled(true);

        loginEvernote = (LinearLayout) findViewById(R.id.login_evernote);
        arrow = (ImageView) findViewById(R.id.bind_arrow);
        loginText = (TextView) findViewById(R.id.login_text);

        mEvernote = new Evernote(this, this);
        if (mEvernote.isLogin()) {
            bindSuccess();
        }
        loginEvernote.setOnClickListener(this);

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

    private void initButtons() {
        CheckBox newNote = (CheckBox) findViewById(R.id.new_note);
        CheckBox fold = (CheckBox) findViewById(R.id.fold);
        CheckBox fullScreen = (CheckBox) findViewById(R.id.full_screen);
        CheckBox randomColor = (CheckBox) findViewById(R.id.random_color);
        CheckBox customizeColor = (CheckBox) findViewById(R.id.customize_color);
        final CheckBox colorRead = (CheckBox) findViewById(R.id.color_read);


        Spinner spinner = (Spinner) findViewById(R.id.m_spinner);
        final String[] maxLinesArr = {"2", "3", "4", "5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, maxLinesArr);
//        ArrayAdapter<String> adapter = new CustomArrayAdapter<String>(this,maxLinesArr);
        spinner.setAdapter(adapter);


        spinner.setSelection(preferences.getInt(MAX_LINES, 3) - 2);
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

        newNote.setChecked(preferences.getBoolean(NEW_NOTE, false));
        newNote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(NEW_NOTE, b).apply();
                settingChanged = true;
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


        fullScreen.setChecked(preferences.getBoolean(FULL_SCREEN, false));
        fullScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(FULL_SCREEN, b).apply();
                settingChanged = true;
            }
        });

        randomColor.setChecked(preferences.getBoolean(RANDOM_COLOR, true));
        randomColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(RANDOM_COLOR, b).apply();
                settingChanged = true;
            }
        });

        customizeColor.setChecked(preferences.getBoolean(CUSTOMIZE_COLOR, true));
        customizeColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(CUSTOMIZE_COLOR, b).apply();
                settingChanged = true;

                if (!b) {
                    colorRead.setEnabled(false);
                } else {
                    colorRead.setEnabled(true);
                }
            }
        });

        colorRead.setChecked(preferences.getBoolean(COLOR_READ, true));
        //初始检查是否可用
        colorRead.setEnabled(preferences.getBoolean(CUSTOMIZE_COLOR, true));
        colorRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(COLOR_READ, b).apply();
                settingChanged = true;
            }
        });
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
                editor.putBoolean(SETTINGS_CHANGED, settingChanged).apply();
                finish();
                overridePendingTransition(R.anim.in_stable,
                        R.anim.out_push_left_to_right);
                return true;
            default:
                return true;
        }
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
