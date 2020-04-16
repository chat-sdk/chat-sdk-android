package co.chatsdk.xmpp.handlers;

import org.jxmpp.jid.impl.JidCreate;

import sdk.chat.core.base.AbstractCoreHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPCoreHandler extends AbstractCoreHandler {

    @Override
    public Completable pushUser() {
        return Completable.defer(() -> {
            XMPPManager.shared().sendOnlinePresence();
            return XMPPManager.shared().userManager.updateMyvCardWithUser(ChatSDK.currentUser());
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable setUserOnline() {
        return Completable.complete();
    }

    @Override
    public Completable setUserOffline() {
        return Completable.complete();
    }

    @Override
    public void goOffline() {
        XMPPManager.shared().sendOfflinePresence();
    }

    @Override
    public void goOnline() {
        XMPPManager.shared().sendOnlinePresence();
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
}
