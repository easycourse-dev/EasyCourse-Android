package io.easycourse.www.easycourse.models.main;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.easycourse.www.easycourse.utils.JSONUtils;
import io.easycourse.www.easycourse.utils.ListsUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

import static io.easycourse.www.easycourse.utils.ListsUtils.isRoomJoined;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class Room extends RealmObject {

    //When true, the room is one to one message
    private boolean isToUser = false;

    //When user quits this room on another platform, this room will not be deleted.
    //Instead, changing isJoinIn to false
    private boolean isJoinIn = false;

    //Basic info of room
    @PrimaryKey
    private String id;
    private String roomName;
    private RealmList<Message> messageList;
    private int unread = 0;
    private boolean silent = false;

    //Group chatting
    private String courseID;
    private String courseName;
    private String university;
    private RealmList<User> memberList;
    private int memberCounts;
    private String memberCountsDesc;
    private String language;

    //UserFragment built room
    private User founder;
    private boolean isPublic = false;

    //System
    private boolean isSystem;

    public Room() {
    }

    public Room(String id, String roomName, String courseName) {
        this.id = id;
        this.roomName = roomName;
        this.courseName = courseName;
        this.memberList = new RealmList<>();
        this.messageList = new RealmList<>();
    }

    public Room(String id, String roomName, String courseID, String memberCountsDesc) {
        this.id = id;
        this.roomName = roomName;
        this.courseID = courseID;
        this.memberCountsDesc = memberCountsDesc;
        this.memberList = new RealmList<>();
        this.messageList = new RealmList<>();
    }

    public Room(String id, String roomName, RealmList<Message> messageList, String courseID, String courseName, String university, RealmList<User> memberList, int memberCounts, String memberCountsDesc, User founder, String language, boolean isPublic, boolean isSystem) {
        this.id = id;
        this.roomName = roomName;
        this.messageList = messageList;
        this.courseID = courseID;
        this.courseName = courseName;
        this.university = university;
        this.memberList = memberList;
        this.memberCounts = memberCounts;
        this.memberCountsDesc = memberCountsDesc;
        this.founder = founder;
        this.language = language;
        this.isPublic = isPublic;
        this.isSystem = isSystem;
    }

    public Room(String id, String roomName, RealmList<Message> messageList, int unread, boolean silent, String courseID, String courseName, String university, RealmList<User> memberList, int memberCounts, String memberCountsDesc, String language, User founder, boolean isPublic, boolean isSystem, boolean isToUser, boolean isJoinIn) {
        this.id = id;
        this.roomName = roomName;
        this.messageList = messageList;
        this.unread = unread;
        this.silent = silent;
        this.courseID = courseID;
        this.courseName = courseName;
        this.university = university;
        this.memberList = memberList;
        this.memberCounts = memberCounts;
        this.memberCountsDesc = memberCountsDesc;
        this.language = language;
        this.founder = founder;
        this.isPublic = isPublic;
        this.isSystem = isSystem;
        this.isToUser = isToUser;
        this.isJoinIn = isJoinIn;
    }

    public static void updateRoomToRealm(Room room, Realm realm) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(room);
        realm.commitTransaction();
    }

    public void updateRoomToRealm(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(this);
        realm.commitTransaction();
        realm.close();
    }

    public static void syncRooms(JSONArray updatedRooms, Realm realm) {
        RealmResults<Room> roomsInRealm = realm.where(Room.class).findAll();

        addNewRooms(updatedRooms, roomsInRealm, realm);
        deleteOldRooms(updatedRooms, roomsInRealm, realm);
    }

    public static boolean isRoomInRealm(Room room, Realm realm) {
        RealmResults<Room> results = realm.where(Room.class)
                .equalTo("id", room.getId())
                .findAll();
        return results.size() != 0;
    }

    public static void deleteRoomFromRealm(final Room room, Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Room> results = realm.where(Room.class)
                        .equalTo("id", room.getId())
                        .findAll();
                results.deleteAllFromRealm();
            }
        });
    }

    public static ArrayList<Room> getRoomsFromRealm(Realm realm) {
        RealmResults<Room> results = realm.where(Room.class)
                .findAll();
        return new ArrayList<>(results);
    }

    @Nullable
    public static Room getRoomById(Realm realm, String id) {
        RealmResults<Room> results = realm.where(Room.class)
                .equalTo("id", id)
                .findAll();
        if (results.size() > 0)
            return results.first();
        return null;
    }

    //Only call if isToUser
    @Nullable
    public static User getOtherUserIfPrivate(final Room room, final User currentUser, Realm realm) {
        if (!room.isToUser()) return null;
        List<User> users = room.getMemberList();
        for (User user : users) {
            if (!user.equals(currentUser)) return user;
        }
        return null;
    }

    public void addMessageToRoom(Message message, Realm realm) {
        realm.beginTransaction();
        this.messageList.add(message);
        realm.copyToRealmOrUpdate(this);
        realm.commitTransaction();
    }

    public boolean isToUser() {
        return isToUser;
    }

    public void setToUser(boolean toUser) {
        isToUser = toUser;
    }

    public boolean isJoinIn() {
        return isJoinIn;
    }

    public void setJoinIn(boolean joinIn) {
        isJoinIn = joinIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(RealmList<Message> messageList) {
        this.messageList = messageList;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    // Increment unread
    public void incUnread(int unread) {
        this.unread += unread;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public List<User> getMemberList() {
        return memberList;
    }

    public void setMemberList(RealmList<User> memberList) {
        this.memberList = memberList;
    }

    public int getMemberCounts() {
        return memberCounts;
    }

    public void setMemberCounts(int memberCounts) {
        this.memberCounts = memberCounts;
    }

    public String getMemberCountsDesc() {
        return memberCountsDesc;
    }

    public void setMemberCountsDesc(String memberCountsDesc) {
        this.memberCountsDesc = memberCountsDesc;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public User getFounder() {
        return founder;
    }

    public void setFounder(User founder) {
        this.founder = founder;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    private static void addNewRooms(JSONArray jsonArray, RealmResults<Room> realmList, Realm realm) {
        String JSONId;
        JSONObject temp;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                temp = jsonArray.getJSONObject(i);
                JSONId = temp.getString("_id");
                if (!ListsUtils.isRoomJoined(realmList, JSONId)) {
                    String courseID = (String) JSONUtils.checkIfJsonExists(temp, "course", null);
                    updateRoomToRealm(new Room(
                            JSONId,
                            (String) JSONUtils.checkIfJsonExists(temp, "name", null),
                            new RealmList<Message>(),
                            courseID,
                            Course.getCourseById(courseID, realm).getCoursename(),
                            (String) JSONUtils.checkIfJsonExists(temp, "university", null),
                            new RealmList<User>(),
                            Integer.parseInt((String) JSONUtils.checkIfJsonExists(temp, "memberCounts", "1")),
                            (String) JSONUtils.checkIfJsonExists(temp, "memberCountsDescription", null),
                            new User((String) JSONUtils.checkIfJsonExists(temp, "founder", null), null, null, null, null, null),
                            (String) JSONUtils.checkIfJsonExists(temp, "language", null),
                            (boolean) JSONUtils.checkIfJsonExists(temp, "isPublic", true),
                            (boolean) JSONUtils.checkIfJsonExists(temp, "isSystem", true)
                    ), realm);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static void deleteOldRooms(JSONArray jsonArray, RealmResults<Room> realmList, Realm realm) {
        String realmId;
        Room realmTemp;
        JSONObject temp;
        for (int i = 0; i < realmList.size(); i++) {
            try {
                realmTemp = realmList.get(i);
                realmId = realmTemp.getId();
                if (!ListsUtils.isRoomJoined(jsonArray, realmId)) {
                    temp = jsonArray.getJSONObject(i);
                    String courseID = (String) JSONUtils.checkIfJsonExists(temp, "course", null);
                    deleteRoomFromRealm(new Room(
                            (String) JSONUtils.checkIfJsonExists(temp, "_id", null),
                            (String) JSONUtils.checkIfJsonExists(temp, "name", null),
                            new RealmList<Message>(),
                            courseID,
                            Course.getCourseById(courseID, realm).getCoursename(),
                            (String) JSONUtils.checkIfJsonExists(temp, "university", null),
                            new RealmList<User>(),
                            Integer.parseInt((String) JSONUtils.checkIfJsonExists(temp, "memberCounts", "1")),
                            (String) JSONUtils.checkIfJsonExists(temp, "memberCountsDescription", null),
                            new User((String) JSONUtils.checkIfJsonExists(temp, "founder", null), null, null, null, null, null),
                            (String) JSONUtils.checkIfJsonExists(temp, "language", null),
                            (boolean) JSONUtils.checkIfJsonExists(temp, "isPublic", true),
                            (boolean) JSONUtils.checkIfJsonExists(temp, "isSystem", true)
                    ), realm);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}