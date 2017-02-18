package io.easycourse.www.easycourse.components.main.chat.viewholders;

import android.content.Context;

import io.easycourse.www.easycourse.components.main.chat.ChatRecyclerViewAdapter;
import io.easycourse.www.easycourse.models.main.Message;
import io.easycourse.www.easycourse.models.main.User;

/**
 * Created by noahrinehart on 2/18/17.
 */

public interface BaseChatViewHolder {
    public void setupView(final Message message, Message prevMessage, User curUser, final String roomId, final Context context, ChatRecyclerViewAdapter chatRecyclerViewAdapter);
}
