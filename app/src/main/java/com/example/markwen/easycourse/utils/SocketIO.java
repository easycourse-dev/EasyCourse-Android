package com.example.markwen.easycourse.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.activities.ChatRoomActivity;
import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Language;
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

    public static final int TEXT_TO_ROOM = 0;
    public static final int TEXT_TO_USER = 1;
    public static final int ROOM_TO_ROOM = 2;
    public static final int ROOM_TO_USER = 3;
    public static final int PIC_TO_ROOM = 4;
    public static final int PIC_TO_USER = 5;


    private Context context;
    private Socket socket;
    private Realm realm;
    private User currentUser;

    public SocketIO(Context context) throws URISyntaxException {
        this.context = context;
        this.realm = Realm.getDefaultInstance();

        currentUser = User.getCurrentUser(context, realm);

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

    //Message type 0:text toRoom, 1:text toUser, 2:shared room toRoom 3:shared room toUser 4:picture
    public void sendMessage(Message message, int messageType, Ack ack){
        if (message == null) return;
        JSONObject jsonParam = null;
        try {
            jsonParam = new JSONObject();
            jsonParam.put("id", message.getId());

            switch (messageType) {
                case TEXT_TO_ROOM:
                    jsonParam.put("toRoom", message.getToRoom());
                    jsonParam.put("text", message.getText());
                    break;

                case TEXT_TO_USER:
                    jsonParam.put("toUser", message.getToRoom());
                    jsonParam.put("text", message.getText());
                    break;

                case ROOM_TO_ROOM:
                    jsonParam.put("toRoom", message.getToRoom());
                    jsonParam.put("sharedRoom", message.getToRoom());
                    break;

                case ROOM_TO_USER:
                    jsonParam.put("toUser", message.getToRoom());
                    jsonParam.put("sharedRoom", message.getToRoom());
                    break;

                case PIC_TO_ROOM:
                    jsonParam.put("toRoom", message.getToRoom());
                    jsonParam.put("imageData", message.getImageData());
                    jsonParam.put("imageWidth", message.getImageWidth());
                    jsonParam.put("imageHeight", message.getImageHeight());
                    break;

                case PIC_TO_USER:
                    jsonParam.put("toUser", message.getToRoom());
                    jsonParam.put("imageData", message.getImageData());
                    jsonParam.put("imageWidth", message.getImageWidth());
                    jsonParam.put("imageHeight", message.getImageHeight());
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "sendMessage: ", e);
        }

        socket.emit("message", jsonParam, ack);
    }


//    //sends a message to user/room
//    public void sendMessage(String messageText, String toRoom, String toUser, String sharedRoomId, byte[] imageData, double imageWidth, double imageHeight) throws JSONException {
//        String uuid = UUID.randomUUID().toString();
//        Message message;
//        User curUser = User.getCurrentUser(context, Realm.getDefaultInstance());
//        if (toUser == null) { //Message to room
//            message = new Message(uuid, null, curUser, messageText, null, imageData, false, imageWidth, imageHeight, toRoom, null, null, new Date());
//        } else { //Message to user
//            message = new Message(uuid, null, curUser, messageText, null, imageData, false, imageWidth, imageHeight, null, toUser, null, new Date());
//        }
//
//        JSONObject jsonParam = new JSONObject();
//        jsonParam.put("id", uuid);
//        jsonParam.put("toRoom", toRoom);
//        jsonParam.put("toUser", toUser);
//        jsonParam.put("sharedRoom", sharedRoomId);
//        jsonParam.put("text", message);
//        jsonParam.put("text", messageText);
//        jsonParam.put("sharedRoom", sharedRoomId);
//        jsonParam.put("imageData", imageData);
//        jsonParam.put("imageWidth", imageWidth);
//        jsonParam.put("imageHeight", imageHeight);
//
//        socket.emit("message", jsonParam, new Ack() {
//            @Override
//            public void call(Object... args) {
//                try {
//                    JSONObject obj = (JSONObject) args[0];
//                    if (obj.has("error")) {
//                        Log.e(TAG, obj.toString());
//                    } else {
//                        JSONObject msgObj = obj.getJSONObject("msg");
//                        saveMessageToRealm(msgObj);
//                    }
//                } catch (JSONException e) {
//                    Log.e(TAG, e.toString());
//                }
//            }
//        });
//    }

    //syncs realm database
    public void syncUser(String displayName, byte[] avatar, ArrayList<String> languages, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        if (displayName != null)
            jsonParam.put("displayName", displayName);
        if (avatar != null)
            jsonParam.put("avatarImage", avatar);
        jsonParam.put("userLang", new JSONArray(languages));
        socket.emit("syncUser", jsonParam, callback);
    }

    public synchronized void syncUser() {
        socket.emit("syncUser", 1, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];

                if (obj.has("error")) {
                    Log.e(TAG, "call: " + obj.toString());
                    return;
                }

                Realm realm = Realm.getDefaultInstance();

                try {
                    JSONObject userObj = (JSONObject) checkIfJsonExists(obj, "user", null);
                    if (userObj == null) return;


                    //Parse and create User
                    String userId = (String) checkIfJsonExists(userObj, "_id", null);
                    String userEmail = (String) checkIfJsonExists(userObj, "email", null);
                    String userDisplayName = (String) checkIfJsonExists(userObj, "displayName", null);
                    String userAvatarUrl = (String) checkIfJsonExists(userObj, "avatarUrl", null);
                    String userUniversity = (String) checkIfJsonExists(userObj, "university", null);


                    JSONArray userLangObj = (JSONArray) checkIfJsonExists(obj, "userLang", null);
                    //TODO: implement userLangs


                    JSONArray joinedCourseArray = (JSONArray) checkIfJsonExists(userObj, "joinedCourse", null);
                    RealmList<Course> userCourses = new RealmList<>();
                    for (int i = 0; i < joinedCourseArray.length(); i++) {
                        JSONObject courseObj = joinedCourseArray.getJSONObject(i);
                        String courseId = (String) checkIfJsonExists(courseObj, "_id", null);
                        if (courseId == null) continue;
                        String courseName = (String) checkIfJsonExists(courseObj, "name", null);
                        String couresTitle = (String) checkIfJsonExists(courseObj, "title", null);
                        String couresDescription = (String) checkIfJsonExists(courseObj, "description", null);
                        int couresCreditHours = Integer.parseInt((String) checkIfJsonExists(courseObj, "creditHours", 0));
                        String courseUniversity = (String) checkIfJsonExists(courseObj, "university", null);


                        realm.beginTransaction();
                        Course course = realm.where(Course.class).equalTo("id", courseId).findFirst();
                        if (course == null) {
                            course = realm.createObject(Course.class, courseId);
                        }

                        course.setCoursename(courseName);
                        course.setTitle(couresTitle);
                        course.setCourseDescription(couresDescription);
                        course.setCreditHours(couresCreditHours);
                        course.setUniversityID(courseUniversity);
                        realm.commitTransaction();
                        userCourses.add(course);
                    }

                    JSONArray joinedRoomArray = (JSONArray) checkIfJsonExists(userObj, "joinedRoom", null);
                    RealmList<Room> joinedRooms = new RealmList<>();
                    for (int i = 0; i < joinedRoomArray.length(); i++) {
                        JSONObject roomObj = joinedRoomArray.getJSONObject(i);
                        String roomId = (String) checkIfJsonExists(roomObj, "_id", null);
                        if (roomId == null) continue;
                        String roomUniversity = (String) checkIfJsonExists(roomObj, "university", null);
                        String roomFounder = (String) checkIfJsonExists(roomObj, "founder", null);
                        String roomName = (String) checkIfJsonExists(roomObj, "name", null);
                        boolean roomIsPublic = (boolean) checkIfJsonExists(roomObj, "isPublic", false);
                        int roomMemberCount = Integer.parseInt((String) checkIfJsonExists(roomObj, "memberCounts", null));
                        boolean roomIsSystem = (boolean) checkIfJsonExists(roomObj, "isSystem", false);
                        String roomMemberCountDescription = (String) checkIfJsonExists(roomObj, "memberCountsDescription", null);
                        String roomCourse = (String) checkIfJsonExists(roomObj, "course", null);


                        realm.beginTransaction();
                        Room room = realm.where(Room.class).equalTo("id", roomId).findFirst();
                        if (room == null) {
                            room = realm.createObject(Room.class, roomId);
                        }

                        room.setRoomName(roomName);
                        room.setCourseID(roomCourse);
                        room.setUniversity(roomUniversity);
                        room.setMemberCounts(roomMemberCount);
                        room.setMemberCountsDesc(roomMemberCountDescription);
                        room.setJoinIn(true);

                        User founderUser = realm.where(User.class).equalTo("id", roomFounder).findFirst();
                        if (founderUser == null) {
                            founderUser = realm.createObject(User.class);
                            founderUser.setId(roomFounder);
                        }

                        room.setFounder(founderUser);
                        room.setPublic(roomIsPublic);
                        room.setSystem(roomIsSystem);
                        realm.commitTransaction();
                        joinedRooms.add(room);
                    }

                    JSONArray silentRoomArray = (JSONArray) checkIfJsonExists(userObj, "silentRoom", null);
                    //TODO: implement silentrooms

                    JSONArray contactsArray = (JSONArray) checkIfJsonExists(userObj, "contacts", null);
                    RealmList<User> contacts = new RealmList<>();
                    for (int i = 0; i < contactsArray.length(); i++) {
                        JSONObject contactObj = contactsArray.getJSONObject(i);
                        String contactId = (String) checkIfJsonExists(contactObj, "_id", null);
                        if (contactId == null) continue;
                        String contactEmail = (String) checkIfJsonExists(contactObj, "email", null);
                        String contactName = (String) checkIfJsonExists(contactObj, "displayName", null);
                        String contactUniversity = (String) checkIfJsonExists(contactObj, "university", null);
                        String contactAvatar = (String) checkIfJsonExists(contactObj, "avatarUrl", null);

                        //TODO: contact joined courses
//                        JSONArray contactJoinedCourses = (JSONArray) checkIfJsonExists(contactObj, "joinedCourse", null);
//                        RealmList<Course> contactCourses = new RealmList<>();
//                        for (int j = 0; j < contactJoinedCourses.length(); j++) {
//                            String contactJoinedCourseId = contactJoinedCourses.getString(i);
//                            Course contactCourse = realm.where(Course.class).equalTo("id", contactJoinedCourseId).findFirst();
//                            if(contactCourse == null)
//                                contactCourse = new Course(contactJoinedCourseId, null, null);
//                            contactCourses.add(contactCourse);
//                        }
                        int contactStatus = Integer.parseInt((String) checkIfJsonExists(contactObj, "status", 0));

                        User contact = realm.where(User.class).equalTo("id", contactId).findFirst();
                        if (contact == null)
                            contact = new User(contactId, contactName, null, contactAvatar, contactEmail, contactUniversity, null, null, null, null, contactStatus);
                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(contact);
                        realm.commitTransaction();
                    }

                    realm.beginTransaction();
                    User currentUser = User.getCurrentUser(context, realm);
                    if (currentUser == null) return;
                    currentUser.setEmail(userEmail);
                    currentUser.setUsername(userDisplayName);
                    currentUser.setProfilePictureUrl(userAvatarUrl);
                    currentUser.setUniversityID(userUniversity);
                    currentUser.setJoinedCourses(userCourses);
                    currentUser.setJoinedRooms(joinedRooms);
                    realm.copyToRealmOrUpdate(currentUser);
                    realm.commitTransaction();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    //saves list of messages to realm
    public void getHistMessage() throws JSONException {
        JSONObject jsonParam = new JSONObject();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Message> list = realm.where(Message.class).findAllSorted("createdAt", Sort.DESCENDING);
        if (list.size() < 1) return;
        Message message = list.first();
        long time = message.getCreatedAt().getTime();
        jsonParam.put("lastUpdateTime", time);
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
//        jsonParam.put("lang", new JSONArray(languageKeys));

        socket.emit("joinCourse", jsonParam, callback);
    }

    public void joinCourse(String courseId, ArrayList<String> languageKeys, Ack callback) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        ArrayList<String> courses = new ArrayList<>();
        courses.add(courseId);
        jsonParam.put("courses", new JSONArray(courses));
        jsonParam.put("lang", new JSONArray(languageKeys));

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

                        Log.d(TAG, "Success: " + obj.toString());
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
        if (obj == null) return;
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
            Room sharedRoom = null;
            if (checkIfJsonExists(obj, "sharedRoom", null) != null) {
                JSONObject sharedRoomJSON = obj.getJSONObject("sharedRoom");
                Log.e(TAG, sharedRoomJSON.toString());
                sharedRoom = new Room(sharedRoomJSON.getString("id"), sharedRoomJSON.getString("name"), sharedRoomJSON.getString("course"), sharedRoomJSON.getString("memberCountsDescription"));
            }
            String dateString = (String) checkIfJsonExists(obj, "createdAt", null);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = null;
            try {
                date = formatter.parse(dateString);

            } catch (ParseException e) {
                Log.e(TAG, "saveMessageToRealm: parseException", e);
            }
            Realm realm = Realm.getDefaultInstance();
            User senderUser = new User(senderId, senderName, senderImageUrl);
            User.updateUserToRealm(senderUser, realm);

            //TODO: check for shared room
            message = new Message(id, remoteId, senderUser, text, imageUrl, imageData, sharedRoom, true, imageWidth, imageHeight, toRoom, false,date);
            if(toRoom == null)
                message.setToUser(true);

            if (sharedRoom != null)
                message.setSharedRoom(sharedRoom);


            //TODO: fix assumptions that room is already created
            if (toRoom == null) { //Is to user
                message.setToUser(true);
                Room currentRoom = Room.getRoomById(realm, toUser);
                if (currentRoom == null)
                    currentRoom = createPrivateRoom(senderId);
                currentRoom.addMessageToRoom(message, realm);

            } else { //Is to room
                message.setToUser(false);
                Room currentRoom = Room.getRoomById(realm, toRoom);
                if (currentRoom != null)
                    currentRoom.addMessageToRoom(message, realm);
//                    currentRoom = createPublicRoom(toUser);
            }
            realm.close();
        } catch (JSONException e) {
            Log.e(TAG, "saveMessageToRealm: ", e);
        }
    }

    public Room createPrivateRoom(String toUserId) {
        Realm tempRealm = Realm.getDefaultInstance();
        User toUser = tempRealm.where(User.class).equalTo("id", toUserId).findFirst();
        User currentUser = User.getCurrentUser(context, tempRealm);
        Room room = new Room(
                toUser.getId(),
                toUser.getUsername(),
                new RealmList<Message>(),
                null,
                "Private Chat",
                null,
                new RealmList<>(currentUser, toUser),
                2,
                "<10",
                toUser,
                null,
                false,
                false);
        room.setToUser(true);
        tempRealm.beginTransaction();
        tempRealm.copyToRealmOrUpdate(room);
        tempRealm.commitTransaction();
        tempRealm.close();
        return room;
    }

    //check if JSON value exists, returns default if not
    public static Object checkIfJsonExists(JSONObject obj, String searchQuery, Object
            defaultObj) throws JSONException {
        if (obj.has(searchQuery)) {
            if (obj.get(searchQuery) instanceof String || obj.get(searchQuery) instanceof Integer)
                return obj.getString(searchQuery);
            else
                return obj.get(searchQuery);
        } else
            return defaultObj;
    }
}