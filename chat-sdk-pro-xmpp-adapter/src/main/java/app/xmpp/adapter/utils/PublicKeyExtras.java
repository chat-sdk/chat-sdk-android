package app.xmpp.adapter.utils;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smack.packet.Stanza;

import sdk.chat.core.session.ChatSDK;

import static app.xmpp.adapter.defines.XMPPDefines.Extras;
import static app.xmpp.adapter.defines.XMPPDefines.ID;
import static app.xmpp.adapter.defines.XMPPDefines.PublicKeyNamespace;

public class PublicKeyExtras {

    public static void addTo(Stanza stanza) {
        if (ChatSDK.encryption() != null) {
            ExtensionElement element = PublicKeyExtras.getPublicKeyExtras();
            if (element != null) {
                stanza.addExtension(element);
            }
        }
    }

    public static ExtensionElement getPublicKeyExtras() {
        if (ChatSDK.encryption() != null) {
            String publicKey = ChatSDK.encryption().publicKey();
            if (publicKey == null || publicKey.isEmpty()) {
                return null;
            }
            String privateKeyId = ChatSDK.encryption().privateKeyId();
            StandardExtensionElement.Builder builder = StandardExtensionElement.builder(Extras, PublicKeyNamespace).setText(publicKey);
            if (privateKeyId != null && !privateKeyId.isEmpty()) {
                builder.addAttribute(ID, privateKeyId);
            }
            return builder.build();
        }
        return null;
    }

    public static void handle(String userEntityID, Stanza stanza) {
        ExtensionElement element = stanza.getExtension(Extras, PublicKeyNamespace);
        if (ChatSDK.encryption() != null && element instanceof StandardExtensionElement) {
            StandardExtensionElement extras = (StandardExtensionElement) element;
            String privateKeyId = extras.getAttributeValue(ID);
            String publicKey = extras.getText();
            if (publicKey != null && !publicKey.isEmpty()) {
                ChatSDK.encryption().addPublicKey(userEntityID, privateKeyId, publicKey);
            }
        }
    }

}
