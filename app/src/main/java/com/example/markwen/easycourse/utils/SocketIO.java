package com.example.markwen.easycourse.utils;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by nisarg on 9/11/16.
 */


public class SocketIO {
    private String CHAT_SERVER_URL = "";

    private Socket mSocket;

    public SocketIO() {
        try {
            mSocket = IO.socket(CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void onConnect(Emitter.Listener onConnect) {
        mSocket.on("connection", onConnect);
        mSocket.connect();
    }

    public void sendMessage(String message, String roomId) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("text", message);
        jsonParam.put("roomId", roomId);
        mSocket.emit("message", jsonParam);
    }
}
