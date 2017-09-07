package co.chatsdk.xmpp.handlers;

import co.chatsdk.core.NM;
import co.chatsdk.core.base.BaseContactHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 06/07/2017.
 */

public class XMPPContactHandler extends BaseContactHandler {

    @Override
    public Completable addContact(final User user, final ConnectionType type) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull final CompletableEmitter e) throws Exception {
                if(type.equals(ConnectionType.Contact)) {
                    XMPPManager.shared().userManager.addUserToRoster(user).doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            e.onError(throwable);
                        }
                    }).subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            e.onComplete();
                        }
                    });
                }
                else {
                    e.onComplete();
                }
            }
        }).concatWith(new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                Timber.d("Contact added notification");
                XMPPContactHandler.super.addContact(user, type);
                NM.events().source().onNext(NetworkEvent.contactAdded(user));
                s.onComplete();
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable deleteContact(final User user, final ConnectionType type) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull final CompletableEmitter e) throws Exception {
                if(type.equals(ConnectionType.Contact)) {
                    XMPPManager.shared().userManager.removeUserFromRoster(user).doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            e.onError(throwable);
                        }
                    }).subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            XMPPContactHandler.super.deleteContact(user, type);
                            NM.events().source().onNext(NetworkEvent.contactDeleted(user));
                            e.onComplete();
                        }
                    });
                }
                else {
                    e.onComplete();
                }
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }
}
