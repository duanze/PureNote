package com.duanze.gasst.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.duanze.gasst.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by duanze on 2015/7/11.
 */
public class About extends Activity implements View.OnClickListener{

    private Context mContext;

    public static void activityStart(Context context) {
        Intent intent = new Intent(context, About.class);

        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        //沉浸式时，对状态栏染色
        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);

        tintManager.setStatusBarTintColor(getResources().getColor(R.color.background_color));

        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
//        // enable navigation bar tint
//        tintManager.setNavigationBarTintEnabled(true);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mContext = this;
        TextView version = (TextView) findViewById(R.id.tv_version);
        version.setText(getVersion(mContext));

        View feedback = findViewById(R.id.btn_feedback);
        feedback.setOnClickListener(this);
        View comment = findViewById(R.id.btn_comment);
        comment.setOnClickListener(this);
        View donate = findViewById(R.id.btn_donate);
        donate.setOnClickListener(this);
    }

    private String getVersion(Context ctx){
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            return pi.versionName;
        }catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return "1.0.0";
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_feedback:
                feedback();
                break;
            case R.id.btn_comment:
                evaluate(mContext);
                break;
            case R.id.btn_donate:
                Donate.activityStart(mContext);
                break;
        }
    }

    private void feedback() {
        // 必须明确使用mailto前缀来修饰邮件地址
        Uri uri = Uri.parse("mailto:端泽<blue3434@qq.com>");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        // intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, "PureNote用户反馈" + " Version:" + getVersion(mContext));
         // 主题
        intent.putExtra(Intent.EXTRA_TEXT, ""); // 正文
        startActivity(Intent.createChooser(intent, "Select email client"));
    }

    private void evaluate(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Couldn't launch the market!",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
