package com.duanze.gasst.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.duanze.gasst.R;
import com.duanze.gasst.util.PictureUtils;

/**
 * Created by duanze on 2015/7/11.
 */
public class Donate extends BaseActivity implements View.OnClickListener {
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

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
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
