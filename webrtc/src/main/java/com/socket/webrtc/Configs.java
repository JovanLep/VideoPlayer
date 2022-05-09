package com.socket.webrtc;

import android.media.AudioFormat;

public class Configs {

    public static final int STREAM_AUDIO = 0;
    public static final int STREAM_VIDEO = 1;

    public static final int PORT = 7007;

    public static final int width = 1080;
    public static final int height = 1920;

    public static final String TAG = "123456";
    public static final long outTimes = 100 * 1000L;

    public static final String TYPE_CALL = "call";
    public static final String TYPE_RECEIVE = "receive";

    public static final int FRONT = 1;//前置摄像头标记
    public static final int BACK = 2;//后置摄像头标记
    public static int currentCameraType = -1;//当前打开的摄像头标记

    //采样率
    public static final int SAMPLE_RATE_INHZ = 44100;

    public static final int SAMPLE_RATE_16000 = 16000;
    //声道数  双通道
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    public static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    //返回音频格式
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
}
