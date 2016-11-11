package com.example.markwen.easycourse.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoom;
import com.example.markwen.easycourse.activities.MainActivity;
import com.example.markwen.easycourse.components.ChatRoomRecyclerViewAdapter;
import com.example.markwen.easycourse.components.signup.RecyclerViewDivider;
import com.example.markwen.easycourse.models.main.Room;

import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class Rooms extends Fragment {

    private static final String TAG = "Rooms";


    private Realm realm;

    private RecyclerView roomsRecyclerView;
    private FloatingActionButton roomsFab;

    private ChatRoomRecyclerViewAdapter chatRoomAdapter;
    private LinearLayoutManager roomsLinearManager;
    private ArrayList<Room> rooms;


    public Rooms() {
    }


    // https://github.com/Clans/FloatingActionButton

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();

//        rooms = fetchRoomsFromRealm();
        rooms = new ArrayList<>();
        rooms.add(new Room("Computer Science", "CS240"));
        rooms.add(new Room("Geography", "EAPS120"));
        rooms.add(new Room("Linear Algebra", "MA265"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.rooms, container, false);
        roomsRecyclerView = (RecyclerView) v.findViewById(R.id.roomsRecyclerView);
        chatRoomAdapter = new ChatRoomRecyclerViewAdapter(rooms, getContext());
        roomsLinearManager = new LinearLayoutManager(getContext());
        roomsLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomsRecyclerView.setLayoutManager(roomsLinearManager);
        roomsRecyclerView.setHasFixedSize(true);
        roomsRecyclerView.setAdapter(chatRoomAdapter);

        roomsRecyclerView.addItemDecoration(new RecyclerViewDivider(getContext()));


        roomsFab = (FloatingActionButton) v.findViewById(R.id.roomsFab);
        roomsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                createNewConversation();
            }
        });

        return v;
    }

    public ArrayList<Room> fetchRoomsFromRealm() {
        if (realm != null)
            return Room.getRoomsFromRealm(realm);
        Log.d(TAG, "Realm not initiated!");
        return null;
    }

    public void createNewConversation() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();
    }
}
