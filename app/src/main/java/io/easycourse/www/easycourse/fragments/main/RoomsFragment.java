package io.easycourse.www.easycourse.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import io.easycourse.www.easycourse.EasyCourse;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.activities.ChatRoomActivity;
import io.easycourse.www.easycourse.activities.MainActivity;
import io.easycourse.www.easycourse.activities.NewRoomActivity;
import io.easycourse.www.easycourse.components.main.RoomRecyclerViewAdapter;
import io.easycourse.www.easycourse.components.signup.RecyclerViewDivider;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.Room;
import io.easycourse.www.easycourse.utils.SocketIO;
import io.easycourse.www.easycourse.utils.eventbus.Event;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class RoomsFragment extends BaseFragment {

    private static final String TAG = "RoomsFragment";


    private RealmChangeListener<RealmResults<Room>> realmChangeListener;


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

        EasyCourse.bus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rooms, container, false);
        ButterKnife.bind(this, v);

        setupFAB();

        setupRecyclerView();


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
        rooms = realm.where(Room.class).equalTo("isJoinIn", true).findAllAsync();
        //if (rooms.size() == 0)
        try {
            if (socketIO != null)
                socketIO.getHistMessage();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        roomRecyclerViewAdapter = new RoomRecyclerViewAdapter(this, getContext(), rooms, socketIO);
        roomRecyclerView.setAdapter(roomRecyclerViewAdapter);
        roomRecyclerView.addItemDecoration(new RecyclerViewDivider(getContext()));
        roomRecyclerView.setHasFixedSize(true);
        LinearLayoutManager chatLinearManager = new LinearLayoutManager(getContext());
        chatLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomRecyclerView.setLayoutManager(chatLinearManager);
        realmChangeListener = new RealmChangeListener<RealmResults<Room>>() {
            @Override
            public void onChange(RealmResults<Room> element) {
                roomRecyclerView.setVisibility(View.VISIBLE);
                roomsRecyclerViewPlaceholder.setVisibility(View.GONE);
            }
        };
        rooms.addChangeListener(realmChangeListener);

        if (rooms.size() == 0) {
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
        Realm tempRelm = Realm.getDefaultInstance();
        RealmResults<Message> messages = tempRelm.where(Message.class).equalTo("toRoom", room.getId()).findAll();
        tempRelm.beginTransaction();
        room.deleteFromRealm();
        if (messages.isValid())
            messages.deleteAllFromRealm();
        tempRelm.commitTransaction();
        tempRelm.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null)
            realm.close();
    }

    @Subscribe
    public void refreshViewAfterSync(Event.SyncEvent syncEvent) {
        if (roomRecyclerViewAdapter != null)
            this.roomRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void refreshViewAfterMessage(Event.MessageEvent messageEvent) {
        if (roomRecyclerViewAdapter != null)
            this.roomRecyclerViewAdapter.notifyDataSetChanged();
    }
}
