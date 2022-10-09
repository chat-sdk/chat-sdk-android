package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smackx.carbons.CarbonCopyReceivedListener;
import org.jivesoftware.smackx.carbons.packet.CarbonExtension;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jxmpp.jid.EntityBareJid;
import org.pmw.tinylog.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.xmpp.adapter.ConnectionManager;
import app.xmpp.adapter.XMPPManager;
import app.xmpp.adapter.defines.XMPPDefines;
import app.xmpp.adapter.message.queue.OutgoingStanza;
import app.xmpp.adapter.utils.PublicKeyExtras;
import app.xmpp.adapter.utils.XMPPMessageWrapper;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.types.ReadStatus;
import sdk.chat.core.utils.StringChecker;


public class XMPPMessageListener implements IncomingChatMessageListener, OutgoingChatMessageListener, MessageListener, CarbonCopyReceivedListener {

    @Override
    public void newIncomingMessage(EntityBareJid fromJID, Message message, Chat chat) {
        // Get the thread here before we parse the message. If the thread is null,
        // it will be created when we parse the message

        Logger.debug("Incoming: " + message.getBody());

        XMPPMessageWrapper xmr = new XMPPMessageWrapper(message);
        if (xmr.isSilent()) {
            return;
        }

        if (message.getLanguage() == null) {
            message.setLanguage("en");
        }

        if (xmr.isOneToOne()) {
            Thread thread = xmr.getThread();
            addMessageToThread(thread, xmr, true);
        }

        ChatStateExtension chatState = xmr.chatStateExtension();
        if (chatState != null) {
            // TODO: Check if group messages ever come through here
            User user = xmr.user();
            XMPPManager.shared().typingIndicatorManager.handleMessage(message, user);
            Logger.debug("Chat State: " + chatState.getChatState());
        }
    }

    @Override
    public void processMessage(Message message) {
        XMPPMessageWrapper xmr = new XMPPMessageWrapper(message);
        if (xmr.isSilent()) {
            return;
        }

//        if (xmr.messageType() == null) {
//            return;
//        }

            // This is a message directly from the MUC
        if (message.getFrom().hasNoResource()) {
            Logger.debug("Subject?");
            String subject = message.getSubject();
            return;
        }

        if (xmr.isDeliveryReceipt()) {
            Logger.debug("MUC: isReadExtension");
            XMPPManager.shared().receiptReceivedListener.onReceiptReceived(message.getFrom(), message.getTo(), xmr.deliveryReceipt().getId(), message);
        } else if (xmr.isChatState()) {
            XMPPManager.shared().typingIndicatorManager.handleMessage(message, xmr.user());
        } else {
            Thread thread = xmr.getThread();
            addMessageToThread(thread, xmr, true);
        }
    }

    public void parse(final List<XMPPMessageWrapper> messageWrappers, boolean notify) {

        for (XMPPMessageWrapper xmr : messageWrappers) {
            if (xmr.isSilent()) {
                continue;
            }
            // This could be an invite. In that case it shouldn't be necessary to handle it because
            // if we joined before, the room should be bookmarked
            String messageId = xmr.getMessage().getStanzaId();

            // The message already exists or there is no message id
            if (messageId == null || ChatSDK.db().fetchEntityWithEntityID(messageId, sdk.chat.core.dao.Message.class) != null) {
                continue;
            }

//            List<XMPPMessageWrapper> messages = threadMessageMap.get(xmr.threadEntityID());
//            if (messages == null) {
//                messages = new ArrayList<>();
//                threadMessageMap.put(xmr.threadEntityID(), messages);
//            }
//
//            messages.add(xmr);

            Logger.debug("Incoming: Parse: " + xmr.body());

            addMessageToThread(xmr, notify);
        }

        if (!notify) {
            ChatSDK.events().source().accept(NetworkEvent.threadsUpdated());
        }

    }

