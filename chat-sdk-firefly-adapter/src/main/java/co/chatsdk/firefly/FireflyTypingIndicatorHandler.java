package co.chatsdk.firefly;

import co.chatsdk.core.api.APIHelper;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import firefly.sdk.chat.chat.Chat;
import firefly.sdk.chat.firebase.rx.DisposableList;
import io.reactivex.Completable;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.types.TypingStateType;

public class FireflyTypingIndicatorHandler implements TypingIndicatorHandler {

    private DisposableList disposableList = new DisposableList();

    public FireflyTypingIndicatorHandler() {
        disposableList.add(Fl.y.getEvents().getTypingStates().subscribe(typingState -> {
            // Get the sender
            String senderId = typingState.from;

            if (!senderId.equals(ChatSDK.currentUserID())) {
                disposableList.add(APIHelper.fetchRemoteUser(senderId).subscribe((user, throwable) -> {
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
            return Fl.y.sendTypingIndicator(otherUser.getEntityID(), typingStateType);
        } else {
            Chat chat = Fl.y.getChat(thread.getEntityID());
            if (chat != null) {
                return chat.sendTypingIndicator(typingStateType);
            } else {
                return Completable.complete();
            }
        }
    }
}
