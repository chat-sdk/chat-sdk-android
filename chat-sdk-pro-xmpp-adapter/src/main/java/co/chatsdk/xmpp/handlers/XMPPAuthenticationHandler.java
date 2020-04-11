package co.chatsdk.xmpp.handlers;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.minidns.record.A;
import org.pmw.tinylog.Logger;

import java.util.concurrent.Callable;

import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.functions.Action;
import sdk.chat.core.base.AbstractAuthenticationHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.AccountDetails;
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
    public Completable authenticate() {
        return Completable.defer(() -> {

            if (isAuthenticatedThisSession()) {
                return Completable.complete();
            }
            if (!isAuthenticated()) {
                return Completable.error(ChatSDK.getException(R.string.authentication_required));
            }
            if (!isAuthenticating()) {
                AccountDetails details = cachedAccountDetails();
                if (details.areValid()) {
                    authenticating = authenticate(details);
                } else {
                    return Completable.error(new Exception());
                }
            }
            return authenticating;
        }).doOnComplete(this::setAuthStateToIdle)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable authenticate (final AccountDetails details) {
        return Completable.defer(() -> {
            if (isAuthenticatedThisSession() || isAuthenticated()) {
                return Completable.error(ChatSDK.getException(R.string.already_authenticated));
            }
            else if (!isAuthenticating()) {
                authenticating = Completable.defer(() -> {
                    // If we'recyclerView already authenticated just finish
                    if(XMPPManager.shared().isConnectedAndAuthenticated()) {
                        return Completable.complete();
                    }

                    switch (details.type) {
                        case Username:
                            return XMPPManager.shared().login(details.username, details.password).andThen(Completable.defer(() -> {
                                keyStorage.save(details.username, details.password);

                                if(!details.username.contains("@")) {
                                    details.username = details.username + "@" + XMPPManager.shared().getDomain();
                                }

                                try {
                                    userAuthenticationCompletedWithJID(JidCreate.bareFrom(details.username));
                                    return Completable.complete();
                                }
                                catch (XmppStringprepException ex) {
                                    return Completable.error(ex);
                                }
                            }));
                        case Register:
                            return XMPPManager.shared().register(details.username, details.password).andThen(Completable.defer(() -> {
                                details.type = AccountDetails.Type.Username;
                                return authenticate(details);
                            }));
                        default:
                            return Completable.error(ChatSDK.getException(R.string.login_method_doesnt_exist));
                    }
                });
            }
            return authenticating;
        }).doOnComplete(this::setAuthStateToIdle)
                .subscribeOn(Schedulers.io());
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
        return Completable.create(emitter -> {
            if(ChatSDK.push() != null) {
                ChatSDK.push().unsubscribeToPushChannel(ChatSDK.currentUser().getPushChannel());
            }
            XMPPManager.shared().logout();

            clearSavedCurrentUserEntityID();
            keyStorage.clear();

            ChatSDK.events().source().onNext(NetworkEvent.logout());

            authenticatedThisSession = false;

            emitter.onComplete();
        });
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
