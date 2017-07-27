package com.gifstar.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.example.thanhtam.gifstar.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.gifstar.apirequest.ApiRequest;
import com.gifstar.apirequest.uploadgifimage.UploadGifImageApiRequest;
import com.gifstar.apirequest.uploadgifimage.UploadGifModel;
import com.gifstar.manager.GifEncoder;
import com.gifstar.manager.Global;
import com.gifstar.manager.GooglePlayApp;
import com.gifstar.manager.ViewExtras;
import com.google.android.gms.plus.PlusShare;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import org.jcodec.api.android.SequenceEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import io.fabric.sdk.android.Fabric;

public class GifActivity extends AppCompatActivity {

    private WebView termWebView;
    private RelativeLayout termsRL;
    private ImageView fbImgView, messengerImgView, twitterImgView, googleImgView, instagramImgView,
            whatsAppImgView, saveImgView, shareImgView;
    private TextView processingTV;
    private byte[] gifData;
    private ImageView imageView, imageTemp;

    private String gifStarLink = "https://gifstar.me/";
    private String currentTime = "";
    private boolean isCreatedGifImage = false;
    private int shareType;
    private boolean isFinishedSaveFrames = false;
    private boolean isCreatedVideo = false;
    private int videoRepeat = 1;
    private boolean isUploadedGifImage = false;
    private ArrayList<Integer> nameList;

