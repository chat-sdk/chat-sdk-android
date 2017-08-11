package co.chatsdk.xmpp;

import android.content.Context;

import com.example.chatsdkxmppadapter.R;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.search.UserSearchManager;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.xmpp.defines.XMPPDefines;
import co.chatsdk.xmpp.enums.ConnectionStatus;
import co.chatsdk.xmpp.listeners.XMPPChatManagerListener;
import co.chatsdk.xmpp.listeners.XMPPConnectionListener;
import co.chatsdk.xmpp.listeners.XMPPRosterListener;
import co.chatsdk.xmpp.utils.JID;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPManager {

    public String serviceName;
    public String serviceHost;
    public int servicePort;
    public String searchService;

    // The main XMPP connection
    private AbstractXMPPConnection connection = null;

    // Listeners
    public XMPPConnectionListener connectionListener;
    private XMPPRosterListener rosterListener;
    private XMPPChatManagerListener chatManagerListener;

    // Managers
    public XMPPUsersManager userManager;
    public XMPPMUCManager mucManager;

    // Smack Managers
    private UserSearchManager userSearchManager;

    // Singleton setup
    public final static XMPPManager instance = new XMPPManager();
    public static XMPPManager shared() {
        return instance;
    }

    protected XMPPManager() {

        Context context = AppContext.shared().context();

        connectionListener = new XMPPConnectionListener(this);
        rosterListener = new XMPPRosterListener(this);

        chatManagerListener = new XMPPChatManagerListener();

        // Managers
        userManager = new XMPPUsersManager(this);

        serviceHost = context.getString(R.string.service_host);
        serviceName = context.getString(R.string.service_name);
        servicePort = new Integer(context.getString(R.string.service_port));
        searchService = context.getString(R.string.search_service);

        // We accept all roster invitations
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

        // Enable stream management
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);

        // Enable reconnection
        ReconnectionManager.setEnabledPerDefault(true);

        // Be careful using this because there can be a race condition
        // between this and the login method
        connectionListener.connectionStatusSource.subscribe(new Consumer<ConnectionStatus>() {
            @Override
            public void accept(ConnectionStatus connectionStatus) throws Exception {
                if (connectionStatus == ConnectionStatus.Authenticated) {
                    CarbonManager.getInstanceFor(getConnection()).enableCarbons();
                }
            }
        });

        // Listen to presence updates and update data accordingly
        getRosterListener().presenceEventSource.subscribe(new Consumer<Presence>() {
            @Override
            public void accept(final Presence presence) throws Exception {
                JID jid = new JID(presence.getFrom());

                userManager.updateUserFromVCard(jid).subscribe(new BiConsumer<User, Throwable>() {
                    @Override
                    public void accept(User user, Throwable throwable) throws Exception {
                        // TODO: Notify the global event bus
                        if(presence.getType().toString().equalsIgnoreCase(XMPPDefines.Unavailable)){
                            user.setOnline(false);
                        } else {
                            user.setOnline(true);
                        }
                        user.setAvailability(presence.getType().toString());
                        user.setStatus(presence.getStatus());
                        user.setStatus(presence.getMode().toString());
                    }
                });
            }
        });


    }

    public void performPostAuthenticationSetup () {

        userManager.loadContactsFromRoster();
        roster().addRosterListener(rosterListener);

        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addChatListener(chatManagerListener);

        mucManager = new XMPPMUCManager(this);

        for(final Thread thread : NM.thread().getThreads(ThreadType.PrivateGroup)) {
            mucManager.joinRoomWithJID(thread.getEntityID()).subscribe(new BiConsumer<MultiUserChat, Throwable>() {
                @Override
                public void accept(MultiUserChat multiUserChat, Throwable throwable) throws Exception {
                    if(throwable != null) {
                        throwable.printStackTrace();
                    }
                }
            });
        }
    }

    public Roster roster() {
        return Roster.getInstanceFor(getConnection());
    }

    public ChatManager chatManager () {
        return ChatManager.getInstanceFor(getConnection());
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
                .setServiceName(serviceName)
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
                    return userManager.updateUserFromVCard(new JID(userJID)).toCompletable();
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
                    accountManager.createAccount(username, password);
                    return Completable.complete();
                }
                catch (Exception exception) {
                    getConnection().disconnect();
                    return Completable.error(exception);
                }
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public void logout(){
        getConnection().disconnect();
    }

    public void goOnline (User user) {

    }

}
