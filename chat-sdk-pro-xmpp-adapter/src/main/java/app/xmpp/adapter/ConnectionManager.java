package app.xmpp.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.Date;

import app.xmpp.adapter.enums.ConnectionStatus;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.RX;

public class ConnectionManager implements ConnectionListener {

    protected BehaviorSubject<ConnectionStatus> connectionStatusSource = BehaviorSubject.create();
    public WeakReference<XMPPManager> manager;

    public static String LastOnlineKey = "last-online";

    public ConnectionManager(XMPPManager manager) {
        this.manager = new WeakReference<>(manager);
    }

    public void updateLastOnline(String bare, String threadId, @NonNull Date date) {
        updateLastOnlineWithKey(makeKey(bare + threadId), date);
    }

    public void updateLastOnline(String bare, @NonNull Date date) {
        updateLastOnlineWithKey(makeKey(bare), date);
    }

    public void updateLastOnlineWithKey(String key, @NonNull Date date) {
        if (XMPPManager.shared().getConnection() != null && XMPPManager.shared().isConnectedAndAuthenticated()) {
            Date lastOnline = getLastOnlineWithKey(key);
            if (lastOnline == null || date.getTime() > lastOnline.getTime()) {
                Logger.info("ConnectionManager: Update last online " + key + ", " + date.toString());
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

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        connectionStatusSource.onNext(ConnectionStatus.Authenticated);
    }

    @Override
    public void connected(XMPPConnection connection) {
        connectionStatusSource.onNext(ConnectionStatus.Connected);
    }

    @Override
    public void connectionClosed() {
        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
    }

    @Override
    public void connectionClosedOnError(Exception e){
        ChatSDK.events().onError(e);
        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
    }

    public Observable<ConnectionStatus> connectionStatus() {
        return connectionStatusSource.subscribeOn(RX.io()).observeOn(RX.io());
    }
}
