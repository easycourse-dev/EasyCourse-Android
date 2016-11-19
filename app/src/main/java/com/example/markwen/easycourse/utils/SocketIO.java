package com.example.markwen.easycourse.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
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
import io.realm.RealmList;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by nisarg on 9/11/16.
 */


public class SocketIO {
    private String CHAT_SERVER_URL = "https://zengjintaotest.com";
    private static final String TAG = "SocketIO";


    private Context context;
    private Socket socket;

    public SocketIO(Context context) {
        this.context = context;

        IO.Options opts = new IO.Options();
        //opts.forceNew = true;
        opts.query = "token=" + APIFunctions.getUserToken(context);
        try {
            socket = IO.socket(CHAT_SERVER_URL, opts);
        } catch (URISyntaxException e) {
            Log.e(TAG, "not connecting " + e.toString());
        }
        socket.connect();
        this.publicListener();
    }

    public void publicListener() {
        socket.connect();
        socket.on("connect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
//                syncUser();
                Log.e(TAG, "connected");
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
                    if (obj.has("error")) {
                        Log.e(TAG, obj.toString());
                    } else {
                        JSONObject msgObj = obj.getJSONObject("msg");
                        saveMessageToRealm(msgObj);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
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
                    Log.e(TAG, e.toString());
                }

                JSONObject obj = (JSONObject) args[0];
                if (obj.has("error")) {
                    Log.e(TAG, obj.toString());
                } else {

                    JSONObject userObj = null;
                    byte[] avatar = null;
                    String avatarUrlString = "";

                    try {
                        userObj = obj.getJSONObject("user");
                        Log.e(TAG, "" + userObj);
                        avatarUrlString = userObj.getString("avatarUrl");
                        URL avatarUrl = new URL(avatarUrlString);
                        HttpURLConnection conn = (HttpURLConnection) avatarUrl.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        conn.setUseCaches(false);

                        InputStream is = conn.getInputStream();
                        avatar = IOUtils.toByteArray(conn.getInputStream());

                    } catch (MalformedURLException e) {
                        Log.e(TAG, e.toString());
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                    User user = null;

                    try {
                        user = new User(userObj.getString("_id"), userObj.getString("displayName"), avatar, avatarUrlString, userObj.getString("email"), userObj.getString("university"));
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }

                    Realm realm = Realm.getDefaultInstance();

                    Log.e(TAG, "user in realm? " + User.isUserInRealm(user, realm));
                    User.updateUserToRealm(user, realm);
                    Log.e(TAG, "user in realm? " + User.isUserInRealm(user, realm));
                }
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
                    if (obj.has("error")) {
                        Log.e(TAG, obj.toString());
                    } else {
                        JSONArray msgArray = obj.getJSONArray("msg");
                        for (int i = 0; i < msgArray.length(); i++) {
                            saveMessageToRealm(msgArray.getJSONObject(i));
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    public boolean logout() {
        final boolean[] logoutSuccess = {false};
        socket.emit("logout", null, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (obj.has("success")) {
                    logoutSuccess[0] = true;
                }
            }
        });
        return logoutSuccess[0];
    }

    public Course[] searchCourses(String searchQuery, int limit, int skip, String unversityId) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("text", searchQuery);
        jsonParam.put("university", unversityId);
        jsonParam.put("limit", limit);
        jsonParam.put("skip", skip);

        final Course[][] courses = {null};

        socket.emit("searchCourse", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (!obj.has("error")) {
                    try {
                        JSONArray courseArrayJSON = obj.getJSONArray("course");
                        courses[0] = new Course[courseArrayJSON.length()];
                        for (int i = 0; i < courseArrayJSON.length(); i++) {
                            String id = (String) checkIfJsonExists(courseArrayJSON.getJSONObject(i), "id", null);
                            String courseName = (String) checkIfJsonExists(courseArrayJSON.getJSONObject(i), "name", null);
                            byte[] coursePicture = (byte[]) checkIfJsonExists(courseArrayJSON.getJSONObject(i), "coursePicture", null);
                            String coursePictureUrl = (String) checkIfJsonExists(courseArrayJSON.getJSONObject(i), "coursePictureUrl", null);
                            String title = (String) checkIfJsonExists(courseArrayJSON.getJSONObject(i), "title", null);
                            String courseDescription = (String) checkIfJsonExists(courseArrayJSON.getJSONObject(i), "description", null);
                            int creditHours = Integer.parseInt((String) checkIfJsonExists(courseArrayJSON.getJSONObject(i), "creditHours", 0));
                            String universityID = (String) checkIfJsonExists(courseArrayJSON.getJSONObject(i), "university", null);
                            Course course = new Course(id, courseName, coursePicture, coursePictureUrl, title, courseDescription, creditHours, universityID);
                            courses[0][i] = course;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }

                    Log.e(TAG, obj.toString());
                }
            }
        });

        return courses[0];
    }

    public boolean dropCourse(String courseID) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("courseId", courseID);

        final boolean[] dropCourseSuccess = {false};

        socket.emit("dropCourse", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (obj.has("error")) {
                    Log.e("com.example.easycourse", obj.toString());
                } else {
                    Log.e("com.example.easycourse", "dropCourse " + obj.toString());

                    try {
                        dropCourseSuccess[0] = obj.getBoolean("success");
                    } catch (JSONException e) {
                        Log.e("com.example.easycourse", e.toString());
                    }
                    syncUser();
                }
            }
        });

        return dropCourseSuccess[0];
    }

    public Room[] searchRooms(String searchQuery, int limit, int skip, String unversityId) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("text", searchQuery);
        jsonParam.put("university", unversityId);
        jsonParam.put("limit", limit);
        jsonParam.put("skip", skip);

        final Room[][] rooms = {null};

        socket.emit("searchRoom", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (!obj.has("error")) {
                    try {
                        JSONArray roomArrayJSON = obj.getJSONArray("room");
                        rooms[0] = new Room[roomArrayJSON.length()];
                        for (int i = 0; i < roomArrayJSON.length(); i++) {
                            String id = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "_id", null);
                            String roomname = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "name", null);
                            String courseName = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "courseName", null);
                            String courseID = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "course", null);
                            String universityID = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "university", null);
                            int memberCounts = Integer.parseInt((String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "memberCounts", 0));
                            String founderId = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "founderId", null);
                            int language = Integer.parseInt((String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "language", 0));
                            boolean isSystem = (boolean) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "isSystem", true);

                            RealmList<Message> messageList = new RealmList<Message>();
                            RealmList<User> memberList = new RealmList<User>();

                            Room room = new Room(id, roomname, messageList, courseID, courseName, universityID, memberList, memberCounts, language, founderId, isSystem);
                            rooms[0][i] = room;
                        }
                    } catch (JSONException e) {
                        Log.e("com.example.easycourse", e.toString());
                    }

                    Log.e("com.example.easycourse", obj.toString());
                }
            }
        });

        return rooms[0];
    }

    public Room joinRoom(String roomID) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("roomId", roomID);

        final Room[] room = {null};

        socket.emit("joinRoom", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (!obj.has("error")) {
                    try {
                        JSONObject roomObjJSON = obj.getJSONObject("room");

                        String id = (String) checkIfJsonExists(roomObjJSON, "_id", null);
                        String roomname = (String) checkIfJsonExists(roomObjJSON, "name", null);
                        String courseName = (String) checkIfJsonExists(roomObjJSON, "courseName", null);
                        String courseID = (String) checkIfJsonExists(roomObjJSON, "course", null);
                        String universityID = (String) checkIfJsonExists(roomObjJSON, "university", null);
                        int memberCounts = Integer.parseInt((String) checkIfJsonExists(roomObjJSON, "memberCounts", 0));
                        String founderId = (String) checkIfJsonExists(roomObjJSON, "founderId", null);
                        int language = Integer.parseInt((String) checkIfJsonExists(roomObjJSON, "language", 0));
                        boolean isSystem = (boolean) checkIfJsonExists(roomObjJSON, "isSystem", true);

                        RealmList<Message> messageList = new RealmList<Message>();
                        RealmList<User> memberList = new RealmList<User>();

                        room[0] = new Room(id, roomname, messageList, courseID, courseName, universityID, memberList, memberCounts, language, founderId, isSystem);
                    } catch (JSONException e) {
                        Log.e("com.example.easycourse", e.toString());
                    }

                    Log.e("com.example.easycourse", obj.toString());
                } else {
                    Log.e("com.example.easycourse", obj.toString());
                }
            }
        });

        return room[0];
    }

    public boolean quitRoom(String roomID) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("roomId", roomID);

        final boolean[] dropRoomSuccess = {false};

        socket.emit("quitRoom", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (obj.has("error")) {
                    Log.e("com.example.easycourse", obj.toString());
                } else {
                    Log.e("com.example.easycourse", "quitRoom " + obj.toString());

                    try {
                        dropRoomSuccess[0] = obj.getBoolean("success");
                    } catch (JSONException e) {
                        Log.e("com.example.easycourse", e.toString());
                    }
                    syncUser();
                }
            }
        });

        return dropRoomSuccess[0];
    }

    public Room createRoom(String name, String courseID) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("name", name);
        jsonParam.put("course", courseID);


        final Room[] room = {null};

        socket.emit("createRoom", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (!obj.has("error")) {
                    try {
                        JSONObject roomObjJSON = obj.getJSONObject("room");

                        String id = (String) checkIfJsonExists(roomObjJSON, "_id", null);
                        String roomname = (String) checkIfJsonExists(roomObjJSON, "name", null);
                        String courseName = (String) checkIfJsonExists(roomObjJSON, "courseName", null);
                        String courseID = (String) checkIfJsonExists(roomObjJSON, "course", null);
                        String universityID = (String) checkIfJsonExists(roomObjJSON, "university", null);
                        int memberCounts = Integer.parseInt((String) checkIfJsonExists(roomObjJSON, "memberCounts", 0));
                        String founderId = (String) checkIfJsonExists(roomObjJSON, "founderId", null);
                        int language = Integer.parseInt((String) checkIfJsonExists(roomObjJSON, "language", 0));
                        boolean isSystem = (boolean) checkIfJsonExists(roomObjJSON, "isSystem", true);

                        RealmList<Message> messageList = new RealmList<Message>();
                        RealmList<User> memberList = new RealmList<User>();

                        room[0] = new Room(id, roomname, messageList, courseID, courseName, universityID, memberList, memberCounts, language, founderId, isSystem);
                    } catch (JSONException e) {
                        Log.e("com.example.easycourse", e.toString());
                    }

                    Log.e("com.example.easycourse", obj.toString());
                } else {
                    Log.e("com.example.easycourse", obj.toString());
                }
            }
        });

        return room[0];
    }

    private void saveMessageToRealm(JSONObject obj) {
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
                Log.e(TAG, "message in realm? " + Message.isMessageInRealm(message, realm));
                Message.updateMessageToRealm(message, realm);
                Log.e(TAG, "message in realm? " + Message.isMessageInRealm(message, realm));
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private Object checkIfJsonExists(JSONObject obj, String searchQuery, Object defaultObj) throws JSONException {
        if (obj.has(searchQuery)) {
            if (obj.get(searchQuery) instanceof String || obj.get(searchQuery) instanceof Integer)
                return obj.getString(searchQuery);
            else
                return obj.get(searchQuery);
        } else
            return defaultObj;
    }
}