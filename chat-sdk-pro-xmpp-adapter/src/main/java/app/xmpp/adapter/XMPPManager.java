package app.xmpp.adapter;

import android.content.Context;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.ChatManager;
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
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.time.EntityTimeManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import app.xmpp.adapter.enums.ConnectionStatus;
import app.xmpp.adapter.listeners.XMPPCarbonCopyReceivedListener;
import app.xmpp.adapter.listeners.XMPPChatStateListener;
import app.xmpp.adapter.listeners.XMPPConnectionListener;
import app.xmpp.adapter.listeners.XMPPMessageListener;
import app.xmpp.adapter.listeners.XMPPPingListener;
import app.xmpp.adapter.listeners.XMPPReceiptReceivedListener;
import app.xmpp.adapter.listeners.XMPPReconnectionListener;
import app.xmpp.adapter.listeners.XMPPRosterListener;
import app.xmpp.adapter.module.XMPPModule;
import app.xmpp.adapter.utils.PresenceHelper;
import app.xmpp.adapter.utils.PublicKeyExtras;
import app.xmpp.adapter.utils.ServerKeyStorage;
import app.xmpp.adapter.utils.XMPPServer;
import io.reactivex.Completable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.StringChecker;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPManager {

    public static String RESOURCE = "co.chatsdk.resource";

    // The main XMPP connection
    private AbstractXMPPConnection connection = null;

    // Listeners
    public XMPPConnectionListener connectionListener;
    protected XMPPRosterListener rosterListener;
    protected XMPPMessageListener messageListener;
    protected XMPPChatStateListener chatStateListener;
    protected XMPPReceiptReceivedListener receiptReceivedListener;
    protected XMPPReconnectionListener reconnectionListener;
    protected XMPPCarbonCopyReceivedListener carbonCopyReceivedListener;
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

    public String getDomain() {
        return server.domain;
    }

    protected XMPPManager() {

        connectionListener = new XMPPConnectionListener(this);
        rosterListener = new XMPPRosterListener(this);

        messageListener = new XMPPMessageListener();
        chatStateListener = new XMPPChatStateListener();
        reconnectionListener = new XMPPReconnectionListener();

        receiptReceivedListener = new XMPPReceiptReceivedListener();
        carbonCopyReceivedListener = new XMPPCarbonCopyReceivedListener();

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
        connectionListener.connectionStatusSource.subscribe(new Observer<ConnectionStatus>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                dm.add(d);
            }

            @Override
            public void onNext(@NonNull ConnectionStatus connectionStatus) {
                if (ChatSDK.currentUser() != null) {
                    if (connectionStatus == ConnectionStatus.Authenticated) {
                        ChatSDK.currentUser().setIsOnline(true);
                    }
                    if (connectionStatus == ConnectionStatus.Disconnected) {
                        ChatSDK.currentUser().setIsOnline(false);
                    }
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                ChatSDK.events().onError(e);
            }

            @Override
            public void onComplete() {}
        });

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

    public PingManager pingManager() {
        return PingManager.getInstanceFor(getConnection());
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

    public boolean isConnectedAndAuthenticated() {
        return isConnected() && connection.isAuthenticated();
    }

    public Single<XMPPConnection> openRegistrationConnection(XMPPServer server) {
        return Single.create((SingleOnSubscribe<XMPPConnection>) e -> {

            if(isConnected()) {
                connection.disconnect();
            }

            XMPPTCPConnectionConfiguration config = configureRegistrationConnection(server);
            connection = new XMPPTCPConnection(config);

            addListeners();

            try {
                connection.connect();
                e.onSuccess(connection);
            }
            catch (Exception exception){
                if (connection.isConnected()) {
                    connection.disconnect();
                }
                e.onError(exception);
            }
        }).subscribeOn(RX.io());
    }

    public Single<XMPPConnection> openConnection(final XMPPServer server, final String jid, final String password){
        return Single.create((SingleOnSubscribe<XMPPConnection>) e -> {

            if(isConnected()) {
                connection.disconnect();
            }

            XMPPTCPConnectionConfiguration config = configureConnection(server, jid, password);

            connection = new XMPPTCPConnection(config);


//            connection.removeConnectionListener(outgoingStanzaQueue);
//            connection.addConnectionListener(outgoingStanzaQueue);

            addListeners();

            mucManager = new XMPPMUCManager(this);

            try {
                connection.connect();
                connection.login();
                e.onSuccess(connection);
            }
            catch (Exception exception) {
                if (connection.isConnected()) {
                    connection.disconnect();
                }
                e.onError(ChatSDK.getException(R.string.username_or_password_incorrect));
            }
        }).subscribeOn(RX.io());
    }

    private void addListeners() {

        connection.removeConnectionListener(connectionListener);
        connection.addConnectionListener(connectionListener);

        chatManager().removeIncomingListener(messageListener);
        chatManager().addIncomingListener(messageListener);

        chatManager().removeOutgoingListener(messageListener);
        chatManager().addOutgoingListener(messageListener);

        chatStateManager().removeChatStateListener(chatStateListener);
        chatStateManager().addChatStateListener(chatStateListener);

        reconnectionManager().removeReconnectionListener(reconnectionListener);
        reconnectionManager().addReconnectionListener(reconnectionListener);

        deliveryReceiptManager().removeReceiptReceivedListener(receiptReceivedListener);
        deliveryReceiptManager().addReceiptReceivedListener(receiptReceivedListener);

        roster().removeRosterListener(rosterListener);
        roster().addRosterListener(rosterListener);

        carbonManager().removeCarbonCopyReceivedListener(carbonCopyReceivedListener);
        carbonManager().addCarbonCopyReceivedListener(carbonCopyReceivedListener);

        pingManager().unregisterPingFailedListener(pingListener);
        pingManager().registerPingFailedListener(pingListener);
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
        // If there is a difference between the server and local time...
        Date remoteTime = XMPPManager.shared().entityTimeManager().getTime(getConnection().getXMPPServiceDomain()).getTime();
        Date localTime = new Date();

        // Difference
        return localTime.getTime() - remoteTime.getTime();
    }

    public void performPostAuthenticationSetup() {

        XMPPManager.shared().sendOnlinePresence();

        userManager.loadContactsFromRoster().subscribe(ChatSDK.events());

        reconnectionManager().setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.FIXED_DELAY);
        reconnectionManager().setFixedDelay(5);

        if (ChatSDK.readReceipts() != null) {
            deliveryReceiptManager().setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
            deliveryReceiptManager().autoAddDeliveryReceiptRequests();
        }

        try {
            if (carbonManager().isSupportedByServer()) {
                carbonManager().enableCarbonsAsync(exception -> ChatSDK.events().onError(exception));
            }
        }
        catch (Exception e) {
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
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }

        ChatSDK.hook().executeHook(HookEvent.DidAuthenticate, new HashMap<String, Object>() {{
            put(HookEvent.User, ChatSDK.currentUser());
        }}).subscribe(ChatSDK.events());

        if (ChatSDK.thread().getThreads(ThreadType.Private1to1).isEmpty()) {
            dm.add(mamManager.getMessageArchive(ChatSDK.currentUserID(), XMPPModule.config().messageHistoryDownloadLimit).subscribe((messages, throwable) -> {
                if (throwable == null) {
                    XMPPMessageParser.parse(messages);
                }
            }));
        }
    }

    private XMPPTCPConnectionConfiguration configureRegistrationConnection(XMPPServer server) throws Exception {
        return configureConnection(server, null, null);
    }

    private XMPPTCPConnectionConfiguration configureConnection(XMPPServer server, String userAlias, String password) throws Exception {

//        boolean sslEnabled = XMPPModule.config().xmppSslEnabled;
//        boolean acceptAllCertificates = XMPPModule.shared().config.xmppAcceptAllCertificates;
//        boolean allowClientSideAuthentication = XMPPModule.shared().config.xmppAllowClientSideAuthentication;
//        boolean disableHostNameVerification = XMPPModule.shared().config.xmppDisableHostNameVerification;
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
                //.setHost(domainString)
                .setPort(server.port)
                .setResource(resource)
                .setCompressionEnabled(compressionEnabled);

//        builder.setHostnameVerifier(new HostnameVerifier() {
//            @Override
//            public boolean verify(String s, SSLSession sslSession) {
//                return false;
//            }
//        });

        if(StringChecker.isNullOrEmpty(userAlias) && StringChecker.isNullOrEmpty(password)) {
            builder.allowEmptyOrNullUsernames();
        }

//        if(securityMode != ConnectionConfiguration.SecurityMode.disabled && acceptAllCertificates) {
//            TLSUtils.acceptAllCertificates(builder);
//        }
//        if(securityMode != ConnectionConfiguration.SecurityMode.disabled && disableHostNameVerification) {
//            TLSUtils.disableHostnameVerificationForTlsCertificates(builder);
//        }
//        if(securityMode != ConnectionConfiguration.SecurityMode.disabled && sslEnabled) {
//            builder.setPort( 5223 );
//            builder.setSocketFactory(new DummySSLSocketFactory());
//        }
//        if(securityMode != ConnectionConfiguration.SecurityMode.disabled && !sslEnabled) {
//
//            //builder.setEnabledSSLProtocols(new String[]{"TSLv1"});
//
//            SSLContext sc = SSLContext.getInstance("TLS");
//            MemorizingTrustManager mtm = new MemorizingTrustManager(ChatSDK.shared().context());
//            sc.init(null, new X509TrustManager[] { mtm }, new java.security.SecureRandom());
//            builder.setCustomSSLContext(sc);
//            builder.setHostnameVerifier(mtm.wrapHostnameVerifier(new org.apache.http.conn.ssl.StrictHostnameVerifier()));
//
////            SSLContext context = SSLContext.getInstance("TLS");
////            sc.init(null, MemorizingTrustManager.getInstanceList(context), new SecureRandom());
////
////            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
////
//////            FileInputStream fis = new FileInputStream("ChatSDKXMPP");
////            ks.load(null, password.toCharArray());
////
////            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
////            kmf.init(ks, password.toCharArray());
////
////            TrustManagerFactory tfm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
////            tfm.init(ks);
////
////            context.init(kmf.getKeyManagers(), tfm.getTrustManagers(), new SecureRandom());
////
////            builder.setCustomSSLContext(context);
////            builder.setCustomX509TrustManager(new ChatSDKTrustManager());
//        }

        return builder.build();
    }

    public Completable login (final String userJID, final String password){
        return Completable.defer(() -> {
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
            return openRegistrationConnection(server).flatMapCompletable(xmppConnection -> {

                AccountManager accountManager = accountManager();
                if (!accountManager.supportsAccountCreation()) {
                    getConnection().disconnect();
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
                    getConnection().disconnect();
                    return Completable.error(exception);
                }
            });
        }).subscribeOn(RX.io());
    }

    public void logout() {
        if (roster() != null) {
            roster().removeRosterListener(rosterListener);
        }
        if (chatManager() != null) {
            chatManager().removeIncomingListener(messageListener);
            chatManager().removeOutgoingListener(messageListener);
        }
        if (chatStateManager() != null) {
            chatStateManager().removeChatStateListener(chatStateListener);
        }

        dm.dispose();

        if(mucManager != null) {
            mucManager.dispose();
        }
        if(userManager != null) {
            userManager.dispose();
        }
        if (getConnection() != null) {
            getConnection().removeConnectionListener(connectionListener);
            getConnection().disconnect();
        }

    }

    public void sendOfflinePresence() {
        if (isConnected()) {
            Presence presence = new Presence(Presence.Type.unavailable, null, 1, Presence.Mode.dnd);
            ChatSDK.currentUser().setIsOnline(false);
            sendPresence(presence);
        }
    }

    public void sendOnlinePresence() {
        if (isConnected()) {
            ChatSDK.currentUser().setIsOnline(true);
            Presence presence = PresenceHelper.presenceForUser(ChatSDK.currentUser());
            presence.setStanzaId(UUID.randomUUID().toString());
            sendPresence(presence);
        }
    }
    
    public void sendPresence (Presence presence) {
        try {

            PublicKeyExtras.addTo(presence);

            sendStanza(presence);
        }
        catch (Exception e) {
            ChatSDK.events().onError(e);
        }
    }

    public void sendStanza(Stanza stanza) {
        try {
//            if (isConnectedAndAuthenticated()) {
                getConnection().sendStanza(stanza);
//            } else {
//                outgoingStanzaQueue.add(stanza);
//            }
        } catch (Exception e) {
            e.printStackTrace();
//            outgoingStanzaQueue.add(stanza);
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
