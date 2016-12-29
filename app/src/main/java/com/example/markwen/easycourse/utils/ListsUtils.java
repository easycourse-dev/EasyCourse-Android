package com.example.markwen.easycourse.utils;

import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;

import java.util.ArrayList;
import java.util.Collections;

import io.realm.RealmList;

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
}
