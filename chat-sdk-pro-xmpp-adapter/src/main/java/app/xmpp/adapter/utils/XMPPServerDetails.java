package app.xmpp.adapter.utils;

import sdk.chat.core.utils.StringChecker;

/**
 * Created by ben on 10/26/17.
 */

public class XMPPServerDetails {

    protected String user;

    protected String address = null;
    protected int port = 0;
    protected String resource = null;

    // We expect the user in the form:
    // user@domain:port/resource
    public XMPPServerDetails (String userAlias) throws Exception {

        int atIndex = userAlias.indexOf("@");
        int portIndex = userAlias.indexOf(":");
        int resourceIndex = userAlias.indexOf("/");

        if (resourceIndex > 0) {
            resource = userAlias.substring(resourceIndex + 1);
        } else {
            resourceIndex = userAlias.length();
        }

        if (portIndex > 0) {
            port = Integer.parseInt(userAlias.substring(portIndex + 1, resourceIndex));
        } else {
            portIndex = resourceIndex;
        }

        if (atIndex > 0) {
            address = userAlias.substring(atIndex + 1, portIndex);
        } else {
            atIndex = portIndex;
        }

        user = userAlias.substring(0, atIndex);
    }

    public boolean hasUser () {
        return !StringChecker.isNullOrEmpty(user);
    }

    public boolean hasPort () {
        return port != 0;
    }

    public boolean hasDomain () {
        return !StringChecker.isNullOrEmpty(address);
    }

    public String getUser() {
        return user;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public XMPPServer getServer() {
        XMPPServer server = new XMPPServer();
        if (!StringChecker.isNullOrEmpty(address)) {
            server.address = address;
            server.domain = address;
        }
        if (port != 0) {
            server.port = port;
        }
        if (!StringChecker.isNullOrEmpty(resource)) {
            server.resource = resource;
        }
        return server;
    }


}
