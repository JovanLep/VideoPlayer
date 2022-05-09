package com.socket.webrtc;

import static com.socket.webrtc.Constants.width;
import static com.socket.webrtc.Constants.height;
import static com.socket.webrtc.Constants.outTimes;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DecodePlayerLiveH264 {

    private MediaCodec mediaCodec;

    public void initDecoder(Surface surface) {
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            final MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
            format.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            mediaCodec.configure(format, surface, null, 0);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void videoCallBack(byte[] data) {
        int index = mediaCodec.dequeueInputBuffer(outTimes);
        if (index >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
            inputBuffer.clear();
            inputBuffer.put(data, 1, data.length - 1);
            mediaCodec.queueInputBuffer(index,
                    0, data.length, System.currentTimeMillis(), 0);
        }
//        获取到解码后的数据  编码 ipbn
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, outTimes);
        while (outputBufferIndex >= 0) {
            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }
}
