package io.easycourse.www.easycourse.components.main.chat;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.easycourse.www.easycourse.components.main.chat.viewholders.IncomingChatPictureViewHolder;
import io.easycourse.www.easycourse.components.main.chat.viewholders.IncomingChatTextViewHolder;
import io.easycourse.www.easycourse.components.main.chat.viewholders.OutgoingChatPictureViewHolder;
import io.easycourse.www.easycourse.components.main.chat.viewholders.OutgoingChatTextViewHolder;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;


/**
 * Created by noahrinehart on 11/19/16.
 */

public class ChatRecyclerViewAdapter extends RealmRecyclerViewAdapter<Message, RecyclerView.ViewHolder> {

    private static final String TAG = "ChatRecyclerViewAdapter";

    private final int INCOMING_TEXT = 1, INCOMING_PIC = 2, OUTGOING_TEXT = 3, OUTGOING_PIC = 4;

    private Context context;
    private AppCompatActivity activity;
    private Realm realm;
    private User curUser;

    public ChatRecyclerViewAdapter(Context context, RealmResults<Message> messages) {
        super(context, messages, true);
        this.context = context;
        this.activity = (AppCompatActivity) context;
        realm = Realm.getDefaultInstance();
        this.curUser = User.getCurrentUser(this.activity, this.realm);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case INCOMING_TEXT:
                View incomingTextView = inflater.inflate(io.easycourse.www.easycourse.R.layout.chat_cell_incoming_text, viewGroup, false);
                viewHolder = new IncomingChatTextViewHolder(incomingTextView, activity);
                break;

            case INCOMING_PIC:
                View incomingPicView = inflater.inflate(io.easycourse.www.easycourse.R.layout.chat_pic_incoming, viewGroup, false);
                viewHolder = new IncomingChatPictureViewHolder(incomingPicView, activity);
                break;

            case OUTGOING_TEXT:
                View outgoingTextView = inflater.inflate(io.easycourse.www.easycourse.R.layout.chat_cell_outgoing_text, viewGroup, false);
                viewHolder = new OutgoingChatTextViewHolder(outgoingTextView, activity);
                break;

            case OUTGOING_PIC:
                View outgoingPicView = inflater.inflate(io.easycourse.www.easycourse.R.layout.chat_pic_outgoing, viewGroup, false);
                viewHolder = new OutgoingChatPictureViewHolder(outgoingPicView, activity);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Message prevMessage = null;


        if (getData() == null || getData().size() < 1)
            return;

        final Message message = getData().get(position);
        if (position != 0)
            prevMessage = getData().get(position - 1);


        switch (viewHolder.getItemViewType()) {
            case OUTGOING_TEXT: {
                OutgoingChatTextViewHolder outgoingViewHolder = (OutgoingChatTextViewHolder) viewHolder;
                outgoingViewHolder.setupView(message, prevMessage, curUser, context, this);
                break;

            }

            case OUTGOING_PIC: {
                OutgoingChatPictureViewHolder outgoingViewHolder = (OutgoingChatPictureViewHolder) viewHolder;
                outgoingViewHolder.setupView(message, prevMessage, curUser, context, this);
                break;
            }


            case INCOMING_TEXT: {
                IncomingChatTextViewHolder incomingViewHolder = (IncomingChatTextViewHolder) viewHolder;
                incomingViewHolder.setupView(message, prevMessage, curUser, realm, context, this);
                break;
            }

            case INCOMING_PIC: {
                IncomingChatPictureViewHolder incomingViewHolder = (IncomingChatPictureViewHolder) viewHolder;
                incomingViewHolder.setupView(message, prevMessage, curUser, realm, context, this);
                break;
            }
        }
    }




    @Override
    public int getItemViewType(int i) {
        if (getData() != null && getData().size() > 0) {
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
        return 0;
    }

    public void closeRealm() {
        if (realm != null)
            realm.close();
    }
}
