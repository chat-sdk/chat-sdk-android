package app.xmpp.adapter;

import android.content.Context;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.bookmarks.BookmarkManager;
import org.jivesoftware.smackx.bookmarks.BookmarkedConference;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.time.EntityTimeManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.pmw.tinylog.Logger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import app.xmpp.adapter.enums.ConnectionStatus;
import app.xmpp.adapter.listeners.XMPPChatStateListener;
import app.xmpp.adapter.listeners.XMPPMessageListener;
import app.xmpp.adapter.listeners.XMPPPingListener;
import app.xmpp.adapter.listeners.XMPPReceiptReceivedListener;
import app.xmpp.adapter.listeners.XMPPReconnectionListener;
import app.xmpp.adapter.listeners.XMPPRosterListener;
import app.xmpp.adapter.module.XMPPModule;
import app.xmpp.adapter.utils.PresenceHelper;
import app.xmpp.adapter.utils.PublicKeyExtras;
import app.xmpp.adapter.utils.ServerKeyStorage;
import app.xmpp.adapter.utils.XMPPMessageWrapper;
import app.xmpp.adapter.utils.XMPPServer;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.Nullable;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.chat.core.utils.StringChecker;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPManager implements AppBackgroundMonitor.Listener {

    protected ConnectionManager connectionManager = new ConnectionManager(this);

    // The main XMPP connection
    protected AbstractXMPPConnection connection = null;
    protected Long serverDelay;

    // Listeners
    protected XMPPRosterListener rosterListener;
    protected XMPPMessageListener messageListener;
    protected XMPPChatStateListener chatStateListener;
    public XMPPReceiptReceivedListener receiptReceivedListener;
    protected XMPPReconnectionListener reconnectionListener;
    protected XMPPPingListener pingListener;
//    protected OutgoingStanzaQueue outgoingStanzaQueue = new OutgoingStanzaQueue();

    // Managers
    public XMPPUserManager userManager;
    public XMPPMUCManager mucManager;
    public XMPPTypingIndicatorManager typingIndicatorManager;
    public XMPPMamManager mamManager;

    // Smack Managers
    protected UserSearchManager userSearchManager;

    // Singleton setup
    public final static XMPPManager instance = new XMPPManager();
    public static XMPPManager shared() {
        return instance;
    }

    protected DisposableMap dm = new DisposableMap();

    protected XMPPServer server;

//    protected Jid jid;
//    protected String password;

    public String getDomain() {
        return server.domain;
    }

    protected XMPPManager() {

        rosterListener = new XMPPRosterListener(this);

        messageListener = new XMPPMessageListener();
        chatStateListener = new XMPPChatStateListener();
        reconnectionListener = new XMPPReconnectionListener();

        receiptReceivedListener = new XMPPReceiptReceivedListener();

        // Managers
        userManager = new XMPPUserManager(this);
        typingIndicatorManager = new XMPPTypingIndicatorManager();
        mamManager = new XMPPMamManager(this);

        // We accept all roster invitations
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

        // Enable stream management
        // TODO: Check this
        XMPPTCPConnection.setUseStreamManagementDefault(XMPPModule.config().streamManagementEnabled);
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(XMPPModule.config().streamManagementEnabled);

        DeliveryReceiptManager.setDefaultAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
        ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());

        PingManager.setDefaultPingInterval(XMPPModule.config().pingInterval);


        // Enable reconnection
        ReconnectionManager.setEnabledPerDefault(true);

        // Be careful using this because there can be a race condition
        // between this and the login method
        dm.add(connectionManager.connectionStatus().subscribe(connectionStatus -> {
            if (ChatSDK.currentUser() != null) {
                if (connectionStatus == ConnectionStatus.Authenticated) {
                    ChatSDK.currentUser().setIsOnline(true);
                }
                if (connectionStatus == ConnectionStatus.Disconnected) {
                    ChatSDK.currentUser().setIsOnline(false);
                }
            }
        }, throwable -> ChatSDK.events().onError(throwable)));

        getRosterListener().getPresenceEvents().flatMapSingle(presence -> {
            return userManager.updateUserFromVCard(presence.getFrom()).doOnSuccess(user -> PresenceHelper.updateUserFromPresence(user, presence));
        }).ignoreElements().subscribe(ChatSDK.events());

        if(debugModeEnabled()) {
            System.setProperty("smack.debuggerClass","org.jivesoftware.smack.debugger.ConsoleDebugger");
            System.setProperty("smack.debugEnabled", "true");
            SmackConfiguration.DEBUG = true;
        }

//        connection.addAsyncStanzaListener(new StanzaListener() {
//            @Override
//            public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException {
//                Logger.debug(packet.toString());
//            }
//        }, new StanzaTypeFilter(IQ.class));

    }


    public Roster roster() {
        return Roster.getInstanceFor(getConnection());
    }

    public MamManager mamManager() {
        return MamManager.getInstanceFor(getConnection());
    }

    public XMPPMamManager xmppMamManager() {
        return mamManager;
    }

    public ServiceDiscoveryManager serviceDiscoveryManager() {
        return ServiceDiscoveryManager.getInstanceFor(getConnection());
    }

    public LastActivityManager lastActivityManager() {
        return LastActivityManager.getInstanceFor(getConnection());
    }

    public BookmarkManager bookmarkManager() {
        return BookmarkManager.getBookmarkManager(getConnection());
    }

    public EntityTimeManager entityTimeManager() {
        return EntityTimeManager.getInstanceFor(getConnection());
    }

