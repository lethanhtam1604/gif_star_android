package com.gifstar.activity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.thanhtam.gifstar.R;
import com.gifstar.manager.Global;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Runtime.getRuntime().maxMemory();
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                    init();
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                }
            }
        };
        timerThread.start();
    }

    private void init() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Global.screenWidth = size.x;
        Global.screenHeight = size.y;
        Global.ratio = (float) size.x / size.y;
    }
}
