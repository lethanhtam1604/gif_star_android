package com.gifstar.apirequest.uploadgifimage;

import android.content.Context;

import com.example.thanhtam.gifstar.R;
import com.gifstar.apirequest.ApiRequest;
import com.google.gson.Gson;

public class UploadGifImageApiRequest extends ApiRequest {

    private Gson jsonParser;

    public UploadGifImageApiRequest(Context context, RequestResponse callback) {
        super(context, callback);
        jsonParser = new Gson();
    }

    public String getLoadingMessage() {
        return context.getString(R.string.processing);
    }

    public String getRequestUrl() {
        return "/upload.php";
    }

    public String getContentType() {
        return "application/json";
    }

    public String getRequestType() {
        return "POST";
    }

    public boolean isOpenProgressBar() {
        return false;
    }

    public String parseBody() {
        return jsonParser.toJson(postObject);
    }

    public Object resultReceiver(String response) {
        return response;
    }
}
