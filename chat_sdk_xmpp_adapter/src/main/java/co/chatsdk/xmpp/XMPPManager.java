package co.chatsdk.xmpp;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.example.chatsdkxmppadapter.R;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.blocking.BlockingCommandManager;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import co.chatsdk.core.ChatSDK;
import co.chatsdk.core.NM;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.xmpp.enums.ConnectionStatus;
import co.chatsdk.xmpp.listeners.XMPPChatManagerListener;
import co.chatsdk.xmpp.listeners.XMPPConnectionListener;
import co.chatsdk.xmpp.listeners.XMPPRosterListener;
import co.chatsdk.xmpp.utils.PresenceHelper;
import io.reactivex.Completable;
import io.reactivex.Observer;
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

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPManager {

    public DomainBareJid serviceName;
    public String serviceHost;
    public int servicePort;
    public DomainBareJid searchService;
    public InetAddress serviceAddress;

    public Resourcepart resource;

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

        connectionListener = new XMPPConnectionListener(this);
        rosterListener = new XMPPRosterListener(this);

        chatManagerListener = new XMPPChatManagerListener();

        // Managers
        userManager = new XMPPUsersManager(this);
        typingIndicatorManager = new XMPPTypingIndicatorManager();

        serviceHost = ChatSDK.shared().xmppServiceHost();
        String serviceNameString = ChatSDK.shared().xmppServiceName();
        servicePort = ChatSDK.shared().xmppServicePort();
        String searchServiceString = ChatSDK.shared().xmppSearchService();
        try {
            serviceAddress = InetAddress.getByName(serviceHost);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String resourceName = ChatSDK.shared().xmppResource();

        try {
            resource = Resourcepart.from(resourceName);
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
        connectionListener.connectionStatusSource.subscribe(new Observer<ConnectionStatus>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposables.add(d);
            }

            @Override
            public void onNext(@NonNull ConnectionStatus connectionStatus) {
                if (connectionStatus == ConnectionStatus.Authenticated) {
                    try {
                        CarbonManager.getInstanceFor(getConnection()).enableCarbons();
                    }
                    catch (XMPPException e) {
                        e.printStackTrace();
                    }
                    catch (SmackException e) {
                        e.printStackTrace();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {}
        });

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

        if(debugModeEnabled()) {
            System.setProperty("smack.debuggerClass","org.jivesoftware.smack.debugger.ConsoleDebugger");
            System.setProperty("smack.debugEnabled", "true");
            SmackConfiguration.DEBUG = true;
        }

    }

    public boolean canWriteOnExternalStorage() {
        // get the state of your external storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // if storage is mounted return true
            return true;
        }
        return false;
    }

    public void performPostAuthenticationSetup () {

        disposables.add(userManager.loadContactsFromRoster().subscribe());
        roster().addRosterListener(rosterListener);

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

                chatManager().addChatListener(chatManagerListener);

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

                chatManager().addChatListener(chatManagerListener);

                try {
                    connection.connect();
                    connection.login();
                    e.onSuccess(connection);
                }
                catch (Exception exception) {
                    connection.disconnect();
                    // TODO: Localize
                    e.onError(new Throwable("Failed to open connection to server"));
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private XMPPTCPConnectionConfiguration configureRegistrationConnection() {
        return XMPPTCPConnectionConfiguration.builder()
                .allowEmptyOrNullUsernames()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setDebuggerEnabled(true)
                .setXmppDomain(serviceName)
                .setHostAddress(serviceAddress)
                .setPort(servicePort)
                .setResource(resource)
                .setDebuggerEnabled(debugModeEnabled()).build();
    }

    private XMPPTCPConnectionConfiguration configureConnection(String userAlias, String password){
        return XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(userAlias, password)
                .setXmppDomain(serviceName)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setHostAddress(serviceAddress)
                .setPort(servicePort)
                .setResource(resource)
                .setDebuggerEnabled(debugModeEnabled())
                .build();
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

    private boolean debugModeEnabled () {
        return ChatSDK.shared().xmppDebugModeEnabled();
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
