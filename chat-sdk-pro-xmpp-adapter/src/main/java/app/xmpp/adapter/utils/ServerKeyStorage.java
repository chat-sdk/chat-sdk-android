package app.xmpp.adapter.utils;

import android.content.Context;

import sdk.chat.core.utils.StringChecker;

public class ServerKeyStorage extends KeyStorage {

    public ServerKeyStorage (Context context) {
        super(context);
    }

    public static String AddressKey = "address";
    public static String DomainKey = "domain";
    public static String PortKey = "port";
    public static String ResourceKey = "resource";

    public void setAddress(String value) {
        put(AddressKey, value);
    }

    public void setDomain(String value) {
        put(DomainKey, value);
    }

    public void setPort(int value) {
        put(PortKey, value);
    }

    public void setResource(String value) {
        put(ResourceKey, value);
    }

    public String getAddress() {
        return get(AddressKey);
    }

    public String getDomain() {
        return get(DomainKey);
    }

    public int getPort() {
        return getInt(PortKey);
    }

    public String getResource() {
        return get(ResourceKey);
    }

    public boolean valid() {
        return !StringChecker.isNullOrEmpty(getAddress());
    }

    public XMPPServer getServer() {
        return new XMPPServer(getAddress(), getDomain(), getPort(), getResource());
    }

    public void setServer(XMPPServer server) {
        setAddress(server.address);
        setDomain(server.domain);
        setPort(server.port);
        setResource(server.resource);
    }


}
