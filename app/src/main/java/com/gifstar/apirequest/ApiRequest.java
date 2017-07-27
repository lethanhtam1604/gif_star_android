package com.gifstar.apirequest;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class ApiRequest extends AsyncTask<Void, Void, String> {

    public static final String API_HOST = "https://gifstar.me";

    public abstract String getLoadingMessage();

    public abstract String getRequestUrl();

    public abstract String getContentType();

    public abstract String getRequestType();

    public abstract String parseBody();

    public abstract Object resultReceiver(String response);

    public abstract boolean isOpenProgressBar();

    protected Context context;
    protected RequestResponse callback;
    protected ProgressDialog progress;

    protected boolean responseSuccess = false;

    public boolean getResponseSuccess() {
        return responseSuccess;
    }

    protected Object postObject;

    public Object getPostObject() {
        return postObject;
    }

    public void setPostObject(Object obj) {
        postObject = obj;
    }

    public ApiRequest(Context context, RequestResponse callback) {
        this.context = context;
        this.callback = callback;
    }

    public String getFullRequestLink() {
        return API_HOST + getRequestUrl();
    }

    protected void onPreExecute() {
        if (isOpenProgressBar()) {
            progress = new ProgressDialog(this.context);
            progress.setMessage(getLoadingMessage());
            progress.show();
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(getFullRequestLink());

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                urlConnection.setRequestMethod(getRequestType());
                urlConnection.setDoOutput(postObject != null);
                urlConnection.setRequestProperty("Content-Type", getContentType());

                // Set post body
                if (postObject != null) {
                    DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
                    out.writeBytes(parseBody());
                    out.flush();
                    out.close();
                }

                responseSuccess = (urlConnection.getResponseCode() - HttpURLConnection.HTTP_OK) <= 3;

                BufferedReader outputReader;
                if (responseSuccess)
                    outputReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                else
                    outputReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));

                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = outputReader.readLine()) != null) {
                    responseOutput.append(line);
                }
                outputReader.close();

                return responseOutput.toString();
            } catch (Exception e) {
                responseSuccess = false;

            } finally {
                urlConnection.disconnect();

            }

        } catch (Exception e) {

        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (progress != null)
            progress.dismiss();
        // Return new value
        callback.requestComplete(responseSuccess, resultReceiver(result));
    }

    public interface RequestResponse {
        void requestComplete(boolean status, Object responseObject);
    }
}
