package com.gifstar.manager;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

public class Global {

    public static Context context;
    public static SettingsManager settingsManager;
    public static float ratio;
    public static int screenWidth;
    public static int screenHeight;
    public static int gifTimeTotal = 4000; //5s
    public static String textGif = "";
    public static ArrayList<Bitmap> bitmaps;
    public static boolean isFinishCreateGifImage = true;

}
