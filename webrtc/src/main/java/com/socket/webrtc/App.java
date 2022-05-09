package com.socket.webrtc;

import android.app.Application;

public class App extends Application {

    private static App myApplication = null;
    public static App getApp() {
        return myApplication;
    }


    public String ipAddress;


    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }
}
