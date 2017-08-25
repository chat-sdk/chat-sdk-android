package co.chatsdk.xmpp.handlers;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jxmpp.jid.impl.JidCreate;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

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

    public Completable setChatState (final State state, final Thread thread) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                ChatState xmppState = null;
                switch (state) {
                    case active:
                        xmppState = ChatState.active;
                        break;
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
                    default:
                        xmppState = ChatState.active;
                }

                // TODO: how does this work for MUC?
                ChatManager manager = ChatManager.getInstanceFor(XMPPManager.shared().getConnection());
                Chat chat = manager.getThreadChat(thread.getEntityID());
                if(chat == null) {
                    chat = manager.createChat(JidCreate.entityBareFrom(thread.getEntityID()));
                }
                XMPPManager.shared().chatStateManager().setCurrentState(xmppState, chat);
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

}
