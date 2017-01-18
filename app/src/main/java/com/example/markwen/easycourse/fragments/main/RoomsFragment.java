package com.example.markwen.easycourse.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markwen.easycourse.EasyCourse;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.activities.ChatRoomActivity;
import com.example.markwen.easycourse.activities.NewRoomActivity;
import com.example.markwen.easycourse.components.main.RoomRecyclerViewAdapter;
import com.example.markwen.easycourse.components.signup.RecyclerViewDivider;
import com.example.markwen.easycourse.models.main.Room;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.SocketIO;
import com.example.markwen.easycourse.utils.eventbus.Event;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class RoomsFragment extends Fragment {

    private static final String TAG = "RoomsFragment";


    private Realm realm;
    private SocketIO socketIO;

    @BindView(R.id.roomsRecyclerView)
    RecyclerView roomRecyclerView;
    @BindView(R.id.mainFab)
    FloatingActionMenu mainFab;
    @BindView(R.id.newRoomFab)
    FloatingActionButton newRoomFab;
    @BindView(R.id.roomsRecyclerViewPlaceholder)
    TextView roomsRecyclerViewPlaceholder;

    RoomRecyclerViewAdapter roomRecyclerViewAdapter;
    RealmResults<Room> rooms;

    public RoomsFragment() {
    }

    //TODO: realm listener to change time and bold text

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        socketIO = EasyCourse.getAppInstance().getSocketIO();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rooms, container, false);
        ButterKnife.bind(this, v);

        setupFAB();

        setupRecyclerView();

        if(socketIO != null)
            socketIO.syncUser();
        if (roomRecyclerViewAdapter != null)
            roomRecyclerViewAdapter.notifyDataSetChanged();

        return v;
    }

    private void setupFAB() {
        // MainFab
        mainFab.setMenuButtonColorNormalResId(R.color.colorAccent);
        mainFab.setMenuButtonColorRippleResId(R.color.colorAccent);
        mainFab.setMenuButtonColorPressedResId(R.color.colorAccent);

        // New Room Fab
        newRoomFab.setColorNormalResId(R.color.colorAccent);
        newRoomFab.setColorRippleResId(R.color.colorAccent);
        newRoomFab.setColorPressedResId(R.color.colorAccent);
        newRoomFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), NewRoomActivity.class));
            }
        });
    }

    private void setupRecyclerView() {
        rooms = realm.where(Room.class).equalTo("isJoinIn", true).findAll();
        if (rooms.size() == 0)
            try {
                if (socketIO != null)
                    socketIO.getAllMessage();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (rooms.size() != 0) {
            roomRecyclerViewAdapter = new RoomRecyclerViewAdapter(this, getContext(), rooms, socketIO);
            roomRecyclerView.setAdapter(roomRecyclerViewAdapter);
            roomRecyclerView.addItemDecoration(new RecyclerViewDivider(getContext()));
            roomRecyclerView.setHasFixedSize(true);
            LinearLayoutManager chatLinearManager = new LinearLayoutManager(getContext());
            chatLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
            roomRecyclerView.setLayoutManager(chatLinearManager);
        } else {
            roomRecyclerView.setVisibility(View.GONE);
            roomsRecyclerViewPlaceholder.setVisibility(View.VISIBLE);
            roomsRecyclerViewPlaceholder.setText("You don't have any joined rooms.\nAdd one by clicking the button below.");
        }
    }

    public void startChatRoom(Room room) {
        Intent chatActivityIntent = new Intent(getContext(), ChatRoomActivity.class);
        chatActivityIntent.putExtra("roomId", room.getId());
        realm.close();
        getActivity().startActivity(chatActivityIntent);
    }

    public void deleteRoom(Room room) {
        Room.deleteRoomFromRealm(room, realm);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Subscribe
    public void refreshView(Event.SyncEvent syncEvent) {
        this.roomRecyclerViewAdapter.notifyDataSetChanged();
    }
}
