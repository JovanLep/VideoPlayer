package com.socket.webrtc;

import static com.socket.webrtc.YuvUtils.nv21ToNv12;
import static com.socket.webrtc.Constants.outTimes;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class EncodePushLiveH264 {

    private MediaCodec mediaCodec;
    private final int width;
    private final int height;
    private int frameIndex;
    private byte[] yuv;
    private byte[] sps_pps_buf;
    private final SocketLive socketLive;

    public EncodePushLiveH264(String type, int width, int height, SocketCallback socketCallback) {
        this.socketLive = new SocketLive(type, socketCallback);
        socketLive.start();
        this.width = width;
        this.height = height;
    }

    public SocketLive getSocketLive() {
        return socketLive;
    }

    public void startLive() {
        try {

            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", height, width);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 1080 * 1920);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);

            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();

            int bufferLength = width * height * 3 / 2;
            yuv = new byte[bufferLength];

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //    一帧数据过来
    public void encodeFrame(boolean isFront, byte[] bytes) {
        byte[] nv12 = nv21ToNv12(bytes);

        if (!isFront) {
            yuv = YuvUtils.nv21dataRotate90(nv12, width, height);
        } else {
            yuv = YuvUtils.rotateYUV420Degree270(nv12, width, height);
        }

        int inputBuffer = mediaCodec.dequeueInputBuffer(outTimes);
        if (inputBuffer >= 0) {
            ByteBuffer[] buffer = mediaCodec.getInputBuffers();
            ByteBuffer inputBUffer = buffer[inputBuffer];
            inputBUffer.clear();
            inputBUffer.put(yuv);
            long presentationTimeUs = computePresentationTime(frameIndex);
            mediaCodec.queueInputBuffer(inputBuffer, 0, yuv.length, presentationTimeUs, 0);
            frameIndex++;
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, outTimes);
        if (outputIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputIndex);
            dealFrame(Objects.requireNonNull(outputBuffer), bufferInfo);
            mediaCodec.releaseOutputBuffer(outputIndex, false);
        }
    }

    private long computePresentationTime(int frameIndex) {
        return frameIndex * 1000 * 1000L / 15;
    }


    private void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo bufferInfo) {
//00 00  00 01
//00 00 01
        //  67    0   1 1    0  0 1 11
        //  1f    0   0 0    1  1 1 11
        int offset = 4;
        if (bb.get(2) == 0x01) {
            offset = 3;
        }
//    sps
        int type = (bb.get(offset) & 0x1F);
//        有 1  没有2  type=7
        if (type == 0x7) {
//            不发送       I帧
            sps_pps_buf = new byte[bufferInfo.size];
            bb.get(sps_pps_buf);
        } else if (type == 0x5) {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
//            bytes           I帧的数据
            byte[] newBuf = new byte[sps_pps_buf.length + bytes.length];
            System.arraycopy(sps_pps_buf, 0, newBuf, 0, sps_pps_buf.length);
            System.arraycopy(bytes, 0, newBuf, sps_pps_buf.length, bytes.length);
//            编码层   推送出去
            socketLive.sendData(newBuf, Constants.STREAM_VIDEO);

        } else {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            this.socketLive.sendData(bytes, Constants.STREAM_VIDEO);
        }
    }
}
