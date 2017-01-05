package com.example.markwen.easycourse.models.main;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class Message extends RealmObject {

    @PrimaryKey
    private String id;
    private String remoteId;
    private User sender;
    private String text;
    private String imageUrl;
    private byte[] imageData;
    private String roomShareId;

    private boolean successSent;
    private double imageWidth;
    private double imageHeight;

    private String toRoom;
    private String toUser;
    private boolean isToUser = true;
    private Date createdAt;

    public Message() {
    }

    //TODO: Testing constructor
    public Message(User sender, String text, String imageUrl, Date createdAt) {
        this.sender = sender;
        this.text = text;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Message(String id, String remoteId, User sender, String text, String imageUrl, byte[] imageData, boolean successSent, double imageWidth, double imageHeight, String toRoom, String toUser, String roomShareId, Date createdAt) {
        this.id = id;
        this.remoteId = remoteId;
        this.sender = sender;
        this.text = text;
        this.imageUrl = imageUrl;
        this.imageData = imageData;
        this.successSent = successSent;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.toRoom = toRoom;
        this.toUser = toUser;
        this.roomShareId = roomShareId;
        this.createdAt = createdAt;
    }

    public static void updateMessageToRealm(Message message, Realm realm) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(message);
        realm.commitTransaction();
    }

    public static boolean isMessageInRealm(Message message, Realm realm) {
        RealmResults<Message> results = realm.where(Message.class)
                .equalTo("id", message.getId())
                .findAll();
        return results.size() != 0;
    }

    public static void deleteMessageFromRealm(final Message message, Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Message> results = realm.where(Message.class)
                        .equalTo("id", message.getId())
                        .findAll();
                results.deleteAllFromRealm();
            }
        });
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public String getSenderId() {
        return sender.getId();
    }

    public void setSenderId(String senderId) {
        this.sender.setId(senderId);
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public boolean isSuccessSent() {
        return successSent;
    }

    public void setSuccessSent(boolean successSent) {
        this.successSent = successSent;
    }

    public double getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(float imageWidth) {
        this.imageWidth = imageWidth;
    }

    public double getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(float imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getToRoom() {
        return toRoom;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToRoom(String toRoom) {
        this.toRoom = toRoom;
    }

    public boolean isToUser() {
        return isToUser;
    }

    public void setToUser(boolean toUser) {
        isToUser = toUser;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getRoomShareId() {
        return roomShareId;
    }

    public void setRoomShareId(String roomShareId) {
        this.roomShareId = roomShareId;
    }
}
