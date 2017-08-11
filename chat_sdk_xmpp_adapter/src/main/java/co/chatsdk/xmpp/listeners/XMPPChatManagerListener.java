package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class XMPPChatManagerListener implements ChatManagerListener {
    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {

        Thread thread = StorageManager.shared().fetchOrCreateEntityWithEntityID(Thread.class, chat.getThreadID());
        chat.addMessageListener(new XMPPChatMessageListener());

        // TODO: Check this - private / public thread
        NetworkEvent event = NetworkEvent.privateThreadAdded(thread);

        // Add the event to the global event bus
        NM.events().source().onNext(event);
    }
}
