package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.xmpp.XMPPMessageParser;
import co.chatsdk.xmpp.defines.XMPPDefines;
import co.chatsdk.xmpp.utils.JID;
import io.reactivex.functions.BiConsumer;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class XMPPChatMessageListener implements ChatMessageListener {

    @Override
    public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message xmppMessage) {
        // This could be a chatstate message
        if(isOneToOneMessage(xmppMessage)) {
            XMPPMessageParser.parse(xmppMessage).subscribe(new BiConsumer<Message, Throwable>() {
                @Override
                public void accept(Message message, Throwable throwable) throws Exception {
                    NetworkEvent event = NetworkEvent.messageAdded(message.getThread(), message);
                    NM.events().source().onNext(event);
                }
            });
        }

        ChatStateExtension chatState = (ChatStateExtension) xmppMessage.getExtension(ChatStateExtension.NAMESPACE);
        if(chatState != null) {
            chatState.getChatState();
            Timber.v("Chat State: " + chatState.getChatState());
        }

    }

    public boolean isOneToOneMessage (org.jivesoftware.smack.packet.Message xmppMessage) {
        return xmppMessage.getType() == org.jivesoftware.smack.packet.Message.Type.chat && xmppMessage.getBody() != null;
    }

}
