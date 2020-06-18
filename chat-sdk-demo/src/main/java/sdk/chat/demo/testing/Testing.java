package sdk.chat.demo.testing;

import android.content.Context;

import app.xmpp.adapter.module.XMPPConfig;
import app.xmpp.adapter.module.XMPPModule;
import sdk.chat.core.session.Config;
import sdk.chat.core.utils.Device;

public class Testing {

    public static XMPPConfig<XMPPModule> myOpenFire(XMPPConfig<XMPPModule> config) {
        return config.setXMPP( "185.62.137.45", "bear");
    }

    public static Config myOpenFire(Context context, Config config) {
        if (Device.honor(context)) {
            config.setDebugUsername("1b");
            config.setDebugPassword("123");
        } else if (Device.nexus(context)) {
            config.setDebugUsername("2b");
            config.setDebugPassword("123");
        } else {
            config.setDebugUsername("3b");
            config.setDebugPassword("123");
        }
        return config;
    }

    public static XMPPConfig rameshEJabberd(XMPPConfig config) {
        return config.setXMPP("18.216.137.86");
    }

    public static Config rameshEJabberd(Context context, Config config) {
        if (Device.honor(context)) {
            config.setDebugUsername("chatsdkaccount1");
            config.setDebugPassword("sEcureXMpp123!#$");
        } else {
            config.setDebugUsername("chatsdkaccount2");
            config.setDebugPassword("sEcureXMpp456!#$");
        }
        return config;
    }

}
