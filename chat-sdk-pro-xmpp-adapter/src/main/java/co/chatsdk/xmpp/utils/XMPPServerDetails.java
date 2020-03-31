package co.chatsdk.xmpp.utils;

import co.chatsdk.core.utils.StringChecker;

/**
 * Created by ben on 10/26/17.
 */

public class XMPPServerDetails {

    String user = null;
    String domain = null;
    int port = -1;

    // We expect the user in the form:
    // user@domain:port
    public XMPPServerDetails (String userAlias) {

        // Get the port
        int checkForPort = userAlias.indexOf(":");
        if (checkForPort != -1) {
            String portString = userAlias.substring(checkForPort + 1);
            if (!StringChecker.isNullOrEmpty(portString)) {
                // Set new port.
                port = Integer.valueOf(portString);
            }
            userAlias = userAlias.substring(0, checkForPort - 1);
        }

        int checkForAt = userAlias.indexOf("@");
        if(checkForAt != -1) {
            domain = userAlias.substring(checkForAt + 1);
            user = userAlias.substring(checkForAt - 1);
        }
        else {
            user = userAlias;
        }
    }

    public boolean hasUser () {
        return !StringChecker.isNullOrEmpty(user);
    }

    public boolean hasPort () {
        return port != -1;
    }

    public boolean hasDomain () {
        return !StringChecker.isNullOrEmpty(domain);
    }

    public String getUser() {
        return user;
    }

    public String getDomain() {
        return domain;
    }

    public int getPort() {
        return port;
    }
}
