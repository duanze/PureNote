package com.duanze.gasst.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.duanze.gasst.R;
import com.duanze.gasst.util.Util;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by duanze on 2015/7/11.
 */
public class About extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    public static void activityStart(Context context) {
        Intent intent = new Intent(context, About.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (null!=actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mContext = this;
        TextView version = (TextView) findViewById(R.id.tv_version);
        version.setText(Util.getVersionName(mContext));

        View feedback = findViewById(R.id.btn_feedback);
        feedback.setOnClickListener(this);
        View comment = findViewById(R.id.btn_comment);
        comment.setOnClickListener(this);
        View donate = findViewById(R.id.btn_donate);
        donate.setOnClickListener(this);

        findViewById(R.id.btn_licenses).setOnClickListener(this);
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
        switch (view.getId()) {
            case R.id.btn_feedback:
                Util.feedback(mContext);
                break;
            case R.id.btn_comment:
                evaluate(mContext);
                break;
            case R.id.btn_donate:
                Donate.actionStart(mContext);
                break;
            case R.id.btn_licenses:
                Licenses.actionStart(mContext);
                break;
        }
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
