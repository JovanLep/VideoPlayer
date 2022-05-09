package com.socket.webrtc.view;

import static com.socket.webrtc.Configs.BACK;
import static com.socket.webrtc.Configs.FRONT;
import static com.socket.webrtc.Configs.currentCameraType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.socket.webrtc.video.EncodePushLiveH264;
import com.socket.webrtc.socket.SocketCallback;
import com.socket.webrtc.socket.SocketLive;

import java.io.IOException;

public class LocalSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Camera camera;
    private Camera.Size size;
    private byte[] buffer;
    private EncodePushLiveH264 encodePushLiveH264;

    public LocalSurfaceView(Context context) {
        super(context);
    }

    public LocalSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void startCaptrue(String type, SocketCallback callback) {
        encodePushLiveH264 = new EncodePushLiveH264(type, size.width, size.height, callback);
        encodePushLiveH264.startLive();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (encodePushLiveH264 != null) {
            encodePushLiveH264.encodeFrame(currentCameraType == FRONT, bytes);
        }
        camera.addCallbackBuffer(bytes);
    }

    public void changeCamera() throws IOException {
        camera.stopPreview();
        camera.release();
        if (currentCameraType == FRONT) {
            camera = openCamera(BACK);
        } else if (currentCameraType == BACK) {
            camera = openCamera(FRONT);
        }
        camera.setPreviewDisplay(getHolder());
        camera.setDisplayOrientation(90);
        buffer = new byte[size.width * size.height * 3 / 2];
        camera.addCallbackBuffer(buffer);
        camera.setPreviewCallbackWithBuffer(this);
        camera.startPreview();
    }

    //nv21 变化成nv12
    private void startPreView() {
        camera = openCamera(FRONT);
        Camera.Parameters parameters = camera.getParameters();
        size = parameters.getPreviewSize();
        try {
            camera.setPreviewDisplay(getHolder());
            camera.setDisplayOrientation(90);
            buffer = new byte[size.width * size.height * 3 / 2];
            camera.addCallbackBuffer(buffer);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        startPreView();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
    }

    @SuppressLint("NewApi")
    private Camera openCamera(int type) {
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontIndex = cameraIndex;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backIndex = cameraIndex;
            }
        }

        currentCameraType = type;
        if (type == FRONT && frontIndex != -1) {
            return Camera.open(frontIndex);
        } else if (type == BACK && backIndex != -1) {
            return Camera.open(backIndex);
        }
        return null;
    }

    public SocketLive getSocketLive() {
        return encodePushLiveH264.getSocketLive();
    }

}
