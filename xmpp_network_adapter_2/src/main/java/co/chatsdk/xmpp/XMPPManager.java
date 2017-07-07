package co.chatsdk.xmpp;

import android.content.Context;

import com.example.chatsdkxmppadapter.R;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.search.UserSearchManager;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoDefines;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.xmpp.defines.XMPPDefines;
import co.chatsdk.xmpp.enums.ConnectionStatus;
import co.chatsdk.xmpp.listeners.XMPPChatManagerListener;
import co.chatsdk.xmpp.listeners.XMPPChatMessageListener;
import co.chatsdk.xmpp.listeners.XMPPConnectionListener;
import co.chatsdk.xmpp.listeners.XMPPRosterListener;
import co.chatsdk.xmpp.utils.JID;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPManager {

    public String serviceName;
    public String serviceHost;
    public int servicePort;
    public String searchService;

    private UserSearchManager userSearchManager;

    // The main XMPP connection
    private AbstractXMPPConnection connection = null;

    // Listeners
    private XMPPConnectionListener connectionListener = new XMPPConnectionListener();
    private XMPPRosterListener rosterListener = new XMPPRosterListener();

    private XMPPChatManagerListener chatManagerListener = new XMPPChatManagerListener();

    // Managers
    public XmppUsersManager userManager;

    public final static XMPPManager instance = new XMPPManager();
    public static XMPPManager shared() {
        return instance;
    }

    protected XMPPManager() {
        userManager = new XmppUsersManager();
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);

        // The connection listener is added once the connection is created.
        connectionListener.connectionStatusSource.subscribe(new Consumer<ConnectionStatus>() {
            @Override
            public void accept(ConnectionStatus connectionStatus) throws Exception {
                if (connectionStatus == ConnectionStatus.Authenticated) {

                    // Once the connection is ready, add the other listeners
                    Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
                    Roster roster = Roster.getInstanceFor(connection);
                    roster.addRosterListener(rosterListener);

                    ChatManager chatManager = ChatManager.getInstanceFor(connection);
                    chatManager.addChatListener(chatManagerListener);

                }
            }
        });

        // Listen to presence updates and update data accordingly
        getRosterListener().presenceEventSource.subscribe(new Consumer<Presence>() {
            @Override
            public void accept(final Presence presence) throws Exception {
                JID jid = new JID(presence.getFrom());

                userManager.updateUserFromVCard(jid).subscribe(new BiConsumer<BUser, Throwable>() {
                    @Override
                    public void accept(BUser user, Throwable throwable) throws Exception {
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

        Context context = AppContext.context;

        serviceHost = context.getString(R.string.service_host);
        serviceName = context.getString(R.string.service_name);
        servicePort = new Integer(context.getString(R.string.service_port));
        searchService = context.getString(R.string.search_service);
    }

    public Roster roster() {
        return Roster.getInstanceFor(getConnection());
    }

    public UserSearchManager userSearchManager () {
        return userSearchManager != null ? userSearchManager : new UserSearchManager(getConnection());
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

//
//
//    Single<AbstractXMPPConnection> getConnection(){
//
//
//        return Single.create(new SingleOnSubscribe<AbstractXMPPConnection>() {
//            @Override
//            public void subscribe(final SingleEmitter<AbstractXMPPConnection> e) throws Exception {
//                if(connection == null){
//                    e.onError(new Throwable("XMPP Connection has not been made"));
//                }
//                else if(!connection.isConnected()){
//                    e.onError(new Throwable("XMPP Service is not connected to server"));
//                }
//                else if(!connection.isAuthenticated()){
//                    e.onError(new Throwable("XMPP Service is not authenticated"));
//                }
//                else {
//                    e.onSuccess(connection);
//                }
//            }
//        });
//                .retryWhen(new Function<Flowable<Throwable>, Publisher<Object>>() {
//            @Override
//            public Publisher<Object> apply(Flowable<Throwable> throwableFlowable) throws Exception {
//                return Single.timer(50, TimeUnit.MILLISECONDS);
//            }
//        });
//    }

    public void goOnline (BUser user) {

    }

}
