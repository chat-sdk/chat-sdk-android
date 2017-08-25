package co.chatsdk.xmpp.utils;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.xmpp.XMPPManager;

/**
 * Created by ben on 8/24/17.
 */

public class PresenceHelper {

    public static void updateUserFromPresence (User user, Presence presence) throws XmppStringprepException {
        if(presence.getType().toString().equalsIgnoreCase(Availability.Unavailable)){
            user.setOnline(false);
        } else {
            user.setOnline(true);
        }
        user.setAvailability(presence.getMode().toString());
        user.setStatus(presence.getStatus());

        RosterEntry entry = XMPPManager.shared().roster().getEntry(JidCreate.bareFrom(user.getEntityID()));
        if(entry != null) {
            user.setPresenceSubscription(entry.getType().toString());
        }

        user.update();
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

        Presence presence = new Presence(Presence.Type.available, user.getStatus(), 1, mode);
        return presence;
    }

}
