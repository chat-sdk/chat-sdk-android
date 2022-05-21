package app.xmpp.adapter;


import android.location.Location;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jxmpp.jid.BareJid;

import java.util.Map;
import java.util.Objects;

import app.xmpp.adapter.defines.XMPPDefines;
import app.xmpp.adapter.utils.PublicKeyExtras;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.GoogleUtils;

/**
 * Created by benjaminsmiley-andrews on 11/07/2017.
 */

public class XMPPMessageBuilder {

    public static XMPPMessageBuilder create() {
        return new XMPPMessageBuilder();
    }

    Message message = new Message();

    StandardExtensionElement.Builder extensionBuilder = StandardExtensionElement.builder(XMPPDefines.Extras, XMPPDefines.MessageNamespace);

    public XMPPMessageBuilder setValues(Map<String, String> values) {
        // If this is an encrypted message, encrypt it
        for(String key : values.keySet()) {
            extensionBuilder.addElement(key, Objects.requireNonNull(values.get(key)));
        }
        return this;
    }

    public XMPPMessageBuilder setBody(String body) {
        message.setBody(body);
        return this;
    }

    public XMPPMessageBuilder setLocation(Location latLng) {
        message.setBody(GoogleUtils.getMapWebURL(latLng));
        return this;
    }

    public XMPPMessageBuilder setType(Integer type) {
        extensionBuilder.addElement(XMPPDefines.Type, type.toString());
        return this;
    }

    public XMPPMessageBuilder setAsChatType() {
        message.setType(Message.Type.chat);
        return this;
    }

    public XMPPMessageBuilder addDeliveryReceiptRequest() {
        if (!DeliveryReceiptManager.hasDeliveryReceiptRequest(message)) {
            DeliveryReceiptRequest.addTo(message);
        }
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

    public XMPPMessageBuilder setEntityID(String entityID) {
        message.setStanzaId(entityID);
        return this;
    }

//    public XMPPMessageBuilder enableArchiving () {
//        message.add
//    }

    public XMPPMessageBuilder setAction(Integer action) {
        extensionBuilder.addElement(XMPPDefines.Action, action.toString());
        return this;
    }

    public XMPPMessageBuilder addNoRetryExtension() {
        message.addExtension(StandardExtensionElement.builder(XMPPDefines.Extras, XMPPDefines.NoRetryNamespace).build());
        return this;
    }

    public XMPPMessageBuilder addLeaveGroupExtension() {
        return addLeaveGroupExtension(null);
    }

    public XMPPMessageBuilder addLeaveGroupExtension(String userJID) {
        setBody(String.format(ChatSDK.getString(R.string.__left_the_group), ChatSDK.currentUser().getName()));
        setType(MessageType.Silent);
        setAction(MessageType.Action.UserLeftGroup);
        addNoRetryExtension();
        if (userJID != null) {
            extensionBuilder.addElement(XMPPDefines.ID, userJID);
        }
        return this;
    }

    public XMPPMessageBuilder addGroupInviteExtension(BareJid chatId) {
        setBody(" ");
        setType(MessageType.Silent);
        setAction(MessageType.Action.GroupInvite);
        extensionBuilder.addElement(XMPPDefines.ID, chatId.toString());
        addNoRetryExtension();
        return this;
    }

    public Message build() {
        message.addExtension(extensionBuilder.build());

        PublicKeyExtras.addTo(message);

        return message;
    }


}
