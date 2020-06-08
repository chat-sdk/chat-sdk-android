package co.chatsdk.xmpp.utils;

import org.jivesoftware.smack.packet.Message;

import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;

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

    public String from() {
        if (message.getType() != Message.Type.groupchat) {
            return message.getFrom().asBareJid().toString();
        }
        return null;
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
