package app.xmpp.adapter;


import android.location.Location;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jxmpp.jid.BareJid;

import java.util.HashMap;

import app.xmpp.adapter.defines.XMPPDefines;
import app.xmpp.adapter.utils.PublicKeyExtras;
import sdk.chat.core.utils.GoogleUtils;

/**
 * Created by benjaminsmiley-andrews on 11/07/2017.
 */

public class XMPPMessageBuilder {

    Message message = new Message();

    StandardExtensionElement.Builder extensionBuilder = StandardExtensionElement.builder(XMPPDefines.Extras, XMPPDefines.MessageNamespace);

    public XMPPMessageBuilder setValues (HashMap<String, Object> values) {
        for(String key : values.keySet()) {
            extensionBuilder.addElement(key, values.get(key).toString());
        }
        return this;
    }

    public XMPPMessageBuilder setBody (String body) {
        message.setBody(body);
        return this;
    }

    public XMPPMessageBuilder setLocation (Location latLng) {
        message.setBody(GoogleUtils.getMapWebURL(latLng));
        return this;
    }

    public XMPPMessageBuilder setType (Integer type) {
        extensionBuilder.addElement(XMPPDefines.Type, type.toString());
        return this;
    }

    public XMPPMessageBuilder setAsChatType() {
        message.setType(Message.Type.chat);
        return this;
    }

    public XMPPMessageBuilder setAsGroupChatType() {
        message.setType(Message.Type.groupchat);
        return this;
    }

    public XMPPMessageBuilder setTo(BareJid to) {
        message.setTo(to);
        return this;
    }

    public XMPPMessageBuilder setEntityID (String entityID) {
        message.setStanzaId(entityID);
        return this;
    }

//    public XMPPMessageBuilder enableArchiving () {
//        message.add
//    }


    public Message build () {
        message.addExtension(extensionBuilder.build());

        PublicKeyExtras.addTo(message);

        return message;
    }


}
