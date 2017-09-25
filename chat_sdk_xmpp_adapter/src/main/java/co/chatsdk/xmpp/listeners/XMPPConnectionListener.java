package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.enums.ConnectionStatus;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class XMPPConnectionListener implements ConnectionListener {

    public BehaviorSubject<ConnectionStatus> connectionStatusSource = BehaviorSubject.create();
    public XMPPRosterListener rosterListener;

    public XMPPConnectionListener (XMPPManager manager) {
        rosterListener = new XMPPRosterListener(manager);
        connectionStatusSource.subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        connection.addConnectionListener(this);
        connectionStatusSource.onNext(ConnectionStatus.Authenticated);
    }

    @Override
    public void connected(XMPPConnection connection) {
        connection.addConnectionListener(this);
        connectionStatusSource.onNext(ConnectionStatus.Connected);
    }

    @Override
    public void connectionClosed() {
        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
    }

    @Override
    public void reconnectingIn(int seconds) {
        connectionStatusSource.onNext(ConnectionStatus.Reconnecting);
    }

    @Override
    public void reconnectionSuccessful() {
        connectionStatusSource.onNext(ConnectionStatus.Connected);
    }

    @Override
    public void reconnectionFailed(Exception e) {
        e.printStackTrace();
        // TODO: When this is included, it can cause a crash
        //connectionStatusSource.onError(e);
        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
    }

    @Override
    public void connectionClosedOnError(Exception e){
        e.printStackTrace();
        connectionStatusSource.onError(e);
        connectionStatusSource.onNext(ConnectionStatus.Disconnected);
    }
}
