package app.xmpp.adapter.utils;

import android.content.Context;

import app.xmpp.adapter.module.XMPPModule;
import sdk.chat.core.utils.Device;
import sdk.chat.core.utils.StringChecker;

public class XMPPServer {
    public String address;
    public String domain;
    public int port;
    public String resource;

    public XMPPServer(String address) {
        this(address, address, 0, null);
    }

    public XMPPServer(String address, int port, String resource) {
        this(address, address, port, resource);
    }

    public XMPPServer(String address, String domain, int port, String resource) {
        this.address = address;
        if (domain == null) {
            domain = address;
        }
        this.domain = domain;
        this.port = port;
        if(resource == null) {
            resource = Device.name();
        }
        this.resource = resource;
    }

    public XMPPServer() {
        this(XMPPModule.config().hostAddress, XMPPModule.config().domain, XMPPModule.config().port, XMPPModule.config().resource);
    }

    public void updateIfNull(XMPPServer server) {
        if (StringChecker.isNullOrEmpty(address)) {
            address = server.address;
        }
        if (StringChecker.isNullOrEmpty(domain)) {
            domain = server.domain;
        }
        if (port == 0) {
            port = server.port;
        }
        if (StringChecker.isNullOrEmpty(resource)) {
            resource = server.resource;
        }
    }

    public void updateIfNullFromDefault(Context context) {
        updateIfNull(defaultServer(context));
    }

    public XMPPServer defaultServer(Context context) {
        return new XMPPServer(null, 5222, Device.name());
    }

    public boolean isValid() {
        return !StringChecker.isNullOrEmpty(address);
    }

    public static XMPPServer from(String username) throws Exception {
        XMPPServerDetails details = new XMPPServerDetails(username);
        return details.getServer();
    }

}
