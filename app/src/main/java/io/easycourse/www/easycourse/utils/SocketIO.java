package io.easycourse.www.easycourse.utils;

import android.content.Context;
import android.util.Log;

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
import java.util.TimeZone;

import io.easycourse.www.easycourse.BuildConfig;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.models.main.Course;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.University;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.eventbus.Event;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by nisarg on 9/11/16.
 */


public class SocketIO {
    private static final String CHAT_SERVER_URL = BuildConfig.SERVER_URL;



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
                saveJsonMessageToRealm(obj, true);
                //Bus event sent in saveJsonMessageToRealm
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
    public void sendMessage(Message message, int messageType, Ack ack) {
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
                    jsonParam.put("sharedRoom", message.getSharedRoom().getId());
                    break;

                case ROOM_TO_USER:
                    jsonParam.put("toUser", message.getToRoom());
                    jsonParam.put("sharedRoom", message.getSharedRoom().getId()
                    );
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
        // Get all messages first
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("lastUpdateTime", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                            saveJsonMessageToRealm(msgArray.getJSONObject(i), false);
                        }
                    }

                    // Once finished, call syncUser to synchronize
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
                                JSONObject userObj = (JSONObject) JSONUtils.checkIfJsonExists(obj, "user", null);
                                if (userObj == null) return;


                                //Parse and create User
//                    String userId = (String) JSONUtils.checkIfJsonExists(userObj, "_id", null);
                                String userEmail = (String) JSONUtils.checkIfJsonExists(userObj, "email", null);
                                String userDisplayName = (String) JSONUtils.checkIfJsonExists(userObj, "displayName", null);
                                String userAvatarUrl = (String) JSONUtils.checkIfJsonExists(userObj, "avatarUrl", null);
                                String userUniversity = (String) JSONUtils.checkIfJsonExists(userObj, "university", null);

//                    JSONArray userLangObj = (JSONArray) JSONUtils.checkIfJsonExists(obj, "userLang", null);
                                //TODO: implement userLangs


                                JSONArray joinedCourseArray = (JSONArray) JSONUtils.checkIfJsonExists(userObj, "joinedCourse", null);
                                RealmList<Course> userCourses = new RealmList<>();
                                for (int i = 0; i < joinedCourseArray.length(); i++) {
                                    JSONObject courseObj = joinedCourseArray.getJSONObject(i);
                                    String courseId = (String) JSONUtils.checkIfJsonExists(courseObj, "_id", null);
                                    if (courseId == null) continue;
                                    String courseName = (String) JSONUtils.checkIfJsonExists(courseObj, "name", null);
                                    String couresTitle = (String) JSONUtils.checkIfJsonExists(courseObj, "title", null);
                                    String couresDescription = (String) JSONUtils.checkIfJsonExists(courseObj, "description", null);
                                    int couresCreditHours = Integer.parseInt((String) JSONUtils.checkIfJsonExists(courseObj, "creditHours", 0));
                                    String courseUniversity = (String) JSONUtils.checkIfJsonExists(courseObj, "university", null);


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

                                JSONArray joinedRoomArray = (JSONArray) JSONUtils.checkIfJsonExists(userObj, "joinedRoom", null);
                                RealmList<Room> joinedRooms = new RealmList<>();
                                if (joinedRoomArray != null) {
                                    for (int i = 0; i < joinedRoomArray.length(); i++) {
                                        JSONObject roomObj = joinedRoomArray.getJSONObject(i);
                                        String roomId = (String) JSONUtils.checkIfJsonExists(roomObj, "_id", null);
                                        if (roomId == null) continue;
                                        String roomUniversity = (String) JSONUtils.checkIfJsonExists(roomObj, "university", null);
                                        String roomFounder = (String) JSONUtils.checkIfJsonExists(roomObj, "founder", null);
                                        String roomName = (String) JSONUtils.checkIfJsonExists(roomObj, "name", null);
                                        boolean roomIsPublic = (boolean) JSONUtils.checkIfJsonExists(roomObj, "isPublic", false);
                                        int roomMemberCount = Integer.parseInt((String) JSONUtils.checkIfJsonExists(roomObj, "memberCounts", null));
                                        boolean roomIsSystem = (boolean) JSONUtils.checkIfJsonExists(roomObj, "isSystem", false);
                                        String roomMemberCountDescription = (String) JSONUtils.checkIfJsonExists(roomObj, "memberCountsDescription", null);
                                        String roomCourse = (String) JSONUtils.checkIfJsonExists(roomObj, "course", null);


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
                                            founderUser = realm.createObject(User.class, roomFounder);
                                            founderUser.setId(roomFounder);
                                        }

                                        room.setFounder(founderUser);
                                        room.setPublic(roomIsPublic);
                                        room.setSystem(roomIsSystem);
                                        realm.copyToRealmOrUpdate(founderUser);
                                        realm.copyToRealmOrUpdate(room);
                                        realm.commitTransaction();
                                        joinedRooms.add(room);
                                    }
                                }

