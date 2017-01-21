package io.easycourse.www.easycourse.components.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.fragments.main.RoomUserListFragment;
import io.easycourse.www.easycourse.models.main.User;
import io.realm.Realm;

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
            if (user == null) return;


            if (user.getProfilePicture() != null) {
                Bitmap bm = BitmapFactory.decodeByteArray(user.getProfilePicture(), 0, user.getProfilePicture().length);
                avatarImageView.setImageBitmap(bm);
            } else if (user.getProfilePictureUrl() != null) {
                Picasso.with(context).load(user.getProfilePictureUrl()).placeholder(R.drawable.ic_person_black_24px).into(avatarImageView);
            } else {
                avatarImageView.setImageResource(R.drawable.ic_person_black_24px);
            }


            nameTextView.setText(user.getUsername());
            if (user.getId().equals(curUser.getId())) { //User is current user
                nameTextView.setTypeface(null, Typeface.BOLD);
            } else { //User is not current user
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragment.goToPrivateRoom(user);
                    }
                });
            }

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
