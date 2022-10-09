package app.xmpp.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import app.xmpp.adapter.enums.ConnectionStatus;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public class ConnectionManager implements ConnectionListener, AppBackgroundMonitor.Listener, PingFailedListener, StanzaListener, StanzaFilter {

    protected BehaviorSubject<ConnectionStatus> connectionStatusSource = BehaviorSubject.create();
    public WeakReference<XMPPManager> manager;

    public static String LastOnlineKey = "last-online";
    public DisposableMap dm = new DisposableMap();

    public Disposable connectionCheckerDisposable = null;

    public boolean disconnectInBackground = true;

    public boolean disconnectionExemption = false;

    protected Disposable connectInFutureDisposable = null;

    protected int pingFailCount = 0;
    protected Disposable pingDisposable;
    protected Disposable messageSentDisposable;

    public ConnectionManager() {

        dm.add(ChatSDK.events().sourceOnBackground()
                .filter(NetworkEvent.filterType(EventType.NetworkStateChanged))
                .subscribe(networkEvent -> {
                    internetConnectivityChanged(networkEvent.getIsOnline());
                }));

        connectionCheckerDisposable = Observable.timer(5, TimeUnit.SECONDS).subscribe(aLong -> {
            XMPPConnection connection = manager.get().getConnection();
            if (connection instanceof XMPPTCPConnection) {
                XMPPTCPConnection tcp = (XMPPTCPConnection) connection;
                int maxRes = tcp.getMaxSmResumptionTime();

            }
        });

    }

    public ConnectionManager(XMPPManager manager) {
        this.manager = new WeakReference<>(manager);
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        connectionStatusSource.onNext(ConnectionStatus.Authenticated);

        connection.removeAsyncStanzaListener(this);
        connection.addAsyncStanzaListener(this, this);

        startPing();
    }

    @Override
    public void connecting(XMPPConnection connection) {
//        ConnectionListener.super.connecting(connection);
    }

    @Override
    public void connected(XMPPConnection connection) {
        connectionStatusSource.onNext(ConnectionStatus.Connected);
    }

    public void startPing() {
        stopPing();
        pingDisposable = Observable.interval(5, 5, TimeUnit.SECONDS).subscribe(aLong -> {
            try {
                XMPPManager.shared().pingManager().pingMyServer(true, 4000);
            } catch (Exception e) {

            }
        });
    }

    public void restartPing() {
        stopPing();
        startPing();
    }

    public void stopPing() {
        if (pingDisposable != null) {
            pingDisposable.dispose();
        }
    }

    @Override
    public void connectionClosed() {
        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
        if (!ChatSDK.appBackgroundMonitor().inBackground()) {
//            reconnect();
        }
        stopPing();
    }

    @Override
    public void connectionClosedOnError(Exception e){
        ChatSDK.events().onError(e);
        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
        if (!ChatSDK.appBackgroundMonitor().inBackground()) {
//            reconnect();
        }
        stopPing();
    }

    public Observable<ConnectionStatus> connectionStatus() {
        return connectionStatusSource.subscribeOn(RX.io()).observeOn(RX.io());
    }

    public void internetConnectivityChanged(boolean isAvailable) {
        if (!isAvailable) {
            manager.get().disconnect();
        } else {
            reconnect();
        }
    }

    @Override
    public void didStart() {
        // In case we disconnected while we were in the background
        disconnectionExemption = false;
        reconnect();
    }

    public void reconnect() {
        if (isInternetAvailable()) {
            cancelConnectInFuture();
            manager.get().reconnect();
        } else {
            connectInFuture(5);
        }
    }

    public boolean isInternetAvailable() {
        return ChatSDK.connectionStateMonitor().isOnline();
    }

    @Override
    public void didStop() {
        if (disconnectInBackground && !disconnectionExemption) {
            manager.get().disconnect();
        }
    }

    public void addDisconnectionExemption() {
        disconnectionExemption = true;
    }

    public void connectInFuture(long seconds) {
        if (connectInFutureDisposable == null || connectInFutureDisposable.isDisposed()) {
            connectInFutureDisposable = Observable.interval(seconds, TimeUnit.SECONDS).subscribe(aLong -> {
                cancelConnectInFuture();
                reconnect();
            });
        }
    }

    public void cancelConnectInFuture() {
        if (connectInFutureDisposable != null) {
            Logger.debug("ConnectionManager - cancelConnectInFuture");
            connectInFutureDisposable.dispose();
        }
        connectInFutureDisposable = null;
    }

    @Override
    public void pingFailed() {
        pingFailCount++;

        if (pingFailCount > 1) {
            pingFailCount = 0;
            XMPPManager.shared().hardReconnect();
        }

    }

    @Override
    public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
        restartPing();
        resentMessageSentTimer();
//        pingFailCount = 0;
    }

    @Override
    public boolean accept(Stanza stanza) {
        return true;
    }


    /**
     * @param bare
     * @param threadId
     * @param date
     * @param offset - the offset is how many milliseconds in the future we want to set this to
     */
    public void updateLastOnline(String bare, String threadId, @NonNull Date date, int offset) {
        updateLastOnlineWithKey(makeKey(bare + threadId), date, offset);
    }

    public void updateLastOnline(String bare, @NonNull Date date, int offset) {
        updateLastOnlineWithKey(makeKey(bare), date, offset);
    }

    public void updateLastOnlineWithKey(String key, @NonNull Date date, int offset) {
        if (XMPPManager.shared().getConnection() != null && XMPPManager.shared().isConnectedAndAuthenticated()) {
            if (offset > 0) {
                date = new Date(date.getTime() + offset);
            }
            Date lastOnline = getLastOnlineWithKey(key);
            if (lastOnline == null || date.getTime() > lastOnline.getTime()) {
                Logger.info("ConnectionManager: Update last online " + key + ", " + date);
                ChatSDK.shared().getKeyStorage().put(key, date.getTime());
            } else {
                Logger.info("ConnectionManager: Don't update old date");
            }
        }
    }
    public Date getLastOnline(String bare, String threadId) {
        return getLastOnlineWithKey(makeKey(bare + threadId));
    }

    public Date getLastOnline(String bare) {
        return getLastOnlineWithKey(makeKey(bare));
    }

    @Nullable
    public Date getLastOnlineWithKey(String key) {
        long dateLong = ChatSDK.shared().getKeyStorage().getLong(key);
        if (dateLong > 0) {
            return new Date(dateLong);
        }
        return null;
    }

    public String makeKey(String id) {
        return LastOnlineKey + "-" + id;
    }

    public void notifyMessageSent() {
        // If we sent a message and don't get a stanza back within 3 seconds, then maybe we have disconnected
        // so increment the ping counter
//        messageSentDisposable = Observable.interval(3, 3, TimeUnit.SECONDS).subscribe(aLong -> {
//            resentMessageSentTimer();
//            pingFailed();
//        });
    }

    public void resentMessageSentTimer() {
//        if (messageSentDisposable != null) {
//            messageSentDisposable.dispose();
//        }
    }
}
