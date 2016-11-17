package com.example.markwen.easycourse.fragments.main;

import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ChatRoomRecyclerViewAdapter;
import com.example.markwen.easycourse.components.signup.RecyclerViewDivider;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class Rooms extends Fragment {

    private static final String TAG = "Rooms";


    private Realm realm;

    private RecyclerView roomsRecyclerView;
    private FloatingActionMenu roomsFab;

    private ChatRoomRecyclerViewAdapter chatRoomAdapter;
    private LinearLayoutManager roomsLinearManager;
    private ArrayList<Room> roomList;

    User currentUser;


    public Rooms() {
    }


    // https://github.com/Clans/FloatingActionButton

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        currentUser = User.getCurrentUser(getActivity(), realm);
        roomList = new ArrayList<>();
        addRooms();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rooms, container, false);
        roomsRecyclerView = (RecyclerView) v.findViewById(R.id.roomsRecyclerView);
        chatRoomAdapter = new ChatRoomRecyclerViewAdapter(roomList, getContext());
        roomsLinearManager = new LinearLayoutManager(getContext());
        roomsLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomsRecyclerView.setLayoutManager(roomsLinearManager);
        roomsRecyclerView.setHasFixedSize(true);
        roomsRecyclerView.setAdapter(chatRoomAdapter);

        roomsRecyclerView.addItemDecoration(new RecyclerViewDivider(getContext()));


        roomsFab = (FloatingActionMenu) v.findViewById(R.id.roomsFab);
        roomsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                createNewConversation();
            }
        });

        return v;
    }


    public void createNewConversation() {
    }

    public void addRooms() {
        RealmList<Room> rooms = currentUser.getJoinedRooms();
        for (Room room : rooms) {
            roomList.add(room);
        }
    }

    //TODO: Check null realm everywhere else
    public ArrayList<Room> fetchRoomsFromRealm() {
        if (realm != null)
            return Room.getRoomsFromRealm(realm);
        Log.d(TAG, "Realm not initiated!");
        return null;
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
