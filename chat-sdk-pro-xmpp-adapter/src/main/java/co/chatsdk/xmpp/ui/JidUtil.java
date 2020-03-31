package co.chatsdk.xmpp.ui;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

public class JidUtil {

    public static String toEntityID(Jid jid) {
        return jid.asBareJid().toString();
    }

    public static Jid fromEntityID(String entityID) throws Exception {
        return JidCreate.entityBareFrom(entityID);
    }

}
