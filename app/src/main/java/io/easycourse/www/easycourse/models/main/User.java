package io.easycourse.www.easycourse.models.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    private String id;
    private String username;
    private byte[] profilePicture;
    private String profilePictureUrl;
    private String email;
    private String universityID;
    private RealmList<Course> joinedCourses = new RealmList<>();
    private RealmList<Room> joinedRooms = new RealmList<>();
    private RealmList<Room> silentRooms = new RealmList<>();
    private RealmList<Language> userLanguages = new RealmList<>();

    private int friendStatus = 0;

    public User() {}

    public User(String id, String username, String profilePictureUrl) {
        this.id = id;
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
    }

    public User(String id, String username, byte[] profilePicture, String profilePictureUrl, String email, String universityID) {
        this.id = id;
        this.username = username;
        this.profilePicture = profilePicture;
        this.profilePictureUrl = profilePictureUrl;
        this.email = email;
        this.universityID = universityID;
    }

    public User(String id, String username, byte[] profilePicture, String profilePictureUrl, String email, String universityID, RealmList<Course> joinedCourses, RealmList<Room> joinedRooms, RealmList<Room> silentRooms, RealmList<Language> userLanguages, int friendStatus) {
        this.id = id;
        this.username = username;
        this.profilePicture = profilePicture;
        this.profilePictureUrl = profilePictureUrl;
        this.email = email;
        this.universityID = universityID;
        this.joinedCourses = joinedCourses;
        this.joinedRooms = joinedRooms;
        this.silentRooms = silentRooms;
        this.userLanguages = userLanguages;
        this.friendStatus = friendStatus;
    }

    public static void updateUserToRealm(User user, Realm realm) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
    }


    @Nullable
    public static User getUserFromRealm(Realm realm, String id) {
        RealmResults<User> results = realm.where(User.class)
                .equalTo("id", id)
                .findAll();
        if (results.size() > 0)
            return results.first();
        else
            return null;
    }

    @Nullable
    public static User getCurrentUser(Context context, Realm realm) {
        SharedPreferences sharedPref = context.getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String id = sharedPref.getString("userId", "");

        RealmResults<User> results = realm.where(User.class)
                .equalTo("id", id)
                .findAll();
        if (results.size() > 0)
            return results.first();
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUniversityID() {
        return universityID;
    }

    public void setUniversityID(String universityID) {
        this.universityID = universityID;
    }

    public int getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(int friendStatus) {
        this.friendStatus = friendStatus;
    }

    public RealmList<Course> getJoinedCourses() {
        return joinedCourses;
    }

    public void setJoinedCourses(RealmList<Course> joinedCourses) {
        this.joinedCourses = joinedCourses;
    }

    public RealmList<Room> getJoinedRooms() {
        return joinedRooms;
    }

    public void setJoinedRooms(RealmList<Room> joinedRooms) {
        this.joinedRooms = joinedRooms;
    }

    public RealmList<Room> getSilentRooms() {
        return silentRooms;
    }

    public void setSilentRooms(RealmList<Room> silentRooms) {
        this.silentRooms = silentRooms;
    }

    public RealmList<Language> getUserLanguages() {
        return userLanguages;
    }

    public void setUserLanguages(RealmList<Language> list) {
        this.userLanguages = list;
    }
}
