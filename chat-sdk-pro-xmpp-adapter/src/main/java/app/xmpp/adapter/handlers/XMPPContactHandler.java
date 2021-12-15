package app.xmpp.adapter.handlers;

import sdk.chat.core.base.BaseContactHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.types.ConnectionType;
import app.xmpp.adapter.XMPPManager;
import io.reactivex.Completable;
import sdk.guru.common.RX;


/**
 * Created by benjaminsmiley-andrews on 06/07/2017.
 */

public class XMPPContactHandler extends BaseContactHandler {

    @Override
    public Completable addContact(final User user, final ConnectionType type) {
        return Completable.defer(() -> {
            if(type.equals(ConnectionType.Contact) && !user.isMe()) {
                return XMPPManager.shared().userManager.addUserToRoster(user).concatWith(XMPPContactHandler.super.addContact(user, type));
            }
            else {
                return XMPPContactHandler.super.addContact(user, type);
            }
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable deleteContact(final User user, final ConnectionType type) {
        return Completable.defer(() -> {
            if(type.equals(ConnectionType.Contact) && !user.isMe()) {
                return XMPPManager.shared().userManager.removeUserFromRoster(user).concatWith(XMPPContactHandler.super.deleteContact(user, type));
            }
            else {
                return XMPPContactHandler.super.deleteContact(user, type);
            }
        }).subscribeOn(RX.io());
    }
}