    public sdk.chat.core.dao.Message addMessageToThread(final XMPPMessageWrapper xmr, boolean notify) {
        return addMessageToThread(null, xmr, notify);
    }

    public sdk.chat.core.dao.Message addMessageToThread(Thread thread, final XMPPMessageWrapper xmr, boolean notify) {
        if (xmr.isSilent()) {
            return null;
        }

        if (xmr.getMessage().getStanzaId() == null) {
            return null;
        }

        final Thread finalThread = thread != null ? thread : xmr.getThread();
        if (finalThread == null) {
            return null;
        }

        // If the thread has been deleted then just continue
        if (finalThread.isDeleted() && finalThread.getLoadMessagesFrom() != null && finalThread.getLoadMessagesFrom().after(xmr.date())) {
            return null;
        }

//        // Here we can update the last online time
//        String bare = ChatSDK.currentUserID();
//        if (bare != null) {
//            // Why do we add 1000?
//            Date date = new Date(xmr.rawDate().getTime() + 1000);
//
//            ConnectionManager manager = XMPPManager.shared().connectionManager();
//            if (finalThread.typeIs(ThreadType.Group)) {
//                manager.updateLastOnline(bare, thread.getEntityID(), date);
//            } else if(XMPPManager.shared().xmppMamManager().isLoaded()) {
//                manager.updateLastOnline(bare, date);
//            }
//        }
        // Here we can update the last online time
        String bare = ChatSDK.currentUserID();
        if (bare != null) {
            Date date = new Date(xmr.date().getTime());

            // Add an offset of 1 to avoid duplicate messages from MAM
            ConnectionManager manager = XMPPManager.shared().connectionManager();
            if (finalThread.typeIs(ThreadType.Group)) {
                manager.updateLastOnline(bare, finalThread.getEntityID(), date, 1);
            } else if(XMPPManager.shared().xmppMamManager().isLoaded()) {
                manager.updateLastOnline(bare, date, 1);
            }
        }

        sdk.chat.core.dao.Message message = buildMessage(xmr, notify);

        ChatSDK.hook().executeHook(HookEvent.MessageReceived, new HashMap<String, Object>() {{
            put(HookEvent.Message, message);
            put(HookEvent.Thread, finalThread);
            put(HookEvent.IsNew_Boolean, true);
        }}).subscribe(ChatSDK.events());

        if (finalThread.typeIs(ThreadType.Private1to1)) {
            finalThread.setDeleted(false);
        }

        finalThread.addMessage(message, notify);
        updateReadReceipts(message, xmr, notify);

        return message;
    }

