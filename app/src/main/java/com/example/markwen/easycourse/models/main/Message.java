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
    private String senderId;
    private String text;
    private String imageUrl;
    private byte[] imageData;

    private boolean successSent;
    private double imageWidth;
    private double imageHeight;

    private String toRoom;
    private boolean isToUser = true;
    private Date createdAt;

    public Message() {

    }

    public Message(String id, String remoteId, String senderId, String text, String imageUrl, byte[] imageData, boolean successSent, double imageWidth, double imageHeight, String toRoom, Date createdAt) {
        this.id = id;
        this.remoteId = remoteId;
        this.senderId = senderId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.imageData = imageData;
        this.successSent = successSent;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.toRoom = toRoom;
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
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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
}
