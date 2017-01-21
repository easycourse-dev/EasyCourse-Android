package io.easycourse.www.easycourse.fragments.main;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.ChatRoomActivity;
import io.easycourse.www.easycourse.components.main.RoomUserListViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.User;
import io.easycourse.www.easycourse.utils.SocketIO;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.socket.client.Ack;


public class RoomUserListFragment extends Fragment {

    private static final String TAG = "RoomUserListFragment";

    @BindView(R.id.room_user_list_recyclerview)
    RecyclerView roomUserListRecyclerView;
    @BindView(R.id.room_user_list_progressbar)
    ProgressBar chatProgressBar;

    private SocketIO socketIO;
    private Room curRoom;
    private User curUser;
    private List<User> users;
    private Realm realm;
    private RoomUserListViewAdapter roomUserListViewAdapter;
    private ChatRoomActivity activity;


    @Contract("null -> null")
    @Nullable
    public static RoomUserListFragment newInstance(Room curRoom, User curUser) {
        if (curRoom == null) return null;
        RoomUserListFragment fragment = new RoomUserListFragment();
        fragment.curRoom = curRoom;
        fragment.curUser = curUser;
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
        activity = (ChatRoomActivity) getActivity();
        realm = Realm.getDefaultInstance();

        activity.getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.gotoChatRoomFragment(curRoom, curUser);
            }
        });

        getRoomUsers();


        return v;
    }

    private void getRoomUsers() {
        chatProgressBar.setVisibility(View.VISIBLE);
        try {
            long time1 = System.currentTimeMillis();
            Log.d(TAG, "getRoomUsers: " + time1);
            socketIO.getRoomMembers(curRoom.getId(), new Ack() {
                @Override
                public void call(Object... args) {
                    long time2 = System.currentTimeMillis();
                    Log.d(TAG, "getRoomUsers: " + time2);
                    try {
                        JSONObject obj = (JSONObject) args[0];
                        JSONArray response = obj.getJSONArray("users");
                        users = new ArrayList<>(response.length());
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
                        RoomUserListFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setupRecyclerView();
                            }
                        });
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
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getUsername().compareTo(o2.getUsername());
            }
        });
        roomUserListViewAdapter = new RoomUserListViewAdapter(getContext(), users, this);
        roomUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        roomUserListRecyclerView.setAdapter(roomUserListViewAdapter);
        roomUserListRecyclerView.setHasFixedSize(true);
        chatProgressBar.setVisibility(View.GONE);
    }


    public void goToPrivateRoom(final User toUser) {
        Room room = new Room(
                toUser.getId(),
                toUser.getUsername(),
                new RealmList<Message>(),
                0,
                false,
                null,
                null,
                null,
                new RealmList<>(curUser, toUser),
                2,
                "<10",
                null,
                curUser,
                false,
                false,
                true,
                true);


        updateRoomInSocket(room);
        Intent chatActivityIntent = new Intent(activity, ChatRoomActivity.class);
        chatActivityIntent.putExtra("roomId", room.getId());
        activity.finish();
        startActivity(chatActivityIntent);
    }

    public void updateRoomInSocket(final Room room){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Room.updateRoomToRealm(room, realm);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(realm != null) realm.close();
    }
}
