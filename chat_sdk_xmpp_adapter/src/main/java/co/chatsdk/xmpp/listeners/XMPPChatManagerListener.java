package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jxmpp.jid.EntityBareJid;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.XMPPMessageParser;
import io.reactivex.functions.BiConsumer;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class XMPPChatManagerListener implements ChatManagerListener, ChatMessageListener {

    public boolean isOneToOneMessage (Message xmppMessage) {
        return xmppMessage.getType() == Message.Type.chat && xmppMessage.getBody() != null;
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        chat.addMessageListener(this);
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        // Get the thread here before we parse the message. If the thread is null,
        // it will be created when we parse the message
        final Thread thread = StorageManager.shared().fetchThreadWithEntityID(message.getFrom().toString());

        if(isOneToOneMessage(message)) {
            XMPPMessageParser.parse(message).subscribe(new BiConsumer<co.chatsdk.core.dao.Message, Throwable>() {
                @Override
                public void accept(co.chatsdk.core.dao.Message message, Throwable throwable) throws Exception {
                    NetworkEvent event = NetworkEvent.messageAdded(message.getThread(), message);
                    NM.events().source().onNext(event);

                    // TODO: Check this - private / public thread
                    // This means that we created the thread when we parsed the message
                    if(thread == null) {
                        NM.events().source().onNext(NetworkEvent.privateThreadAdded(message.getThread()));
                    }
                }
            });
        }

        ChatStateExtension chatState = (ChatStateExtension) message.getExtension(ChatStateExtension.NAMESPACE);
        if(chatState != null) {
            User user = StorageManager.shared().fetchUserWithEntityID(message.getFrom().asBareJid().toString());
            XMPPManager.shared().typingIndicatorManager.handleMessage(message, user);
            Timber.v("Chat State: " + chatState.getChatState());
        }
    }
}