//    public OfflineMessageManager offlineMessageManager() {
//        return OfflineMessageManager.getInstanceFor(getConnection());
//    }

    public PingManager pingManager() {
        return PingManager.getInstanceFor(getConnection());
    }

    public ServerPingWithAlarmManager serverPingWithAlarmManager() {
        return ServerPingWithAlarmManager.getInstanceFor(getConnection());
    }

    public VCardManager vCardManager() {
        return VCardManager.getInstanceFor(getConnection());
    }

    public ChatManager chatManager() {
        return ChatManager.getInstanceFor(getConnection());
    }

    public ChatStateManager chatStateManager() {
        return ChatStateManager.getInstance(getConnection());
    }

    public DeliveryReceiptManager deliveryReceiptManager() {
        return DeliveryReceiptManager.getInstanceFor(getConnection());
    }

    public ReconnectionManager reconnectionManager() {
        return ReconnectionManager.getInstanceFor(getConnection());
    }

    public ConnectionManager connectionManager() {
        return connectionManager;
    }

    public AccountManager accountManager() {
        return AccountManager.getInstance(getConnection());
    }

    public CarbonManager carbonManager() {
        return CarbonManager.getInstanceFor(getConnection());
    }

    public UserSearchManager userSearchManager() {
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

    @Override
    public void didStart() {
        if (getConnection() != null && !getConnection().isConnected()) {
            try {
                if (!getConnection().isConnected()) {
                    getConnection().connect();
                }
                if (!getConnection().isAuthenticated()) {
                    getConnection().login();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void didStop() {
        if (getConnection() != null && getConnection().isConnected()) {
            try {
                getConnection().disconnect(new Presence(Presence.Type.unavailable));
            } catch (Exception e) {

            }
        }
    }

    public boolean isConnectedAndAuthenticated() {
        return isConnected() && connection.isAuthenticated();
    }

    public Single<XMPPConnection> openConnection(final XMPPServer server) {
        return openConnection(server, null, null);
    }

    public Single<XMPPConnection> openConnection(final XMPPServer server, @Nullable final String jid, @Nullable final String password){
        return Single.create((SingleOnSubscribe<XMPPConnection>) e -> {

            if(isConnected()) {
                connection.disconnect();
            }

            XMPPTCPConnectionConfiguration config = configureConnection(server, null, null);
            connection = new XMPPTCPConnection(config);

            connection.addAsyncStanzaListener(packet -> {
                ////
                Logger.info(packet);
            }, new StanzaFilter() {
                @Override
                public boolean accept(Stanza stanza) {
                    return true;
                }
            });

            addListeners();
            mucManager = new XMPPMUCManager(this);

            try {
                connection.connect();

                if (jid != null && password != null) {
                    connection.login(jid, password);
                }

                e.onSuccess(connection);
            }
            catch (Exception exception) {

                if (connection.isConnected()) {
                    connection.instantShutdown();
                }

                String message = exception.getMessage();
                if (message.contains("java.net.SocketTimeoutException")) {
                    e.onError(ChatSDK.getException(R.string.login_failed_timeout));
                } else if (message.contains("not-authorized")) {
                    e.onError(ChatSDK.getException(R.string.username_or_password_incorrect));
                } else if (message.contains("SSL/TLS")) {
                    e.onError(exception);
                } else {
                    e.onError(ChatSDK.getException(password != null ? R.string.login_failed : R.string.registration_failed));
                }
            }
        }).subscribeOn(RX.io());
    }

    protected void removeListeners() {
        if (getConnection() != null) {
            getConnection().removeConnectionListener(connectionManager);
            chatManager().removeIncomingListener(messageListener);
            chatManager().removeOutgoingListener(messageListener);
            chatStateManager().removeChatStateListener(chatStateListener);
            reconnectionManager().removeReconnectionListener(reconnectionListener);
            deliveryReceiptManager().removeReceiptReceivedListener(receiptReceivedListener);
            roster().removeRosterListener(rosterListener);
            carbonManager().removeCarbonCopyReceivedListener(messageListener);
            pingManager().unregisterPingFailedListener(pingListener);
        }
        AppBackgroundMonitor.shared().removeListener(this);
    }

    protected void addListeners() {
        removeListeners();

        getConnection().addConnectionListener(connectionManager);
        chatManager().addIncomingListener(messageListener);
        chatManager().addOutgoingListener(messageListener);
        chatStateManager().addChatStateListener(chatStateListener);
        reconnectionManager().addReconnectionListener(reconnectionListener);
        deliveryReceiptManager().addReceiptReceivedListener(receiptReceivedListener);
        roster().addRosterListener(rosterListener);
        carbonManager().addCarbonCopyReceivedListener(messageListener);
        pingManager().registerPingFailedListener(pingListener);
        AppBackgroundMonitor.shared().addListener(this);
    }

    public Date clientToServerTime(Date date) {
        try {
            return new Date(date.getTime() - getServerDelay());
        }
        catch (Exception e) {
            return date;
        }
    }

    public Date serverToClientTime(Date date) {
        try {
            return new Date(date.getTime() + getServerDelay());
        }
        catch (Exception e) {
            return date;
        }
    }

    public long getServerDelay() throws Exception {
        if (serverDelay == null) {
            Jid jid = getConnection().getXMPPServiceDomain();
            if(entityTimeManager().isTimeSupported(jid)) {
                Date remoteTime = entityTimeManager().getTime(jid).getTime();
                Date localTime = new Date();
                serverDelay = localTime.getTime() - remoteTime.getTime();
            } else {
                serverDelay = 0L;
            }
        }
        return serverDelay;
    }

    public void performPostAuthenticationSetup() {

        sendAvailablePresence();

        userManager.loadContactsFromRoster().subscribe(ChatSDK.events());

        reconnectionManager().setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.FIXED_DELAY);
        reconnectionManager().setFixedDelay(5);
        reconnectionManager().enableAutomaticReconnection();

        serverPingWithAlarmManager().setEnabled(true);

        if (ChatSDK.readReceipts() != null) {
            deliveryReceiptManager().setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
            deliveryReceiptManager().autoAddDeliveryReceiptRequests();
        }

        try {
            if (carbonManager().isSupportedByServer()) {
                carbonManager().enableCarbonsAsync(exception -> ChatSDK.events().onError(exception));
            }
        } catch (Exception e) {
                Logger.warn(e.getLocalizedMessage());
                ChatSDK.events().onError(e);
        }
        try {
            if (mamManager().isSupported()) {
                mamManager().enableMamForAllMessages();
            }
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
        try {
            if (bookmarkManager().isSupported()) {
                List<BookmarkedConference> conferences = new ArrayList<>(bookmarkManager().getBookmarkedConferences());
                for (BookmarkedConference conference: conferences) {
                    mucManager.joinChatFromBookmark(conference);
                }
            }
        }
        catch (Exception e) {
            Logger.warn(e.getLocalizedMessage());
            ChatSDK.events().onError(e);
        }

        ChatSDK.hook().executeHook(HookEvent.DidAuthenticate, new HashMap<String, Object>() {{
            put(HookEvent.User, ChatSDK.currentUser());
        }}).subscribe(ChatSDK.events());

        loadArchiveMessagesSinceLastOnline();
    }

    public void loadArchiveMessagesSinceLastOnline() {
        // Load since the last time we were online
        Date date = null;
        if (ChatSDK.currentUserID() != null) {
            date = connectionManager.getLastOnline(ChatSDK.currentUserID());
        }
        loadArchiveMessages(date);
    }

    public void loadArchiveMessages(Date date) {
        dm.add(mamManager.getMessageArchive(ChatSDK.currentUserID(), date, XMPPModule.config().messageHistoryDownloadLimit).subscribe((messages, throwable) -> {
            if (throwable == null) {
                List<XMPPMessageWrapper> wrappers = new ArrayList<>();
                for(Message message: messages) {
                    // Check if message already exists
                    if (message.getBody() == null || message.getBody().isEmpty()) {
                        Logger.debug("");
                    }
                    XMPPMessageWrapper xmr = new XMPPMessageWrapper(message);
                    xmr.debug();
                    wrappers.add(xmr);
                }
                messageListener.parse(wrappers);
            }
        }));
    }

    private XMPPTCPConnectionConfiguration configureConnection(XMPPServer server, String userAlias, String password) throws Exception {

        boolean compressionEnabled = XMPPModule.shared().config.compressionEnabled;
        String securityModeString = XMPPModule.shared().config.securityMode;

        ConnectionConfiguration.SecurityMode securityMode = ConnectionConfiguration.SecurityMode.valueOf(securityModeString);

        InetAddress hostAddress = InetAddress.getByName(server.address);

        Resourcepart resource = Resourcepart.from(server.resource);
        DomainBareJid domain = JidCreate.domainBareFrom(server.domain);

        this.server = server;

        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(userAlias, password)
                .setXmppDomain(domain)
                .setSecurityMode(securityMode)
                .setHostAddress(hostAddress)
                .setPort(server.port)
                .setResource(resource)
                .setSendPresence(true)
                .setCompressionEnabled(compressionEnabled);

        if(StringChecker.isNullOrEmpty(userAlias) && StringChecker.isNullOrEmpty(password)) {
            builder.allowEmptyOrNullUsernames();
        }

        return builder.build();
    }

    public Completable login(final String userJID, final String password){
        return Completable.defer(() -> {

//            this.jid = JidCreate.from(userJID);
//            this.password = password;

            XMPPServer server = getCurrentServer(ChatSDK.ctx());
            if (server == null) {
                return Completable.error(ChatSDK.getException(R.string.xmpp_server_must_be_specified));
            }
            return openConnection(server, userJID, password).flatMapCompletable(xmppConnection -> {

                if(xmppConnection.isConnected()) {
                    return userManager.updateUserFromVCard(xmppConnection.getUser().asBareJid()).ignoreElement();
                }
                else {
                    return Completable.error(ChatSDK.getException(R.string.cannot_connect));
                }
            });
        }).subscribeOn(RX.io());
    }

    private boolean debugModeEnabled() {
        return XMPPModule.config().debugEnabled;
    }

    public Completable register(final String username, final String password){
        return Completable.defer(() -> {
            XMPPServer server = getCurrentServer(ChatSDK.ctx());
            if (server == null) {
                return Completable.error(ChatSDK.getException(R.string.xmpp_server_must_be_specified));
            }
            return openConnection(server).flatMapCompletable(xmppConnection -> {

                AccountManager accountManager = accountManager();
                if (!accountManager.supportsAccountCreation()) {
//                    getConnection().disconnect();
                    return Completable.error(ChatSDK.getException(R.string.registration_not_supported));
                }
                try {
                    accountManager.sensitiveOperationOverInsecureConnection(true);
                    accountManager.createAccount(Localpart.from(username), password);

                    connection.login(username, password);

                    if(xmppConnection.isConnected()) {
                        return userManager.updateUserFromVCard(xmppConnection.getUser().asBareJid()).ignoreElement();
                    }
                    else {
                        return Completable.error(ChatSDK.getException(R.string.cannot_connect));
                    }
                }
                catch (Exception exception) {
                    if (connection.isConnected()) {
                        connection.instantShutdown();
                    }
//                    return Completable.error(exception);
                    return Completable.error(ChatSDK.getException(R.string.registration_failed));
                }
            });
        }).subscribeOn(RX.io());
    }

    public void logout() {
        removeListeners();

        dm.dispose();

        if(mucManager != null) {
            mucManager.dispose();
        }
        if(userManager != null) {
            userManager.dispose();
        }
        if (getConnection() != null) {
            getConnection().disconnect();
        }
    }

    public void sendUnavailablePresence() {
        if (isConnected()) {
            Presence presence = new Presence(Presence.Type.unavailable);
//            Presence presence = new Presence(Presence.Type.unavailable, null, 1, Presence.Mode.dnd);
            ChatSDK.currentUser().setIsOnline(false);
            sendPresence(presence);
        }
    }

    public void sendAvailablePresence() {
        if (isConnected()) {
            ChatSDK.currentUser().setIsOnline(true);
            Presence presence = PresenceHelper.presenceForUser(ChatSDK.currentUser());
            presence.setStanzaId(UUID.randomUUID().toString());
            sendPresence(presence);
        }
    }

    public void sendPresence(Presence presence) {
        PublicKeyExtras.addTo(presence);
        sendStanza(presence);
    }

    public void sendStanza(Stanza stanza) {
        try {
            getConnection().sendStanza(stanza);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public XMPPTCPConnection getTCPConnection() {
        AbstractXMPPConnection connection = getConnection();
        if (connection instanceof XMPPTCPConnection) {
            return (XMPPTCPConnection) connection;
        }
        return null;
    }

    public boolean isConnected() {
        return getConnection() != null && getConnection().isConnected();
    }

    public static XMPPServer getCurrentServer(Context context) {

        // First get configured server
        XMPPServer server = null;

        if (XMPPModule.config().allowServerConfiguration) {
            ServerKeyStorage storage = new ServerKeyStorage(context);
            server = storage.getServer();
            if (server.isValid()) {
                return server;
            }
        }

        server = XMPPModule.config().getServer();
        if (server.isValid()) {
            return server;
        }

        return null;
    }


    public static void setCurrentServer(Context context, XMPPServer server) {
        // First get configured server
        ServerKeyStorage storage = new ServerKeyStorage(context);
        storage.setServer(server);
    }

}
