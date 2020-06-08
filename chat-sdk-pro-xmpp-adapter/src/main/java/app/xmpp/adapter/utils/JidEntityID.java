package app.xmpp.adapter.utils;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

public class JidEntityID {

    public static String toEntityID(Jid jid) {
        return jid.asBareJid().toString();
    }

    public static Jid fromEntityID(String entityID) throws Exception {
        return JidCreate.entityBareFrom(entityID);
    }

}
