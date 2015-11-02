package com.duanze.gasst.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;

import com.duanze.gasst.activity.StartActivity;
import com.duanze.gasst.R;

public class MyActionProvider extends ActionProvider {
    StartActivity activity;
    SharedPreferences preferences;

    public MyActionProvider(Context context) {
        super(context);
        activity = (StartActivity) context;
        preferences = activity.getSharedPreferences("data",
                Activity.MODE_PRIVATE);
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu) {

//        subMenu.clear();
//        subMenu.add(R.string.m_color_grid).setOnMenuItemClickListener(new OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if (activity.getMode() != StartActivity.MODE_GRID) {
//                    preferences.edit().putInt("mode", StartActivity.MODE_GRID).apply();
//                    activity.setMode(StartActivity.MODE_GRID);
//                    activity.changeContent();
////                    activity.gridRefresh();
//                }
//                return true;
//            }
//        });
//        subMenu.add(R.string.m_classic_list).setOnMenuItemClickListener(new OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if (activity.getMode() != StartActivity.MODE_LIST) {
//                    preferences.edit().putInt("mode", StartActivity.MODE_LIST).apply();
//                    activity.setMode(StartActivity.MODE_LIST);
//                    activity.changeContent();
////                    activity.listRefresh();
//                }
//                return true;
//            }
//        });
    }

    @Override
    public boolean hasSubMenu() {
        return true;
    }
}
