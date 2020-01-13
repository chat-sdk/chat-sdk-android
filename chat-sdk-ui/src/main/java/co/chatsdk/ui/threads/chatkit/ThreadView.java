package co.chatsdk.ui.threads.chatkit;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;

public class ThreadView implements IDialog {

    Thread thread;

    public ThreadView(Thread thread) {
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
    public List<? extends IUser> getUsers() {
        ArrayList<UserView> users = new ArrayList<>();
        for (User user: thread.getUsers()) {
            users.add(new UserView(user));
        }
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        return new MessageView(thread.lastMessage());
    }

    @Override
    public void setLastMessage(IMessage message) {
        System.out.println("Implement this");
    }

    @Override
    public int getUnreadCount() {
        return thread.getUnreadMessagesCount();
    }
}
