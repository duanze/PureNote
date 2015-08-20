package com.duanze.gasst.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.duanze.gasst.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by duanze on 2015/7/11.
 */
public class Donate extends Activity {


    public static void activityStart(Context context) {
        Intent intent = new Intent(context, Donate.class);

        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donate);
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
}
