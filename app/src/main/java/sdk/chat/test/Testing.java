package sdk.chat.test;

import android.content.Context;
import android.os.Build;

import co.chatsdk.core.session.Config;
import co.chatsdk.core.utils.Device;
import co.chatsdk.xmpp.module.XMPPConfig;

public class Testing {

    public static XMPPConfig myOpenFire(XMPPConfig config) {
        return config.xmpp("bear", "185.62.137.45", 5222, null);
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
        return config.xmpp("18.216.137.86", "18.216.137.86", 5222, null);
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
