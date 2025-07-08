package app.xmpp.receipts;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jxmpp.jid.impl.JidCreate;

import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.interfaces.ThreadType;
import app.xmpp.adapter.XMPPManager;
import app.xmpp.adapter.defines.XMPPDefines;

public class ReadReceiptHelper {

    public static Message getAck (String messageId, ThreadX thread) throws Exception {

        Message ack = new Message();

        if (thread.typeIs(ThreadType.Group)) {
            MultiUserChat chat = XMPPManager.shared().mucManager.chatForThreadID(thread.getEntityID());
            ack.setTo(chat.getRoom());
            ack.setType(Message.Type.groupchat);
        } else {
            ack.setType(Message.Type.chat);
            ack.setTo(JidCreate.from(thread.getEntityID()));
        }

        ack.addExtension(new DeliveryReceipt(messageId));

//        ExtensionElement extension = StandardExtensionElement.builder(XMPPDefines.Extras, XMPPDefines.MessageReadNamespace).build();
//        ack.addExtension(extension);

        return ack;
    }

    public static Message getReadAck (String messageId, ThreadX thread) throws Exception {
        Message ack = getAck(messageId, thread);
        ExtensionElement extension = StandardExtensionElement.builder(XMPPDefines.Extras, XMPPDefines.MessageReadNamespace).build();
        ack.addExtension(extension);
        return ack;
    }
}
