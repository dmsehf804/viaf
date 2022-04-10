package com.kakaovx.palmdetector;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommHelper {
    private String url = "http://10.12.200.137:5000/";

    public CommHelper(final String _url) {
        url = _url;
    }

    public String identifyPalmBitmap(Bitmap palmBitmap, ArrayList<Bitmap> gapBitmaps) throws IOException {
        if (palmBitmap == null ||
                gapBitmaps == null || gapBitmaps.size() != 4)
            return null;

        final OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("palm_id", "test");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        palmBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] palmByteArray = stream.toByteArray();

        builder = builder.addFormDataPart("palm", "palm.png", RequestBody.create(MediaType.parse("image/*png"), palmByteArray));

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(String.format("%s%s", url, "authenticate"))
                .addHeader("Content-Type", " application/x-www-form-urlencoded")
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() == false)
                return null;

            String result = response.body().string();
            Log.d("PalmDetector-CommHelper", result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject == null)
                    return null;

                String user_id = jsonObject.getString("user_id");


                return user_id;
            }
            catch (Exception ex) {

                ex.printStackTrace();
                return null;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


}
