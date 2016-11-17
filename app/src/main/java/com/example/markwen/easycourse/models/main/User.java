package com.example.markwen.easycourse.models.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class User extends RealmObject {

    @PrimaryKey
    private String id;
    private String username;
    private byte[] profilePicture;
    private String profilePictureUrl;
    private String email;
    private String universityID;

    private int friendStatus = 0;

    public User() {

    }

    public User(String id, String username, byte[] profilePicture, String profilePictureUrl, String email, String universityID) {
        this.id = id;
        this.username = username;
        this.profilePicture = profilePicture;
        this.profilePictureUrl = profilePictureUrl;
        this.email = email;
        this.universityID = universityID;
    }

    public static void updateUserToRealm(User user, Realm realm) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
    }

    public static boolean isUserInRealm(User user, Realm realm) {
        RealmResults<User> results = realm.where(User.class)
                .equalTo("id", user.getId())
                .findAll();
        return results.size() != 0;
    }

    public static void deleteUserFromRealm(final User user, Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<User> results = realm.where(User.class)
                        .equalTo("id", user.getId())
                        .findAll();
                results.deleteAllFromRealm();
            }
        });
    }

    public static User getCurrentUser(Activity activity, Realm realm) {
        SharedPreferences sharedPref = activity.getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String id = sharedPref.getString("userId", "");

        RealmResults<User> results = realm.where(User.class)
                .equalTo("id", id)
                .findAll();

        return results.first();
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
}
