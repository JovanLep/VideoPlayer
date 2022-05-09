package com.socket.webrtc.utils;

import android.os.Environment;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class YuvUtils {

    private static byte[] nv12;

    public static byte[] nv21ToNv12(byte[] nv21) {
        //0 ... bytes
//        y的范围  0~ width * height
//        nv21   0----nv21.size
        int size = nv21.length;
        nv12 = new byte[size];
        int len = size * 2 / 3;
        System.arraycopy(nv21, 0, nv12, 0, len);
        int i = len;
        while (i < size - 1) {
            nv12[i] = nv21[i + 1];
            nv12[i + 1] = nv21[i];
            i += 2;
        }
        return nv12;
    }

    public static void portraitData2Raw(byte[] data, byte[] output, int width, int height) {
        int y_len = width * height;
        int uvHeight = height >> 1;
        int k = 0;
        for (int j = 0; j < width; j++) {
            for (int i = height - 1; i >= 0; i--) {
                output[k++] = data[width * i + j];
            }
        }
        for (int j = 0; j < width; j += 2) {
            for (int i = uvHeight - 1; i >= 0; i--) {
                output[k++] = data[y_len + width * i + j];
                output[k++] = data[y_len + width * i + j + 1];
            }
        }
    }

    public static byte[] nv21dataRotate90(byte[] nv21_src_data, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        byte[] nv21Data = new byte[buffser_size];
        // Rotate the Y luma
        int i = 0;
        int startPos = (height - 1) * width;
        for (int x = 0; x < width; x++) {
            int offset = startPos;
            for (int y = height - 1; y >= 0; y--) {
                nv21Data[i] = nv21_src_data[offset + x];
                i++;
                offset -= width;
            }
        }
        // Rotate the U and V color components
        i = buffser_size - 1;
        for (int x = width - 1; x > 0; x = x - 2) {
            int offset = y_size;
            for (int y = 0; y < height / 2; y++) {
                nv21Data[i] = nv21_src_data[offset + x];
                i--;
                nv21Data[i] = nv21_src_data[offset + (x - 1)];
                i--;
                offset += width;
            }
        }
        return nv21Data;
    }

    public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth, int imageHeight){
        byte[] yuv =new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i =0;
        for(int x = imageWidth-1;x >=0;x--){
            for(int y =0;y < imageHeight;y++){
                yuv[i]= data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth*imageHeight;

        for(int x = imageWidth-1;x >0;x=x-2){
            for(int y =0;y < imageHeight/2;y++){
                yuv[i]= data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i++;
                yuv[i]= data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i++;
            }
        }
        return yuv;
    }

    public static void writeBytes(byte[] array) {
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

    public static void writeBytesPcm(byte[] array) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/codec.pcm", true);
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
    public static String writeContent(byte[] array) {

        char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder sb = new StringBuilder();

        for (byte b : array) {
            sb.append(hexChar[(b & 0xf0) >> 4]);
            sb.append(hexChar[b & 0x0f]);
        }

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

    //做直播的时候出现的情况！！！！    出现视频质量不清晰。     如何定位问题    即将推流的数据保存到本地 。保证是否一样
    public static String writeContentPcm(byte[] array) {
        char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder sb = new StringBuilder();

        for (byte b : array) {
            sb.append(hexChar[(b & 0xf0) >> 4]);
            sb.append(hexChar[b & 0x0f]);
        }
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/codecPcm.txt", true);
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
