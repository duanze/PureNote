package com.duanze.gasst.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.duanze.gasst.R;
import com.larswerkman.licenseview.LicenseView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Licenses extends BaseActivity {

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, Licenses.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        LicenseView licenseView = (LicenseView) findViewById(R.id.licenseview);
        try {
            licenseView.setLicenses(R.xml.licenses);
        } catch (Resources.NotFoundException e1) {
        } catch (XmlPullParserException e1) {
        } catch (IOException e1) {
        }

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (null!=actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
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
