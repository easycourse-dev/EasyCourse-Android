package com.example.markwen.easycourse.utils;

import com.example.markwen.easycourse.models.main.Course;
import com.example.markwen.easycourse.models.main.Language;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by markw on 12/29/2016.
 */

public class ListsUtils {
    public static boolean isRoomJoined(ArrayList<Room> joinedRooms, Room room) {
        for (int i = 0; i < joinedRooms.size(); i++) {
            if (joinedRooms.get(i).getId().equals(room.getId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRoomJoined(RealmList<Room> joinedRooms, Room room) {
        for (int i = 0; i < joinedRooms.size(); i++) {
            if (joinedRooms.get(i).getId().equals(room.getId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRoomJoined(RealmResults<Room> joinedRooms, String room) {
        for (int i = 0; i < joinedRooms.size(); i++) {
            if (joinedRooms.get(i).getId().equals(room)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRoomJoined(JSONArray joinedRooms, String room) {
        for (int i = 0; i < joinedRooms.length(); i++) {
            try {
                if (joinedRooms.getJSONObject(i).getString("_id").equals(room)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isCourseJoined(RealmResults<Course> joinedCourse, String course) {
        for (int i = 0; i < joinedCourse.size(); i++) {
            if (joinedCourse.get(i).getId().equals(course)) {
                return true;
            }
        }
        return false;
    }

    public static com.example.markwen.easycourse.models.signup.Course isCourseJoined(ArrayList<com.example.markwen.easycourse.models.signup.Course> joinedCourse, String course) {
        for (int i = 0; i < joinedCourse.size(); i++) {
            if (joinedCourse.get(i).getId().equals(course)) {
                return joinedCourse.get(i);
            }
        }
        return null;
    }

    public static boolean isCourseJoined(JSONArray joinedCourse, String course) {
        for (int i = 0; i < joinedCourse.length(); i++) {
            try {
                if (joinedCourse.getJSONObject(i).getString("_id").equals(course)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isUserInList(RealmList<User> userList, User user) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> stringArrayToArrayList(String[] array) {
        ArrayList<String> temp = new ArrayList<>();
        Collections.addAll(temp, array);
        return temp;
    }

    public static boolean isLanguageInList(RealmList<Language> userList, String code) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLanguageInList(JSONArray userList, String code) {
        for (int i = 0; i < userList.length(); i++) {
            try {
                if (userList.getString(i).equals(code)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
