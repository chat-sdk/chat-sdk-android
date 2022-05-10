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
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
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

    protected String typingText = null;

    public ThreadHolder(Thread thread) {
        this.thread = thread;
        creationDate = thread.getCreationDate();

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    updateLastMessage();
                }));

                dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageReadReceiptUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    updateUnreadCount();
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .filter(NetworkEvent.filterThreadContainsUser(thread))
                .subscribe(networkEvent -> {
                    updateDisplayName();
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {

                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadMetaUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    updateDisplayName();
                }));

        update();
    }

    public void update() {
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
        displayName = thread.getDisplayName();
    }

    public void updateLastMessage() {
        Message message = thread.lastMessage();
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

//        if (displayName == null || displayName.isEmpty()) {
//            update();
//        }
//        return displayName;
    }

    @Override
    public List<UserHolder> getUsers() {
//        if (users == null) {
//            List<UserHolder> list = new ArrayList<>();
//            for (User user: thread.getUsers()) {
//                if (!user.isMe()) {
//                    list.add(new UserHolder(user));
//                }
//            }
//            users = list;
//        }

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
        if (typingText != null) {
            return 0;
        }
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


}
