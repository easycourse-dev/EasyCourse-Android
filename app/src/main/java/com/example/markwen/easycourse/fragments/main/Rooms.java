package com.example.markwen.easycourse.fragments.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.RoomRecyclerViewAdapter;
import com.example.markwen.easycourse.components.signup.RecyclerViewDivider;
import com.example.markwen.easycourse.models.main.Room;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class Rooms extends Fragment {

    private static final String TAG = "Rooms";


    private Realm realm;


    @BindView(R.id.roomsRecyclerView)
    RecyclerView roomRecyclerView;
    @BindView(R.id.roomsFab)
    FloatingActionMenu roomFab;

    RoomRecyclerViewAdapter roomRecyclerViewAdapter;
    RealmResults<Room> rooms;


    public Rooms() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rooms, container, false);
        ButterKnife.bind(this, v);

        setupRecyclerView();

        return v;
    }


    private void setupRecyclerView() {
        rooms = realm.where(Room.class).findAll();
        roomRecyclerViewAdapter = new RoomRecyclerViewAdapter(getContext(), rooms);
        roomRecyclerView.setAdapter(roomRecyclerViewAdapter);
        roomRecyclerView.addItemDecoration(new RecyclerViewDivider(getContext()));
        roomRecyclerView.setHasFixedSize(true);
        LinearLayoutManager chatLinearManager = new LinearLayoutManager(getContext());
        chatLinearManager.setOrientation(LinearLayoutManager.VERTICAL);
        roomRecyclerView.setLayoutManager(chatLinearManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
