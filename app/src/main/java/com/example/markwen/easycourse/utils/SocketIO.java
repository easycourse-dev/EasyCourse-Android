package com.example.markwen.easycourse.utils;

import android.content.Context;
import android.util.Log;

import com.example.markwen.easycourse.EasyCourse;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.Realm;
import io.realm.RealmList;
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
    public void sendMessage(String message, String roomId, String toUserId, byte[] imageData, float imageWidth, float imageHeight) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("id", UUID.randomUUID().toString());
        jsonParam.put("toRoom", roomId);
        jsonParam.put("toUser", toUserId);
        jsonParam.put("text", message);
//        jsonParam.put("imageUrl", imageUrl);
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
        if(displayName != null)
            jsonParam.put("displayName", displayName);
        if(avatarUrl != null)
            jsonParam.put("avatarUrl", avatarUrl);
        socket.emit("syncUser", jsonParam);
        syncUser();
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
                        userObj.remove("_id");

                        JSONArray silentRoomsJSON = userObj.getJSONArray("silentRoom");

                        Realm realm = Realm.getDefaultInstance();
                        User.updateUserFromJson(userObj.toString(), realm);
                        realm.beginTransaction();

                        User.getUserFromRealm(realm, id).setProfilePicture(avatar);
                        for (int i = 0; i < silentRoomsJSON.length(); i++) {
                            String roomID = silentRoomsJSON.getString(i);
                            Log.e(TAG, "silent room:"+roomID);
                            Room room = Room.getRoomById(realm, roomID);
                            User.getUserFromRealm(realm, id).getSilentRooms().add(room);
                        }
                        realm.commitTransaction();
                        realm.close();
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                    //User.updateUserToRealm(user, realm);

                    Log.e(TAG, "syncUser: "+obj.toString());

                }
            }
        });
    }

    //saves list of messages to realm
    private void getHistMessage() throws JSONException {
        JSONObject jsonParam = new JSONObject();
        //TODO: find time last on app
//        Realm realm = Realm.getDefaultInstance();
//        RealmResults<Message> list = realm.where(Message.class).findAllSorted("createdAt", Sort.DESCENDING);
//        if(list.size() < 1) return;
//        Message message = list.first();
//        long time = message.getCreatedAt().getTime();
//        jsonParam.put("lastUpdateTime", time);
        jsonParam.put("lastUpdateTime", 0);
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

    public void logout(Ack callback) {
        socket.emit("logout", null, callback);
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
    public void blockUser(String otherUserId, Ack callback)  throws JSONException{
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
        /*socket.emit("dropCourse", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (obj.has("error")) {
                    Log.e(TAG, obj.toString());
                } else {

                    try {
                        dropCourseSuccess[0] = obj.getBoolean("success");
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                    syncUser();
                }
            }
        });*/
    }

    public void getCourseInfo(String courseId, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("courseId", courseId);

        socket.emit("getCourseInfo", jsonParam, callback);
    }

//    public Future<ArrayList<Room>> searchRooms(String searchQuery, int limit, final int skip, String unversityId, final ArrayList<Room> rooms) throws JSONException {
//        final JSONObject jsonParam = new JSONObject();
//        jsonParam.put("text", searchQuery);
//        jsonParam.put("university", unversityId);
//        jsonParam.put("limit", limit);
//        jsonParam.put("skip", skip);
//        socket.emit("searchRoom", jsonParam, new Ack() {
//            @Override
//            public void call(Object... args) {
//                JSONObject obj = (JSONObject) args[0];
//                if (!obj.has("error")) {
//                    try {
//                        JSONArray roomArrayJSON = obj.getJSONArray("room");
//                        for (int i = 0; i < roomArrayJSON.length(); i++) {
//                            String id = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "_id", null);
//                            String roomName = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "name", null);
//                            String courseName = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "courseName", null);
//                            String courseID = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "course", null);
//                            String universityID = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "university", null);
//                            int memberCounts = Integer.parseInt((String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "memberCounts", "0"));
//                            String founderId = (String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "founderId", null);
//                            int language = Integer.parseInt((String) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "language", "0"));
//                            boolean isSystem = (boolean) checkIfJsonExists(roomArrayJSON.getJSONObject(i), "isSystem", true);
//
//                            Room room = new Room(id, roomName, new RealmList<Message>(), courseID, courseName, universityID, new RealmList<User>(), memberCounts, language, founderId, isSystem);
//                            Room room2 = new Room(id, roomName, new RealmList<Message>(), courseID, courseName, universityID, new RealmList<User>(), memberCounts, )
//                            rooms.add(room);
//                        }
//                    } catch (JSONException e) {
//                        Log.e(TAG, e.toString());
//                    }
//                }
//            }
//        });
//
//        // Returns a Future object to handle async
//        return new Future<ArrayList<Room>>() {
//            @Override
//            public boolean cancel(boolean b) {
//                return false;
//            }
//
//            @Override
//            public boolean isCancelled() {
//                return false;
//            }
//
//            @Override
//            public boolean isDone() {
//                return false;
//            }
//
//            @Override
//            public ArrayList<Room> get() throws InterruptedException, ExecutionException {
//                return rooms;
//            }
//
//            @Override
//            public ArrayList<Room> get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
//                return null;
//            }
//        };
//    }

    public void joinRoom(String roomID, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("roomId", roomID);

        socket.emit("joinRoom", jsonParam, callback);
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

                        room[0] = new Room(id, roomName, new RealmList<Message>(), courseID, courseName, universityID, new RealmList<User>(), memberCounts, memberCountsDesc, null, language, isPublic, isSystem);

                        Realm realm = Realm.getDefaultInstance();
                        Room.updateRoomToRealm(room[0], realm);
                        realm.close();

                        Log.e(TAG, "Success: "+obj.toString());
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONEx"+e.toString());
                    }

                } else {
                    Log.e(TAG, "Error"+obj.toString());
                }
            }
        });
    }

    public void getRoomMembers(String roomId, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("roomId", roomId);

        socket.emit("getRoomMembers", jsonParam, callback);
    }

    public void getUserInfo(String userID) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("userId", userID);

        socket.emit("getUserInfo", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {

                JSONObject obj = (JSONObject) args[0];
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

                }
            }
        });
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
                String id = (String) checkIfJsonExists(obj, "_id", null);
                String remoteId = (String) checkIfJsonExists(obj, "id", null);
                String senderId = (String) checkIfJsonExists(obj, "sender", null);
                String text = (String) checkIfJsonExists(obj, "text", null);
                String imageUrl = (String) checkIfJsonExists(obj, "imageUrl", null);
                byte[] imageData = (byte[]) checkIfJsonExists(obj, "imageData", null);
                boolean successSent = (boolean) checkIfJsonExists(obj, "successSent", false);
                String toRoom = (String) checkIfJsonExists(obj, "toRoom", null);
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

                message = new Message(id, remoteId, senderId, text, imageUrl, imageData, successSent, imageWidth, imageHeight, toRoom, date);
                Message.updateMessageToRealm(message, realm);
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
}