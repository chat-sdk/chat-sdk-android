package co.chatsdk.xmpp.handlers;

import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jxmpp.jid.impl.JidCreate;

import java.util.Calendar;
import java.util.Date;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.LastOnlineHandler;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ben on 8/16/17.
 */

public class XMPPLastOnlineHandler implements LastOnlineHandler {
    @Override
    public Single<Date> getLastOnline(final User user) {
        return Single.create(new SingleOnSubscribe<Date>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Date> e) throws Exception {
                LastActivity activity = XMPPManager.shared().lastActivityManager().getLastActivity(JidCreate.bareFrom(user.getEntityID()));
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, - (int) activity.getIdleTime());
                e.onSuccess(calendar.getTime());
            }
        }).subscribeOn(Schedulers.single());
    }

    @Override
    public Completable setLastOnline(User user) {
        return null;
    }
}
