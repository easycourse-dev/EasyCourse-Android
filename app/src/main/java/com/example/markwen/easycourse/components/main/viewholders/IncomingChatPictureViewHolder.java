package com.example.markwen.easycourse.components.main.viewholders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.ChatRecyclerViewAdapter;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.DateUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by nrinehart on 12/22/16.
 */

public class IncomingChatPictureViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "IncomingChatPictureView";

    @BindView(R.id.linearIncomingPicCell)
    private LinearLayout incomingPicLinearLayout;
    @BindView(R.id.textViewIncomingPicTime)
    private TextView incomingPicTime;
    @BindView(R.id.textViewIncomingPicName)
    private TextView incomingPicName;
    @BindView(R.id.imageViewIncomingUserImage)
    private ImageView incomingPicUserImage;
    @BindView(R.id.imageViewIncomingPicImage)
    private ImageView incomingPicImageView;


    protected IncomingChatPictureViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setupView(Message message, Message prevMessage, User curUser, Realm realm, Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter) {

        String reportDateOutgoing = DateUtils.getTimeString(message, prevMessage);
        if (reportDateOutgoing != null) {
            incomingPicTime.setVisibility(View.VISIBLE);
            incomingPicTime.setText(reportDateOutgoing);
        } else {
            incomingPicTime.setVisibility(View.GONE);
        }

        User thisUser = User.getUserFromRealm(realm, message.getSenderId());


        if (thisUser != null) {
            try {
                if (thisUser.getProfilePictureUrl() != null)
                    Picasso.with(context)
                            .load(curUser.getProfilePictureUrl()).resize(36, 36).centerInside()
                            .placeholder(R.drawable.ic_person_black_24px)
                            .into(incomingPicUserImage);

                if (!message.getImageUrl().isEmpty()) {
                    Picasso.with(context)
                            .load(message.getImageUrl()).resize(36, 36).centerInside()
                            .into(incomingPicImageView);
                }


            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            incomingPicName.setText(thisUser.getUsername());
            //TODO: add click listener to fullsize image with animation
        }
    }

}
