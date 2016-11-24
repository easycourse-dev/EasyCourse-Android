package com.example.markwen.easycourse.components.main;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.utils.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;


/**
 * Created by noahrinehart on 11/19/16.
 */

public class ChatRecyclerViewAdapter extends RealmRecyclerViewAdapter<Message, RecyclerView.ViewHolder> {

    private final int INCOMING = 0, OUTGOING = 1;

    private Context context;

    public ChatRecyclerViewAdapter(Context context, RealmResults<Message> messages) {
        super(context, messages, true);
        this.context = context;
    }

    class IncomingChatViewHolder extends RecyclerView.ViewHolder {
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

        IncomingChatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class OutgoingChatViewHolder extends RecyclerView.ViewHolder {

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

        OutgoingChatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case OUTGOING:
                View outgoingView = inflater.inflate(R.layout.chat_cell_outgoing_text, viewGroup, false);
                viewHolder = new ChatRecyclerViewAdapter.OutgoingChatViewHolder(outgoingView);
                break;
            default:
                View incomingView = inflater.inflate(R.layout.chat_cell_incoming_text, viewGroup, false);
                viewHolder = new ChatRecyclerViewAdapter.IncomingChatViewHolder(incomingView);
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
            case OUTGOING:
                final ChatRecyclerViewAdapter.OutgoingChatViewHolder outgoingViewHolder = (ChatRecyclerViewAdapter.OutgoingChatViewHolder) viewHolder;

                String reportDateOutgoing = getTimeString(message, prevMessage);
                if (reportDateOutgoing != null) {
                    outgoingViewHolder.outgoingTime.setVisibility(View.VISIBLE);
                    outgoingViewHolder.outgoingTime.setText(reportDateOutgoing);
                } else {
                    outgoingViewHolder.outgoingTime.setVisibility(View.GONE);
                }

                //TODO: Set image from local data
                Glide.with(context)
                        .load(message.getImageUrl()).fitCenter()
                        .placeholder(R.drawable.ic_person_black_24px)
                        .into(outgoingViewHolder.outgoingImageView);

                //TODO: Fetch user name from senderId
                outgoingViewHolder.outgoingName.setText(message.getText());
                outgoingViewHolder.outgoingMessage.setText(message.getText());
                break;

            case INCOMING:
                ChatRecyclerViewAdapter.IncomingChatViewHolder incomingViewHolder = (ChatRecyclerViewAdapter.IncomingChatViewHolder) viewHolder;

                String reportDateIncoming = getTimeString(message, prevMessage);
                if (reportDateIncoming != null) {
                    incomingViewHolder.incomingTime.setVisibility(View.VISIBLE);
                    incomingViewHolder.incomingTime.setText(reportDateIncoming);
                } else {
                    incomingViewHolder.incomingTime.setVisibility(View.GONE);
                }

                //TODO: Set image from local data
                Glide.with(context)
                        .load(message.getImageUrl()).fitCenter()
                        .placeholder(R.drawable.ic_person_black_24px)
                        .into(incomingViewHolder.incomingImageView);

                //TODO: Fetch user name from senderId
                incomingViewHolder.incomingName.setText(message.getSenderId());
                incomingViewHolder.incomingMessage.setText(message.getText());
                break;
        }
    }

    @Nullable
    private String getTimeString(Message message, Message prevMessage) {
        if (prevMessage == null) return null;
        Date messageDate = message.getCreatedAt();
        Date prevMessageDate = prevMessage.getCreatedAt();
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
        if (message.isToUser())
            return OUTGOING;
        return INCOMING;
    }


}
