package co.chatsdk.xmpp.handlers;

import co.chatsdk.core.base.BaseContactHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;


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
                return Completable.complete();
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deleteContact(final User user, final ConnectionType type) {
        return Completable.defer(() -> {
            if(type.equals(ConnectionType.Contact) && !user.isMe()) {
                return XMPPManager.shared().userManager.removeUserFromRoster(user).concatWith(XMPPContactHandler.super.deleteContact(user, type));
            }
            else {
                return Completable.complete();
            }
        }).subscribeOn(Schedulers.io());
    }
}
