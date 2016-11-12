package com.example.markwen.easycourse.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by nisarg on 9/11/16.
 */


public class SocketIO {
    private String CHAT_SERVER_URL = "https://zengjintaotest.com";
    private Context context;
    private Socket socket;
    private Activity that;

    public SocketIO(final Activity that, Context context){
        this.context = context;
        this.that = that;
        IO.Options opts = new IO.Options();
        //opts.forceNew = true;
        opts.query = "token=" + APIFunctions.getUserToken(context);
        try {
            socket = IO.socket(CHAT_SERVER_URL, opts);
        } catch (URISyntaxException e) {
            Log.e("com.example.easycourse", "not connecting "+e.toString());
            e.printStackTrace();
        }
        socket.connect();
        this.publicListener();
    }

    public void publicListener(){
        socket.connect();
        socket.on("connect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit("syncUser",1);
                Log.e("com.example.easycourse", "connected");
            }
        });

        socket.on("syncUser", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    getHistMessage();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject obj = (JSONObject) args[0];
                Log.e("com.example.easycourse", "got syncUser");

                Log.e("com.example.easycourse", "" + obj);
            }
        });
        
    }


    public void sendMessage(String message, String roomId, String toUserId, String imageUrl, int imageWidth, int imageHeight) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("id", UUID.randomUUID().toString());
        jsonParam.put("toRoom", roomId);
        jsonParam.put("toUser", toUserId);
        jsonParam.put("text", message);
        jsonParam.put("imageUrl", imageUrl);
        jsonParam.put("imageWidth", imageWidth);
        jsonParam.put("imageHeight", imageHeight);

        socket.emit("message", jsonParam);
    }

    public void syncUser(){
        socket.emit("syncUser",1);
    }

    public void getHistMessage() throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("updatedTime", System.currentTimeMillis()/1000);
        socket.emit("getHistMessage", jsonParam);
    }
}
