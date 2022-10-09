package app.xmpp.adapter.handlers;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.impl.JidCreate;

import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.TypingIndicatorHandler;
import sdk.chat.core.interfaces.ThreadType;
import app.xmpp.adapter.XMPPManager;
import io.reactivex.Completable;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews on 11/07/2017.
 */

public class XMPPTypingIndicatorHandler implements TypingIndicatorHandler {
    @Override
    public void typingOn(Thread thread) {

    }

    @Override
    public void typingOff(Thread thread) {

    }

    public Completable setChatState(final State state, final Thread thread) {
        return Completable.create(e -> {
            ChatState xmppState;
            switch (state) {
                case composing:
                    xmppState = ChatState.composing;
                    break;
                case paused:
                    xmppState = ChatState.paused;
                    break;
                case inactive:
                    xmppState = ChatState.inactive;
                    break;
                case gone:
                    xmppState = ChatState.gone;
                    break;
                case active:
                default:
                    xmppState = ChatState.active;
            }

            XMPPConnection connection = XMPPManager.shared().getConnection();
            if (connection != null) {
                if(thread.typeIs(ThreadType.Private1to1)) {
                    // Issue here when we type
                    ChatManager chatManager = XMPPManager.shared().chatManager();
                    Chat chat = chatManager.chatWith(JidCreate.entityBareFrom(thread.getEntityID()));
                    try {
                        XMPPManager.shared().chatStateManager().setCurrentState(xmppState, chat);
                    } catch (Exception ex) {
                        e.onComplete();
                    }
                }
                else if (thread.typeIs(ThreadType.Group)) {
                    MultiUserChat chat = XMPPManager.shared().mucManager.chatForThreadID(thread.getEntityID());
                    if(chat != null && ChatSDK.thread().hasVoice(thread, ChatSDK.currentUser())) {
                        Message message = new Message();
                        ChatStateExtension extension = new ChatStateExtension(xmppState);
                        message.addExtension(extension);
                        try {
                            chat.sendMessage(message);
                        } catch (Exception ex) {
                            e.onComplete();
                        }
                    }
                }
            }
            e.onComplete();
        }).subscribeOn(RX.io());
    }

}
