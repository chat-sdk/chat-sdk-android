package app.xmpp.adapter.message.queue;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smack.packet.Stanza;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import app.xmpp.adapter.defines.XMPPDefines;

public class OutgoingStanza {

    public static String Delay = "delay";
    public static String DelayXMLNS = "co:chatsdk:delay";
    public static Long delay = TimeUnit.SECONDS.toMillis(10);

    public Stanza stanza;
    public Date date = new Date();
    public boolean sent = false;
    public Long due = delay;

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
        return timeInQueue() > due;
    }

    public Long timeInQueue() {
        return new Date().getTime() - date.getTime();
    }

    public Long getDelay() {
        return -TimeUnit.MILLISECONDS.toSeconds(timeInQueue());
    }

    public void willTrySend() {

        due = timeInQueue() + delay;

        stanza.removeExtension(XMPPDefines.Extras, XMPPDefines.DelayNamespace);
        ExtensionElement extension = StandardExtensionElement.builder(XMPPDefines.Extras, XMPPDefines.DelayNamespace)
                .addElement(Delay, getDelay().toString())
                .build();
        stanza.addExtension(extension);
    }
}
