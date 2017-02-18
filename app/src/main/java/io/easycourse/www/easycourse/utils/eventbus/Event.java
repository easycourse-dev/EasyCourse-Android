package io.easycourse.www.easycourse.utils.eventbus;

import io.easycourse.www.easycourse.models.main.Message;

public class Event {

    public static class ConnectEvent{}

    public static class MessageEvent{
        private Message message;
        public MessageEvent(Message message) {
            this.message = message;
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    public static class DisconnectEvent{}

    public static class ReconnectEvent{}

    public static class ReconnectAttemptEvent{}

    public static class SyncEvent{}

}
