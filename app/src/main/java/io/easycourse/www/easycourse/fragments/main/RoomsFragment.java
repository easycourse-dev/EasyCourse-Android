package io.easycourse.www.easycourse.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.easycourse.www.easycourse.activities.ChatRoomActivity;
import io.easycourse.www.easycourse.activities.NewRoomActivity;
import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.components.main.RoomRecyclerViewAdapter;
import io.easycourse.www.easycourse.components.signup.RecyclerViewDivider;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.utils.SocketIO;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

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


    @BindView(io.easycourse.www.easycourse.R.id.roomsRecyclerView)
    RecyclerView roomRecyclerView;
    @BindView(io.easycourse.www.easycourse.R.id.mainFab)
    FloatingActionMenu mainFab;
    @BindView(io.easycourse.www.easycourse.R.id.newRoomFab)
    FloatingActionButton newRoomFab;

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
        View v = inflater.inflate(io.easycourse.www.easycourse.R.layout.fragment_rooms, container, false);
        ButterKnife.bind(this, v);

        setupFAB();

        setupRecyclerView();

        socketIO.syncUser();
        roomRecyclerViewAdapter.notifyDataSetChanged();

        return v;
    }

    private void setupFAB() {
        // MainFab
        mainFab.setMenuButtonColorNormalResId(io.easycourse.www.easycourse.R.color.colorAccent);
        mainFab.setMenuButtonColorRippleResId(io.easycourse.www.easycourse.R.color.colorAccent);
        mainFab.setMenuButtonColorPressedResId(io.easycourse.www.easycourse.R.color.colorAccent);

        // New Room Fab
        newRoomFab.setColorNormalResId(io.easycourse.www.easycourse.R.color.colorAccent);
        newRoomFab.setColorRippleResId(io.easycourse.www.easycourse.R.color.colorAccent);
        newRoomFab.setColorPressedResId(io.easycourse.www.easycourse.R.color.colorAccent);
        newRoomFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), NewRoomActivity.class));
            }
        });
    }

    private void setupRecyclerView() {
        rooms = realm.where(Room.class).findAll();
        roomRecyclerViewAdapter = new RoomRecyclerViewAdapter(this, getContext(), rooms);
        roomRecyclerView.setAdapter(roomRecyclerViewAdapter);
        roomRecyclerView.addItemDecoration(new RecyclerViewDivider(getContext()));
        roomRecyclerView.setHasFixedSize(true);
        LinearLayoutManager chatLinearManager = new LinearLayoutManager(getContext());
        chatLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomRecyclerView.setLayoutManager(chatLinearManager);
    }

    public void startChatRoom(Room room) {
        Intent chatActivityIntent = new Intent(getContext(), ChatRoomActivity.class);
        chatActivityIntent.putExtra("roomId", room.getId());
        getActivity().startActivity(chatActivityIntent);
        realm.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
