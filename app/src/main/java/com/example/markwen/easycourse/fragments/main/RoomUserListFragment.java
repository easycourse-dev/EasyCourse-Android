package com.example.markwen.easycourse.fragments.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.markwen.easycourse.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RoomUserListFragment extends Fragment {

    @BindView(R.id.room_user_list_recyclerview)
    RecyclerView roomUserListRecyclerView;



    public RoomUserListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_room_user_list, container, false);
        ButterKnife.bind(this, v);
        roomUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return v;
    }

}
