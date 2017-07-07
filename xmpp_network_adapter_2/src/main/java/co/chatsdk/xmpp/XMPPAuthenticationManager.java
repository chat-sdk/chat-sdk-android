package co.chatsdk.xmpp;

import android.content.Context;
import android.content.ServiceConnection;

import com.example.chatsdkxmppadapter.R;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.concurrent.Callable;

import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.xmpp.enums.ConnectionStatus;
import co.chatsdk.xmpp.utils.JID;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.operators.single.SingleFromCallable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by kykrueger on 2016-10-23.
 */

public class XMPPAuthenticationManager {

    Context context;

    public XMPPAuthenticationManager() {
        context = AppContext.context.getApplicationContext();
    }

    public Completable login (final String userJID, final String password){
        return XMPPManager.shared().openConnection(userJID, password).flatMapCompletable(new Function<XMPPConnection, Completable>() {
            @Override
            public Completable apply(XMPPConnection xmppConnection) throws Exception {
                //xmppConnection.getUser();

                if(xmppConnection.isConnected()) {
                    return XMPPManager.shared().userManager.updateUserFromVCard(new JID(userJID)).toCompletable();
                }
                else {
                    return Completable.error(new Throwable("Connection is not connected"));
                }
            }
        });
    }

    public Completable register(final String username, final String password){
        return XMPPManager.shared().openRegistrationConnection().flatMapCompletable(new Function<XMPPConnection, Completable>() {
            @Override
            public Completable apply(XMPPConnection xmppConnection) throws Exception {

                AccountManager accountManager = AccountManager.getInstance(XMPPManager.shared().getConnection());
                if (!accountManager.supportsAccountCreation()) {
                    XMPPManager.shared().getConnection().disconnect();
                    return Completable.error(new Exception("Server does not support account creation"));
                }
                try {
                    accountManager.sensitiveOperationOverInsecureConnection(true);
                    accountManager.createAccount(username, password);
                    return Completable.complete();
                }
                catch (Exception exception) {
                    XMPPManager.shared().getConnection().disconnect();
                    return Completable.error(exception);
                }
            }
        });
    }

    public void logout(){
        XMPPManager.shared().getConnection().disconnect();
    }

    public void reconnectProcedure(){

    }

//    public Observable<BUser> getCurrentUser(final String userJID) {
//        userManager
//
//        return Observable.create(new ObservableOnSubscribe<BUser>() {
//            @Override
//            public void subscribe(final ObservableEmitter<BUser> e) throws Exception {
//                if (userJID == null) {
//                    e.onError(new Throwable("AuthCredentials null"));
//                    return;
//                }
//                BUser user = StorageManager.shared().fetchOrCreateEntityWithEntityID(BUser.class, userJID);
//                if(user != null) {
//                    e.onNext(user);
//                }
//                else {
//                    userManager.getUser()
//                }
//            }
//        });
//
//        return new Observable<String>(){
//            @Override
//            protected void subscribeActual(Observer<? super String> observer) {
//            }
//        }.flatMap(new Function<String, Observable<BUser>>() {
//            @Override
//            public Observable<BUser> apply(String userAlias) throws Exception {
//                return userManager.getUser(userAlias);
//            }
//        }).retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
//            @Override
//            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
//                return Observable.timer(50, TimeUnit.MILLISECONDS);
//            }
//        });
//    }




}
