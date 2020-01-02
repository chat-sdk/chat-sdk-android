package co.chatsdk.firestream;

import co.chatsdk.core.api.APIHelper;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import firestream.chat.chat.Chat;
import firestream.chat.events.ConnectionEvent;
import firestream.chat.events.EventType;
import firestream.chat.firebase.rx.DisposableList;
import firestream.chat.namespace.Fire;
import io.reactivex.Completable;
import firestream.chat.types.TypingStateType;
import io.reactivex.disposables.Disposable;

public class FirestreamTypingIndicatorHandler implements TypingIndicatorHandler {

    private DisposableList dm = new DisposableList();

    public FirestreamTypingIndicatorHandler() {

        // We want to add these listeners when we connect and remove them when we disconnect
        Disposable d = Fire.Stream.getConnectionEvents().subscribe(connectionEvent -> {
            if (connectionEvent.getType() == ConnectionEvent.Type.DidConnect) {

                dm.add(Fire.Stream.getEvents().getTypingStates().subscribe(typingState -> {
                    // Get the sender
                    String senderId = typingState.from;

                    if (!senderId.equals(ChatSDK.currentUserID())) {
                        dm.add(APIHelper.fetchRemoteUser(senderId).subscribe((user, throwable) -> {
                            if (throwable == null) {
                                Thread thread = ChatSDK.db().fetchThreadWithEntityID(senderId);
                                if (thread != null) {
                                    NetworkEvent networkEvent = NetworkEvent.typingStateChanged(null, thread);
                                    if (typingState.getBodyType().equals(TypingStateType.typing())) {
                                        networkEvent = NetworkEvent.typingStateChanged(user.getName(), thread);
                                    }
                                    ChatSDK.events().source().onNext(networkEvent);
                                }
                            }
                        }));
                    }

                }));

            }
            if (connectionEvent.getType() == ConnectionEvent.Type.WillDisconnect) {
                dm.disposeAll();
            }
        });

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
            return Fire.Stream.sendTypingIndicator(otherUser.getEntityID(), typingStateType);
        } else {
            Chat chat = Fire.Stream.getChat(thread.getEntityID());
            if (chat != null) {
                return chat.sendTypingIndicator(typingStateType);
            } else {
                return Completable.complete();
            }
        }
    }
}
