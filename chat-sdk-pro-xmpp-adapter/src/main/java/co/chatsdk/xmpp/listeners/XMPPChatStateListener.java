package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.xmpp.XMPPManager;

public class XMPPChatStateListener implements ChatStateListener {
    @Override
    public void stateChanged(Chat chat, ChatState state, Message message) {
        User user = ChatSDK.db().fetchUserWithEntityID(message.getFrom().asBareJid().toString());
        XMPPManager.shared().typingIndicatorManager.handleMessage(state, message, user);
    }
}