    public sdk.chat.core.dao.Message buildMessage(final XMPPMessageWrapper xmr, boolean notify) {

        // Check to see if the message exists already...
        sdk.chat.core.dao.Message message = ChatSDK.db().fetchMessageWithEntityID(xmr.getMessage().getStanzaId());
        boolean exists = message != null;
        if (!exists) {
//            message = new sdk.chat.core.dao.Message();
//            message.setEntityID(xmr.getMessage().getStanzaId());
            message = ChatSDK.db().fetchOrCreateMessageWithEntityID(xmr.getMessage().getStanzaId());
        }

        // If there is a difference between the server and local time...
        message.setDate(xmr.date());

//        ChatSDK.db().insertOrReplaceEntity(message);

        // Is there a delay sending it?
        StandardExtensionElement delayExtension = xmr.delayExtra();
        if (delayExtension != null) {
            StandardExtensionElement delayElement = delayExtension.getFirstElement(OutgoingStanza.Delay, OutgoingStanza.DelayXMLNS);
            if (delayElement != null) {
                try {
                    Double timeDelay = Double.parseDouble(delayElement.getText());
                    Date newDate = new Date(message.getDate().getTime() + timeDelay.longValue() * 1000);
                    message.setDate(newDate);
                } catch (Exception e) {}
            }
        }

        StandardExtensionElement extras = xmr.extras();
        if (extras != null) {

            if (extras.getFirstElement(XMPPDefines.Type) != null) {
                String type = extras.getFirstElement(XMPPDefines.Type).getText();
                message.setType(Integer.parseInt(type));
            } else {
                message.setType(MessageType.Text);
            }

            // Handle Meta
            Map<String, Object> meta = new HashMap<>();

            for(StandardExtensionElement element : extras.getElements()) {
                meta.put(element.getElementName(), element.getText());
//                message.setValueForKey(element.getText(), element.getElementName());
            }

            if (!exists) {
                if (ChatSDK.encryption() != null && meta.containsKey(Keys.MessageEncryptedPayloadKey)) {
                    Object dataObject = meta.get(Keys.MessageEncryptedPayloadKey);
                    if (dataObject instanceof String) {
                        String data = (String) dataObject;

                        try {
                            Map<String, Object> encryptedMeta = ChatSDK.encryption().decrypt(data);
                            if (encryptedMeta != null) {
                                meta = encryptedMeta;
                            }
                        } catch (Exception e) {
                            message.setEncryptedText(data);
                        }
                    }
                }
                message.setMetaValues(meta);

//                String body = xmr.body();
//
//                if (body != null) {
//                    message.setText(body);
//                } else {
//                    Logger.debug("No Body");
//                }

                String body = xmr.body();

                if (!StringChecker.isNullOrEmpty(body) && StringChecker.isNullOrEmpty(message.getText())) {
                    message.setText(body);
                }
            }

        } else {
            message.setMessageType(new MessageType(MessageType.Text));
        }

        if (message.getText().isEmpty() && message.typeIs(MessageType.Text) && xmr.body() != null) {
            message.setText(xmr.body());
        }

        User user = xmr.user();

        message.setSender(user);

        ChatSDK.db().update(message, false);

        message.setMessageStatus(MessageSendStatus.None, notify);

        // Handle the public keys
        if (user != null) {
            PublicKeyExtras.handle(user.getEntityID(), xmr.getMessage());
        }

        return message;
    }

    public void updateReadReceipts(sdk.chat.core.dao.Message message, XMPPMessageWrapper xmr, boolean notify) {
        // As soon as the message is received, we can set it as read
        if (message.getSender().isMe()) {
            message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.read(), new Date(), notify);
        } else {
            if (xmr.isGroupChat() && ChatSDK.readReceipts() != null) {
                ChatSDK.readReceipts().markDelivered(message);
            }
            message.markDelivered(notify);
        }
    }


    @Override
    public void onCarbonCopyReceived(CarbonExtension.Direction direction, Message carbonCopy, Message wrappingMessage) {
        XMPPMessageWrapper xmr = new XMPPMessageWrapper(carbonCopy);
        if (xmr.deliveryReceipt() != null) {
            XMPPManager.shared().receiptReceivedListener.onReceiptReceived(carbonCopy.getFrom(), carbonCopy.getTo(), xmr.deliveryReceipt().getId(), carbonCopy);
        } else {
            if (carbonCopy.getStanzaId() != null) {
                addMessageToThread(xmr, true);
            }
        }

        ChatStateExtension chatState = xmr.chatStateExtension();
        if (chatState != null) {
            // TODO: Check if group messages ever come through here
            // Because it's a carbon, the to field is actually the thread...
            String from = carbonCopy.getTo().asBareJid().toString();

            User user = ChatSDK.db().fetchUserWithEntityID(from);
            Thread thread = ChatSDK.db().fetchEntityWithEntityID(from, Thread.class);
//            User user = xmr.user();
            XMPPManager.shared().typingIndicatorManager.handleMessage(null, carbonCopy, user, thread);
        }

        Logger.debug("Carbon received");
    }

    @Override
    public void newOutgoingMessage(EntityBareJid to, MessageBuilder messageBuilder, Chat chat) {

    }
}
