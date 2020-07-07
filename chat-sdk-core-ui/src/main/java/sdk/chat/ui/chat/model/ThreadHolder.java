package sdk.chat.ui.chat.model;

import androidx.annotation.NonNull;

import com.stfalcon.chatkit.commons.models.IDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.ui.custom.MessageCustomizer;
import sdk.guru.common.RX;

public class ThreadHolder implements IDialog<MessageHolder> {

    protected Thread thread;
    protected List<UserHolder> users = new ArrayList<>();
    protected MessageHolder lastMessage = null;
    protected Integer unreadCount = null;
    protected Date creationDate;
    protected String displayName;

    public ThreadHolder(Thread thread) {
        this.thread = thread;
        creationDate = thread.getCreationDate();
        update();
    }

    public Completable updateAsync() {
        return Completable.create(emitter -> {
            update();
            emitter.onComplete();
        }).subscribeOn(RX.computation());
    }

    public void update() {
        Message message = thread.lastMessage();
        if (message != null) {
            lastMessage = MessageCustomizer.shared().onNewMessageHolder(message);
        } else {
            lastMessage = null;
        }
        users.clear();
        for (User user: thread.getUsers()) {
            if (!user.isMe()) {
                users.add(new UserHolder(user));
            }
        }
        unreadCount = thread.getUnreadMessagesCount();
        displayName = thread.getDisplayName();
    }

    @Override
    public String getId() {
        return thread.getEntityID();
    }

    public void markRead() {
        unreadCount = null;
    }

    @Override
    public String getDialogPhoto() {
        String url = thread.getImageUrl();
        if (url == null) {
            if (getUsers().size() == 1) {
                url = getUsers().get(0).getAvatar();
            }
        }
        return url;
    }

    @Override
    public String getDialogName() {
        return displayName;
    }

    @Override
    public List<UserHolder> getUsers() {
        if (users == null) {
            List<UserHolder> list = new ArrayList<>();
            for (User user: thread.getUsers()) {
                if (!user.isMe()) {
                    list.add(new UserHolder(user));
                }
            }
            users = list;
        }
        return users;
    }

    @Override
    public MessageHolder getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(MessageHolder message) {
        lastMessage = message;
        unreadCount = null;
    }

    @Override
    public int getUnreadCount() {
        if (unreadCount == null) {
            unreadCount = thread.getUnreadMessagesCount();
        }
        return unreadCount;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ThreadHolder && getId().equals(((ThreadHolder)object).getId());
    }

    public Thread getThread() {
        return thread;
    }

    public @NonNull Date getDate() {
        if (lastMessage != null) {
            return lastMessage.getCreatedAt();
        }
        if (creationDate != null) {
            return creationDate;
        }
        return new Date();
    }

    public Long getWeight() {
        return thread.getWeight();
    }
}
