package com.socket.h265toupinplayer;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class SocketLive {

    //另外一台设备的socket
    private WebSocket webSocket;
    //网络通信里面只有cs模型
    private SocketCallback socketCallback;
    private MyWebSocketClient myWebSocketClient;

    int port;

    public SocketLive(SocketCallback socketCallback, int port) {
        this.socketCallback = socketCallback;
        this.port = port;
    }

    public void start() {
        try {
            URI uri = new URI("ws://172.16.7.109:9007");
            myWebSocketClient = new MyWebSocketClient(uri);
            myWebSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private class MyWebSocketClient extends WebSocketClient {

        public MyWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.i("TAG", "打开 socket  onOpen: ");
        }

        @Override
        public void onMessage(String message) {

        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            super.onMessage(bytes);
            Log.i("TAG", "收到: " + bytes.remaining());
            byte[] buf = new byte[bytes.remaining()];
            bytes.get(buf);
            socketCallback.callBack(buf);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }

    public interface SocketCallback {
        void callBack(byte[] buf);
    }
}

