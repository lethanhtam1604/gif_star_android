package com.gifstar.manager;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.EditText;

import com.gifstar.activity.GifActivity;

import java.util.ArrayList;

public class GifManager {

    private int time = 0;
    private int gifTime = 200; //4ms
    private CameraPreview cameraPreview;
    private EditText inputET;

    public GifManager(CameraPreview cameraPreview, EditText inputET) {
        this.cameraPreview = cameraPreview;
        this.inputET = inputET;
    }

    private Handler timerHandler;
    private Runnable timerRunnable;

    private String titleGifStar = "";

    public void createGifImage(boolean isFinish) {
        if (isFinish) {
            if (time < 2000) {
                Global.gifTimeTotal = 2000;
                getFrameFromCamera();
                return;
            }
            timerHandler.removeCallbacks(timerRunnable);
            time = Global.gifTimeTotal + 1;
            getFrameFromCamera();
            return;
        }
        Global.bitmaps = new ArrayList<>();
        titleGifStar = inputET.getText().toString();
        inputET.setText("Capturing...");
        inputET.setEnabled(false);
        playAudio("camera_focus_beep_01.mp3");
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timerHandler.postDelayed(this, gifTime);
                time += gifTime;
                getFrameFromCamera();
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void getFrameFromCamera() {
        if (time > Global.gifTimeTotal) {
            Global.isFinishCreateGifImage = true;
            timerHandler.removeCallbacks(timerRunnable);
            time = 0;
            inputET.setEnabled(true);
            if (titleGifStar != "") {
                inputET.setText(titleGifStar);
                Global.textGif = titleGifStar;
            } else
                Global.textGif = "";
            inputET.setText("");
            Global.gifTimeTotal = 4000;
            Intent intent = new Intent(Global.context, GifActivity.class);
            Global.context.startActivity(intent);
            playAudio("camera_focus_beep_01.mp3");
            return;
        }
        addFrames();
    }

    private void addFrames() {
        Bitmap bitmap = cameraPreview.saveImage();
        drawTitleGifStarOnBitmap(bitmap);
        Global.bitmaps.add(bitmap);
    }

    private void drawTitleGifStarOnBitmap(Bitmap finalBitmap) {
        Canvas canvas = new Canvas(finalBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setARGB(255, 0, 0, 0);
        paint.setColor(Color.WHITE);
        paint.setTextSize(25);
        canvas.drawBitmap(finalBitmap, 0, 0, paint);
        String gifStar = "gifstar.me";
        Rect rectGifStar = new Rect();
        paint.getTextBounds(gifStar, 0, gifStar.length(), rectGifStar);
        int heightGifStar = Math.abs(rectGifStar.height());
        int widthGifStar = Math.abs(rectGifStar.width());
        int xGifStar = canvas.getWidth() - widthGifStar - 15;
        int yGifStar = canvas.getHeight() - heightGifStar + 18;
        canvas.drawText("gifstar.me", xGifStar, yGifStar, paint);

        if (Global.textGif.compareTo("") != 0) {
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);
            Rect rectTitleGif = new Rect();
            paint.getTextBounds(Global.textGif, 0, Global.textGif.length(), rectTitleGif);
            int heightTitleGif = Math.abs(rectTitleGif.height());
            int xTitleGif = canvas.getWidth() / 2;
            int yTitleGif = heightTitleGif + 30;

            Paint paintRect = new Paint();
            paintRect.setColor(Color.WHITE);
            paintRect.setAlpha(50);
            canvas.drawRect(0, yTitleGif - 50, canvas.getWidth(), yTitleGif + 25, paintRect);

            canvas.drawText(Global.textGif, xTitleGif, yTitleGif, paint);
        }
    }

    private void playAudio(String fileName) {
        try {
            MediaPlayer mp = new MediaPlayer();
            AssetFileDescriptor afd = Global.context.getAssets().openFd(fileName);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.setVolume(1f, 1f);
            mp.prepare();
            mp.start();
        } catch (Exception e) {

        }
    }
}
