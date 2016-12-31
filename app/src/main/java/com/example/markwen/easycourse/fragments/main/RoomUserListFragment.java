package com.example.markwen.easycourse.fragments.main;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.RoomUserListViewAdapter;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.socket.client.Ack;


public class RoomUserListFragment extends Fragment {

    private static final String TAG = "RoomUserListFragment";

    @BindView(R.id.room_user_list_recyclerview)
    RecyclerView roomUserListRecyclerView;

    private SocketIO socketIO;
    private Room curRoom;
    private List<User> users;
    private RoomUserListViewAdapter roomUserListViewAdapter;


    @Contract("null -> null")
    @Nullable
    public static RoomUserListFragment newInstance(Room curRoom) {
        if (curRoom == null) return null;
        RoomUserListFragment fragment = new RoomUserListFragment();
        fragment.curRoom = curRoom;
        return fragment;
    }


    public RoomUserListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_room_user_list, container, false);
        ButterKnife.bind(this, v);
        socketIO = EasyCourse.getAppInstance().getSocketIO();

        getRoomUsers();


        return v;
    }

    private void getRoomUsers() {
        try {
            socketIO.getRoomMembers(curRoom.getId(), new Ack() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        JSONArray response = obj.getJSONArray("users");
                        List<User> users = new ArrayList<>(response.length());
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject object = (JSONObject) response.get(i);
                            String id = null;
                            String displayName = null;
                            String avatarUrl = null;
                            if (object.has("_id"))
                                id = object.getString("_id");
                            if (object.has("displayName"))
                                displayName = object.getString("displayName");
                            if (object.has("avatarUrl"))
                                avatarUrl = object.getString("avatarUrl");
                            User user = new User(id, displayName, null, avatarUrl, null, null);
                            users.add(user);
                        }
                        RoomUserListFragment.this.users = users;
                        setupRecyclerView();
                    } catch (Exception e) {
                        Log.e(TAG, "call: ", e);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "onCreateView: ", e);

        }
    }


    private void setupRecyclerView() {
        roomUserListViewAdapter = new RoomUserListViewAdapter(getContext(), users);
        roomUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        roomUserListRecyclerView.setAdapter(roomUserListViewAdapter);
        roomUserListRecyclerView.setHasFixedSize(true);

    }

}
