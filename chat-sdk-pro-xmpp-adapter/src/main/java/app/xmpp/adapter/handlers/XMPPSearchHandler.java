package app.xmpp.adapter.handlers;

import org.pmw.tinylog.Logger;

import sdk.chat.core.base.AbstractSearchHandler;
import sdk.chat.core.dao.User;
import app.xmpp.adapter.XMPPManager;
import io.reactivex.Observable;


/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPSearchHandler extends AbstractSearchHandler {

    @Override
    public Observable<User> usersForIndex(String value, int limit) {
        return usersForIndex(value, "user");
    }

    @Override
    public Observable<User> usersForIndex(String value, int limit, String index) {
        return XMPPManager.shared().userManager.searchUser(index, value).flatMap(jid -> XMPPManager.shared().userManager.updateUserFromVCard(jid).toObservable());
    }

    @Override
    public Observable<User> usersForIndexes(String value, int limit, String... indexes) {
        Logger.debug("Search with multiple indexes isn't supported for XMPP");
        if (indexes.length > 0) {
            return usersForIndexes(value, indexes[0]);
        }
        else {
            return usersForIndex(value);
        }
    }

    public boolean canAddUserById() {
        return true;
    }

}
