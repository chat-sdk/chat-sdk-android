package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;

import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import app.xmpp.adapter.XMPPManager;

public class XMPPChatStateListener implements ChatStateListener {
    @Override
    public void stateChanged(Chat chat, ChatState state, Message message) {
        if (ChatSDK.auth().isAuthenticatedThisSession()) {
            User user = ChatSDK.db().fetchUserWithEntityID(message.getFrom().asBareJid().toString());
            XMPPManager.shared().typingIndicatorManager.handleMessage(state, message, user);
        }
    }
}
