package com.gifstar.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Size previewSize;

    public CameraPreview(Context context) {
        super(context);

        initCamera();
    }

    private void initCamera() {
        mCamera = openCamera();
        mCamera.setPreviewCallback(previewCallback);
        setParametersCamera(90);

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    private Camera openCamera() {
        int cameraID = Global.settingsManager.getTypeCamera();
        return Camera.open(cameraID);
    }

    private void setParametersCamera(int rotation) {
        Camera.Parameters params = mCamera.getParameters();
        previewSize = params.getPreviewSize();
        params.setPreviewSize(previewSize.width, previewSize.height);

        params.setFlashMode("auto");
        setFocusable(true);
        setFocusableInTouchMode(true);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK)
            params.setRotation(90);
        else
            params.setRotation(270);

        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setParameters(params);
        } catch (Exception e) {
        }
    }

    public void switchCamera() {
        if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Global.settingsManager.setTypeCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            Global.settingsManager.setTypeCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        releaseCamera();
        initCamera();

        try {

            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    SurfaceHolder surfaceHolder;

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera == null) {
                initCamera();
            }

            this.surfaceHolder = surfaceHolder;
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();


        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (mHolder.getSurface() == null)
            return;

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
        }

        try {
            this.surfaceHolder = surfaceHolder;
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
        } catch (IOException e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();
    }

    byte[] byteArray;
    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            byteArray = data;
        }
    };

    public Bitmap saveImage() {
        Bitmap bitmap = null;
        Camera.Parameters params = mCamera.getParameters();

        int imageFormat = params.getPreviewFormat();
        if (imageFormat == ImageFormat.NV21) {
            Rect rect = new Rect(0, 0, previewSize.width, previewSize.height);
            YuvImage img = new YuvImage(byteArray, ImageFormat.NV21, previewSize.width, previewSize.height, null);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                img.compressToJpeg(rect, 20, byteArrayOutputStream);

                bitmap = BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());

                float minSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
                if(minSize >= 960) {
                    bitmap = scaleDown(bitmap, 960, true);
                }
                else {
                    bitmap = scaleDown(bitmap, 640, true);
                }

                Matrix matrix = new Matrix();

                if (bitmap.getWidth() > bitmap.getHeight()) {
                    if (Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        matrix.postRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    } else {
                        matrix.setScale(-1, 1);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        matrix = new Matrix();
                        matrix.postRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    }
                } else if ((Global.settingsManager.getTypeCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT)) {
                    matrix.setScale(-1, 1);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }

                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
            } catch (Exception e) {
            }

        }

        return bitmap;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
}