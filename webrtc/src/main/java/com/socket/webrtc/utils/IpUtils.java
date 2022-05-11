package com.socket.webrtc.utils;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.socket.webrtc.Configs;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpUtils {

    public static String getNetWorkIp(Context context) {
        ConnectivityManager conMann = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkMobile = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo networkWifi = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkWifi.isConnected()) {
            Log.e(Configs.TAG, "getNetWorkType: networkWifi");
            return wifiIpAddress(context);
        } else if (networkMobile.isConnected()) {
            Log.e(Configs.TAG, "getNetWorkType: networkMobile");
            return localIpAddress();
        } else {
            return null;
        }
    }

    private static String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    private static String localIpAddress() {
        try {
            NetworkInterface networkInterface;
            InetAddress inetAddress;
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
            return null;
        } catch (SocketException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String intToIp(int ipInt) {
        return (ipInt & 0xFF) + "." +
                ((ipInt >> 8) & 0xFF) + "." +
                ((ipInt >> 16) & 0xFF) + "." +
                ((ipInt >> 24) & 0xFF);
    }
}
