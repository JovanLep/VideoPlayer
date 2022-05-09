package com.socket.h265toupinpusher;

import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;

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
import java.util.Arrays;

public class CodecLiveH264 extends Thread {

    private MediaProjection mediaProjection;
    private MediaCodec mediaCodec;

    private int width = 1080;
    private int height = 1920;

    private VirtualDisplay virtualDisplay;
    private byte[] sps_pps_buf;

    private SocketLive socketLive;

    public CodecLiveH264(MediaProjection mediaProjection, SocketLive socketLive) {
        this.mediaProjection = mediaProjection;
        this.socketLive = socketLive;
    }

    public void startLive() {
        try {
            //            mediacodec  中间联系人      dsp芯片   帧
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(KEY_BIT_RATE, width * height);
            format.setInteger(KEY_FRAME_RATE, 20);
            format.setInteger(KEY_I_FRAME_INTERVAL, 1);
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = mediaCodec.createInputSurface();
            //创建场地
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "les",
                    width, height, 1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface,
                    null, null);

        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }


    @Override
    public void run() {

        mediaCodec.start();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        while (true) {
            try {
                int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 100 * 1000);

                if (outputBufferId >= 0) {

                    ByteBuffer byteBuffer = mediaCodec.getOutputBuffer(outputBufferId);
//                byte[] outData = new byte[bufferInfo.size];
//                outputBuffer.get(outData);
//                writeBytes(outData);
//                writeContent(outData);
                    dealFrame(byteBuffer, bufferInfo);
                    //最后释放
                    mediaCodec.releaseOutputBuffer(outputBufferId, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo bufferInfo) {

        int offset = 4;

        if (bb.get(2) == 0x01) {
            offset = 3;
        }

        int type = (bb.get(offset) & 0x1F);

        if (type == 7) {
            //得到了sps和pps
            sps_pps_buf = new byte[bufferInfo.size];
            bb.get(sps_pps_buf);
            //等I 过来 加到I帧之前
        } else if (type == 5) {
            byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            byte[] new_I = new byte[sps_pps_buf.length + bytes.length];
            System.arraycopy(sps_pps_buf, 0, new_I, 0, sps_pps_buf.length);
            System.arraycopy(bytes, 0, new_I, sps_pps_buf.length, bytes.length);
            //用socket发送出去
            socketLive.sendData(new_I);
        } else {
            byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            socketLive.sendData(bytes);
            Log.v("les", "视频数据  " + Arrays.toString(bytes));
        }
    }

    public void writeBytes(byte[] array) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/codec.h264", true);
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
            sb.append(hexChar[b & 0x0f]);
        }

        Log.e("TAG", "writeContent: " + sb.toString());
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/codecH264.txt", true);
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
        return sb.toString();
    }
}
