package app.xmpp.adapter.handlers;

import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jxmpp.jid.impl.JidCreate;

import java.util.Calendar;
import java.util.Date;

import app.xmpp.adapter.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.LastOnlineHandler;
import sdk.guru.common.Optional;
import sdk.guru.common.RX;

/**
 * Created by ben on 8/16/17.
 */

public class XMPPLastOnlineHandler implements LastOnlineHandler {
    @Override
    public Single<Optional<Date>> getLastOnline(final User user) {
        return Single.create((SingleOnSubscribe<Optional<Date>>) e -> {
            if (XMPPManager.shared().isConnectedAndAuthenticated()) {
                try {
                    LastActivity activity = XMPPManager.shared().lastActivityManager().getLastActivity(JidCreate.bareFrom(user.getEntityID()));
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.SECOND, - (int) activity.getIdleTime());
                    e.onSuccess(Optional.with(calendar.getTime()));
                } catch (Exception ex) {
                    e.onSuccess(Optional.empty());
                }
            } else {
                e.onSuccess(Optional.empty());
            }
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable updateLastOnline() {
        return null;
    }
}
