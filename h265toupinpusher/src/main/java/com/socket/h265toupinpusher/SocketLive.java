package com.socket.h265toupinpusher;

import android.media.projection.MediaProjection;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SocketLive {

    //另外一台设备的socket
    private WebSocket webSocket;
    CodecLiveH264 codecLiveH264;
    //网络通信里面只有cs模型

    public void start(MediaProjection mediaProjection) {
        webSocketServer.start();
        codecLiveH264 = new CodecLiveH264( mediaProjection,this);
        codecLiveH264.startLive();
    }


    public void sendData(byte[] bytes) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.send(bytes);
        }
    }

    public void close() {
        try {
            webSocket.close();
            webSocketServer.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //当成服务端
    private WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(9007)) {
        @Override
        public void onOpen(WebSocket webSocket, ClientHandshake handshake) {
            SocketLive.this.webSocket = webSocket;
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {

        }

        @Override
        public void onMessage(WebSocket conn, String message) {

        }

        @Override
        public void onError(WebSocket conn, Exception ex) {

        }

        @Override
        public void onStart() {

        }
    };
}
