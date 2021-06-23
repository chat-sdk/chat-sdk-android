package app.xmpp.adapter.utils;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.HashMap;

import app.xmpp.adapter.module.XMPPModule;
import sdk.chat.core.dao.User;
import sdk.chat.core.defines.Availability;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import app.xmpp.adapter.XMPPManager;


/**
 * Created by ben on 8/24/17.
 */

public class PresenceHelper {

    public static void updateUserFromPresence(User user, Presence presence) throws XmppStringprepException {
        if (!user.isMe()) {
            HashMap<String, Object> oldMeta = new HashMap<>(user.metaMap());

            PublicKeyExtras.handle(user.getEntityID(), presence);

            String availability = presence.getMode().toString();
            if(presence.getType().equals(Presence.Type.unavailable)) {
                availability = Availability.Unavailable;
            }

            user.setAvailability(availability, false);
            user.setStatus(presence.getStatus(), false);

            boolean wasOnline = user.getIsOnline();

            boolean online = presence.getType() != Presence.Type.unavailable;
            online = online && XMPPModule.config().onlinePresenceModes.contains(presence.getMode());

            user.setIsOnline(online, false);

            RosterEntry entry = XMPPManager.shared().roster().getEntry(JidCreate.bareFrom(user.getEntityID()));
            if(entry != null) {
                user.setPresenceSubscription(entry.getType().toString(), false);
            }

            if (!oldMeta.entrySet().equals(user.metaMap().entrySet()) || wasOnline != user.getIsOnline()) {
                ChatSDK.events().source().accept(NetworkEvent.userPresenceUpdated(user));
            }
        }
    }

    public static Presence presenceForUser (User user) {
        Presence.Mode mode = Presence.Mode.available;
        String availability = user.getAvailability();

        if(availability != null) {
            if(availability.equalsIgnoreCase(Availability.Away)) {
                mode = Presence.Mode.away;
            }
            if(availability.equalsIgnoreCase(Availability.XA)) {
                mode = Presence.Mode.xa;
            }
            if(availability.equalsIgnoreCase(Availability.Busy)) {
                mode = Presence.Mode.dnd;
            }
        }

        return new Presence(Presence.Type.available, user.getStatus(), 1, mode);
    }

}
