package co.chatsdk.xmpp.handlers;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.base.AbstractAuthenticationHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.utils.KeyStorage;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPAuthenticationHandler extends AbstractAuthenticationHandler {

    KeyStorage keyStorage = new KeyStorage();

    public XMPPAuthenticationHandler () {
    }

    @Override
    public Boolean accountTypeEnabled(int type) {
        return type == AccountType.Password || type == AccountType.Register;
    }

    @Override
    public Completable authenticate (final AccountDetails details) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull final CompletableEmitter e) throws Exception {
                // If we're already authenticated just finish
                if(XMPPManager.shared().isConnectedAndAuthenticated()) {
                    e.onComplete();
                    return;
                }

                switch (details.type) {
                    case Username:
                        XMPPManager.shared().login(details.username, details.password).subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                keyStorage.put(KeyStorage.UsernameKey, details.username);
                                keyStorage.put(KeyStorage.PasswordKey, details.password);

                                String jidString = details.username + "@" + XMPPManager.shared().serviceName.toString();

                                Timber.v("Authentication Complete");

                                userAuthenticationCompletedWithJID(JidCreate.bareFrom(jidString));
                                Timber.v("Setup tasks complete");
                                e.onComplete();
                            }
                        });
                        return;
                    case Register:
                        XMPPManager.shared().register(details.username, details.password).subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                e.onComplete();
                            }
                        });
                        return;
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    private void userAuthenticationCompletedWithJID (Jid jid) {

        final Map<String, Object> loginInfoMap =  new HashMap<String, Object>();

        loginInfoMap.put(Defines.Prefs.AuthenticationID, jid.asBareJid().toString());

        setLoginInfo(loginInfoMap);

        AbstractXMPPConnection conn = XMPPManager.shared().getConnection();
        if(conn.isAuthenticated() && conn.isConnected()) {

            User user = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, jid.asBareJid().toString());


            XMPPManager.shared().goOnline(user);

            if(NM.push() != null) {
                NM.push().subscribeToPushChannel(jid.asBareJid().toString());
            }

            XMPPManager.shared().performPostAuthenticationSetup();

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
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
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
        XMPPManager.shared().logout();
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
