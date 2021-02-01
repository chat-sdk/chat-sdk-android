package app.xmpp.adapter.module;

import app.xmpp.adapter.handlers.XMPPNetworkAdapter;
import app.xmpp.adapter.utils.XMPPServer;
import sdk.chat.core.base.BaseNetworkAdapter;
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
    public int messageHistoryDownloadLimit = 100;
    public boolean debugEnabled = false;
    public int pingInterval = 15;
    public boolean streamManagementEnabled = true;

    public boolean allowServerConfiguration = true;


    public boolean sendSystemMessageForRoleChange = true;
    public boolean sendSystemMessageForAffiliationChange = false;

    public Class<? extends BaseNetworkAdapter> networkAdapter = XMPPNetworkAdapter.class;

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

    /**
     * Enable or disable stream management
     * @param enabled
     * @return
     */
    public XMPPConfig<T> setStreamManagementEnabled(boolean enabled) {
        this.streamManagementEnabled = streamManagementEnabled;
        return this;
    }

    /**
     * Ping interval in seconds
     * @param interval
     * @return
     */
    public XMPPConfig<T> setPingInterval(int interval) {
        this.pingInterval = pingInterval;
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

    /**
     * Override the Firebase network adapter class
     * @param networkAdapter
     * @return
     */
    public XMPPConfig<T> setNetworkAdapter(Class<? extends BaseNetworkAdapter> networkAdapter) {
        this.networkAdapter = networkAdapter;
        return this;
    }

    /**
     * Should we send a local message when our role changes
     * @param sendSystemMessageForRoleChange
     * @return
     */
    public XMPPConfig<T> setSendSystemMessageForRoleChange(boolean sendSystemMessageForRoleChange) {
        this.sendSystemMessageForRoleChange = sendSystemMessageForRoleChange;
        return this;
    }

    /**
     * Should we send a local message when our affiliation changes
     * @param sendSystemMessageForAffiliationChange
     * @return
     */
    public XMPPConfig<T> setSendSystemMessageForAffiliationChange(boolean sendSystemMessageForAffiliationChange) {
        this.sendSystemMessageForAffiliationChange = sendSystemMessageForAffiliationChange;
        return this;
    }


}
