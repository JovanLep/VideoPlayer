package com.socket.h265toupinpusher;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CodecLiveH265 extends Thread {

    private MediaProjection mediaProjection;
    private int width;
    private int height;
    private MediaCodec mediaCodec;
    private VirtualDisplay virtualDisplay;
    private byte[] vps_sps_pps_buf;

    public CodecLiveH265(MediaProjection mediaProjection) {
        this.mediaProjection = mediaProjection;
    }

    public void startLive() {
        width = 720;
        height = 1080;


        try {
            MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mediaCodec = MediaCodec.createEncoderByType("video/hevc");
            mediaCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            Surface inputSurface = mediaCodec.createInputSurface();

            virtualDisplay = mediaProjection.createVirtualDisplay("les", width, height, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                    , inputSurface, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }

    @Override
    public void run() {
        super.run();
        mediaCodec.start();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        while (true) {
            int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 10 * 1000);
            if (outputBufferId >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getInputBuffer(outputBufferId);
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                writeBytes(outData);
                writeContent(outData);
                //最后释放
                mediaCodec.releaseOutputBuffer(outputBufferId, false);
            }
        }
    }

    public void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo bufferInfo) {

        int offset = 4;

        if (bb.get(3) == 0x01) {
            offset = 3;
        }

        //求帧类型1
        int type = (bb.get(offset) & 0x7e) >> 1;

        if (type == 32) {
            //得到了vps、sps和pps   它们一起输出
            vps_sps_pps_buf=new byte[bufferInfo.size];
            bb.get(vps_sps_pps_buf);
        }
    }

    public void writeBytes(byte[] array) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/codec.h265", true);
            fileOutputStream.write(array);
            fileOutputStream.write('\n');

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //做直播的时候出现的情况！！！！    出现视频质量不清晰。     如何定位问题    即将推流的数据保存到本地 。保证是否一样

    public String writeContent(byte[] array) {

        char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(hexChar[(b & 0xf0) >> 4]);
            sb.append(hexChar[b & 0xf0]);
            Log.e("TAG", "writeContent: " + sb.toString());
            FileWriter fileWriter = null;

            try {
                fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/codecH265.txt", true);
                fileWriter.write(sb.toString());
                fileWriter.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileWriter != null)
                        fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
