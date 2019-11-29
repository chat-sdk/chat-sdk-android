package co.chatsdk.firestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.firebase.FirebaseThreadHandler;
import co.chatsdk.firebase.wrappers.MessageWrapper;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import sdk.chat.micro.MicroChatSDK;

public class FirestoreThreadHandler extends FirebaseThreadHandler {

    public Completable sendMessage(final Message message) {

        if (!message.getMessageType().is(MessageType.Text)) {
            return Completable.error(new Throwable("Only text messages are currently supported"));
        }

        if (!message.getThread().typeIs(ThreadType.Private1to1)) {
            return Completable.error(new Throwable("Only 1 to 1 threads are currently supported"));
        }

        User otherUser = message.getThread().otherUser();

        return MicroChatSDK.shared().sendMessageWithText(otherUser.getEntityID(), message.getText())
                .doOnSuccess(message::setEntityID)
                .ignoreElement();
    }

    @Override
    public Single<Thread> createThread(final String name, final List<User> users) {
        return createThread(name, users, -1);
    }

    @Override
    public Single<Thread> createThread(final String name, final List<User> users, final int type) {
        return createThread(name, users, type, null);
    }

    @Override
    public Single<Thread> createThread(String name, List<User> users, int type, String entityID) {
        return createThread(name, users,type, entityID, null);
    }

    @Override
    public Single<Thread> createThread(String name, List<User> users, int type, String entityID, String imageURL) {
        return Single.create((SingleOnSubscribe<Thread>) e -> {
            ArrayList<User> allUsers = new ArrayList<>();
            allUsers.addAll(users);

            // Make sure that the current user is in the list and
            // that they are not the first item
            allUsers.remove(ChatSDK.currentUser());
            allUsers.add(ChatSDK.currentUser());

            if(allUsers.size() == 2 && (type == -1 || type == ThreadType.Private1to1)) {
                Thread thread = ChatSDK.db().fetchThreadWithUsers(allUsers);
                if(thread == null) {
                    thread = DaoCore.getEntityForClass(Thread.class);
                    DaoCore.createEntity(thread);

                    thread.setEntityID(users.get(0).getEntityID());
                    thread.setCreatorEntityId(ChatSDK.currentUser().getEntityID());
                    thread.setCreationDate(new Date());
                    thread.setType(ThreadType.Private1to1);
                    thread.addUsers(allUsers);
                }
                e.onSuccess(thread);
            } else {
                e.onError(new Throwable("Group threads not supported"));
            }

        }).subscribeOn(Schedulers.single());
    }


}
