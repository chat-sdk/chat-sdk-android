package app.xmpp.adapter.handlers;

import org.jxmpp.jid.impl.JidCreate;

import app.xmpp.adapter.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.core.base.AbstractCoreHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPCoreHandler extends AbstractCoreHandler {

    @Override
    public Completable pushUser() {
        return Completable.defer(() -> {
            XMPPManager.shared().sendAvailablePresence();
            return XMPPManager.shared().userManager.updateMyvCardWithUser(ChatSDK.currentUser());
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable sendAvailablePresence() {
        return Completable.create(emitter -> {
            XMPPManager.shared().sendAvailablePresence();
            emitter.onComplete();
        });
    }

    @Override
    public Completable sendUnavailablePresence() {
        return Completable.create(emitter -> {
            XMPPManager.shared().sendUnavailablePresence();
            emitter.onComplete();
        });
    }

    @Override
    public Completable userOn(User user) {
        return Completable.defer(() -> XMPPManager.shared().userManager.updateUserFromVCard(JidCreate.entityBareFrom(user.getEntityID())).ignoreElement());
    }

    @Override
    public void userOff(User user) {

    }

    @Override
    public void save() {}

    @Override
    public Single<User> getUserForEntityID(final String entityID) {
        return Single.defer(() -> {
            // Check that this

            String jid = entityID;

            if (!jid.contains(XMPPManager.shared().getDomain())) {
                jid += "@" + XMPPManager.shared().getDomain();
            }

            final User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, jid);
            return userOn(user).toSingle(() -> user);
        });
    }

    @Override
    public User getUserNowForEntityID(String entityID) {

        String jid = entityID;

        if (!jid.contains(XMPPManager.shared().getDomain())) {
            jid += "@" + XMPPManager.shared().getDomain();
        }

        final User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, jid);
        userOn(user).subscribe(ChatSDK.events());

        return user;
    }

    @Override
    public void addBackgroundDisconnectExemption() {
        XMPPManager.shared().connectionManager().addDisconnectionExemption();
    }

}
