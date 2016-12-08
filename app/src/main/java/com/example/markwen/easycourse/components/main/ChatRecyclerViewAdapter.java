package com.example.markwen.easycourse.components.main;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;


/**
 * Created by noahrinehart on 11/19/16.
 */

public class ChatRecyclerViewAdapter extends RealmRecyclerViewAdapter<Message, RecyclerView.ViewHolder> {

    private static final String TAG = "ChatRecyclerViewAdapter";

    private final int INCOMING_TEXT = 0, INCOMING_PIC = 1, OUTGOING_TEXT = 2, OUTGOING_PIC = 3;

    private Context context;
    private Activity activity;
    private Realm realm;
    private User curUser;

    public ChatRecyclerViewAdapter(Context context, RealmResults<Message> messages) {
        super(context, messages, true);
        this.context = context;
        this.activity = (Activity) context;
        realm = Realm.getDefaultInstance();
        this.curUser = User.getCurrentUser(this.activity, this.realm);
    }

    class IncomingChatTextViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.linearIncomingChatCell)
        LinearLayout incomingLinearLayout;
        @BindView(R.id.textViewIncomingTextTime)
        TextView incomingTime;
        @BindView(R.id.imageViewIncomingTextImage)
        ImageView incomingImageView;
        @BindView(R.id.textViewIncomingTextName)
        TextView incomingName;
        @BindView(R.id.textViewIncomingTextMessage)
        TextView incomingMessage;

        IncomingChatTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class IncomingChatPictureViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.linearIncomingPicCell)
        LinearLayout incomingPicLinearLayout;
        @BindView(R.id.textViewIncomingPicTime)
        LinearLayout incomingPicTime;
        @BindView(R.id.imageViewIncomingUserImage)
        ImageView incomingPicUserView;
        @BindView(R.id.textViewIncomingPicName)
        TextView incomingPicName;
        @BindView(R.id.imageViewIncomingPicImage)
        ImageView incomingPicImageView;


        IncomingChatPictureViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class OutgoingChatTextViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.linearOutgoingChatCell)
        LinearLayout outgoingLinearLayout;
        @BindView(R.id.textViewOutgoingTextTime)
        TextView outgoingTime;
        @BindView(R.id.imageViewOutgoingTextImage)
        ImageView outgoingImageView;
        @BindView(R.id.textViewOutgoingTextName)
        TextView outgoingName;
        @BindView(R.id.textViewOutgoingTextMessage)
        TextView outgoingMessage;

        OutgoingChatTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class OutgoingChatPictureViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.linearOutgoingPicCell)
        LinearLayout incomingPicLinearLayout;
        @BindView(R.id.textViewOutgoingPicTime)
        LinearLayout incomingPicTime;
        @BindView(R.id.imageViewOutgoingUserImage)
        ImageView incomingPicUserView;
        @BindView(R.id.textViewOutgoingPicName)
        TextView incomingPicName;
        @BindView(R.id.imageViewOutgoingImage)
        ImageView incomingPicImageView;


        OutgoingChatPictureViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case OUTGOING_TEXT:
                View outgoingView = inflater.inflate(R.layout.chat_cell_outgoing_text, viewGroup, false);
                viewHolder = new OutgoingChatTextViewHolder(outgoingView);
                break;
            default:
                View incomingView = inflater.inflate(R.layout.chat_cell_incoming_text, viewGroup, false);
                viewHolder = new IncomingChatTextViewHolder(incomingView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Message prevMessage = null;
        Message message = getData().get(position);
        if (position != 0)
            prevMessage = getData().get(position - 1);


        switch (viewHolder.getItemViewType()) {
            case OUTGOING_TEXT: {
                final OutgoingChatTextViewHolder outgoingViewHolder = (OutgoingChatTextViewHolder) viewHolder;

                String reportDateOutgoing = getTimeString(message, prevMessage);
                if (reportDateOutgoing != null) {
                    outgoingViewHolder.outgoingTime.setVisibility(View.VISIBLE);
                    outgoingViewHolder.outgoingTime.setText(reportDateOutgoing);
                } else {
                    outgoingViewHolder.outgoingTime.setVisibility(View.GONE);
                }
                try {
                    if(this.curUser != null && !this.curUser.getProfilePictureUrl().isEmpty()) {
                        Picasso.with(context)
                                .load(this.curUser.getProfilePictureUrl()).centerInside()
                                .placeholder(R.drawable.ic_person_black_24px)
                                .into(outgoingViewHolder.outgoingImageView);


                        outgoingViewHolder.outgoingName.setText(this.curUser.getUsername());
                        outgoingViewHolder.outgoingMessage.setText(message.getText());
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, e.toString());
                }
                break;
            }

            case OUTGOING_PIC: {
                break;
            }


            case INCOMING_TEXT: {
                IncomingChatTextViewHolder incomingViewHolder = (IncomingChatTextViewHolder) viewHolder;

                String reportDateIncoming = getTimeString(message, prevMessage);
                if (reportDateIncoming != null) {
                    incomingViewHolder.incomingTime.setVisibility(View.VISIBLE);
                    incomingViewHolder.incomingTime.setText(reportDateIncoming);
                } else {
                    incomingViewHolder.incomingTime.setVisibility(View.GONE);
                }

                User thisUser = User.getUserFromRealm(this.realm, message.getSenderId());

                try {
                    if(!this.curUser.getProfilePictureUrl().isEmpty())
                        Picasso.with(context)
                                .load(thisUser.getProfilePictureUrl()).centerInside()
                                .placeholder(R.drawable.ic_person_black_24px)
                                .into(incomingViewHolder.incomingImageView);

                    if (thisUser.getUsername() != null)
                        incomingViewHolder.incomingName.setText(thisUser.getUsername());
                    incomingViewHolder.incomingMessage.setText(message.getText());
                } catch (NullPointerException e) {
                    Log.e(TAG, e.toString());
                }
                break;
            }

            case INCOMING_PIC: {

                break;
            }
        }
    }

    @Nullable
    private String getTimeString(Message message, Message prevMessage) {
        if (prevMessage == null) return null;
        Date messageDate = message.getCreatedAt();
        if (messageDate == null) return null;
        Date prevMessageDate = prevMessage.getCreatedAt();
        if (prevMessageDate == null) return null;
        long diffInMinutes = DateUtils.timeDifferenceInMinutes(messageDate, prevMessageDate);
        if (diffInMinutes >= 1) {
            //If today
            if (DateUtils.isToday(messageDate) && DateUtils.isToday(prevMessageDate)) {
                //Exclude date in time
                DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
                return df.format(messageDate);

            } else {
                //Include date in time
                DateFormat df = new SimpleDateFormat("MM:dd:yy hh:mm a", Locale.US);
                return df.format(messageDate);
            }

        }
        return null;
    }

    @Override
    public int getItemViewType(int i) {
        final Message message = getData().get(i);
        if (message.isToUser()) {
            if (message.getImageUrl() != null)
                return OUTGOING_PIC;
            else
                return OUTGOING_TEXT;
        } else {
            if (message.getImageUrl() != null)
                return INCOMING_PIC;
            else
                return INCOMING_TEXT;
        }
    }


}
