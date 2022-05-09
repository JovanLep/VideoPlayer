package com.socket.h265toupinplayer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class H264Player implements SocketLive.SocketCallback {

    private MediaCodec mediaCodec;
    private int width=720;
    private int height=1080;

    public H264Player(Surface surface) {

        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mediaCodec.configure(videoFormat, surface, null, 0);
            mediaCodec.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callBack(byte[] data) {
        //处理消息  这就是一帧
        int index = mediaCodec.dequeueInputBuffer(10 * 1000);

        if (index >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
            inputBuffer.clear();
            inputBuffer.put(data, 0, data.length);
            mediaCodec.queueInputBuffer(index, 0, data.length, System.currentTimeMillis(), 0);

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100 * 1000);

            while (outIndex >= 0) {
                mediaCodec.releaseOutputBuffer(outIndex, true);
                outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        }
    }
}
