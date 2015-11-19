package com.duanze.easypreferences;

import java.util.ArrayList;

/**
 * Created by Duanze on 2015/11/19.
 */
public class PreferencesUtil {
    /**
     * the name of the SharedPreferences
     */
    private String name;
    private ArrayList<Preference> mPreferences;

    public String getName() {
        return name;
    }

    public ArrayList<Preference> getPreferences() {
        return mPreferences;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPreferences(ArrayList<Preference> preferences) {
        mPreferences = preferences;
    }
}
