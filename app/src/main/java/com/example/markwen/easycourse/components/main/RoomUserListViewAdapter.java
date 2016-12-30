package com.example.markwen.easycourse.components.main;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by nrinehart on 12/30/16.
 */

public class RoomUserListViewAdapter extends RealmRecyclerViewAdapter<User, RecyclerView.ViewHolder> {

    private static final String TAG = "RoomUserListViewAdapter";

    private AppCompatActivity activity;
    private Realm realm;
    private User curUser;

    public RoomUserListViewAdapter(Context context, RealmResults<User> users) {
        super(context, users, true);
        this.activity = (AppCompatActivity) context;
        realm = Realm.getDefaultInstance();
        this.curUser = User.getCurrentUser(this.activity, this.realm);
    }

    private class RoomUserListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cell_user_list_textview)
        TextView nameTextView;

        RoomUserListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        void setupView(final User user, User curUser, Context context) {
            if(user == null) return;
            nameTextView.setText(user.getUsername());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cell_user_list, parent, false);
        RoomUserListViewHolder viewHolder = new RoomUserListViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getData() == null || getData().size() < 1) return;

        final User user = getData().get(position);
        RoomUserListViewHolder viewHolder = (RoomUserListViewHolder) holder;
        viewHolder.setupView(user, curUser, context);
    }
}
