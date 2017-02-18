package io.easycourse.www.easycourse.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class NotificationMessage extends RealmObject {

    @PrimaryKey
    private String id;
    private int roomId;
    private String roomName;
    private String message;
    private Date createdAt;

    public NotificationMessage() {
    }

    public NotificationMessage(String id, int roomId, String roomName, String message, Date createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.roomName = roomName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
