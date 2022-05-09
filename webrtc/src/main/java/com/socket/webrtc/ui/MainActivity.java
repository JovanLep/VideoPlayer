package com.socket.webrtc.ui;

import static com.socket.webrtc.Configs.TYPE_CALL;
import static com.socket.webrtc.Configs.TYPE_RECEIVE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.socket.webrtc.audio.AudioRecodeLive;
import com.socket.webrtc.Configs;
import com.socket.webrtc.video.DecodePlayerLiveH264;
import com.socket.webrtc.view.LocalSurfaceView;
import com.socket.webrtc.R;
import com.socket.webrtc.socket.SocketCallback;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SocketCallback {
    private LocalSurfaceView callMe;
    private SurfaceView calledUsers;
    private Surface surface;
    private DecodePlayerLiveH264 decodecPlayerLiveH264;
    private AudioRecodeLive audioRecodeLive;
    private String userType;
    private boolean isPlay = true;
    private ImageView mkView;
    private TextView tip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callMe = findViewById(R.id.call_me);
        calledUsers = findViewById(R.id.called_users);
        mkView = findViewById(R.id.receive);
        tip = findViewById(R.id.tip);
        userType = getIntent().getStringExtra("userType");
        initView();
    }

    private void initView() {
        calledUsers.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                surface = holder.getSurface();
                decodecPlayerLiveH264 = new DecodePlayerLiveH264();
                decodecPlayerLiveH264.initDecoder(surface);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            }
        });

    }

    @Override
    public void callBack(byte[] data) {

        if (data[0] == Configs.STREAM_VIDEO) {
            // 视频
            if (decodecPlayerLiveH264 != null) {
                decodecPlayerLiveH264.videoCallBack(data);
            }
        } else if (data[0] == Configs.STREAM_AUDIO) {
            // 音频
            if (audioRecodeLive != null) {
                audioRecodeLive.doPlay(data);
            }
        }
    }

    //为了不产生回音、、
    public void connect(View view) {
        if (userType.equals(TYPE_CALL)) {
            callMe.startCaptrue(TYPE_CALL, this);
            tip.setText("主叫");
        } else {
            callMe.startCaptrue(TYPE_RECEIVE, this);
            tip.setText("被叫");
        }
        audioRecodeLive = new AudioRecodeLive(callMe.getSocketLive());
        audioRecodeLive.initPlay(this);
        audioRecodeLive.startRecode(this);
    }

    public void audioState(View view) {
        if (isPlay) {
            mkView.setImageResource(R.mipmap.icon_mk);
            isPlay = false;
            if (audioRecodeLive != null) {
                audioRecodeLive.stopPlay();
            }
        } else {
            if (audioRecodeLive != null) {
                audioRecodeLive.Play();
            }
            isPlay = true;
            mkView.setImageResource(R.mipmap.icon_mk_open);
        }
    }

    public void change(View view) {
        try {
            callMe.changeCamera();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}