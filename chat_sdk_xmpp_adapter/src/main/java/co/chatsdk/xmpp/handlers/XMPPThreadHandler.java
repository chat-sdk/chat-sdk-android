package co.chatsdk.xmpp.handlers;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.XMPPMessageBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPThreadHandler extends AbstractThreadHandler {

    @Override
    public Single<Thread> createThread(final String name, final List<User> users) {
        return Single.create(new SingleOnSubscribe<Thread>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<Thread> e) throws Exception {
                ArrayList<User> allUsers = new ArrayList<>();
                allUsers.addAll(users);

                // Make sure that the current user is in the list and
                // that they are not the first item
                allUsers.remove(NM.currentUser());
                allUsers.add(NM.currentUser());

                if(allUsers.size() == 2) {
                    Thread thread = StorageManager.shared().fetchThreadWithUsers(allUsers);
                    if(thread == null) {
                        thread = DaoCore.getEntityForClass(Thread.class);
                        DaoCore.createEntity(thread);

                        thread.setEntityID(users.get(0).getEntityID());
                        thread.setCreatorEntityId(NM.currentUser().getEntityID());
                        thread.setCreationDate(new Date());
                        thread.setType(ThreadType.Private1to1);
                        thread.addUsers(allUsers);
                    }
                    e.onSuccess(thread);
                }
                else if (allUsers.size() > 2) {
                    allUsers.remove(NM.currentUser());
                    XMPPManager.shared().mucManager.createRoom(name, "", allUsers).subscribe(new BiConsumer<Thread, Throwable>() {
                        @Override
                        public void accept(Thread thread, Throwable throwable) throws Exception {
                            if(throwable == null) {
                                e.onSuccess(thread);
                            }
                            else {
                                e.onError(throwable);
                            }
                        }
                    });
                }
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable removeUsersFromThread(Thread thread, List<User> users) {
        return Completable.error(new Throwable("Method not implemented"));
    }

    @Override
    public Completable addUsersToThread(Thread thread, List<User> users) {
        return Completable.error(new Throwable("Method not implemented"));
    }

    @Override
    public Completable deleteThread(Thread thread) {
        return Completable.error(new Throwable("Method not implemented"));
    }

    @Override
    public Completable leaveThread(Thread thread) {
        return Completable.error(new Throwable("Method not implemented"));
    }

    @Override
    public Completable joinThread(Thread thread) {
        return Completable.error(new Throwable("Method not implemented"));
    }

    @Override
    public Completable sendMessage(final Message message) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {

                XMPPMessageBuilder builder = new XMPPMessageBuilder()
                        .setType(message.getType())
                        .setValues(message.values())
                        .setEntityID(message.getEntityID())
                        .setBody(message.getTextString());

                if(message.getType() == Message.Type.LOCATION) {
                    builder.setLocation(message.getLocation());
                }
                if(message.getType() == Message.Type.IMAGE) {
                    builder.setBody((String) message.valueForKey(Keys.MessageImageURL));
                }

                if(message.getThread().getType() == ThreadType.Private1to1) {
                    ChatManager chatManager = XMPPManager.shared().chatManager();
                    Chat chat = chatManager.getThreadChat(message.getThread().getEntityID());
                    if(chat == null) {
                        chat = chatManager.createChat(JidCreate.entityBareFrom(message.getThread().getEntityID()));
                    }
                    chat.sendMessage(builder.build());
                }
                else if (message.getThread().getType() == ThreadType.PrivateGroup) {
                    MultiUserChat chat = XMPPManager.shared().mucManager.chatForThreadID(message.getThread().getEntityID());
                    if(chat != null) {
                        chat.sendMessage(builder.build());
                    }
                    else {
                        e.onError(new Throwable("Unable send message to group chat"));
                    }
                }

                // TODO:  Check this
                message.setDelivered(Message.Delivered.Yes);

                NetworkEvent event = NetworkEvent.messageAdded(message.getThread(), message);
                NM.events().source().onNext(event);

                e.onComplete();

            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void sendLocalSystemMessage(String text, Thread thread) {

    }

    @Override
    public void sendLocalSystemMessage(String text, CoreHandler.bSystemMessageType type, Thread thread) {

    }

    @Override
    public Completable pushThread(Thread thread) {
        return null;
    }


}
