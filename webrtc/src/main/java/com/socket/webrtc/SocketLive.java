package com.socket.webrtc;

import android.text.TextUtils;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;

import static com.socket.webrtc.Constants.TAG;

//音视频通话客户端
public class SocketLive {

    private final SocketCallback socketCallback;
    private final String type;

    public SocketLive(String type, SocketCallback socketCallback) {
        this.type = type;
        this.socketCallback = socketCallback;
    }

    //主叫端
    MyWebSocketClient myWebSocketClient;
    //被叫
    private WebSocket webSocket;

    public void start() {
        if (type.equals(Constants.TYPE_CALL)) {
            callStart();
        } else if (type.equals(Constants.TYPE_RECEIVE)) {
            receivedStart();
        }
    }

    public void close() {
        if (type.equals(Constants.TYPE_CALL)) {
            callClose();
        } else if (type.equals(Constants.TYPE_RECEIVE)) {
            receiveClose();
        }
    }

    public synchronized void sendData(byte[] bytes, int streamType) {
        byte[] newBuf = new byte[bytes.length + 1];
        if (streamType == Constants.STREAM_VIDEO) {
            newBuf[0] = 1;
        } else {
            newBuf[0] = 0;
        }
        //接收端 1
        System.arraycopy(bytes, 0, newBuf, 1, bytes.length);

        if (type.equals(Constants.TYPE_CALL) && webSocket != null && webSocket.isOpen()) {
            webSocket.send(newBuf);
        } else if (type.equals(Constants.TYPE_RECEIVE) && myWebSocketClient != null && (myWebSocketClient.isOpen())) {
            myWebSocketClient.send(newBuf);
        }
    }

    private final WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(Constants.PORT)) {
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            SocketLive.this.webSocket = conn;
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        }

        @Override
        public void onMessage(WebSocket conn, String message) {

        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer bytes) {
            byte[] buf = new byte[bytes.remaining()];
            bytes.get(buf);
            socketCallback.callBack(buf);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
        }

        @Override
        public void onStart() {
        }
    };


    private class MyWebSocketClient extends WebSocketClient {

        public MyWebSocketClient(URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
        }

        @Override
        public void onMessage(String s) {
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            byte[] buf = new byte[bytes.remaining()];
            bytes.get(buf);
            socketCallback.callBack(buf);
        }

        @Override
        public void onClose(int i, String s, boolean b) {
        }

        @Override
        public void onError(Exception e) {
        }
    }

    private void receivedStart() {
        if (TextUtils.isEmpty(App.getApp().ipAddress)) return;
        try {
            String str = "ws://" + App.getApp().ipAddress + ":" + Constants.PORT;
            URI url = new URI(str);
            myWebSocketClient = new MyWebSocketClient(url);
            myWebSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callStart() {
        webSocketServer.start();
    }


    private void callClose() {
        try {
            webSocket.close();
            webSocketServer.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void receiveClose() {
        try {
            myWebSocketClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
