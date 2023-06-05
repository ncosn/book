package cn.njupt.iot.b19060226.book;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
public class OCRAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    private static final String TAG = "OCR.Space";

    private String mApiKey;
    private boolean isOverlayRequired = false;
    private String mLanguage;
    private SortActivity context;
    private ProgressDialog mProgressDialog;
    private OCRCallback callback;

    OCRAsyncTask(SortActivity activity, String apiKey, boolean isOverlayRequired, String language) {
        this.context = activity;
        this.mApiKey = apiKey;
        this.isOverlayRequired = isOverlayRequired;
        this.mLanguage = language;
    }

    OCRAsyncTask(SortActivity activity, String apiKey, String language) {
        this.context = activity;
        this.mApiKey = apiKey;
        this.mLanguage = language;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("识别中");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            URL url = new URL("https://api.ocr.space/parse/image");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            JSONObject postDataParams = new JSONObject();

            postDataParams.put("apikey", mApiKey);
            postDataParams.put("isOverlayRequired", isOverlayRequired);
//            postDataParams.put("url", params[0]);
            String base64 = "data:image/jpeg;base64,"+params[0];
            Log.d(TAG, "doInBackground: base64Image:"+base64);
            postDataParams.put("base64Image",base64);
            postDataParams.put("language", mLanguage);
            postDataParams.put("OCREngine",2);

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(getPostDataString(postDataParams));
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.d(TAG, response.toString());
            JSONObject jsonData = new JSONObject(String.valueOf(response));

            // Filter the return values to results and errors
            if (jsonData.has("ParsedResults")) {
                return jsonData.getJSONArray("ParsedResults").getJSONObject(0);
            } else if (jsonData.has("IsErroredOnProcessing")
                    && jsonData.getBoolean("IsErroredOnProcessing")) {
                return jsonData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (callback != null && response != null) {
            callback.onOCRCallbackResults(response);
        }
    }

    private String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        Iterator<String> itr = params.keys();

        boolean first = true;
        while (itr.hasNext()) {
            String key = itr.next();
            Object value = params.get(key);

            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }

    OCRAsyncTask setCallback(OCRCallback callback) {
        this.callback = callback;
        return this;
    }

    public interface OCRCallback {
        void onOCRCallbackResults(JSONObject response);
    }
}
