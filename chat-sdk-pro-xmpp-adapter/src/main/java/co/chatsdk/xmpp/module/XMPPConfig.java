package co.chatsdk.xmpp.module;

import sdk.guru.common.BaseConfig;
import co.chatsdk.xmpp.utils.XMPPServer;

public class XMPPConfig<T> extends BaseConfig<T> {

    public String domain;
    public String hostAddress;
    public int port = 5222;
    public String resource = null;
//    public boolean xmppSslEnabled;
//    public boolean xmppAcceptAllCertificates;
//    public boolean xmppDisableHostNameVerification;
//    public boolean xmppAllowClientSideAuthentication;
    public boolean compressionEnabled;
    public String securityMode = "disabled";
    public int mucMessageHistory = 20;
    public boolean debugEnabled = false;

    public boolean allowServerConfiguration = true;

    public String mucServiceName = "conference";

    public XMPPConfig(T onBuild) {
        super(onBuild);
    }

    public XMPPConfig<T> setXMPP(String hostAddress) {
        return setXMPP(hostAddress, 0);
    }

    public XMPPConfig<T> setXMPP(String hostAddress, String domain) {
        return setXMPP(hostAddress, domain, 0);
    }

    public XMPPConfig<T> setXMPP(String hostAddress, int port) {
        return setXMPP(hostAddress, hostAddress, port);
    }

    public XMPPConfig<T> setXMPP(String hostAddress, String domain, int port) {
        return setXMPP(hostAddress, domain, port, null);
    }

    public XMPPConfig<T> setXMPP(String hostAddress, String domain, int port, String resource) {
        this.hostAddress = hostAddress;
        this.domain = domain;
        if (port != 0) {
            this.port = port;
        }
        this.resource = resource;
        return this;
    }

//    public XMPPConfig<T> xmppSslEnabled(boolean sslEnabled) {
//        this.xmppSslEnabled = sslEnabled;
//        return this;
//    }

    public XMPPConfig<T> setMucMessageHistory(int history) {
        this.mucMessageHistory = history;
        return this;
    }

//    public XMPPConfig<T> xmppDisableHostNameVerification(boolean disableHostNameVerification) {
//        this.xmppDisableHostNameVerification = disableHostNameVerification;
//        return this;
//    }

    /**
     * This setting is not currently implemented
     *
     * @param allowClientSideAuthentication
     * @return
     */
//    public XMPPConfig<T> xmppAllowClientSideAuthentication(boolean allowClientSideAuthentication) {
//        this.xmppAllowClientSideAuthentication = allowClientSideAuthentication;
//        return this;
//    }
//
    public XMPPConfig<T> setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
        return this;
    }
//
//
//    public XMPPConfig<T> xmppAcceptAllCertificates(boolean acceptAllCertificates) {
//        this.xmppAcceptAllCertificates = acceptAllCertificates;
//        return this;
//    }

    /**
     * Set TSL security mode. Allowable values are
     * "required"
     * "ifpossible"
     * "disabled"
     *
     * @param securityMode
     * @return
     */
    public XMPPConfig<T> setSecurityMode(String securityMode) {
        this.securityMode = securityMode;
        return this;
    }

    public XMPPConfig<T> setMucServiceName(String mucServiceName) {
        this.mucServiceName = mucServiceName;
        return this;
    }

    public XMPPConfig<T> setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        return this;
    }

    public XMPPServer getServer() {
        return new XMPPServer(hostAddress, domain, port, resource);
    }

    public XMPPConfig<T> setAllowServerConfiguration(boolean allowServerConfiguration) {
        this.allowServerConfiguration = allowServerConfiguration;
        return this;
    }

}
