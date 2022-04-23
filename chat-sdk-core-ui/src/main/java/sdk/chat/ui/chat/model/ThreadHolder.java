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

    public ThreadHolder(Thread thread) {
        this.thread = thread;
        creationDate = thread.getCreationDate();

//        dm.add(ChatSDK.events().sourceOnMain()
//                .filter(NetworkEvent.filterType(EventType.MessageUpdated))
//                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
//                .subscribe(networkEvent -> {
//                    updateLastMessage();
//                }));
//
//        dm.add(ChatSDK.events().sourceOnMain()
//                .filter(NetworkEvent.filterType(EventType.MessageReadReceiptUpdated))
//                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
//                .subscribe(networkEvent -> {
//
//                }));
//
//        dm.add(ChatSDK.events().sourceOnMain()
//                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated))
//                .filter(NetworkEvent.filterThreadContainsUser(thread))
//                .subscribe(networkEvent -> {
//
//                }));
//
//        dm.add(ChatSDK.events().sourceOnMain()
//                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
//                .filter(NetworkEvent.filterThreadContainsUser(thread))
//                .subscribe(networkEvent -> {
//                    updateDisplayName();
//                }));
//
//        dm.add(ChatSDK.events().sourceOnMain()
//                .filter(NetworkEvent.filterType(EventType.TypingStateUpdated))
//                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
//                .subscribe(networkEvent -> {
//                }));
//
//        dm.add(ChatSDK.events().sourceOnMain()
//                .filter(NetworkEvent.filterType(EventType.ThreadMetaUpdated))
//                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
//                .subscribe(networkEvent -> {
//                    updateDisplayName();
//                }));
//
//        update();
    }

    public Completable updateAsync() {
        return Completable.complete();
//        return Completable.create(emitter -> {
//            update();
//            emitter.onComplete();
//        }).subscribeOn(RX.computation());
    }

    public void update() {
//        updateLastMessage();
//        updateDisplayName();
//        updateUsers();
//        updateUnreadCount();
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
        updateUsers();
        return users;
    }

    @Override
    public MessageHolder getLastMessage() {
        updateLastMessage();
        return lastMessage;
    }

    @Override
    public void setLastMessage(MessageHolder message) {
        lastMessage = message;
        unreadCount = null;
    }

    @Override
    public int getUnreadCount() {
//        if (unreadCount == null) {
//            unreadCount = thread.getUnreadMessagesCount();
//        }
        updateUnreadCount();
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
