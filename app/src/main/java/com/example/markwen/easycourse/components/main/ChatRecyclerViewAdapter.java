package com.example.markwen.easycourse.components.main;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.main.viewholders.IncomingChatPictureViewHolder;
import com.example.markwen.easycourse.components.main.viewholders.IncomingChatTextViewHolder;
import com.example.markwen.easycourse.components.main.viewholders.OutgoingChatPictureViewHolder;
import com.example.markwen.easycourse.components.main.viewholders.OutgoingChatTextViewHolder;
import com.example.markwen.easycourse.models.main.Message;
import com.example.markwen.easycourse.models.main.User;
import com.example.markwen.easycourse.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

        final Message message;
        try {
            message = getData().get(position);
        } catch (NullPointerException e) {
            Log.e(TAG, "onBindViewHolder:", e);
            return;
        }
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

    public boolean showPopup(LinearLayout linearLayout, TextView textView, final Message message) {
        if (message.getText() == null) return false;

        PopupMenu popup = new PopupMenu(this.context, linearLayout);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.itemPopupCopy:
                        //Copy item to clipboard
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(message.getText(), message.getText());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
                        return true;

//                    case R.id.itemPopupDelete:
//                        //Delete item from realm
//                        return true;
                }
                return false;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.chat_message_popup, popup.getMenu());
        popup.show();
        return false;
    }


    @Nullable
    private String getTimeString(Message message, Message prevMessage) {
        if (prevMessage == null) return null;
        Date messageDate = message.getCreatedAt();
        if (messageDate == null) return null;
        Date prevMessageDate = prevMessage.getCreatedAt();
        if (prevMessageDate == null) return null;
        long diffInMinutes = DateUtils.timeDifferenceInMinutes(messageDate, prevMessageDate);
        if (diffInMinutes >= 5) {
            //If today
            if (DateUtils.isToday(messageDate)) {
                //Exclude date in time
                TimeZone UTC = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
                df.setTimeZone(UTC);
                return df.format(messageDate);

            } else {
                //Include date in time
                TimeZone UTC = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.US);
                df.setTimeZone(UTC);
                return df.format(messageDate);
            }

        }
        return null;
    }

    public void closeRealm() {
        if (realm != null)
            realm.close();
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