    //Facebook
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_gif);
        Runtime.getRuntime().maxMemory();
        createGifStarFolder();

        termsRL = (RelativeLayout) findViewById(R.id.termsRL);
        termsRL.setVisibility(View.GONE);

        termWebView = (WebView) findViewById(R.id.termWebView);
        termWebView.loadUrl("file:///android_asset/terms.html");

        fbImgView = (ImageView) findViewById(R.id.fbImgView);
        messengerImgView = (ImageView) findViewById(R.id.messengerImgView);
        twitterImgView = (ImageView) findViewById(R.id.twitterImgView);
        googleImgView = (ImageView) findViewById(R.id.googleImgView);
        instagramImgView = (ImageView) findViewById(R.id.instagramImgView);
        whatsAppImgView = (ImageView) findViewById(R.id.whatsAppImgView);
        saveImgView = (ImageView) findViewById(R.id.saveImgView);
        shareImgView = (ImageView) findViewById(R.id.shareImgView);

        processingTV = (TextView) findViewById(R.id.processingTV);
        imageView = (ImageView) findViewById(R.id.image);
        imageTemp = (ImageView) findViewById(R.id.imageTemp);

        Picasso.with(this).load(R.drawable.fb).into(fbImgView);
        Picasso.with(this).load(R.drawable.messenger).into(messengerImgView);
        Picasso.with(this).load(R.drawable.twitter).into(twitterImgView);
        Picasso.with(this).load(R.drawable.google).into(googleImgView);
        Picasso.with(this).load(R.drawable.instagram).into(instagramImgView);
        Picasso.with(this).load(R.drawable.whatsapp).into(whatsAppImgView);
        Picasso.with(this).load(R.drawable.gallery).into(saveImgView);
        Picasso.with(this).load(R.drawable.share).into(shareImgView);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    nameList = new ArrayList<>();
                    for (int i = 0; i < Global.bitmaps.size(); i++)
                        SaveImage(Global.bitmaps.get(i));

                    isFinishedSaveFrames = true;
                } catch (Exception e) {

                }
            }
        };
        thread.start();

        playGifImage();
        currentTime = getCurrentDateAndTime();

        //Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
    }

    private void createGifStarFolder() {
        File dir = new File("/sdcard/GifStar/");
        if (!dir.exists())
            dir.mkdirs();
    }

    int i = 0;
    Handler handler;
    private boolean firstCash = false;
    private int indexTemp = 0;

    private void playGifImage() {
        processingTV.setVisibility(View.GONE);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (i >= Global.bitmaps.size())
                    i = 0;
                else {
                    if (!isFinishedSaveFrames) {
                        imageView.setImageBitmap(Global.bitmaps.get(i++));
                    } else {

                        File file = new File("/sdcard/GifStar/Frames/GifStar" + nameList.get(i) + ".png");

                        if (!firstCash) {
                            imageView.setImageBitmap(Global.bitmaps.get(i));
                        }

                        if (!firstCash && indexTemp > Global.bitmaps.size()) {
                            firstCash = true;
                            imageView.setVisibility(View.GONE);
                            for (int i = 0; i < Global.bitmaps.size(); i++)
                                Global.bitmaps.get(i).recycle();
                        } else {
                            indexTemp++;
                        }
                        Picasso.with(GifActivity.this).load(file).into(imageTemp);
                        i++;
                    }
                }
                handler.postDelayed(this, 200);
            }
        }, 200);
    }

    private void SaveImage(Bitmap finalBitmap) {
        File dir = new File("/sdcard/GifStar/Frames");
        if (!dir.exists())
            dir.mkdirs();

        int random = random();
        File file = new File("/sdcard/GifStar/Frames/GifStar" + random + ".png");
        nameList.add(random);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 20, out);
            out.flush();
            out.close();

        } catch (Exception e) {
        }
    }

    private int random() {
        Random r = new Random();
        int i = r.nextInt(1000000 - 1) + 1;
        return i;
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    private void createGifImage() {
        if (!isCreatedGifImage) {
            isCreatedGifImage = true;
            showDialog();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    GifEncoder encoder = new GifEncoder();
                    encoder.start(bos);

                    for (int i = 0; i < Global.bitmaps.size(); i++) {
                        File f = new File("/mnt/sdcard/GifStar/Frames/GifStar" + nameList.get(i) + ".png");
                        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                        encoder.addFrame(bitmap);
                        bitmap.recycle();
                    }
                    encoder.finish();

                    gifData = bos.toByteArray();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissDialog();
                            if (shareType == 1 || shareType == 4 || shareType == 8)
                                uploadGifImageToServer();
                            else {
                                if (shareType == 2)
                                    shareMessenger();
                                else if (shareType == 3)
                                    shareTwitter();
                                else if (shareType == 7)
                                    shareAlbum();
                            }
                        }
                    });

                    FileOutputStream outStream = null;
                    try {
                        outStream = new FileOutputStream("/sdcard/GifStar/GifStar.gif");
                        outStream.write(bos.toByteArray());
                        outStream.close();
                    } catch (Exception e) {
                    }
                }
            };
            thread.start();
        }
    }

    private void uploadGifImageToServer() {
        new MaterialDialog.Builder(GifActivity.this)
                .title("Confirmation")
                .titleColor(ViewExtras.getColor(GifActivity.this, R.color.colorPrimary))
                .content("Hey, just to let you know that the gif will be uploaded and shared!")
                .positiveText("OK")
                .negativeText("Cancel")
                .positiveColor(ViewExtras.getColor(GifActivity.this, R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        dialog.cancel();
                        showHub();
                        isUploadedGifImage = true;
                        uploadGifImage();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        dialog.cancel();
                    }
                })
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show();
    }

    public void termsAndPrivacyClicked(View view) {
        termsRL.setVisibility(View.VISIBLE);
    }

    public void closeTermsClicked(View view) {
        termsRL.setVisibility(View.GONE);
    }

    public void backPageClicked(View view) {
        clearBitmaps();
        Global.textGif = "";
        this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clearBitmaps();
            this.finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void clearBitmaps() {
        handler.removeCallbacksAndMessages(null);
        for (int i = 0; i < Global.bitmaps.size(); i++) {
            File file = new File("/sdcard/GifStar/Frames/GifStar" + nameList.get(i) + ".png");
            Picasso.with(this).invalidate(file);
            Global.bitmaps.get(i).recycle();
        }
        deleteDirectory(new File("sdcard/GifStar/Frames"));
    }

    private void uploadGifImage() {
        String data = Base64.encodeToString(gifData, Base64.DEFAULT);
        String image = "image/gif;base64," + data;
        ApiRequest request = new UploadGifImageApiRequest(this, new UploadGifImageRequestResponse());
        request.setPostObject(new UploadGifModel(image));
        request.execute();
    }

    public class UploadGifImageRequestResponse implements ApiRequest.RequestResponse {
        @Override
        public void requestComplete(boolean status, Object responseObject) {
            gifStarLink += (String) responseObject;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideHub();
                    if (shareType == 1)
                        shareFB();
                    else if (shareType == 4)
                        shareGoogle();
                    else if (shareType == 8)
                        shareCommon();
                }
            });
        }
    }

    public void shareFBClicked(View view) {
        if (!GooglePlayApp.isInstalledMessenger(this, "com.facebook.katana"))
            return;

        shareType = 1;

        if (isCreatedGifImage) {
            if (!isUploadedGifImage)
                uploadGifImageToServer();
            else
                shareFB();
        } else
            createGifImage();
    }

    private void shareFB() {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(gifStarLink))
                .build();
        shareDialog.show(content);
    }

    public void shareMessengerClicked(View view) {
        if (!GooglePlayApp.isInstalledMessenger(this, "com.facebook.orca"))
            return;

        shareType = 2;

        if (isCreatedGifImage) {
            shareMessenger();
        } else
            createGifImage();
    }

    private void shareMessenger() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("image/*");

        String mediaPath = Environment.getExternalStorageDirectory() + File.separator + "GifStar/GifStar.gif";

        File media = new File(mediaPath);
        Uri uri = Uri.fromFile(media);

        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setPackage("com.facebook.orca");

        try {
            startActivity(sendIntent);
        } catch (Exception e) {
        }
    }

    public void shareTwitterClicked(View view) {
        if (!GooglePlayApp.isInstalledMessenger(this, "com.twitter.android"))
            return;

        shareType = 3;

        if (isCreatedGifImage) {
            shareTwitter();
        } else
            createGifImage();
    }

    private void shareTwitter() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("image/*");

        String mediaPath = Environment.getExternalStorageDirectory() + File.separator + "GifStar/GifStar.gif";

        File media = new File(mediaPath);
        Uri uri = Uri.fromFile(media);

        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setPackage("com.twitter.android");

        try {
            startActivity(sendIntent);
        } catch (Exception e) {
        }
    }

    public void shareGoogleClicked(View view) {
        if (!GooglePlayApp.isInstalledMessenger(this, "com.google.android.apps.plus"))
            return;

        shareType = 4;

        if (isCreatedGifImage) {
            if (!isUploadedGifImage)
                uploadGifImageToServer();
            else
                shareGoogle();
        } else
            createGifImage();
    }

    private void shareGoogle() {
        Intent shareIntent = new PlusShare.Builder(this)
                .setType("text/plain")
                .setStream(Uri.parse(gifStarLink))
                .getIntent();

        startActivityForResult(shareIntent, 0);
    }

    public void shareInstagramClicked(View view) {
        if (!GooglePlayApp.isInstalledMessenger(this, "com.instagram.android"))
            return;

        shareType = 5;

        if (isCreatedVideo) {
            shareInstagram();
        } else
            createVideo();
    }

    private void shareInstagram() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*");

        String mediaPath = Environment.getExternalStorageDirectory() + File.separator + "GifStar/GifStar.mp4";

        File media = new File(mediaPath);
        Uri uri = Uri.fromFile(media);

        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setPackage("com.instagram.android");

        startActivity(shareIntent);
    }

    public void shareWhatsAppClicked(View view) {
        if (!GooglePlayApp.isInstalledMessenger(this, "com.whatsapp"))
            return;

        shareType = 6;

        if (isCreatedVideo) {
            shareWhatsApp();
        } else
            createVideo();
    }

    private void shareWhatsApp() {
        Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("video/*");

        String mediaPath = Environment.getExternalStorageDirectory() + File.separator + "GifStar/GifStar.mp4";

        File media = new File(mediaPath);
        Uri uri = Uri.fromFile(media);

        waIntent.putExtra(Intent.EXTRA_STREAM, uri);
        waIntent.setPackage("com.whatsapp");

        startActivity(waIntent);
    }

    public void saveClicked(View view) {
        shareType = 7;

        if (isCreatedGifImage) {
            shareAlbum();
        } else
            createGifImage();
    }

    private void shareAlbum() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "GifStar/GifStar" + currentTime + ".gif";

        try {
            File file = new File("/sdcard/GifStar/GifStar.gif");
            InputStream in = new FileInputStream(file);

            FileOutputStream out = new FileOutputStream(path);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
        }
        Toast.makeText(this, path + " saved",
                Toast.LENGTH_LONG).show();
    }

    public static String getCurrentDateAndTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public void shareClicked(View view) {
        shareType = 8;

        if (isCreatedGifImage) {
            if (!isUploadedGifImage)
                uploadGifImageToServer();
            else
                shareCommon();
        } else
            createGifImage();
    }

    private void shareCommon() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, gifStarLink);
        this.startActivity(Intent.createChooser(share, "Share"));
    }

    public void shareAppClicked(View view) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, "https://gifstar.me");
        this.startActivity(Intent.createChooser(share, "Share"));
    }

    private void createVideo() {
        if (!isCreatedVideo) {
            isCreatedVideo = true;
            showDialog();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        File file = new File("/sdcard/GifStar/GifStar.mp4");
                        SequenceEncoder encoder = new SequenceEncoder(file);
                        for (int i = 0; i < Global.bitmaps.size(); i++) {
                            File f = new File("/mnt/sdcard/GifStar/Frames/GifStar" + nameList.get(i) + ".png");
                            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                            encoder.encodeImage(bitmap);
                            bitmap.recycle();

                            if (i == Global.bitmaps.size() - 1 && videoRepeat < 5) {
                                videoRepeat++;
                                i = 0;
                            }
                        }
                        encoder.finish();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog();
                                if (shareType == 5)
                                    shareInstagram();
                                else if (shareType == 6)
                                    shareWhatsApp();
                            }
                        });
                    } catch (Exception e) {
                    }

                }
            };
            thread.start();
        }
    }

    MaterialDialog materialDialog;

    private void showDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .title("Saving image")
                .content("Please wait...")
                .canceledOnTouchOutside(false)
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    private void dismissDialog() {
        materialDialog.cancel();
    }

    private KProgressHUD hub;

    private void showHub() {
        hub = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        hub.show();
    }

    private void hideHub() {
        hub.dismiss();
    }
}