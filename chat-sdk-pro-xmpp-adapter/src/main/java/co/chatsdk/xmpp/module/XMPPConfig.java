package co.chatsdk.xmpp.module;

import android.content.Context;

public class XMPPConfig {

    public String xmppDomain;
    public String xmppHostAddress;
    public int xmppPort;
    public String xmppResource = "Android";
    public boolean xmppSslEnabled;
    public boolean xmppAcceptAllCertificates;
    public boolean xmppDisableHostNameVerification;
    public boolean xmppAllowClientSideAuthentication;
    public boolean xmppCompressionEnabled;
    public String xmppSecurityMode = "disabled";
    public int xmppMucMessageHistory = 20;
    public boolean debugEnabled = false;

    public String xmppMucServiceName = "conference";

    /**
     * In this case the resource will be set to the device's IMEI number
     *
     * @param domain
     * @param hostAddress
     * @param port
     * @return
     */
    public XMPPConfig xmpp(String domain, String hostAddress, int port) {
        return xmpp(domain, hostAddress, port, null);
    }

    public XMPPConfig xmpp(String domain, String hostAddress, int port, String resource) {
        return xmpp(domain, hostAddress, port, resource, false);
    }

    public XMPPConfig xmpp(String domain, String hostAddress, int port, String resource, boolean sslEnabled) {
        this.xmppDomain = domain;
        this.xmppHostAddress = hostAddress;
        this.xmppPort = port;
        this.xmppResource = resource;
        this.xmppSslEnabled = sslEnabled;
        return this;
    }

    public XMPPConfig xmppSslEnabled(boolean sslEnabled) {
        this.xmppSslEnabled = sslEnabled;
        return this;
    }

    public XMPPConfig xmppMucMessageHistory(int history) {
        this.xmppMucMessageHistory = history;
        return this;
    }

    public XMPPConfig xmppDisableHostNameVerification(boolean disableHostNameVerification) {
        this.xmppDisableHostNameVerification = disableHostNameVerification;
        return this;
    }

    /**
     * This setting is not currently implemented
     *
     * @param allowClientSideAuthentication
     * @return
     */
    public XMPPConfig xmppAllowClientSideAuthentication(boolean allowClientSideAuthentication) {
        this.xmppAllowClientSideAuthentication = allowClientSideAuthentication;
        return this;
    }

    public XMPPConfig xmppCompressionEnabled(boolean compressionEnabled) {
        this.xmppCompressionEnabled = compressionEnabled;
        return this;
    }


    public XMPPConfig xmppAcceptAllCertificates(boolean acceptAllCertificates) {
        this.xmppAcceptAllCertificates = acceptAllCertificates;
        return this;
    }

    /**
     * Set TSL security mode. Allowable values are
     * "required"
     * "ifpossible"
     * "disabled"
     *
     * @param securityMode
     * @return
     */
    public XMPPConfig xmppSecurityMode(String securityMode) {
        this.xmppSecurityMode = securityMode;
        return this;
    }

    public void setXmppMucServiceName(String xmppMucServiceName) {
        this.xmppMucServiceName = xmppMucServiceName;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

}
