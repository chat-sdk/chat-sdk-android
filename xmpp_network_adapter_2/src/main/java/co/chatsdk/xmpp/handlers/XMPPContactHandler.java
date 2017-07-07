package co.chatsdk.xmpp.handlers;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.NM;
import co.chatsdk.core.base.BaseContactHandler;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.handlers.ContactHandler;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.functions.Action;

/**
 * Created by benjaminsmiley-andrews on 06/07/2017.
 */

public class XMPPContactHandler extends BaseContactHandler {

    @Override
    public Completable addContact(final BUser user, final ConnectionType type) {
        if(type.equals(ConnectionType.Contact)) {
            return XMPPManager.shared().userManager.addUserToRoster(user).doOnComplete(new Action() {
                @Override
                public void run() throws Exception {
                    XMPPContactHandler.super.addContact(user, type);
                }
            });
        }
        else {
            super.addContact(user, type);
            return Completable.complete();
        }
    }

    @Override
    public Completable deleteContact(final BUser user,final ConnectionType type) {
        if(type.equals(ConnectionType.Contact)) {
            return XMPPManager.shared().userManager.removeUserFromRoster(user).doOnComplete(new Action() {
                @Override
                public void run() throws Exception {
                    XMPPContactHandler.super.deleteContact(user, type);
                }
            });
        }
        else {
            super.deleteContact(user, type);
            return Completable.complete();
        }
    }
}
