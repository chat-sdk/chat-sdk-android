package co.chatsdk.xmpp;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.DisposableListenerList;
import co.chatsdk.xmpp.listeners.XMPPChatMessageListener;
import co.chatsdk.xmpp.listeners.XMPPChatParticipantListener;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ben on 8/8/17.
 * https://github.com/igniterealtime/Smack/blob/master/documentation/extensions/muc.md
 */

public class XMPPMUCManager {

    private WeakReference<XMPPManager> manager;

    private MultiUserChatManager chatManager;
    private HashMap<MultiUserChat, HashMap<String, String>> userLookup = new HashMap<>();

    private DisposableList disposables = new DisposableList();
    private DisposableListenerList disposableListeners = new DisposableListenerList();

    public XMPPMUCManager (XMPPManager manager_) {
        manager = new WeakReference<>(manager_);
        chatManager = MultiUserChatManager.getInstanceFor(manager.get().getConnection());
        chatManager.addInvitationListener(new InvitationListener() {
            @Override
            public void invitationReceived(XMPPConnection conn, MultiUserChat room, EntityJid inviter, String reason,
                                           String password, Message message, MUCUser.Invite invitation) {

                final Thread thread = threadForRoomID(room.getRoom().toString());
                joinRoom(room).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onComplete() {
                        NetworkEvent event = NetworkEvent.privateThreadAdded(thread);
                        NM.events().source().onNext(event);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        DaoCore.deleteEntity(thread);
                    }
                });
            }
        });
    }


    public Single<Thread> createRoom (final String name, final String description, final ArrayList<User> users) {
        return Single.create(new SingleOnSubscribe<Thread>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<Thread> e) throws Exception {
                // Create a new group chat
                final String roomID = getRandomRoomID();
                disposables.add(joinRoomWithJID(roomID).subscribe(new BiConsumer<MultiUserChat, Throwable>() {
                    @Override
                    public void accept(MultiUserChat multiUserChat, Throwable throwable) throws Exception {

                        Form configurationForm = multiUserChat.getConfigurationForm();
                        Form form = configurationForm.createAnswerForm();

                        for(FormField f : form.getFields()) {
                            Timber.v(f.getVariable());
                            if(f.getVariable().equals("muc#roomconfig_persistentroom")) {
                                form.setAnswer(f.getVariable(), true);
                            }
                            if(f.getVariable().equals("muc#roomconfig_publicroom")) {
                                form.setAnswer(f.getVariable(), true);
                            }
                            if(f.getVariable().equals("muc#roomconfig_maxusers")) {
                                List<String> list = new ArrayList<>();
                                list.add("200");
                                form.setAnswer(f.getVariable(), list);
                            }
                            if(f.getVariable().equals("muc#roomconfig_whois")) {
                                List<String> list = new ArrayList<>();
                                list.add("anyone");
                                form.setAnswer(f.getVariable(), list);
                            }
                            if(f.getVariable().equals("muc#roomconfig_membersonly")) {
                                form.setAnswer(f.getVariable(), true);
                            }
                            if(f.getVariable().equals("muc#roomconfig_roomname")) {
                                form.setAnswer(f.getVariable(), name);
                            }
                            if(f.getVariable().equals("muc#roomconfig_roomdesc")) {
                                form.setAnswer(f.getVariable(), description);
                            }
                        }

                        multiUserChat.sendConfigurationForm(form);

                        ArrayList<Completable> comp = new ArrayList<>();
                        for(User user: users) {
                            comp.add(inviteUser(user, roomID));
                        }

                        disposables.add(Completable.merge(comp).doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                e.onError(throwable);
                            }
                        }).subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                Thread thread = threadForRoomID(roomID);
                                thread.setName(name);
                                NetworkEvent event = NetworkEvent.privateThreadAdded(thread);
                                NM.events().source().onNext(event);
                                e.onSuccess(thread);
                            }
                        }));
                    }
                }));

            }
        }).subscribeOn(Schedulers.single());
    }

    public Single<MultiUserChat> joinRoomWithJID (final String roomJID) {
        return Single.create(new SingleOnSubscribe<MultiUserChat>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<MultiUserChat> e) throws Exception {
                final MultiUserChat chat = chatManager.getMultiUserChat(JidCreate.entityBareFrom(roomJID));
                joinRoom(chat).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onComplete() {
                        e.onSuccess(chat);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }
                });

            }
        }).subscribeOn(Schedulers.single());
    }

    public Completable joinRoom (final MultiUserChat chat) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {

                Jid jid = JidCreate.bareFrom(NM.currentUser().getEntityID());
                Thread thread = threadForRoomID(chat.getRoom().toString());
                thread.addUser(NM.currentUser());

                XMPPChatMessageListener chatMessageListener = new XMPPChatMessageListener(XMPPMUCManager.this, chat);
                disposableListeners.add(chatMessageListener);

                XMPPChatParticipantListener chatParticipantListener = new XMPPChatParticipantListener(XMPPMUCManager.this, chat);
                disposableListeners.add(chatParticipantListener);

                chat.addMessageListener(chatMessageListener);

                chat.addPresenceInterceptor(new PresenceListener() {
                    @Override
                    public void processPresence(Presence presence) {
                        Timber.v("presence: " + presence.toString());
                    }
                });

                chat.addParticipantListener(chatParticipantListener);

                // Get the username from the JID
                Localpart local = jid.getLocalpartOrNull();
                if(local != null) {
                    Resourcepart resourcepart = Resourcepart.from(local.toString());

                    chat.join(chat.getEnterConfigurationBuilder(resourcepart).build());
                }

                e.onComplete();
            }
        }).subscribeOn(Schedulers.single());
    }

    public Completable inviteUser (final User user, final String threadID) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                MultiUserChat chat = chatManager.getMultiUserChat(JidCreate.entityBareFrom(threadID));
                chat.invite(JidCreate.entityBareFrom(user.getEntityID()), "");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.single());
    }

    private String getRandomRoomID () {
        return UUID.randomUUID().toString() + "@conference." + XMPPManager.shared().serviceName;
    }

    public Thread threadForRoomID (String roomJID) {
        Thread thread = StorageManager.shared().fetchThreadWithEntityID(roomJID);
        if(thread == null) {
            thread = DaoCore.getEntityForClass(Thread.class);
            DaoCore.createEntity(thread);

            thread.setEntityID(roomJID);
            thread.setCreatorEntityId(NM.currentUser().getEntityID());
            thread.setCreationDate(new Date());
            thread.setType(ThreadType.PrivateGroup);
            DaoCore.updateEntity(thread);
        }
        return thread;
    }

    public MultiUserChat chatForThreadID (String threadID) {
        try {
            return chatManager.getMultiUserChat(JidCreate.entityBareFrom(threadID));
        }
        catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addUserToLookup (MultiUserChat room, String userRoomID, String userJID) {
        HashMap<String, String> map = lookupMapForRoom(room);
        map.put(userRoomID, userJID);
    }

    public void removeUserFromLookup (MultiUserChat room, String userRoomID) {
        HashMap<String, String> map = lookupMapForRoom(room);
        map.remove(userRoomID);
    }

    public String userJID(MultiUserChat room, String userRoomID) {
        return lookupMapForRoom(room).get(userRoomID);
    }

    private HashMap<String, String> lookupMapForRoom (MultiUserChat chat) {
        HashMap<String, String> map = userLookup.get(chat);
        if(map == null) {
            map = new HashMap<>();
            userLookup.put(chat, map);
        }
        return map;
    }

    public void dispose () {
        disposables.dispose();
        disposableListeners.dispose();
    }
}