                                JSONArray silentRoomArray = (JSONArray) JSONUtils.checkIfJsonExists(userObj, "silentRoom", null);
                                RealmList<Room> silentRooms = new RealmList<>();
                                if (silentRoomArray != null) {
                                    for (int i = 0; i < silentRoomArray.length(); i++) {
                                        String roomId = silentRoomArray.getString(i);
                                        realm.beginTransaction();
                                        Room room = realm.where(Room.class).equalTo("id", roomId).findFirst();
                                        if (room == null) {
                                            room = realm.createObject(Room.class, roomId);
                                        }
                                        room.setSilent(true);
                                        realm.copyToRealmOrUpdate(room);
                                        realm.commitTransaction();
                                        silentRooms.add(room);
                                    }
                                }

                                JSONArray contactsArray = (JSONArray) JSONUtils.checkIfJsonExists(userObj, "contacts", null);
                                RealmList<User> contacts = new RealmList<>();
                                if (contactsArray != null) {
                                    for (int i = 0; i < contactsArray.length(); i++) {
                                        JSONObject contactObj = contactsArray.getJSONObject(i);
                                        String contactId = (String) JSONUtils.checkIfJsonExists(contactObj, "_id", null);
                                        if (contactId == null) continue;
                                        String contactEmail = (String) JSONUtils.checkIfJsonExists(contactObj, "email", null);
                                        String contactName = (String) JSONUtils.checkIfJsonExists(contactObj, "displayName", null);
                                        String contactUniversity = (String) JSONUtils.checkIfJsonExists(contactObj, "university", null);
                                        String contactAvatar = (String) JSONUtils.checkIfJsonExists(contactObj, "avatarUrl", null);

//                            JSONArray contactJoinedCourses = (JSONArray) JSONUtils.checkIfJsonExists(contactObj, "joinedCourse", null);
                                        RealmList<Course> contactCourses = new RealmList<>();
//                            if (contactJoinedCourses != null) {
//                                for (int j = 0; j < contactJoinedCourses.length(); j++) {
//                                    String contactJoinedCourseId = contactJoinedCourses.getString(i);
//                                    realm.beginTransaction();
//                                    Course contactCourse = realm.where(Course.class).equalTo("id", contactJoinedCourseId).findFirst();
//                                    if (contactCourse == null)
//                                        contactCourse = realm.createObject(Course.class, contactJoinedCourseId);
//                                    realm.commitTransaction();
//                                    contactCourses.add(contactCourse);
//                                }
//                            }
                                        int contactStatus = Integer.parseInt((String) JSONUtils.checkIfJsonExists(contactObj, "status", 0));

                                        User contact = realm.where(User.class).equalTo("id", contactId).findFirst();
                                        if (contact == null)
                                            contact = new User(contactId, contactName, null, contactAvatar, contactEmail, contactUniversity, contactCourses, null, null, null, contactStatus);
                                        realm.beginTransaction();
                                        realm.copyToRealmOrUpdate(contact);
                                        realm.commitTransaction();
                                        createPrivateRoom(contactId);
                                    }
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
                                currentUser.setSilentRooms(silentRooms);
                                realm.copyToRealmOrUpdate(currentUser);
                                realm.commitTransaction();

                                EasyCourse.getAppInstance().setUniversityId(context, userUniversity);

                                EasyCourse.bus.post(new Event.SyncEvent());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
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
                            saveJsonMessageToRealm(msgArray.getJSONObject(i), false);
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
                            saveJsonMessageToRealm(msgArray.getJSONObject(i), false);
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


        socket.emit("getRoomInfo", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                if (!obj.has("error")) {
                    try {
                        JSONObject temp = obj.getJSONObject("room");

                        String id = (String) JSONUtils.checkIfJsonExists(temp, "_id", null);
                        String roomName = (String) JSONUtils.checkIfJsonExists(temp, "name", null);
                        String courseID = (String) JSONUtils.checkIfJsonExists(temp, "course", null);
                        String courseName = Course.getCourseById(courseID, realm).getCoursename();
                        String universityID = (String) JSONUtils.checkIfJsonExists(temp, "university", null);
                        boolean isPublic = (boolean) JSONUtils.checkIfJsonExists(temp, "isPublic", true);
                        int memberCounts = Integer.parseInt((String) JSONUtils.checkIfJsonExists(temp, "memberCounts", "1"));
                        String memberCountsDesc = (String) JSONUtils.checkIfJsonExists(temp, "memberCountsDescription", null);
                        String language = (String) JSONUtils.checkIfJsonExists(temp, "language", "0");
                        boolean isSystem = (boolean) JSONUtils.checkIfJsonExists(temp, "isSystem", true);

                        Room room = new Room(id, roomName, null, courseID, courseName, universityID, null, memberCounts, memberCountsDesc, new User(), language, isPublic, isSystem);
                        room.updateRoomToRealm();

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
                    String id = (String) JSONUtils.checkIfJsonExists(temp, "_id", null);
                    String name = (String) JSONUtils.checkIfJsonExists(temp, "name", null);

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

    public void removeFriend(final String friendId, Ack ack) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("otherUser", friendId);
        socket.emit("removeFriend", jsonParam, ack);
        socket.emit("removeFriend", jsonParam, new Ack() {
            @Override
            public void call(Object... args) {

            }
        });
    }

    private void saveJsonMessageToRealm(JSONObject obj, boolean unread) {
        if (obj == null) return;
        Message message;
        try {
            JSONObject sender = obj.getJSONObject("sender");
            String senderId = (String) JSONUtils.checkIfJsonExists(sender, "_id", null);
            String senderName = (String) JSONUtils.checkIfJsonExists(sender, "displayName", null);
            String senderImageUrl = (String) JSONUtils.checkIfJsonExists(sender, "avatarUrl", null);

            String id = (String) JSONUtils.checkIfJsonExists(obj, "_id", null);
            String remoteId = (String) JSONUtils.checkIfJsonExists(obj, "id", null);
            String text = (String) JSONUtils.checkIfJsonExists(obj, "text", null);
            String imageUrl = (String) JSONUtils.checkIfJsonExists(obj, "imageUrl", null);
            byte[] imageData = (byte[]) JSONUtils.checkIfJsonExists(obj, "imageData", null);
            String toRoom = (String) JSONUtils.checkIfJsonExists(obj, "toRoom", null);
            String toUser = (String) JSONUtils.checkIfJsonExists(obj, "toUser", null);
            double imageWidth = Double.parseDouble((String) JSONUtils.checkIfJsonExists(obj, "imageWidth", "0.0"));
            double imageHeight = Double.parseDouble((String) JSONUtils.checkIfJsonExists(obj, "imageHeight", "0.0"));
            Room sharedRoom = null;
            if (JSONUtils.checkIfJsonExists(obj, "sharedRoom", null) != null) {
                JSONObject sharedRoomJSON = obj.getJSONObject("sharedRoom");
                sharedRoom = createOrFindSharedRoom(sharedRoomJSON);
            }
            String dateString = (String) JSONUtils.checkIfJsonExists(obj, "createdAt", null);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT")); // Super weird...
            Date date = null;
            try {
                date = formatter.parse(dateString);
            } catch (ParseException e) {
                Log.e(TAG, "saveJsonMessageToRealm: parseException", e);
            }
            Realm realm = Realm.getDefaultInstance();

            User senderUser = new User(senderId, senderName, senderImageUrl);
            User.updateUserToRealm(senderUser, realm);

            if (toRoom != null) { //To room
                message = new Message(id, remoteId, senderUser, text, imageUrl, imageData, sharedRoom, true, imageWidth, imageHeight, toRoom, false, date);
                Room currentRoom = Room.getRoomById(realm, toRoom);
                if (currentRoom != null) {
                    realm.beginTransaction();
                    if (unread) {
                        currentRoom.incUnread(1);
                    }
                    realm.copyToRealmOrUpdate(currentRoom);
                    realm.commitTransaction();
                }
            } else { //To user
                message = new Message(id, remoteId, senderUser, text, imageUrl, imageData, sharedRoom, true, imageWidth, imageHeight, senderId, true, date);
                Room currentRoom = Room.getRoomById(realm, toUser);
                if (currentRoom == null)
                    currentRoom = createPrivateRoom(senderId);
                if (currentRoom != null) {
                    realm.beginTransaction();
                    currentRoom.setJoinIn(true);
                    if (unread) {
                        currentRoom.incUnread(1);
                    }
                    realm.copyToRealmOrUpdate(currentRoom);
                    realm.commitTransaction();
                }
            }

            realm.beginTransaction();
            realm.copyToRealmOrUpdate(message);
            realm.commitTransaction();
            realm.close();
            EasyCourse.bus.post(new Event.MessageEvent(message));
        } catch (JSONException e) {
            Log.e(TAG, "saveJsonMessageToRealm: ", e);
        }
    }

    private Room createPrivateRoom(String toUserId) {
        Realm tempRealm = Realm.getDefaultInstance();
        User toUser = tempRealm.where(User.class).equalTo("id", toUserId).findFirst();
        User currentUser = User.getCurrentUser(context, tempRealm);

        if (currentUser == null || toUserId.equals(currentUser.getId())) return null;

        Room room = new Room(
                toUser.getId(),
                toUser.getUsername(),
                new RealmList<Message>(),
                0,
                false,
                null,
                null,
                null,
                new RealmList<>(currentUser, toUser),
                2,
                "<10",
                null,
                currentUser,
                false,
                false,
                true,
                true);

        tempRealm.beginTransaction();
        Room tempRoom = tempRealm.copyToRealmOrUpdate(room);
        tempRealm.commitTransaction();
        tempRealm.close();
        return tempRoom;
    }

    private Room createOrFindSharedRoom(JSONObject sharedRoomObj) throws JSONException {
        Realm tempRealm = Realm.getDefaultInstance();
        String roomId = (String) JSONUtils.checkIfJsonExists(sharedRoomObj, "id", null);
        Room realmRoom = tempRealm.where(Room.class).equalTo("id", roomId).findFirst();
        if (realmRoom != null) return realmRoom;

        String name = (String) JSONUtils.checkIfJsonExists(sharedRoomObj, "name", null);
        String course = (String) JSONUtils.checkIfJsonExists(sharedRoomObj, "course", null);
        String memberCountsDescription = (String) JSONUtils.checkIfJsonExists(sharedRoomObj, "memberCountsDescription", null);

        Room room = new Room(
                roomId,
                name,
                course,
                memberCountsDescription
        );
        Room.updateRoomToRealm(room, tempRealm);
        return Room.getRoomById(tempRealm, roomId);
    }
}