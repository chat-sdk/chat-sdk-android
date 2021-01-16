package app.xmpp.adapter.message.queue;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smack.packet.Stanza;

import java.util.Date;

import app.xmpp.adapter.defines.XMPPDefines;

public class OutgoingStanza {

    public static String Delay = "delay";
    public static String DelayXMLNS = "co:chatsdk:delay";

    public Stanza stanza;
    public Date date = new Date();
    public boolean sent = false;
    public double delay = 10.0;

    public OutgoingStanza(Stanza stanza) {
        this.stanza = stanza;
    }

    public void markSent() {
        sent = true;
    }

    public String elementID() {
        return stanza.getStanzaId();
    }

    public boolean isSent() {
        return sent;
    }

    public boolean isDue() {
        return -delay() > delay;
    }

    public Long delay() {
        return date.getTime() - new Date().getTime();
    }

    public void willTrySend() {
        stanza.removeExtension(XMPPDefines.Extras, XMPPDefines.DelayNamespace);
        ExtensionElement extension = StandardExtensionElement.builder(XMPPDefines.Extras, XMPPDefines.DelayNamespace)
                .addElement(Delay, delay().toString())
                .build();
        stanza.addExtension(extension);
    }
}
