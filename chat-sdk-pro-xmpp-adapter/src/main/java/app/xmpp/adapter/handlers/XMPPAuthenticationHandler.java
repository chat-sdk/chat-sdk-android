package app.xmpp.adapter.handlers;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import app.xmpp.adapter.R;
import app.xmpp.adapter.XMPPManager;
import sdk.chat.core.utils.KeyStorage;
import io.reactivex.Completable;
import sdk.chat.core.base.AbstractAuthenticationHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.AccountDetails;
import sdk.guru.common.RX;


/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPAuthenticationHandler extends AbstractAuthenticationHandler {

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
        }).doOnComplete(this::setAuthStateToIdle);
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
                                ChatSDK.shared().getKeyStorage().save(details.username, details.password);

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
        }).doOnComplete(this::setAuthStateToIdle);
    }

    private void userAuthenticationCompletedWithJID (Jid jid) {

        setCurrentUserEntityID(jid.asBareJid().toString());

        AbstractXMPPConnection conn = XMPPManager.shared().getConnection();
        if(conn.isAuthenticated() && conn.isConnected()) {

            User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, jid.asBareJid().toString());
            if(user.getName() == null || user.getName().isEmpty()) {
                Localpart name = jid.getLocalpartOrNull();
                if(name != null) {
                    user.setName(name.toString());
                }
            }

            XMPPManager.shared().performPostAuthenticationSetup();
        }
    }

    public AccountDetails cachedAccountDetails () {
        return AccountDetails.username(ChatSDK.shared().getKeyStorage().get(KeyStorage.UsernameKey), ChatSDK.shared().getKeyStorage().get(KeyStorage.PasswordKey));
    }

    @Override
    public Boolean isAuthenticated() {
        XMPPConnection connection = XMPPManager.shared().getConnection();
        return connection != null && connection.isAuthenticated() || cachedAccountDetails().areValid();
    }

    @Override
    public Completable logout() {
        return Completable.create(emitter -> {
            XMPPManager.shared().logout();

            clearCurrentUserEntityID();
            ChatSDK.shared().getKeyStorage().clear();

            ChatSDK.events().source().accept(NetworkEvent.logout());

            emitter.onComplete();
        }).subscribeOn(RX.computation());
    }

    // TODO: Implement this
    @Override
    public Completable changePassword(String email, String oldPassword, String newPassword) {
        return Completable.create(emitter -> {
            XMPPManager.shared().accountManager().changePassword(newPassword);
            emitter.onComplete();
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable sendPasswordResetMail(String email) {
        return Completable.error(new Throwable("Password email not supported"));
    }

}
