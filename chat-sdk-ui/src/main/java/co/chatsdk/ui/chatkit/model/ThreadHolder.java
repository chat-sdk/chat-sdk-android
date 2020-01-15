package co.chatsdk.ui.chatkit.model;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;

public class ThreadHolder implements IDialog<MessageHolder> {

    Thread thread;

    public ThreadHolder(Thread thread) {
        this.thread = thread;
    }

    @Override
    public String getId() {
        return thread.getEntityID();
    }

    @Override
    public String getDialogPhoto() {
        return thread.getImageUrl();
    }

    @Override
    public String getDialogName() {
        return thread.getDisplayName();
    }

    @Override
    public List<UserHolder> getUsers() {
        ArrayList<UserHolder> users = new ArrayList<>();
        for (User user: thread.getUsers()) {
            if (!user.isMe()) {
                users.add(new UserHolder(user));
            }
        }
        return users;
    }

    @Override
    public MessageHolder getLastMessage() {
        if (thread.getMessages().isEmpty()) {
            return null;
        }
        return new MessageHolder(thread.lastMessage());
    }

    @Override
    public void setLastMessage(MessageHolder message) {
        System.out.println("Implement this");
    }

    @Override
    public int getUnreadCount() {
        return thread.getUnreadMessagesCount();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ThreadHolder && getId().equals(((ThreadHolder)object).getId());
    }

    public Thread getThread() {
        return thread;
    }

}
