package co.chatsdk.xmpp.handlers;

import org.jivesoftware.smack.AbstractXMPPConnection;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.base.AbstractAuthenticationHandler;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.xmpp.XMPPAuthenticationManager;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.utils.JID;
import co.chatsdk.xmpp.utils.KeyStorage;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPAuthenticationHandler extends AbstractAuthenticationHandler {

    XMPPAuthenticationManager manager;
    KeyStorage keyStorage = new KeyStorage();

    public XMPPAuthenticationHandler () {
        manager = new XMPPAuthenticationManager();
    }

    @Override
    public Boolean accountTypeEnabled(int type) {
        return type == AccountType.Password || type == AccountType.Register;
    }

    @Override
    public Completable authenticate (final AccountDetails details) {

        // If we're already authenticated just finish
        if(XMPPManager.shared().isConnectedAndAuthenticated()) {
             return Completable.complete();
        }

        Action onComplete = new Action() {
            @Override
            public void run() throws Exception {
                keyStorage.put(KeyStorage.UsernameKey, details.username);
                keyStorage.put(KeyStorage.PasswordKey, details.password);

                JID jid = new JID(details.username, XMPPManager.shared().serviceName);

                userAuthenticationCompletedWithJID(jid);
            }
        };

        switch (details.type) {
            case Username:
                return manager.login(details.username, details.password).doOnComplete(onComplete);
            case Register:
                return manager.register(details.username, details.password);
        }

        return null;
    }

    private void userAuthenticationCompletedWithJID (JID jid) {

        final Map<String, Object> loginInfoMap =  new HashMap<String, Object>();

        loginInfoMap.put(Defines.Prefs.AuthenticationID, jid.bare());

        setLoginInfo(loginInfoMap);

        AbstractXMPPConnection conn = XMPPManager.shared().getConnection();
        if(conn.isAuthenticated() && conn.isConnected()) {

            BUser user = StorageManager.shared().fetchOrCreateEntityWithEntityID(BUser.class, jid.bare());

            for(BThread thread : NM.thread().getThreads(ThreadType.PrivateGroup)) {
                // TODO: Join any MUC rooms
            }

            XMPPManager.shared().goOnline(user);

            if(NM.push() != null) {
                NM.push().subscribeToPushChannel(jid.bare());
            }

        }

    }

    @Override
    public Completable authenticateWithCachedToken() {
        return cachedAccountDetails().flatMapCompletable(new Function<AccountDetails, Completable>() {
            @Override
            public Completable apply(AccountDetails accountDetails) throws Exception {
                return authenticate(accountDetails);
            }
        });
    }

    public Single<AccountDetails> cachedAccountDetails () {
        return Single.create(new SingleOnSubscribe<AccountDetails>() {
            @Override
            public void subscribe(SingleEmitter<AccountDetails> e) throws Exception {
                AccountDetails details = AccountDetails.username(keyStorage.get(KeyStorage.UsernameKey), keyStorage.get(KeyStorage.PasswordKey));
                if(details.loginDetailsValid()) {
                    e.onSuccess(details);
                }
                else {
                    e.onError(new Throwable("Login details not valid"));
                }
            }
        });
    }

    @Override
    public Boolean userAuthenticated() {
        return XMPPManager.shared().getConnection().isAuthenticated();
    }

    @Override
    public Completable logout() {
        if(NM.push() != null) {
            NM.push().unsubscribeToPushChannel(NM.currentUser().getEntityID());
        }
        manager.logout();
        setLoginInfo(new HashMap<String, Object>());

        NM.events().source().onNext(NetworkEvent.logout());

        return Completable.complete();
    }


    // TODO: Implement this
    @Override
    public Completable changePassword(String email, String oldPassword, String newPassword) {
        return Completable.error(new Throwable("Password change not supported"));
    }

    @Override
    public Completable sendPasswordResetMail(String email) {
        return Completable.error(new Throwable("Password email not supported"));
    }
}
