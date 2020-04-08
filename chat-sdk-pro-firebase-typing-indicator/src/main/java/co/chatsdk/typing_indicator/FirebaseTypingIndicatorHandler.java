package co.chatsdk.typing_indicator;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.FirebaseReferenceManager;
import co.chatsdk.firebase.utils.FirebaseRX;
import io.reactivex.Completable;

/**
 * Created by KyleKrueger on 01.07.2017.
 */

public class FirebaseTypingIndicatorHandler implements TypingIndicatorHandler {

    private Timer timer;
    private final long typingTimeout  = 3000;
    private boolean typing = false;

    @Override
    public void typingOn(final Thread thread) {

        DatabaseReference ref = FirebasePaths.threadRef(thread.getEntityID())
                .child(FirebasePaths.TypingPath);

        ValueEventListener typingListener = ref.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
            String message = null;
            if(hasValue && snapshot.getValue() instanceof HashMap) {
                HashMap<String, String> data = (HashMap<String, String>) snapshot.getValue();
                if(data.keySet().size() == 1 && data.get(ChatSDK.currentUser().getEntityID()) != null) {
                    // In this case we are typing
                }
                else {
                    if (thread.typeIs(ThreadType.Private1to1)) {
                        message = "";
                    }
                    else {
                        message = typingMessageForNames(data);
                    }
                }
            }
            NetworkEvent networkEvent = NetworkEvent.typingStateChanged(message, thread);
            ChatSDK.events().source().onNext(networkEvent);
        }));
        FirebaseReferenceManager.shared().addRef(ref, typingListener);

    }

    @Override
    public void typingOff(Thread thread) {
        DatabaseReference ref = FirebasePaths.threadRef(thread.getEntityID())
                .child(FirebasePaths.TypingPath);
        FirebaseReferenceManager.shared().removeListeners(ref);
    }

    @Override
    public Completable setChatState (State state, Thread thread) {
        if(state == State.composing) {
            if(!typing) {
                typing = true;
                startTypingTimer(thread);
                return startTyping(thread);
            }
        }
        else {
            if(typing) {
                typing = false;
                return stopTyping(thread);
            }
        }
        return Completable.complete();
    }

    private void startTypingTimer (final Thread thread) {
        if(timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopTyping(thread);
            }
        }, typingTimeout);
    }

    private Completable startTyping(Thread thread) {
        DatabaseReference ref = FirebasePaths.threadRef(thread.getEntityID())
                .child(FirebasePaths.TypingPath).child(ChatSDK.currentUser().getEntityID());
        return FirebaseRX.set(ref, ChatSDK.currentUser().getName(), true);
    }

    private Completable stopTyping(Thread thread){
        if(timer != null) {
            timer.cancel();
        }
        DatabaseReference ref = FirebasePaths.threadRef(thread.getEntityID())
                .child(FirebasePaths.TypingPath).child(ChatSDK.currentUser().getEntityID());
        return FirebaseRX.remove(ref);
    }

    public String typingMessageForNames(HashMap<String, String> usersTyping) {
        String message = "";

        for (String key : usersTyping.keySet()) {
            if (!key.equals(ChatSDK.currentUser().getEntityID())) {
                message += usersTyping.get(key) + ", ";
            }
        }

        if (message.length() > 2) {
            message = message.substring(0, message.length() - 2);
        }

        return message + " ";
    }
}
