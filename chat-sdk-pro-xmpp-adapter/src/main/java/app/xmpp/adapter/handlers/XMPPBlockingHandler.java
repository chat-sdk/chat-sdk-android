package app.xmpp.adapter.handlers;

import org.jivesoftware.smackx.blocking.BlockingCommandManager;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.util.ArrayList;

import sdk.chat.core.dao.User;
import sdk.chat.core.defines.Availability;
import sdk.chat.core.handlers.BlockingHandler;
import sdk.chat.core.session.ChatSDK;
import app.xmpp.adapter.XMPPManager;
import io.reactivex.Completable;
import sdk.guru.common.RX;

import static app.xmpp.adapter.defines.XMPPDefines.BlockingCommandNamespace;

/**
 * Created by ben on 8/16/17.
 */

public class XMPPBlockingHandler implements BlockingHandler {

    @Override
    public Completable blockUser(final String userEntityID) {
        return Completable.create(e -> {
            if(!blockingSupported()) {
                e.onComplete();
            }
            else {
                ArrayList<Jid> toBlock = new ArrayList<>();
                Jid jid = JidCreate.bareFrom(userEntityID);

                toBlock.add(jid);
                blockingCommandManager().blockContacts(toBlock);



                User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEntityID);

                // Set the user as offline
                user.setAvailability(Availability.Unavailable);
                Localpart local = jid.getLocalpartOrNull();
                String username = local != null ? local.toString() : "";
                user.setName(username);

                e.onComplete();
            }
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable unblockUser(final String userEntityID) {
        return Completable.defer(() -> {
            if(!blockingSupported()) {
                return Completable.complete();
            }
            else {
                ArrayList<Jid> toUnblock = new ArrayList<>();
                Jid jid = JidCreate.bareFrom(userEntityID);
                toUnblock.add(jid);
                blockingCommandManager().unblockContacts(toUnblock);

                return XMPPManager.shared().userManager.updateUserFromVCard(jid).ignoreElement();
            }
        }).subscribeOn(RX.io());
    }

    @Override
    public Boolean isBlocked(final String userEntityID) {
        if(blockingSupported()) {
            try {
                Jid jid = JidCreate.bareFrom(userEntityID);
                return blockingCommandManager().getBlockList().contains(jid);
            }
            catch (Exception ex) {
                ChatSDK.events().onError(ex);
            }
        }
        return false;
    }

    @Override
    public boolean blockingSupported() {
        return XMPPManager.shared().serviceDiscoveryManager().getFeatures().contains(BlockingCommandNamespace);
    }

    // Blocking
    private BlockingCommandManager blockingCommandManager () {
        return BlockingCommandManager.getInstanceFor(XMPPManager.shared().getConnection());
    }

}

