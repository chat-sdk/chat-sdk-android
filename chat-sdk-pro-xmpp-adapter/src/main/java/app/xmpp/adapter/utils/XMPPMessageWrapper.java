package app.xmpp.adapter.utils;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;

import app.xmpp.adapter.defines.XMPPDefines;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;

public class XMPPMessageWrapper {

    public static XMPPMessageWrapper with(Message message) {
        return new XMPPMessageWrapper(message);
    }

    protected Message message;

    public XMPPMessageWrapper(Message message) {
        this.message = message;
    }

    public String getThreadEntityID() {
        if (message.getType() == Message.Type.groupchat) {
            return message.getFrom().asBareJid().toString();
        } else {

            String to = message.getTo().asBareJid().toString();
            String from = message.getFrom().asBareJid().toString();

            User currentUser = ChatSDK.currentUser();
            String threadID = from;

            if (from.equals(currentUser.getEntityID())) {
                threadID = to;
            }

            return threadID;
        }
    }


    public boolean hasAction(Integer action) {
        if (type() == MessageType.Silent) {
            return action().equals(action);
        }
        return false;
    }

    public StandardExtensionElement extras() {
        ExtensionElement element = message.getExtension(XMPPDefines.Extras, XMPPDefines.MessageNamespace);
        if (element instanceof StandardExtensionElement) {
            return  (StandardExtensionElement) element;
        }
        return null;
    }

    public String from() {
        if (message.getType() != Message.Type.groupchat) {
            return message.getFrom().asBareJid().toString();
        }
        return null;
    }

    public User user() {
        String from = from();
        if (from != null) {
            return ChatSDK.core().getUserNowForEntityID(from);
        }
        return null;
    }

    public Integer type() {
        StandardExtensionElement extras = extras();
        if (extras != null) {
            return Integer.parseInt(extras.getFirstElement(XMPPDefines.Type).getText());
        }
        return MessageType.None;
    }

    public Integer action() {
        StandardExtensionElement extras = extras();
        if (extras != null) {
            return Integer.parseInt(extras.getFirstElement(XMPPDefines.Action).getText());
        }
        return MessageType.None;
    }


//    public String to() {
//        return message.getFrom().asBareJid().toString();
//    }

    public Message getMessage() {
        return message;
    }

    public boolean isGroupChat() {
        return message.getType() == Message.Type.groupchat;
    }

}
