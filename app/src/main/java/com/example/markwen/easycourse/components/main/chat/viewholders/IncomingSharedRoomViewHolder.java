package com.example.markwen.easycourse.components.main.chat.viewholders;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by nisarg on 5/1/17.
 */

public class IncomingSharedRoomViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "IncomingSharedRoomView";

    private AppCompatActivity activity;


    public IncomingSharedRoomViewHolder(View itemView, AppCompatActivity activity) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.activity = activity;
    }
}
