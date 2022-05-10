package sdk.chat.ui.chat.model;

import androidx.annotation.NonNull;

import com.stfalcon.chatkit.commons.models.IDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.ui.ChatSDKUI;
import sdk.guru.common.DisposableMap;

public class ThreadHolder implements IDialog<MessageHolder> {

    protected Thread thread;
    protected List<UserHolder> users = new ArrayList<>();
    protected MessageHolder lastMessage = null;
    protected Integer unreadCount = null;
    protected Date creationDate;
    protected String displayName;
    protected DisposableMap dm = new DisposableMap();

    protected boolean isDirty;

//    protected String typingText = null;

    public ThreadHolder(Thread thread) {
        this.thread = thread;
        creationDate = thread.getCreationDate();
        update();
    }

    public void update() {
        updateLastMessage();
        updateDisplayName();
        updateUsers();
        updateUnreadCount();
    }

    public void checkDirty() {
        updateLastMessage();
        updateDisplayName();
        updateUsers();
        updateUnreadCount();
    }


    public void updateUnreadCount() {
        unreadCount = thread.getUnreadMessagesCount();
    }

    public void updateUsers() {
        users.clear();
        for (User user: thread.getUsers()) {
            if (!user.isMe()) {
                users.add(new UserHolder(user));
            }
        }
    }

    public void updateDisplayName() {
        String newName = thread.getDisplayName();
        if (!isDirty) {
            isDirty = !newName.equals(displayName);
        }
        displayName = newName;
    }

    public void updateLastMessage() {
        Message message = thread.lastMessage();

        if (!isDirty) {
            String lastMessageId = lastMessage != null ? lastMessage.getId() : "";
            String messageId = message != null ? message.getEntityID() : "";
            isDirty = !messageId.equals(lastMessageId);
        }

        if (message != null) {
            lastMessage = ChatSDKUI.shared().getMessageRegistrationManager().onNewMessageHolder(message);
        } else {
            lastMessage = null;
        }
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
        return null;
//
//        String url = thread.getImageUrl();
//        if (url == null) {
//            if (getUsers().size() == 1) {
//                url = getUsers().get(0).getAvatar();
//            }
//        }
//
//        return url;
    }

    @Override
    public String getDialogName() {
        return thread.getDisplayName();
    }

    @Override
    public List<UserHolder> getUsers() {
        if (users == null) {
            updateUsers();
        }
        return users;
    }

    @Override
    public MessageHolder getLastMessage() {
        // TODO: Thread
        if (lastMessage == null) {
            updateLastMessage();
        }
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
            updateUnreadCount();
        }
        return unreadCount;
    }

    public boolean contentsIsEqual(ThreadHolder holder) {
        // Do some null checks
        if (getDialogName() != null && holder.getDialogName() != null) {
            if (!getDialogName().equals(holder.getDialogName())) {
                return false;
            }
        }
        if (getDialogPhoto() != null && holder.getDialogPhoto() != null) {
            if (!getDialogPhoto().equals(holder.getDialogPhoto())) {
                return false;
            }
        }
        if (getLastMessage() != null && holder.getLastMessage() != null) {
            if (!getLastMessage().equals(holder.getLastMessage())) {
                return false;
            }
        }
        if (getUnreadCount() != holder.getUnreadCount()) {
            return false;
        }
        Set<UserHolder> thisUsers = new HashSet<>(getUsers());
        Set<UserHolder> thatUsers = new HashSet<>(holder.getUsers());
        if (!thisUsers.equals(thatUsers)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ThreadHolder && getId().equals(((ThreadHolder)object).getId());
    }

    public Thread getThread() {
        return thread;
    }

    public @NonNull Date getDate() {
        return thread.orderDate();
    }

    public Long getWeight() {
        return thread.getWeight();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void markClean() {
        isDirty = false;
    }

}
