package co.chatsdk.xmpp.utils;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.pmw.tinylog.Logger;

import java.util.HashMap;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.binders.AvailabilityHelper;
import co.chatsdk.xmpp.XMPPManager;


/**
 * Created by ben on 8/24/17.
 */

public class PresenceHelper {

    public static void updateUserFromPresence (User user, Presence presence) throws XmppStringprepException {
        if (!user.isMe()) {
            HashMap<String, Object> oldMeta = new HashMap<>(user.metaMap());

            String availability = presence.getMode().toString();
            if(presence.getType().equals(Presence.Type.unavailable)) {
                availability = Availability.Unavailable;
            }

            user.setAvailability(availability, false);
            user.setStatus(presence.getStatus(), false);

            boolean wasOnline = user.getIsOnline();

            user.setIsOnline(presence.getType() != Presence.Type.unavailable, false);

            RosterEntry entry = XMPPManager.shared().roster().getEntry(JidCreate.bareFrom(user.getEntityID()));
            if(entry != null) {
                user.setPresenceSubscription(entry.getType().toString(), false);
            }

            if (!oldMeta.entrySet().equals(user.metaMap().entrySet()) || wasOnline != user.getIsOnline()) {
                ChatSDK.events().source().onNext(NetworkEvent.userPresenceUpdated(user));
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
