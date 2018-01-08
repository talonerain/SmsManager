package com.sdo.gmm.sms;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    ContentObserver smsObserver;
    TextView txtContent;
    TextView txtSendResult;
    TextView txtFailReason;
    TextView recieve_hint;
    TextView tv_status;
    boolean status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnStart = (Button) findViewById(R.id.btn_start_listen);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!status) {
                    status = true;
                    startListen();
                }
            }
        });
        Button btnStop = (Button) findViewById(R.id.btn_stop_listen);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status) {
                    status = false;
                    stopListen();
                }
            }
        });
        txtContent = (TextView) findViewById(R.id.sms_content);
        txtSendResult = (TextView) findViewById(R.id.send_result);
        txtFailReason = (TextView) findViewById(R.id.failed_reson);
        recieve_hint = (TextView) findViewById(R.id.recieve_hint);
        tv_status = (TextView) findViewById(R.id.tv_status);
    }

    void startListen() {
        Log.d("sss", "开启监听");
        tv_status.setText("当前状态：正在监听短信...");
        smsObserver = new SmsObserver(new Handler(), MainActivity.this, new Callback() {

            @Override
            public void recieve(final String sms) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recieve_hint.setVisibility(View.VISIBLE);
                        txtContent.setVisibility(View.VISIBLE);
                        txtContent.setText(sms);
                    }
                });
            }

            @Override
            public void send(int result) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtSendResult.setVisibility(View.VISIBLE);
                        txtSendResult.setText("上报结果：成功");
                        txtFailReason.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void failed(final String msg) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtSendResult.setVisibility(View.VISIBLE);
                        txtSendResult.setText("上报结果：失败");
                        txtFailReason.setVisibility(View.VISIBLE);
                        txtFailReason.setText("失败原因：" + msg);
                    }
                });
            }
        });
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true,
                smsObserver);
    }

    void stopListen() {
        Log.d("sss", "关闭监听");
        tv_status.setText("当前状态：未监听短信");
        txtContent.setVisibility(View.GONE);
        txtSendResult.setVisibility(View.GONE);
        txtFailReason.setVisibility(View.GONE);
        recieve_hint.setVisibility(View.GONE);
        getContentResolver().unregisterContentObserver(smsObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(smsObserver);
    }

    interface Callback{
        void recieve(String sms);

        void send(int result);

        void failed(String msg);
    }
}
