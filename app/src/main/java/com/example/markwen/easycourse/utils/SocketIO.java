package com.example.markwen.easycourse.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.markwen.easycourse.models.main.User;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import io.realm.Realm;
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

                Log.e("com.example.easycourse", "" + obj);
                byte[] avatar = null;
                String avatarUrlString = "";
                try {
                    avatarUrlString = obj.getString("avatarUrl");
                    URL avatarUrl = new URL(avatarUrlString);
                    HttpURLConnection conn = (HttpURLConnection) avatarUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    conn.setUseCaches(false);

                    InputStream is = conn.getInputStream();

                    try {
                        avatar = IOUtils.toByteArray(conn.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } catch (MalformedURLException e) {
                    Log.e("com.example.easycourse", e.toString());
                } catch (JSONException e) {
                    Log.e("com.example.easycourse", e.toString());
                } catch (IOException e) {
                    Log.e("com.example.easycourse", e.toString());
                }
                Realm.init(context);
                Realm realm = Realm.getDefaultInstance();

                User user = null;
                try {
                    user = new User(obj.getString("_id"), obj.getString("displayName"), avatar, avatarUrlString, obj.getString("email"), obj.getString("university"));
                } catch (JSONException e) {
                    Log.e("com.example.easycourse", e.toString());
                }

                Log.e("com.example.easycourse", "user in realm? "+User.isUserInRealm(user, realm));
                User.updateUserToRealm(user, realm);
                Log.e("com.example.easycourse", "user in realm? "+User.isUserInRealm(user, realm));

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
