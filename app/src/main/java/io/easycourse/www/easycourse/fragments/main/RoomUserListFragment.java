package io.easycourse.www.easycourse.fragments.main;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.ChatRoomActivity;
import io.easycourse.www.easycourse.activities.UserDetailActivity;
import io.easycourse.www.easycourse.components.main.RoomUserListViewAdapter;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.models.main.User;
import io.realm.Realm;
import io.socket.client.Ack;


public class RoomUserListFragment extends BaseFragment {

    private static final String TAG = "RoomUserListFragment";

    @BindView(R.id.room_user_list_recyclerview)
    RecyclerView roomUserListRecyclerView;
    @BindView(R.id.room_user_list_progressbar)
    ProgressBar chatProgressBar;

    private Room curRoom;
    private List<User> users;
    private RoomUserListViewAdapter roomUserListViewAdapter;
    private ChatRoomActivity activity;


    @Nullable
    public static RoomUserListFragment newInstance(Room curRoom, User curUser) {
        if (curRoom == null) return null;
        RoomUserListFragment fragment = new RoomUserListFragment();
        fragment.curRoom = curRoom;
        fragment.currentUser = curUser;
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
                activity.gotoChatRoomFragment(curRoom, currentUser);
            }
        });

        getRoomUsers();


        return v;
    }

    private void getRoomUsers() {
        chatProgressBar.setVisibility(View.VISIBLE);
        try {
            socketIO.getRoomMembers(curRoom.getId(), new Ack() {
                @Override
                public void call(Object... args) {
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
        Thread thread = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Collections.sort(users, new Comparator<User>() {
                                @Override
                                public int compare(User o1, User o2) {
                                    return o1.getUsername().compareTo(o2.getUsername());
                                }
                            });
                            roomUserListViewAdapter = new RoomUserListViewAdapter(getContext(), users, RoomUserListFragment.this, currentUser);
                            roomUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            roomUserListRecyclerView.setAdapter(roomUserListViewAdapter);
                            roomUserListRecyclerView.setHasFixedSize(true);
                            chatProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        };
        thread.start();
    }


    public void openUserDetails(final User toUser) {
        if (toUser == null) {
            Toast.makeText(activity, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getContext(), UserDetailActivity.class);
        intent.putExtra("user", toUser.getId());
        startActivityForResult(intent, 99);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) realm.close();
    }
}
