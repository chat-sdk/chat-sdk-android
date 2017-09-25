package co.chatsdk.xmpp.handlers;

import co.chatsdk.core.NM;
import co.chatsdk.core.base.AbstractCoreHandler;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPCoreHandler extends AbstractCoreHandler {

    @Override
    public Completable pushUser() {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                XMPPManager.shared().goOnline(NM.currentUser());
                e.onComplete();
            }
        }).concatWith(XMPPManager.shared().userManager.updateMyvCardWithUser(NM.currentUser()))
                .subscribeOn(Schedulers.single());
    }

    @Override
    public void setUserOnline() {

    }

    @Override
    public void setUserOffline() {

    }

    @Override
    public void goOffline() {

    }

    @Override
    public void goOnline() {

    }

    @Override
    public Single<Boolean> isOnline() {
        return null;
    }

    @Override
    public void observeUser(String entityID) {

    }

    @Override
    public void save() {

    }
}
