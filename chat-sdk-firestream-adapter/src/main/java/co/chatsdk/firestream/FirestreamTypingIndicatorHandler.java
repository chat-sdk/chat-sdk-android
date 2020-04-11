package co.chatsdk.firestream;

import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.TypingIndicatorHandler;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import firestream.chat.events.ConnectionEvent;
import firestream.chat.interfaces.IChat;
import firestream.chat.namespace.Fire;
import firestream.chat.types.TypingStateType;
import io.reactivex.Completable;

public class FirestreamTypingIndicatorHandler implements TypingIndicatorHandler {

    public FirestreamTypingIndicatorHandler() {

        // We want to add these listeners when we connect and remove them when we disconnect
        Fire.stream().manage(Fire.stream().getConnectionEvents().subscribe(connectionEvent -> {
            if (connectionEvent.getType() == ConnectionEvent.Type.DidConnect) {

                Fire.stream().manage(Fire.stream().getSendableEvents().getTypingStates().subscribe(event -> {
                    // Get the sender
                    String senderId = event.get().getFrom();

                    if (!senderId.equals(ChatSDK.currentUserID())) {
                        Fire.stream().manage(ChatSDK.core().getUserForEntityID(senderId).subscribe((user, throwable) -> {
                            if (throwable == null) {
                                Thread thread = ChatSDK.db().fetchThreadWithEntityID(senderId);
                                if (thread != null) {
                                    NetworkEvent networkEvent = NetworkEvent.typingStateChanged(null, thread);
                                    if (event.get().getTypingStateType().equals(TypingStateType.typing())) {
                                        networkEvent = NetworkEvent.typingStateChanged(user.getName(), thread);
                                    }
                                    ChatSDK.events().source().onNext(networkEvent);
                                }
                            }
                        }));
                    }

                }));

            }
        }));

    }

    @Override
    public void typingOn(Thread thread) {
    }

    @Override
    public void typingOff(Thread thread) {

    }

    @Override
    public Completable setChatState(State state, Thread thread) {

        TypingStateType typingStateType = TypingStateType.none();
        if (state == State.composing) {
            typingStateType = TypingStateType.typing();
        }

        if (thread.typeIs(ThreadType.Private1to1)) {
            User otherUser = thread.otherUser();
            return Fire.stream().sendTypingIndicator(otherUser.getEntityID(), typingStateType);
        } else {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.sendTypingIndicator(typingStateType);
            } else {
                return Completable.complete();
            }
        }
    }
}
