package com.socket.videodemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaCodec;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private SurfaceView sview;
    private H264Player h264Player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Surface画布
        sview = (SurfaceView) findViewById(R.id.sview);
        sview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                Surface surface = surfaceHolder.getSurface();
                File file = new File(Environment.getExternalStorageDirectory(), "out.h264");
                h264Player=new H264Player(file.getAbsolutePath(),surface);
                h264Player.play();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });

    }
}