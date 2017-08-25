package co.chatsdk.xmpp.handlers;

import org.jxmpp.jid.Jid;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.core.interfaces.StorageAdapter;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class XMPPSearchHandler implements SearchHandler {

    @Override
    public Observable<User> usersForIndex(String index, String value) {
        return XMPPManager.shared().userManager.searchUser("user", value).flatMap(new Function<Jid, ObservableSource<User>>() {
            @Override
            public ObservableSource<User> apply(@NonNull final Jid jid) throws Exception {
                return XMPPManager.shared().userManager.updateUserFromVCard(jid).toObservable();
            }
        });
    }

}
