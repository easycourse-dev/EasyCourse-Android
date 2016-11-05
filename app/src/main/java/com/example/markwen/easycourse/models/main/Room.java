package com.example.markwen.easycourse.models.main;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class Room extends RealmObject {

    //When true, the room is one to one message
    private boolean isToUser = false;

    //When user quits this room on another platform, this room will not be deleted.
    //Instead, chaning isJoinIn to false
    private boolean isJoinIn = false;

    //Basic info of room
    private String id;
    private String roomname;
    private RealmList<Message> messageList;
    private int unread = 0;
    private boolean silent = false;

    //Group chatting
    private String courseID;
    private String courseName;
    private String university;
    private RealmList<User> memberList;
    private int memberCounts;
    private int language;

    //User built room
    private String founderID;
    private boolean isPublic = false;

    //System
    private boolean isSystem;


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

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
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

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public String getFounderID() {
        return founderID;
    }

    public void setFounderID(String founderID) {
        this.founderID = founderID;
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
}
