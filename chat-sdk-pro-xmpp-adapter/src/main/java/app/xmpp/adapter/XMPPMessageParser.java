package app.xmpp.adapter;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smackx.delay.packet.DelayInformation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.xmpp.adapter.defines.XMPPDefines;
import app.xmpp.adapter.utils.XMPPMessageParseHelper;
import app.xmpp.adapter.utils.XMPPMessageWrapper;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.types.ReadStatus;

/**
 * Created by benjaminsmiley-andrews on 11/07/2017.
 */

public class XMPPMessageParser {

    public static void parse(final List<org.jivesoftware.smack.packet.Message> xmppMessages) {

        Map<String, List<XMPPMessageWrapper>> threadMessageMap = new HashMap<>();

        for (org.jivesoftware.smack.packet.Message message: xmppMessages) {
            XMPPMessageWrapper xmw = XMPPMessageWrapper.with(message);

            List<XMPPMessageWrapper> messages = threadMessageMap.get(xmw.getThreadEntityID());
            if (messages == null) {
                messages = new ArrayList<>();
                threadMessageMap.put(xmw.getThreadEntityID(), messages);
            }

            messages.add(xmw);
        }

        for (String threadID: threadMessageMap.keySet()) {
            // First message
            List<XMPPMessageWrapper> messages = threadMessageMap.get(threadID);
            if (messages != null) {
                Thread thread = XMPPMessageParseHelper.getThread(messages.get(0).getMessage());
                for (XMPPMessageWrapper message: messages) {
                    String from = message.from();
                    if (from != null) {
                        addMessageToThread(thread, message, from);
                    }
                }
            }
        }
    }

    public static Message addMessageToThread(Thread thread, final XMPPMessageWrapper xmppMessage, String from) {
        Message message = buildMessage(xmppMessage, from);

        ChatSDK.hook().executeHook(HookEvent.MessageReceived, new HashMap<String, Object>() {{
            put(HookEvent.Message, message);
            put(HookEvent.Thread, thread);
            put(HookEvent.IsNew_Boolean, true);
        }}).subscribe(ChatSDK.events());

        thread.addMessage(message);
        updateReadReceipts(message, xmppMessage);
        return message;
    }

    public static Message buildMessage(final XMPPMessageWrapper xmr, String from) {

        Message message = ChatSDK.db().fetchEntityWithEntityID(xmr.getMessage().getStanzaId(), Message.class);
        if (message == null) {
            message = ChatSDK.db().createEntity(Message.class);
            message.setEntityID(xmr.getMessage().getStanzaId());
        }

        message.setText(xmr.getMessage().getBody());

        DelayInformation delay = xmr.getMessage().getExtension(DelayInformation.ELEMENT, DelayInformation.NAMESPACE);
        if(delay != null) {
            // If there is a difference between the server and local time...
            message.setDate(XMPPManager.shared().serverToClientTime(delay.getStamp()));
        }
        else {
            message.setDate(new Date());
        }

        message.setMessageStatus(MessageSendStatus.Sent);

        // Does the message have an extension?
        ExtensionElement extension = xmr.getMessage().getExtension(XMPPDefines.Extras, XMPPDefines.MessageNamespace);
        if(extension instanceof StandardExtensionElement) {
            StandardExtensionElement extras = (StandardExtensionElement) extension;

            String type = extras.getFirstElement(XMPPDefines.Type).getText();
            message.setType(Integer.parseInt(type));

            for(StandardExtensionElement element : extras.getElements()) {
                message.setValueForKey(element.getText(), element.getElementName());
            }
        }
        else {
            message.setMessageType(new MessageType(MessageType.Text));
        }

        User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, from);

        message.setSender(user);
        message.update();


        return message;
    }

    public static void updateReadReceipts(Message message, XMPPMessageWrapper xmr) {
        // As soon as the message is received, we can set it as read
        if (message.getSender().isMe()) {
            message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.read(), new Date());
        } else {
            if (xmr.isGroupChat() && ChatSDK.readReceipts() != null) {
                ChatSDK.readReceipts().markDelivered(message);
            }
            message.markDelivered();
        }
    }
}
