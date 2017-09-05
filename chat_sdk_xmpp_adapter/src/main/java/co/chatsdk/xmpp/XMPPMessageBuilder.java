package co.chatsdk.xmpp;

import com.google.android.gms.maps.model.LatLng;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;

import java.util.HashMap;

import co.chatsdk.xmpp.defines.XMPPDefines;

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

    public XMPPMessageBuilder setLocation (LatLng latLng) {
        message.setBody("http://maps.google.com/maps?z=12&t=m&q=loc:" + latLng.latitude + "+" + latLng.longitude);
        return this;
    }

    public XMPPMessageBuilder setType (Integer type) {
        extensionBuilder.addElement(XMPPDefines.Type, type.toString());
        return this;
    }

    public XMPPMessageBuilder setEntityID (String entityID) {
        message.setStanzaId(entityID);
        return this;
    }

    public Message build () {
        message.addExtension(extensionBuilder.build());
        return message;
    }


}
