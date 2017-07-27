package com.gifstar.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private final String MyPREFERENCES = "GIFStar";
    private final String TypeCamera = "TypeCamera";
    private static SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;

    private static SettingsManager instance = null;

    private SettingsManager(Context context) {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }

    public static SettingsManager getInstance(Context context) {
        if (instance == null)
            instance = new SettingsManager(context);
        return instance;
    }

    public void setTypeCamera(int value) {
        editor.putInt(TypeCamera, value);
        editor.commit();
    }

    public int getTypeCamera() {
        return sharedpreferences.getInt(TypeCamera, 0);
    }
}
