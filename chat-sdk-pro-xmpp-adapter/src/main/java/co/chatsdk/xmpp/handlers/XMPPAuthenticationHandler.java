package co.chatsdk.xmpp.handlers;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.pmw.tinylog.Logger;

import co.chatsdk.core.base.AbstractAuthenticationHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.xmpp.R;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.utils.KeyStorage;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPAuthenticationHandler extends AbstractAuthenticationHandler {

    KeyStorage keyStorage = new KeyStorage();

    public XMPPAuthenticationHandler () {
    }

    @Override
    public Boolean accountTypeEnabled(AccountDetails.Type type) {
        return type == AccountDetails.Type.Username || type == AccountDetails.Type.Register;
    }

    @Override
    public Completable authenticate (final AccountDetails details) {
        return Completable.create(e -> {
            // If we'recyclerView already authenticated just finish
            if(XMPPManager.shared().isConnectedAndAuthenticated()) {
                e.onComplete();
                return;
            }

            switch (details.type) {
                case Username:
                    XMPPManager.shared().login(details.username, details.password).subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {}

                        @Override
                        public void onComplete() {

                            keyStorage.save(details.username, details.password);

                            if(!details.username.contains("@")) {
                                details.username = details.username + "@" + XMPPManager.shared().getDomain();
                            }

                            Logger.debug("Authentication Complete");

                            try {
                                userAuthenticationCompletedWithJID(JidCreate.bareFrom(details.username));
                                Logger.debug("Setup tasks complete");
                                e.onComplete();
                            }
                            catch (XmppStringprepException ex) {
                                e.onError(ex);
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable ex) {
                            e.onError(ex);
                        }
                    });
                    break;
                case Register:
                    XMPPManager.shared().register(details.username, details.password).subscribe(() -> {
                        // Once the account is created, authenticate the user
                        details.type = AccountDetails.Type.Username;
                        authenticate(details).subscribe(() -> e.onComplete(), throwable -> e.onError(throwable));
                    }, throwable -> {
                        e.onError(throwable);
                    });
                    break;
                default:
                    e.onError(new Throwable(ChatSDK.shared().context().getString(R.string.login_method_doesnt_exist)));
            }
        }).subscribeOn(Schedulers.io());
    }

    private void userAuthenticationCompletedWithJID (Jid jid) {

        saveCurrentUserEntityID(jid.asBareJid().toString());

        AbstractXMPPConnection conn = XMPPManager.shared().getConnection();
        if(conn.isAuthenticated() && conn.isConnected()) {

            User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, jid.asBareJid().toString());
            if(user.getName() == null || user.getName().isEmpty()) {
                Localpart name = jid.getLocalpartOrNull();
                if(name != null) {
                    user.setName(name.toString());
                }
            }

            if(ChatSDK.push() != null) {
                ChatSDK.push().subscribeToPushChannel(user.getPushChannel());
            }

            XMPPManager.shared().performPostAuthenticationSetup();

            authenticatedThisSession = true;
        }
    }

    @Override
    public Completable authenticate() {
        return Completable.defer(() -> {
            AccountDetails details = cachedAccountDetails();
            if (details.areValid()) {
                return authenticate(details);
            }
            return Completable.error(new Exception());
        });
    }

    public AccountDetails cachedAccountDetails () {
        return AccountDetails.username(keyStorage.get(KeyStorage.UsernameKey), keyStorage.get(KeyStorage.PasswordKey));
    }

    @Override
    public Boolean isAuthenticated() {
        XMPPConnection connection = XMPPManager.shared().getConnection();
        return connection != null && connection.isAuthenticated() || cachedAccountDetails().areValid();
    }

    @Override
    public Completable logout() {

        if(ChatSDK.push() != null) {
            ChatSDK.push().unsubscribeToPushChannel(ChatSDK.currentUser().getPushChannel());
        }
        XMPPManager.shared().logout();

        clearSavedCurrentUserEntityID();
        keyStorage.clear();

        ChatSDK.events().source().onNext(NetworkEvent.logout());

        authenticatedThisSession = false;

        return Completable.complete();
    }

    // TODO: Implement this
    @Override
    public Completable changePassword(String email, String oldPassword, String newPassword) {
        return Completable.create(emitter -> {
            XMPPManager.shared().accountManager().changePassword(newPassword);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable sendPasswordResetMail(String email) {
        return Completable.error(new Throwable("Password email not supported"));
    }

}
