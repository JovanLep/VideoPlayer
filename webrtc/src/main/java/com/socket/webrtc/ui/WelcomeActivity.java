package com.socket.webrtc.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.socket.webrtc.App;
import com.socket.webrtc.Configs;
import com.socket.webrtc.R;
import com.socket.webrtc.adapter.IpAddressAdapter;
import com.socket.webrtc.utils.IpUtils;

import java.util.Arrays;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private EditText edtAddress;
    private RecyclerView recOnlineIp;
    private Button btWebrtcJoin;
    private TextView tvIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initView();
        checkPermission();
        initEvents();
    }


    private void initView() {
        edtAddress = findViewById(R.id.edt_address);
        recOnlineIp = findViewById(R.id.rec_online_ip);
        btWebrtcJoin = findViewById(R.id.bt_webrtc_join);
        tvIp = findViewById(R.id.tv_ip);

    }

    @SuppressLint("SetTextI18n")
    private void initEvents() {
        tvIp.setText("本机ip地址：" + IpUtils.getNetWorkIp(this));
        //-------------------音视频 通话-----------------------
        findViewById(R.id.bt_webrtc_call).setOnClickListener(view -> {
            String ip = edtAddress.getText().toString();
            if (TextUtils.isEmpty(ip.trim())) {
                Toast.makeText(WelcomeActivity.this, "主叫作为临时服务器IP不能为空~", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            intent.putExtra("userType", Configs.TYPE_CALL);
            startActivity(intent);
        });

        findViewById(R.id.bt_webrtc_receive).setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            intent.putExtra("userType", Configs.TYPE_RECEIVE);
            startActivity(intent);
        });

        //-------------------音视频 会议-----------------------
        List<String> ipList = Arrays.asList("172.16.7.109", "172.16.7.101", "172.16.7.77", "172.16.7.113");
        recOnlineIp.setLayoutManager(new LinearLayoutManager(this));
        IpAddressAdapter adapter = new IpAddressAdapter(this, ipList);
        recOnlineIp.setAdapter(adapter);
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CAMERA
            }, 1);
        }
        return false;
    }
}