package com.example.markwen.easycourse.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.User;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.socket.client.Ack;
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

    public SocketIO(final Activity that, Context context) {
        this.context = context;
        this.that = that;

        IO.Options opts = new IO.Options();
        //opts.forceNew = true;
        opts.query = "token=" + APIFunctions.getUserToken(context);
        try {
            socket = IO.socket(CHAT_SERVER_URL, opts);
        } catch (URISyntaxException e) {
            Log.e("com.example.easycourse", "not connecting " + e.toString());
        }
        socket.connect();
        this.publicListener();
    }

    public void publicListener() {
        socket.connect();
        socket.on("connect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                syncUser();
                Log.e("com.example.easycourse", "connected");
            }
        });

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                saveMessageToRealm(obj);
            }
        });
    }

    public void sendMessage(String message, String roomId, String toUserId, String imageUrl, float imageWidth, float imageHeight) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("id", UUID.randomUUID().toString());
        jsonParam.put("toRoom", roomId);
        jsonParam.put("toUser", toUserId);
        jsonParam.put("text", message);
        jsonParam.put("imageUrl", imageUrl);
        jsonParam.put("imageWidth", imageWidth);
        jsonParam.put("imageHeight", imageHeight);

        socket.emit("message", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    JSONObject msgObj = obj.getJSONObject("msg");
                    saveMessageToRealm(msgObj);
                } catch (JSONException e) {
                    Log.e("com.example.easycourse", e.toString());
                }
            }
        });
    }

    public void syncUser() {
        socket.emit("syncUser", 1, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    getHistMessage();
                } catch (JSONException e) {
                    Log.e("com.example.easycourse", e.toString());
                }

                JSONObject obj = (JSONObject) args[0];
                JSONObject userObj = null;
                byte[] avatar = null;
                String avatarUrlString = "";

                try {
                    userObj = obj.getJSONObject("user");
                    Log.e("com.example.easycourse", "" + userObj);
                    avatarUrlString = userObj.getString("avatarUrl");
                    URL avatarUrl = new URL(avatarUrlString);
                    HttpURLConnection conn = (HttpURLConnection) avatarUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    conn.setUseCaches(false);

                    InputStream is = conn.getInputStream();
                    avatar = IOUtils.toByteArray(conn.getInputStream());

                } catch (MalformedURLException e) {
                    Log.e("com.example.easycourse", e.toString());
                } catch (JSONException e) {
                    Log.e("com.example.easycourse", e.toString());
                } catch (IOException e) {
                    Log.e("com.example.easycourse", e.toString());
                }

                User user = null;
                try {
                    user = new User(userObj.getString("_id"), userObj.getString("displayName"), avatar, avatarUrlString, userObj.getString("email"), userObj.getString("university"));
                } catch (JSONException e) {
                    Log.e("com.example.easycourse", e.toString());
                }

                Realm.init(context);
                Realm realm = Realm.getDefaultInstance();

                Log.e("com.example.easycourse", "user in realm? " + User.isUserInRealm(user, realm));
                User.updateUserToRealm(user, realm);
                Log.e("com.example.easycourse", "user in realm? " + User.isUserInRealm(user, realm));

            }
        });
    }

    public void getHistMessage() throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("updatedTime", System.currentTimeMillis() / 1000);
        socket.emit("getHistMessage", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    JSONArray msgArray = obj.getJSONArray("msg");
                    for(int i = 0; i < msgArray.length(); i++){
                        saveMessageToRealm(msgArray.getJSONObject(i));
                    }
                } catch (JSONException e) {
                    Log.e("com.example.easycourse", e.toString());
                }
            }
        });
    }

    private void saveMessageToRealm(JSONObject obj){
        if (obj != null) {
            Message message = null;
            try {
                String id = (String) checkIfJsonExists(obj, "id", null);
                String remoteId = (String) checkIfJsonExists(obj, "id", null);
                String senderId = (String) checkIfJsonExists(obj, "sender", null);
                String text = (String) checkIfJsonExists(obj, "text", null);
                String imageUrl = (String) checkIfJsonExists(obj, "imageUrl", null);
                byte[] imageData = (byte[]) checkIfJsonExists(obj, "imageData", null);
                boolean successSent = (boolean) checkIfJsonExists(obj, "successSent", false);
                String toRoom = (String) checkIfJsonExists(obj, "toRoom", null);
                double imageWidth = Double.parseDouble((String) checkIfJsonExists(obj, "imageWidth", 0.0));
                double imageHeight = Double.parseDouble((String) checkIfJsonExists(obj, "imageHeight", 0.0));
                Date date = (Date) checkIfJsonExists(obj, "date", null);

                Realm.init(context);
                Realm realm = Realm.getDefaultInstance();

                message = new Message(id, remoteId, senderId, text, imageUrl, imageData, successSent, imageWidth, imageHeight, toRoom, date);
                Log.e("com.example.easycourse", "message in realm? " + Message.isMessageInRealm(message, realm));
                Message.updateMessageToRealm(message, realm);
                Log.e("com.example.easycourse", "message in realm? " + Message.isMessageInRealm(message, realm));
            } catch (JSONException e) {
                Log.e("com.example.easycourse", e.toString());
            }
        }
    }

    private Object checkIfJsonExists(JSONObject obj, String searchQuery, Object defaultObj) throws JSONException {
        if (obj.has(searchQuery)) {
            if(obj.get(searchQuery) instanceof String || obj.get(searchQuery) instanceof Integer)
                return obj.getString(searchQuery);
            else
                return obj.get(searchQuery);
        }
        else
            return defaultObj;
    }
}
