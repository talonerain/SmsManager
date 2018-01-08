package com.sdo.gmm.sms;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liusiyu.taloner on 2018/1/5.
 */

public class SmsObserver extends ContentObserver {
    Context context;
    Map<String, String> params;
    MainActivity.Callback callback;
    String baseUrl = "";
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public SmsObserver(Handler handler, Context context, MainActivity.Callback callback) {
        super(handler);
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void onChange(boolean selfChange) {
        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://sms/inbox"), null, null, null, null);

        cursor.moveToNext();
        StringBuilder sb = new StringBuilder();
        sb.append("address="+cursor.getString(cursor.getColumnIndex("address")));
        sb.append(", body="+cursor.getString(cursor.getColumnIndex("body")));
        sb.append(", date="+cursor.getString(cursor.getColumnIndex("date")));
        Log.i("短信内容", sb.toString());

        String address = cursor.getString(cursor.getColumnIndex("address"));
        String body = cursor.getString(cursor.getColumnIndex("body"));
        String date = cursor.getString(cursor.getColumnIndex("date"));
        callback.recieve(body);
        sendResult(address, body, date);
        cursor.close();
        super.onChange(selfChange);
    }

    private void sendResult(String address, String body, String date) {
        StringBuilder urlGet = new StringBuilder();
        urlGet.append(baseUrl).append("?sms_number=").append(address)
                .append("&message_content=").append(body)
                .append("&message_date=").append(date);
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder()
                .get()
                .url(urlGet.toString());
        final Request request = builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Sms", "发送失败");
                callback.failed("上报接口发送失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    try {
                        JSONObject json = new JSONObject(result);
                        if (json.optInt("return_code", -1) == 0) {
                            Log.d("Sms", "上报成功");
                            callback.send(0);
                        } else {
                            Log.d("Sms", "上报失败");
                            callback.failed(json.optString("return_message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 404) {
                    callback.failed("404 not found");
                } else {
                    callback.failed("接口错误");
                }
            }
        });
    }
}
