package co.chatsdk.xmpp.handlers;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.handlers.BlockingHandler;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.util.ArrayListSupplier;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ben on 8/16/17.
 */

public class XMPPBlockingHandler implements BlockingHandler {

    @Override
    public Completable blockUser(final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                ArrayList<Jid> toBlock = new ArrayList<>();
                Jid jid = JidCreate.bareFrom(user.getEntityID());

                toBlock.add(jid);
                XMPPManager.shared().blockingCommandManager().blockContacts(toBlock);

                // Set the user as offline
                user.setAvailability(Availability.Unavailable);
                Localpart local = jid.getLocalpartOrNull();
                String username = local != null ? local.toString() : "";
                user.setName(username + " ("+ AppContext.shared().context().getString(co.chatsdk.ui.R.string.blocked)+")");

                user.update();

                e.onComplete();
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable unblockUser(final User user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull final CompletableEmitter e) throws Exception {
                ArrayList<Jid> toBlock = new ArrayList<>();
                Jid jid = JidCreate.bareFrom(user.getEntityID());
                toBlock.add(jid);
                XMPPManager.shared().blockingCommandManager().unblockContacts(toBlock);

                XMPPManager.shared().userManager.updateUserFromVCard(jid).subscribe(new Consumer<User>() {
                    @Override
                    public void accept(@NonNull User user) throws Exception {
                        e.onComplete();
                    }
                });
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<Boolean> isBlocked(final User user) {
        try {
            return XMPPManager.shared().userManager.userBlocked(JidCreate.bareFrom(user.getEntityID()));
        }
        catch (XmppStringprepException e) {
            return Single.error(e);
        }
    }
}
