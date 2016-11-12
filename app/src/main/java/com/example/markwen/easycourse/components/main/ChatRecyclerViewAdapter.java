package com.example.markwen.easycourse.components.main;

/**
 * Created by noahrinehart on 11/12/16.
 */

import android.content.Context;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int INCOMING = 0, OUTGOING = 1;

    private ArrayList<Message> chatList = new ArrayList<>();
    private Context context;

    public ChatRecyclerViewAdapter(ArrayList<Message> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;
    }

    private static class IncomingChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout incomingLinearLayout;
        TextView incomingTime;
        ImageView incomingImageView;
        TextView incomingName;
        TextView incomingMessage;

        IncomingChatViewHolder(View itemView) {
            super(itemView);
            incomingLinearLayout = (LinearLayout) itemView.findViewById(R.id.linearIncomingChatCell);
            incomingTime = (TextView) itemView.findViewById(R.id.textViewIncomingTextTime);
            incomingImageView = (ImageView) itemView.findViewById(R.id.imageViewIncomingTextImage);
            incomingName = (TextView) itemView.findViewById(R.id.textViewIncomingTextName);
            incomingMessage = (TextView) itemView.findViewById(R.id.textViewIncomingTextMessage);
        }
    }

    private static class OutgoingChatViewHolder extends RecyclerView.ViewHolder {

        LinearLayout outgoingLinearLayout;
        TextView outgoingTime;
        ImageView outgoingImageView;
        TextView outgoingName;
        TextView outgoingMessage;

        OutgoingChatViewHolder(View itemView) {
            super(itemView);
            outgoingLinearLayout = (LinearLayout) itemView.findViewById(R.id.linearOutgoingChatCell);
            outgoingTime = (TextView) itemView.findViewById(R.id.textViewOutgoingTextTime);
            outgoingImageView = (ImageView) itemView.findViewById(R.id.imageViewOutgoingTextImage);
            outgoingName = (TextView) itemView.findViewById(R.id.textViewOutgoingTextName);
            outgoingMessage = (TextView) itemView.findViewById(R.id.textViewOutgoingTextMessage);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case OUTGOING:
                View outgoingView = inflater.inflate(R.layout.chat_cell_outgoing_text, viewGroup, false);
                viewHolder = new OutgoingChatViewHolder(outgoingView);
                break;
            default:
                View incomingView = inflater.inflate(R.layout.chat_cell_incoming_text, viewGroup, false);
                viewHolder = new IncomingChatViewHolder(incomingView);
                break;
        }
        return viewHolder;
    }

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
    public void onBindViewHolder(final RecyclerView.ViewHolder chatViewHolder, int i) {
        Message prevMessage = null;
        Message message = chatList.get(i);
        if (i != 0)
            prevMessage = chatList.get(i - 1);


        switch (chatViewHolder.getItemViewType()) {
            case OUTGOING:
                final OutgoingChatViewHolder outgoingViewHolder = (OutgoingChatViewHolder) chatViewHolder;

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
                        .into(outgoingViewHolder.outgoingImageView);

                //TODO: Fetch user name from senderId
                outgoingViewHolder.outgoingName.setText(message.getSenderId());
                //TODO: Remove excape charactors
                outgoingViewHolder.outgoingMessage.setText(message.getText());
                break;

            case INCOMING:
                IncomingChatViewHolder incomingViewHolder = (IncomingChatViewHolder) chatViewHolder;

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
                        .into(incomingViewHolder.incomingImageView);

                //TODO: Fetch user name from senderId
                incomingViewHolder.incomingName.setText(message.getSenderId());
                incomingViewHolder.incomingMessage.setText(message.getText());
                break;
        }
    }

    @Override
    public int getItemViewType(int i) {
        final Message message = chatList.get(i);
        if (message.isToUser())
            return OUTGOING;
        return INCOMING;
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }


    public ArrayList<Message> getChatRoomList() {
        return chatList;
    }
}

