package com.example.markwen.easycourse.components.main;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.fragments.main.RoomUserListFragment;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.User;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by nrinehart on 12/30/16.
 */

public class RoomUserListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "RoomUserListViewAdapter";

    private List<User> users;
    private AppCompatActivity activity;
    private Realm realm;
    private User curUser;
    private RoomUserListFragment fragment;

    public RoomUserListViewAdapter(Context context, List<User> users, RoomUserListFragment fragment) {
        this.users = users;
        this.activity = (AppCompatActivity) context;
        realm = Realm.getDefaultInstance();
        this.curUser = User.getCurrentUser(this.activity, this.realm);
        this.fragment = fragment;
    }

    class RoomUserListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cardViewUserList)
        CardView cardView;
        @BindView(R.id.imageViewUserList)
        ImageView avatarImageView;
        @BindView(R.id.textViewNameUserList)
        TextView nameTextView;

        RoomUserListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        void setupView(final User user, final User curUser, final Context context) {
            if(user == null) return;
            nameTextView.setText(user.getUsername());
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user == curUser) return;
                    fragment.goToPrivateRoom(user);
//                    Toast.makeText(context, user.getUsername() + " clicked!", Toast.LENGTH_SHORT).show();
                }
            });

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
        if (users == null || users.size() < 1) return;

        final User user = users.get(position);
        RoomUserListViewHolder viewHolder = (RoomUserListViewHolder) holder;
        viewHolder.setupView(user, curUser, activity);
    }

    @Override
    public int getItemCount() {
        if (users == null || users.size() < 1)
            return 0;
        else
            return users.size();

    }
}