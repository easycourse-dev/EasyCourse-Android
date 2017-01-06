package com.example.markwen.easycourse.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.activities.ChatRoomActivity;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.University;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.eventbus.Event;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.example.markwen.easycourse.utils.JSONUtils.checkIfJsonExists;

/**
 * Created by nisarg on 9/11/16.
 */


public class SocketIO {
    private static final String CHAT_SERVER_URL = "https://zengjintaotest.com";
    private static final String TAG = "SocketIO";


    private Context context;
    private Socket socket;
    private Realm realm;

    public SocketIO(Context context) throws URISyntaxException {
        this.context = context;
        this.realm = Realm.getDefaultInstance();

        IO.Options opts = new IO.Options();
        opts.query = "token=" + APIFunctions.getUserToken(context);
        socket = IO.socket(CHAT_SERVER_URL, opts);
        socket.connect();
        this.publicListener();
    }

    //public socket on listeners
    private void publicListener() {
        socket.connect();
        socket.on("connect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                syncUser();
                EasyCourse.bus.post(new Event.ConnectEvent());
                Log.d(TAG, "connected");
            }
        });

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                saveMessageToRealm(obj);
                //Bus event sent in saveMessageToRealm
                Log.d(TAG, "message");
            }
        });

        socket.on("disconnect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                EasyCourse.bus.post(new Event.DisconnectEvent());
                Log.e(TAG, "disconnected");
            }
        });

        socket.on("reconnect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                syncUser();
                EasyCourse.bus.post(new Event.ReconnectEvent());
                Log.e(TAG, "reconnected");
            }
        });

        socket.on("reconnectAttempt", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                EasyCourse.bus.post(new Event.ReconnectAttemptEvent());
                Log.e(TAG, "reconnectAttempt");
            }
        });
    }

    //sends a message to user/room
    public void sendMessage(String messageText, String toRoom, String toUserId, String sharedRoomId, byte[] imageData, double imageWidth, double imageHeight) throws JSONException {
        String uuid = UUID.randomUUID().toString();
        JSONObject jsonParam = new JSONObject();
        Message message;
        User curUser = User.getCurrentUser(context, Realm.getDefaultInstance());
        if (toUserId == null) { //Message to room
            message = new Message(uuid, null, curUser, messageText, null, imageData, false, imageWidth, imageHeight, toRoom, null, new Date());
        } else { //Message to user
            message = new Message(uuid, null, curUser, messageText, null, imageData, false, imageWidth, imageHeight, null, toUserId, new Date());
        }
        Message.updateMessageToRealm(message, Realm.getDefaultInstance());
        jsonParam.put("id", uuid);
        jsonParam.put("toRoom", toRoom);
        jsonParam.put("toUser", toUserId);
        jsonParam.put("text", messageText);
//        jsonParam.put("sharedRoom", sharedRoomId);
        jsonParam.put("imageData", imageData);
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

    //syncs realm database
    public void syncUser(String displayName, String avatarUrl) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        if (displayName != null)
            jsonParam.put("displayName", displayName);
        if (avatarUrl != null)
            jsonParam.put("avatarUrl", avatarUrl);
        socket.emit("syncUser", jsonParam);
        syncUser();
    }

    public synchronized void syncUser() {
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
                        if (userObj.has("avatarUrl")) {
                            avatarUrlString = userObj.getString("avatarUrl");
                            URL avatarUrl = new URL(avatarUrlString);
                            HttpURLConnection conn = (HttpURLConnection) avatarUrl.openConnection();
                            conn.setDoInput(true);
                            conn.connect();

                            avatar = IOUtils.toByteArray(conn.getInputStream());
                        }

                    } catch (JSONException | IOException e) {
                        Log.e(TAG, e.toString());
                    }

                    try {
                        String id = (String) checkIfJsonExists(userObj, "_id", null);
                        String university = (String) checkIfJsonExists(userObj, "university", null);

                        userObj.put("id", id);
                        userObj.put("profilePictureUrl", avatarUrlString);
                        userObj.put("universityID", university);
                        userObj.put("profilePicture", avatar);
                        userObj.remove("_id");

                        JSONArray silentRoomsJSON = userObj.getJSONArray("silentRoom"); // Array of room IDs
                        JSONArray joinedRoomsJSON = userObj.getJSONArray("joinedRoom"); // Array of objects
                        JSONArray joinedCoursesJSON = userObj.getJSONArray("joinedCourse"); // Array of objects

                        Realm realm = Realm.getDefaultInstance();
                        User.updateUserFromJson(userObj.toString(), realm);
                        Course.syncAddCourse(joinedCoursesJSON, realm);
                        Room.syncRooms(joinedRoomsJSON, realm);
                        Course.syncRemoveCourse(joinedCoursesJSON, realm);
                        realm.beginTransaction();

                        // Adding silent rooms
                        User.getUserFromRealm(realm, id).setProfilePicture(avatar);
                        for (int i = 0; i < silentRoomsJSON.length(); i++) {
                            String roomID = silentRoomsJSON.getString(i);
                            Log.e(TAG, "silent room:" + roomID);
                            Room room = Room.getRoomById(realm, roomID);
                            User.getUserFromRealm(realm, id).getSilentRooms().add(room);
                        }
                        realm.commitTransaction();
                        realm.close();
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                    //User.updateUserToRealm(user, realm);

                    Log.e(TAG, "syncUser: " + obj.toString());

                }
            }
        });
    }

    //saves list of messages to realm
    private void getHistMessage() throws JSONException {
        JSONObject jsonParam = new JSONObject();
        //TODO: find time last on app
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Message> list = realm.where(Message.class).findAllSorted("createdAt", Sort.DESCENDING);
        if (list.size() < 1) return;
        Message message = list.first();
        long time = message.getCreatedAt().getTime();
        jsonParam.put("lastUpdateTime", time);
//        jsonParam.put("lastUpdateTime", 0);
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

    public void getAllMessage() throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("lastUpdateTime", 1);
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

    public void logout(Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("deviceToken", EasyCourse.getAppInstance().getDeviceToken());

        socket.emit("logout", jsonParam, callback);
    }

    public void searchCourses(String searchQuery, int limit, int skip, String universityId, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("text", searchQuery);
        jsonParam.put("university", universityId);
        jsonParam.put("limit", limit);
        jsonParam.put("skip", skip);

        socket.emit("searchCourse", jsonParam, callback);
    }

    //Block user
    public void blockUser(String otherUserId, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("otherUser", otherUserId);
        socket.emit("blockUser", jsonParam, callback);
    }

    //Search subrooms within a course
    public void searchCourseSubrooms(String searchQuery, int limit, int skip, String courseID, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("text", searchQuery);
        jsonParam.put("courseId", courseID);
        jsonParam.put("limit", limit);
        jsonParam.put("skip", skip);

        socket.emit("searchCourseSubrooms", jsonParam, callback);
    }

    //Join courses with language keys
    public void joinCourse(ArrayList<String> courses, ArrayList<String> languageKeys, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("courses", new JSONArray(courses));
        jsonParam.put("lang", new JSONArray(languageKeys));

        socket.emit("joinCourse", jsonParam, callback);
    }

    public void joinCourse(String courseId, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        ArrayList<String> courses = new ArrayList<>();
        courses.add(courseId);
        jsonParam.put("courses", new JSONArray(courses));
        jsonParam.put("lang", new JSONArray());

        socket.emit("joinCourse", jsonParam, callback);
    }

    //convert and save JSON message object to realm
    public void dropCourse(String courseID, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("courseId", courseID);

        socket.emit("dropCourse", jsonParam, callback);
    }

    public void getCourseInfo(String courseId, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("courseId", courseId);

        socket.emit("getCourseInfo", jsonParam, callback);
    }

    public void joinRoom(String roomID, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("roomId", roomID);

        socket.emit("joinRoom", jsonParam, callback);
    }

    public void silentRoom(String roomID, boolean silent, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("roomId", roomID);
        jsonParam.put("silent", silent);
        socket.emit("silentRoom", jsonParam, callback);
    }

    public void quitRoom(String roomID, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("roomId", roomID);

        socket.emit("quitRoom", jsonParam, callback);
    }

    public void createRoom(String name, String courseID, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("name", name);
        jsonParam.put("course", courseID);

        socket.emit("createRoom", jsonParam, callback);
    }

    public void getRoomInfo(final String roomID) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("roomId", roomID);


        final Room[] room = {null};

        socket.emit("getRoomInfo", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (!obj.has("error")) {
                    try {
                        JSONObject temp = obj.getJSONObject("room");

                        String id = (String) checkIfJsonExists(temp, "_id", null);
                        String roomName = (String) checkIfJsonExists(temp, "name", null);
                        String courseID = (String) checkIfJsonExists(temp, "course", null);
                        String courseName = Course.getCourseById(courseID, realm).getCoursename();
                        String universityID = (String) checkIfJsonExists(temp, "university", null);
                        boolean isPublic = (boolean) checkIfJsonExists(temp, "isPublic", true);
                        int memberCounts = Integer.parseInt((String) checkIfJsonExists(temp, "memberCounts", "1"));
                        String memberCountsDesc = (String) checkIfJsonExists(temp, "memberCountsDescription", null);
                        String language = (String) checkIfJsonExists(temp, "language", "0");
                        boolean isSystem = (boolean) checkIfJsonExists(temp, "isSystem", true);

                        room[0] = new Room(id, roomName, new RealmList<Message>(), courseID, courseName, universityID, new RealmList<User>(), memberCounts, memberCountsDesc, new User(), language, isPublic, isSystem);

                        Realm realm = Realm.getDefaultInstance();
                        Room.updateRoomToRealm(room[0], realm);
                        realm.close();

                        Log.e(TAG, "Success: " + obj.toString());
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONEx" + e.toString());
                    }

                } else {
                    Log.e(TAG, "Error" + obj.toString());
                }
            }
        });
    }

    public void getRoomMembers(String roomId, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("roomId", roomId);

        socket.emit("getRoomMembers", jsonParam, callback);
    }

    public void getUserInfoJson(String userId, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("userId", userId);
        socket.emit("getUserInfo", jsonParam, callback);
    }

    public void getUserInfo(String userID) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("userId", userID);

        socket.emit("getUserInfo", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                parseUserJsonInfo((JSONObject) args[0]);
            }
        });
    }

    public User parseUserJsonInfo(JSONObject obj) {
        if (obj.has("error")) {
            Log.e(TAG, obj.toString());
        } else {
            Log.e(TAG, "getUserInfo" + obj.toString());
            JSONObject userObj = null;
            byte[] avatar = null;
            String avatarUrlString = "";
            String emailString = "";
            String universityId = "";

            try {
                userObj = obj.getJSONObject("user");
                if (userObj.has("avatarUrl")) {
                    avatarUrlString = userObj.getString("avatarUrl");
                    URL avatarUrl = new URL(avatarUrlString);
                    HttpURLConnection conn = (HttpURLConnection) avatarUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    //conn.setUseCaches(false);
                    avatar = IOUtils.toByteArray(conn.getInputStream());
                }
                if (userObj.has("email")) {
                    emailString = userObj.getString("email");
                } else {
                    emailString = null;
                }
                if (userObj.has("university")) {
                    universityId = userObj.getString("university");
                } else {
                    universityId = null;
                }
            } catch (JSONException | IOException e) {
                Log.e(TAG, e.toString());
            } catch (NullPointerException e) {
                Log.e(TAG, "no avatarUrl", e);
            }

            User user = null;
            try {
                user = new User(
                        userObj.getString("_id"),
                        userObj.getString("displayName"),
                        avatar,
                        avatarUrlString,
                        emailString,
                        universityId);
            } catch (JSONException | NullPointerException e) {
                Log.e(TAG, e.toString());
            }

            Realm.init(context);
            Realm realm = Realm.getDefaultInstance();
            User.updateUserToRealm(user, realm);
            realm.close();
            try {
                getUniversityInfo(universityId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return user;
        }
        return null;
    }

    public void getUniversityInfo(String univId, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("univId", univId);

        socket.emit("getUniversityInfo", jsonParam, callback);
    }

    public void getUniversityInfo(String univId) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("univId", univId);

        socket.emit("getUniversityInfo", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                try {
                    JSONObject temp = obj.getJSONObject("univ");
                    String id = (String) checkIfJsonExists(temp, "_id", null);
                    String name = (String) checkIfJsonExists(temp, "name", null);

                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(new University(id, name));
                    realm.commitTransaction();
                    realm.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveMessageToRealm(JSONObject obj) {
        if (obj != null) {
            Message message;
            try {
                JSONObject sender = obj.getJSONObject("sender");
                String senderId = (String) checkIfJsonExists(sender, "_id", null);
                String senderName = (String) checkIfJsonExists(sender, "displayName", null);
                String senderImageUrl = (String) checkIfJsonExists(sender, "avatarUrl", null);

                String id = (String) checkIfJsonExists(obj, "_id", null);
                String remoteId = (String) checkIfJsonExists(obj, "id", null);
                String text = (String) checkIfJsonExists(obj, "text", null);
                String imageUrl = (String) checkIfJsonExists(obj, "imageUrl", null);
                byte[] imageData = (byte[]) checkIfJsonExists(obj, "imageData", null);
                boolean successSent = (boolean) checkIfJsonExists(obj, "successSent", false);
                String toRoom = (String) checkIfJsonExists(obj, "toRoom", null);
                String toUser = (String) checkIfJsonExists(obj, "toUser", null);
                double imageWidth = Double.parseDouble((String) checkIfJsonExists(obj, "imageWidth", "0.0"));
                double imageHeight = Double.parseDouble((String) checkIfJsonExists(obj, "imageHeight", "0.0"));
                String dateString = (String) checkIfJsonExists(obj, "createdAt", null);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

                Date date = null;
                try {
                    date = formatter.parse(dateString);

                } catch (ParseException e) {
                    Log.e(TAG, "saveMessageToRealm: parseException", e);
                }

                Realm realm = Realm.getDefaultInstance();
                message = new Message(id, remoteId, new User(senderId, senderName, senderImageUrl), text, imageUrl, imageData, true, imageWidth, imageHeight, toRoom, toUser, date);

                User senderUser = new User(senderId, senderName, senderImageUrl);
                User.updateUserToRealm(senderUser, realm);

                boolean roomExists = false;

                if (message.getToRoom() == null) { //If message is to user
                    RealmResults<Room> rooms = realm.where(Room.class).equalTo("isToUser", true).findAll(); //Find private rooms
                    for(Room room : rooms) { //For each room
                        if(room.getMemberList().contains(senderUser)) { //If the room has the sender
                            realm.beginTransaction();
                            room.getMessageList().add(message); //Add the message to that room
                            realm.copyToRealmOrUpdate(room);
                            realm.commitTransaction();
                            roomExists = true;
                        }
                    }

                    if(!roomExists) { //If the room does not exist
                        createPrivateRoom(senderUser, message); //Create a private room and add the message
                    }

                } else { //Else the message is to a room
                    message.setToUser(false);
                    Message.updateMessageToRealm(message, realm);
                }

                EasyCourse.bus.post(new Event.MessageEvent(message));
                realm.close();
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    //check if JSON value exists, returns default if not
    public Object checkIfJsonExists(JSONObject obj, String searchQuery, Object defaultObj) throws JSONException {
        if (obj.has(searchQuery)) {
            if (obj.get(searchQuery) instanceof String || obj.get(searchQuery) instanceof Integer)
                return obj.getString(searchQuery);
            else
                return obj.get(searchQuery);
        } else
            return defaultObj;
    }

    public void createPrivateRoom(final User toUser, final Message message) {
        try {
            this.createRoom(toUser.getUsername(), null, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    if (!obj.has("error")) {
                        try {
                            JSONObject temp = obj.getJSONObject("room");

                            String id = (String) checkIfJsonExists(temp, "_id", null);
                            String roomName = (String) checkIfJsonExists(temp, "name", null);
                            String courseID = (String) checkIfJsonExists(temp, "course", null);
                            String universityID = (String) checkIfJsonExists(temp, "university", null);
                            boolean isPublic = (boolean) checkIfJsonExists(temp, "isPublic", true);
                            int memberCounts = Integer.parseInt((String) checkIfJsonExists(temp, "memberCounts", "1"));
                            String memberCountsDesc = (String) checkIfJsonExists(temp, "memberCountsDescription", null);
                            String language = (String) checkIfJsonExists(temp, "language", "0");
                            boolean isSystem = (boolean) checkIfJsonExists(temp, "isSystem", true);

                            Realm realm = Realm.getDefaultInstance();
                            User curUser = User.getCurrentUser(context, realm);
                            Room room = new Room(
                                    id,
                                    roomName,
                                    new RealmList<>(message),
                                    courseID,
                                    "Private Chat",
                                    universityID,
                                    new RealmList<>(User.getCurrentUser(context, realm), toUser),
                                    memberCounts,
                                    memberCountsDesc,
                                    curUser,
                                    language,
                                    isPublic,
                                    isSystem);
                            room.setToUser(true);
                            Room.updateRoomToRealm(room, realm);
                            realm.close();

                        } catch (JSONException e) {
                            Log.e(TAG, "call: ", e);
                        }
                    } else {
                        Log.e(TAG, "call: ");
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "goToPrivateRoom: ", e);
        }
    }

}