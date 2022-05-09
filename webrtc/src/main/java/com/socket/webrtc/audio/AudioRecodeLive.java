package com.socket.webrtc.audio;

import static com.socket.webrtc.Configs.CHANNEL_CONFIG_OUT;
import static com.socket.webrtc.Configs.SAMPLE_RATE_INHZ;
import static com.socket.webrtc.Configs.CHANNEL_CONFIG;
import static com.socket.webrtc.Configs.AUDIO_FORMAT;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.core.app.ActivityCompat;

import com.socket.webrtc.Configs;
import com.socket.webrtc.socket.SocketLive;

public class AudioRecodeLive {

    private AudioTrack audioTrack;

    private AudioRecord audioRecord;
    private HandlerThread handlerThread;
    private Handler workHandler;
    private boolean isRecording = false;
    private SocketLive socketLive;

    public AudioRecodeLive(SocketLive socketLive) {
        this.socketLive = socketLive;
    }

    public void startRecode(Context context) {

        //子线程
        handlerThread = new HandlerThread("handlerThread");
        workHandler = new Handler();
        handlerThread.start();

        AudioManager audioManager= (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        //    bufferSizeInBytes  最小数据量   双通道 44100  16   怎么计算？
        // 函数计算
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

        //开始录音
        audioRecord.startRecording();
        isRecording = true;
        byte[] data = new byte[minBufferSize];
        workHandler.post(() -> {
            while (isRecording) {
                audioRecord.read(data, 0, minBufferSize);
                socketLive.sendData(data, Configs.STREAM_AUDIO);
            }
        });
    }

    public void initPlay(Context context) {
        AudioManager audioManager= (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        int streamType = AudioManager.STREAM_MUSIC;
        int mode = AudioTrack.MODE_STREAM;

        int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT);
        audioTrack = new AudioTrack(streamType, SAMPLE_RATE_INHZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT, minBufferSize, mode);
        audioTrack.setVolume(16f);
        Play();
    }

    public void stopPlay() {
        audioTrack.stop();
    }

    public void Play() {
        audioTrack.play();
    }

    public void doPlay(byte[] mBuffer) {
        //参考视频播放
        int ret = audioTrack.write(mBuffer, 1, mBuffer.length - 1);

        switch (ret) {
            case AudioTrack.ERROR_INVALID_OPERATION:
            case AudioTrack.ERROR_BAD_VALUE:
            case AudioManager.ERROR_DEAD_OBJECT:
                return;
            default:
                break;
        }
    }
}
