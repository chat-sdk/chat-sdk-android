package co.chatsdk.xmpp;

import android.content.Context;

import com.example.chatsdkxmppadapter.R;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.PresenceEventListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntries;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.blocking.BlockingCommandManager;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.xmpp.defines.XMPPDefines;
import co.chatsdk.xmpp.enums.ConnectionStatus;
import co.chatsdk.xmpp.listeners.XMPPChatManagerListener;
import co.chatsdk.xmpp.listeners.XMPPConnectionListener;
import co.chatsdk.xmpp.listeners.XMPPRosterListener;
import co.chatsdk.xmpp.utils.PresenceHelper;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPManager {

    public DomainBareJid serviceName;
    public String serviceHost;
    public int servicePort;
    public DomainBareJid searchService;

    // The main XMPP connection
    private AbstractXMPPConnection connection = null;

    // Listeners
    public XMPPConnectionListener connectionListener;
    private XMPPRosterListener rosterListener;
    private XMPPChatManagerListener chatManagerListener;

    // Managers
    public XMPPUsersManager userManager;
    public XMPPMUCManager mucManager;
    public XMPPTypingIndicatorManager typingIndicatorManager;

    // Smack Managers
    private UserSearchManager userSearchManager;

    // Singleton setup
    public final static XMPPManager instance = new XMPPManager();
    public static XMPPManager shared() {
        return instance;
    }

    private DisposableList disposables = new DisposableList();

    protected XMPPManager() {

        Context context = AppContext.shared().context();

        connectionListener = new XMPPConnectionListener(this);
        rosterListener = new XMPPRosterListener(this);

        chatManagerListener = new XMPPChatManagerListener();

        // Managers
        userManager = new XMPPUsersManager(this);
        typingIndicatorManager = new XMPPTypingIndicatorManager();

        serviceHost = context.getString(R.string.service_host);
        String serviceNameString = context.getString(R.string.service_name);
        servicePort = new Integer(context.getString(R.string.service_port));
        String searchServiceString = context.getString(R.string.search_service);

        try {
            searchService = JidCreate.domainBareFrom(searchServiceString);
            serviceName = JidCreate.domainBareFrom(serviceNameString);
        }
        catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        // We accept all roster invitations
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

        // Enable stream management
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);

        // Enable reconnection
        ReconnectionManager.setEnabledPerDefault(true);

        // Be careful using this because there can be a race condition
        // between this and the login method
        disposables.add(connectionListener.connectionStatusSource.subscribe(new Consumer<ConnectionStatus>() {
            @Override
            public void accept(ConnectionStatus connectionStatus) throws Exception {
                if (connectionStatus == ConnectionStatus.Authenticated) {
                    CarbonManager.getInstanceFor(getConnection()).enableCarbons();
                }
            }
        }));

        // Listen to presence updates and update data accordingly
        disposables.add(getRosterListener().presenceEventSource.subscribe(new Consumer<Presence>() {
            @Override
            public void accept(final Presence presence) throws Exception {
                userManager.updateUserFromVCard(presence.getFrom()).subscribe(new BiConsumer<User, Throwable>() {
                    @Override
                    public void accept(User user, Throwable throwable) throws Exception {
                        PresenceHelper.updateUserFromPresence(user, presence);
                        NM.events().source().onNext(NetworkEvent.userMetaUpdated(user));
                    }
                });
            }
        }));


    }

    public void performPostAuthenticationSetup () {

        android.os.Debug.startMethodTracing("lsd");

        disposables.add(userManager.loadContactsFromRoster().subscribe());
        roster().addRosterListener(rosterListener);

        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addChatListener(chatManagerListener);

        mucManager = new XMPPMUCManager(this);

        for(final Thread thread : NM.thread().getThreads(ThreadType.PrivateGroup)) {
            disposables.add(mucManager.joinRoomWithJID(thread.getEntityID()).subscribe(new BiConsumer<MultiUserChat, Throwable>() {
                @Override
                public void accept(MultiUserChat multiUserChat, Throwable throwable) throws Exception {
                    if(throwable != null) {
                        throwable.printStackTrace();
                    }
                }
            }));
        }

        android.os.Debug.stopMethodTracing();

    }

    public Roster roster() {
        return Roster.getInstanceFor(getConnection());
    }

    public BlockingCommandManager blockingCommandManager () {
        return BlockingCommandManager.getInstanceFor(getConnection());
    }

    public LastActivityManager lastActivityManager () {
        return LastActivityManager.getInstanceFor(getConnection());
    }

    public ChatManager chatManager () {
        return ChatManager.getInstanceFor(getConnection());
    }

    public ChatStateManager chatStateManager () {
        return ChatStateManager.getInstance(getConnection());
    }

    public UserSearchManager userSearchManager () {
        if(userSearchManager == null) {
            userSearchManager = new UserSearchManager(getConnection());
        }
        return userSearchManager;
    }

    // Getters and setters
    public XMPPRosterListener getRosterListener() {
        return rosterListener;
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    public boolean isConnectedAndAuthenticated() {
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

    public Single<XMPPConnection> openRegistrationConnection(){
        return Single.create(new SingleOnSubscribe<XMPPConnection>() {
            @Override
            public void subscribe(final SingleEmitter<XMPPConnection> e) throws Exception {

                if(connection != null && connection.isConnected()) {
                    connection.removeConnectionListener(connectionListener);
                    connection.disconnect();
                }

                XMPPTCPConnectionConfiguration config = configureRegistrationConnection();
                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(50000);

                try {
                    connection.connect();
                    e.onSuccess(connection);
                }
                catch (Exception exception){
                    connection.disconnect();
                    e.onError(exception);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public Single<XMPPConnection> openConnection(final String jid, final String password){
        return Single.create(new SingleOnSubscribe<XMPPConnection>() {
            @Override
            public void subscribe(final SingleEmitter<XMPPConnection> e) throws Exception {

                if(connection != null && connection.isConnected()) {
                    connection.removeConnectionListener(connectionListener);
                    connection.disconnect();
                }

                XMPPTCPConnectionConfiguration config = configureConnection(jid, password);

                connection = new XMPPTCPConnection(config);
                connection.setPacketReplyTimeout(50000);

                connection.addConnectionListener(connectionListener);

                try {
                    connection.connect();
                    connection.login();
                    e.onSuccess(connection);
                }
                catch (Exception exception) {
                    connection.disconnect();
                    e.onError(exception);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private XMPPTCPConnectionConfiguration configureRegistrationConnection() {
        XMPPTCPConnectionConfiguration connectionConfig;
        connectionConfig = XMPPTCPConnectionConfiguration.builder()
                .allowEmptyOrNullUsernames()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setDebuggerEnabled(true)
                .setServiceName(serviceName)
                .setHost(serviceHost)
                .setPort(servicePort)
                .build();

        return connectionConfig;
    }

    private XMPPTCPConnectionConfiguration configureConnection(String userAlias, String password){

        XMPPTCPConnectionConfiguration connectionConfig;
        connectionConfig = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(userAlias, password)
                .setXmppDomain(serviceName)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setHost(serviceHost)
                .setPort(servicePort)
                .build();

        return connectionConfig;
    }

    public Completable login (final String userJID, final String password){
        return openConnection(userJID, password).flatMapCompletable(new Function<XMPPConnection, Completable>() {
            @Override
            public Completable apply(XMPPConnection xmppConnection) throws Exception {

                if(xmppConnection.isConnected()) {
                    return userManager.updateUserFromVCard(JidCreate.bareFrom(userJID)).toCompletable();
                }
                else {
                    return Completable.error(new Throwable("Connection is not connected"));
                }
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable register(final String username, final String password){
        return openRegistrationConnection().flatMapCompletable(new Function<XMPPConnection, Completable>() {
            @Override
            public Completable apply(XMPPConnection xmppConnection) throws Exception {

                AccountManager accountManager = AccountManager.getInstance(getConnection());
                if (!accountManager.supportsAccountCreation()) {
                    getConnection().disconnect();
                    return Completable.error(new Exception("Server does not support account creation"));
                }
                try {
                    accountManager.sensitiveOperationOverInsecureConnection(true);
                    accountManager.createAccount(Localpart.from(username), password);
                    return Completable.complete();
                }
                catch (Exception exception) {
                    getConnection().disconnect();
                    return Completable.error(exception);
                }
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public void logout () {
        getConnection().removeConnectionListener(connectionListener);
        roster().removeRosterListener(rosterListener);
        chatManager().removeChatListener(chatManagerListener);

        disposables.dispose();
        mucManager.dispose();
        userManager.dispose();

        getConnection().disconnect();
    }

    public void goOnline (User user) {
        Presence presence = PresenceHelper.presenceForUser(user);
        try {
            getConnection().sendStanza(presence);
        }
        catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
