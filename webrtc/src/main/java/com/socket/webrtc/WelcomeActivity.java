package com.socket.webrtc;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        checkPermission();

        EditText edtAddress = findViewById(R.id.edt_address);

        findViewById(R.id.bt_webrtc_call).setOnClickListener(view -> {
            String ip = edtAddress.getText().toString();
            if (TextUtils.isEmpty(ip.trim())) {
                Toast.makeText(WelcomeActivity.this, "主叫作为临时服务器IP不能为空~", Toast.LENGTH_SHORT).show();
                return;
            }
            App.getApp().ipAddress = ip.trim();
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            intent.putExtra("userType", Constants.TYPE_CALL);
            startActivity(intent);
        });

        findViewById(R.id.bt_webrtc_receive).setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            intent.putExtra("userType", Constants.TYPE_RECEIVE);
            startActivity(intent);
        });
    }


    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            }, 1);
        }
        return false;
    }
}