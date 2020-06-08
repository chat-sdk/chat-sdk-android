package co.chatsdk.xmpp.module;

import co.chatsdk.xmpp.utils.XMPPServer;
import sdk.guru.common.BaseConfig;

public class XMPPConfig<T> extends BaseConfig<T> {

    public String domain;
    public String hostAddress;
    public int port = 5222;
    public String resource = null;
//    public boolean xmppSslEnabled;
//    public boolean xmppAcceptAllCertificates;
//    public boolean xmppDisableHostNameVerification;
//    public boolean xmppAllowClientSideAuthentication;
    public boolean compressionEnabled = true;
    public String securityMode = "disabled";
    public int mucMessageHistoryDownloadLimit = 20;
    public int messageHistoryDownloadLimit = 30;
    public boolean debugEnabled = false;

    public boolean allowServerConfiguration = true;

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

    /**
     * Set the XMPP server details
     * @param hostAddress XMPP server address
     * @param domain XMPP server domain
     * @param port
     * @param resource
     * @return
     */
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

    /**
     * How many historic messages to load when joining a multi-user chat
     * @param limit
     * @return
     */
    public XMPPConfig<T> setMucMessageHistoryDownloadLimit(int limit) {
        this.mucMessageHistoryDownloadLimit = limit;
        return this;
    }

    /**
     * How many historic 1-to-1 messages to load
     * @param limit
     * @return
     */
    public XMPPConfig<T> setMessageHistoryDownloadLimit(int limit) {
        this.messageHistoryDownloadLimit = limit;
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

    /**
     * Is XMPP Compression enabled
     * @param compressionEnabled
     * @return
     */
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

    /**
     * Enable debug mode
     * @param debugEnabled
     * @return
     */
    public XMPPConfig<T> setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        return this;
    }

    public XMPPServer getServer() {
        return new XMPPServer(hostAddress, domain, port, resource);
    }

    /**
     * Allow the user to define a custom server
     * @param allowServerConfiguration
     * @return
     */
    public XMPPConfig<T> setAllowServerConfiguration(boolean allowServerConfiguration) {
        this.allowServerConfiguration = allowServerConfiguration;
        return this;
    }


}
