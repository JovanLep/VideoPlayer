package com.socket.videodemo;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class H264Player implements Runnable {
    private String path;
    private MediaCodec mediaCodec;
    private Surface surface;

    public H264Player(String path, Surface surface) {
        this.path = path;
        this.surface = surface;
//        "video/avc"
//        初始化编码器

        try {
            //支持硬解  avc
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 364, 364);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaCodec.configure(videoFormat, surface, null, 0);

        } catch (IOException e) {
            Log.e("TAG", "H264Player: 不支持");
            e.printStackTrace();
        }
        Log.e("TAG", "H264Player: 支持");
    }

    public void play() {
        mediaCodec.start();
        new Thread(this).start();
    }

    @Override
    public void run() {
        decodeH264();
    }

    private void decodeH264(){
        //过程语言
        byte[] bytes = null;
        try {
            bytes = getBytes(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int startIndex = 0;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        while (true) {
            int nextFrameStart = findByFrame(bytes, startIndex + 2, bytes.length);
            //说明等待时间。
//        ByteBuffer[] byteBuffers=mediaCodec.getInputBuffers();
            int inIndex = mediaCodec.dequeueInputBuffer(10 * 1000);
            if (inIndex >= 0) {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inIndex);
                //到底丢多少？    按照帧来丢
//            通过分隔符来判断一个完整帧  起始  结束分隔符
                int length = nextFrameStart - startIndex;
//                丢一部分数据
                inputBuffer.put(bytes, startIndex, length);

                //数据从cpu到dsp
                mediaCodec.queueInputBuffer(inIndex, 0, length, 0, 0);

            }
            int outIndex = mediaCodec.dequeueOutputBuffer(info, 1100);
            if (outIndex >= 0) {
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mediaCodec.releaseOutputBuffer(outIndex, true);
            }
            startIndex = nextFrameStart;
        }
    }

    private int findByFrame(byte[] bytes, int start, int totalsize) {

        for (int i = start; i < totalsize-4; i++) {
            if (((bytes[i] == 0x00) && (bytes[i + 1] == 0x00) && (bytes[i + 2] == 0x00) && (bytes[i + 3] == 0x01))
                    || (bytes[i] == 0x00) && (bytes[i + 1] == 0x00) && (bytes[i + 2] == 0x01)) {
                return i;
            }
        }
        return -1;
    }

    private byte[] getBytes(String path) throws IOException {
        InputStream is =new DataInputStream(new FileInputStream(path));
        int len;
        int size=1024;
        byte[] buf;
        ByteArrayOutputStream bos =new ByteArrayOutputStream();
        buf=new byte[size];
        while ((len= is.read(buf,0,size))!=-1){
            bos.write(buf,0,len);
        }
        buf=bos.toByteArray();
        return buf;
    }
}
