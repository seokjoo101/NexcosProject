package com.example.seokjoo.contactex.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Seokjoo on 2016-08-02.
 */


public class Global {
    public static final String TAG = "nexcos";
    public static final String TAG_ = "MyDataChannel";

    public Context context;

    public Global(Context con) {
        this.context = con;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

    public String getString(String key) {
        String value = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, null);

        return value;
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }

    public boolean getBoolean(String key) {
        boolean value = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, false);
        return value;
    }


    public static String Mytopic;
    public static String ToTopic;
    public static String ToName;
}
