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
 * Created by duanze on 15-8-22.
 */
public class RecycleBin extends Activity {

    public static void actionStart(Context mContext){
        Intent intent = new Intent(mContext,RecycleBin.class);
        mContext.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.in_push_right_to_left, R.anim.out_stable);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);

//沉浸式时，对状态栏染色
// create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintColor(getResources().getColor(R.color.background_color));

// enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
// // enable navigation bar tint
// tintManager.setNavigationBarTintEnabled(true);

        ActionBar actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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
        finish();
        overridePendingTransition(R.anim.in_stable,
                R.anim.out_push_left_to_right);
    }

}
