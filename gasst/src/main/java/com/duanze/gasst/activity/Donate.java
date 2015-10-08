package com.duanze.gasst.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.duanze.gasst.MainActivity;
import com.duanze.gasst.R;
import com.duanze.gasst.util.PictureUtils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by duanze on 2015/7/11.
 */
public class Donate extends Activity implements View.OnClickListener {
    private static final String TAG = Donate.class.getSimpleName();
    private Context mContext;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, Donate.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_donate);

        if (MainActivity.TINT_STATUS_BAR) {
            //沉浸式时，对状态栏染色
            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.background_color));
            // enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
//        // enable navigation bar tint
//        tintManager.setNavigationBarTintEnabled(true);
        }

        ActionBar actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        findViewById(R.id.btn_save_donate_img).setOnClickListener(this);
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
    public void onClick(View v) {
        Bitmap donate = BitmapFactory.decodeResource(getResources(), R.drawable.donate_duanze);
        StringBuilder path = new StringBuilder();
        if (PictureUtils.saveImageToGallery(mContext, donate, path, true)) {
            Toast.makeText(mContext, getString(R.string.saved_picture_to) + path.toString(), Toast.LENGTH_SHORT).show();
        }
        if (!donate.isRecycled()) {
            donate.recycle();
        }
    }
}
