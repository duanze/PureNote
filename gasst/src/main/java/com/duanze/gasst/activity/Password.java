package com.duanze.gasst.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.duanze.gasst.R;

/**
 * Created by duanze on 2015/7/11.
 */
public class Password extends BaseActivity {
    private String password;
    private SharedPreferences preferences;

    public static void actionStart(Context context, String hint, String password) {
        Intent intent = new Intent(context, Password.class);
        intent.putExtra(Settings.PASSWORD_HINT, hint);
        intent.putExtra(Settings.PASSWORD, password);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(Settings.DATA, MODE_PRIVATE);
        setContentView(R.layout.activity_password);
        password = getIntent().getStringExtra(Settings.PASSWORD);
        String h = getIntent().getStringExtra(Settings.PASSWORD_HINT);
        if (!"".equals(h)) {
            TextView hint = (TextView) findViewById(R.id.tv_hint);
            hint.setText("Hint:" + h);
            hint.setVisibility(View.VISIBLE);
        }

        EditText input = (EditText) findViewById(R.id.et_input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String ans = editable.toString();
                final boolean useUniversal = preferences.getBoolean(Settings.USE_UNIVERSAL_PASSWORD,
                        true);
                if (useUniversal && Settings.UNIVERSAL_PASSWORD.equals(ans) ||
                        ans.equals(password)) {
                    unLock();
                }
            }
        });

        final TextView forgot = (TextView) findViewById(R.id.tv_forgot);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUniversalPassword();
            }
        });
    }

    private void unLock() {
        finish();
    }

    private void getUniversalPassword() {
        // 必须明确使用mailto前缀来修饰邮件地址
        Uri uri = Uri.parse("mailto:端泽<blue3434@qq.com>");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        // intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, "PureNote忘记密码"); // 主题
        intent.putExtra(Intent.EXTRA_TEXT, "只需发送这封邮件即可。"); // 正文
        startActivity(Intent.createChooser(intent, "Select email client"));

        preferences.edit().putBoolean(Settings.SHOW_UNIVERSAL_SWITCH, true).apply();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
