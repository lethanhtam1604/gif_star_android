package com.gifstar.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.example.thanhtam.gifstar.R;
import com.gifstar.manager.CameraPreview;
import com.gifstar.manager.GifManager;
import com.gifstar.manager.Global;
import com.gifstar.manager.SettingsManager;
import com.gifstar.manager.ViewExtras;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {
    boolean recording = false;
    private GifManager gifManager;

    private CameraPreview cameraPreview;
    private EditText inputET;
    private ImageView captureBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Runtime.getRuntime().maxMemory();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 23) {
            ArrayList<String> requestList = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(Manifest.permission.CAMERA);
            }

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(Manifest.permission.INTERNET);
            }

            if (requestList.size() > 0) {
                String[] requestArr = new String[requestList.size()];
                requestArr = requestList.toArray(requestArr);
                ActivityCompat.requestPermissions(this, requestArr, 1);
            } else {
                begin();
            }
        } else {
            begin();
        }
    }

    private void begin() {
        Fabric.with(this, new Crashlytics());
        initialize();
        cameraPreview = new CameraPreview(this);
        FrameLayout camera_view = (FrameLayout) findViewById(R.id.camera_view);
        camera_view.addView(cameraPreview);

        inputET = (EditText) findViewById(R.id.inputET);
        gifManager = new GifManager(cameraPreview, inputET);
        captureBtn = (ImageView) findViewById(R.id.captureBtn);
        captureBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Global.isFinishCreateGifImage = false;
                        Global.textGif = inputET.getText().toString();
                        gifManager.createGifImage(Global.isFinishCreateGifImage);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(!Global.isFinishCreateGifImage) {
                            Global.isFinishCreateGifImage = true;
                            gifManager.createGifImage(Global.isFinishCreateGifImage);
                            Global.isFinishCreateGifImage = !Global.isFinishCreateGifImage;
                        }
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean isGratedFully = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isGratedFully = false;
                break;
            }
        }

        if (isGratedFully) {
            begin();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.app_name)
                    .titleColor(ViewExtras.getColor(this, R.color.colorPrimary))
                    .content("Can not grant permission fully!")
                    .positiveText("Exit")
                    .positiveColor(ViewExtras.getColor(this, R.color.colorPrimary))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            dialog.cancel();
                            MainActivity.this.finish();
                        }
                    })
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .show();
        }
    }

    public void initialize() {
        Global.context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Global.settingsManager = SettingsManager.getInstance(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        Global.isFinishCreateGifImage = false;
    }

    public void camera_Clicked(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(inputET.getWindowToken(), 0);
    }

    public void switchCameraClicked(View view) {
        if (!recording) {
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                cameraPreview.switchCamera();
            } else {
                Toast toast = Toast.makeText(this, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    public void shareAppClicked(View view) {
        Uri uri = Uri.parse("market://details?id=" + Global.context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            Global.context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Global.context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + Global.context.getPackageName())));
        }
    }
}