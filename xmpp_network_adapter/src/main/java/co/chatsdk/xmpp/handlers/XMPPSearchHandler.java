package co.chatsdk.xmpp.handlers;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.utils.JID;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPSearchHandler implements SearchHandler {

    @Override
    public Observable<BUser> usersForIndex(String index, String value) {
        return XMPPManager.shared().userManager.searchUser("user", value).flatMap(new Function<JID, ObservableSource<BUser>>() {
            @Override
            public ObservableSource<BUser> apply(@NonNull JID jid) throws Exception {
                // Get the user
                return XMPPManager.shared().userManager.updateUserFromVCard(jid).toObservable();
            }
        });
    }

}
