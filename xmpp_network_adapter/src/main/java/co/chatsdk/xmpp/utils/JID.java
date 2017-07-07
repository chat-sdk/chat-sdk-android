package co.chatsdk.xmpp.utils;

import co.chatsdk.xmpp.XMPPManager;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 05/07/2017.
 */

public class JID {

    private String user;
    private String domain;
    private String resource;

    public JID (String user, String domain) {
        this.user = user;
        this.domain = domain;
    }

    public JID (String user, String domain, String resource) {
        this(user, domain);
        this.resource = resource;
    }

    /**
     * The JID is in the form [user]@[domain]/[resource]
     * @param jid
     */
    public JID (String jid) {

        // The bare ID i.e. [user]@[domain]
        String bare = null;

        if(hasResource(jid)) {
            String [] resourceSplit = jid.split("/");
            resource = resourceSplit[1];
            bare = resourceSplit[0];
        }

        // If there is no resource, then the JID is only the bare ID
        bare = bare == null ? jid : bare;

        if(hasDomain(bare)) {
            String [] bareSplit = bare.split("@");
            user = bareSplit[0];
            domain = bareSplit[1];
        }
        else {
            user = bare;
            domain = XMPPManager.shared().serviceName;
        }

    }

    private boolean hasResource (String jid) {
        return jid.split("/").length == 2;
    }

    private boolean hasDomain (String jid) {
        return jid.split("@").length == 2;
    }

    public String bare () {
        if(domain == null) {
            Timber.v("WARNING: Bare JID used when no domain is set");
        }
        return user + "@" + domain;
    }

    public String full () {
        return bare() + "/" + resource;
    }

    public String user () {
        return user;
    }

}
