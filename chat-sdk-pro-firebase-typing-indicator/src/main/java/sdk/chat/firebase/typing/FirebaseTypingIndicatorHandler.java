package sdk.chat.firebase.typing;

import com.google.firebase.database.DatabaseReference;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import sdk.chat.firebase.adapter.utils.Generic;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.TypingIndicatorHandler;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.guru.realtime.RealtimeReferenceManager;
import sdk.chat.firebase.adapter.utils.FirebaseRX;
import io.reactivex.Completable;
import sdk.guru.realtime.RXRealtime;

/**
 * Created by KyleKrueger on 01.07.2017.
 */

public class FirebaseTypingIndicatorHandler implements TypingIndicatorHandler {

    private Timer timer;
    private boolean typing = false;

    @Override
    public void typingOn(final Thread thread) {

        DatabaseReference ref = ref(thread);

        if (!RealtimeReferenceManager.shared().isOn(ref)) {

            RXRealtime realtime = new RXRealtime();
            realtime.on(ref).doOnNext(change -> {

                String message = null;
                Map<String, String> data = change.getSnapshot().getValue(Generic.mapStringString());
                if (data != null) {
                    // In this case we are typing
                    if(data.keySet().size() != 1 || data.get(ChatSDK.currentUser().getEntityID()) == null) {
                        if (thread.typeIs(ThreadType.Private1to1)) {
                            message = "";
                        }
                        else {
                            message = typingMessageForNames(data);
                        }
                    }
                } else {
                    NetworkEvent networkEvent = NetworkEvent.typingStateChanged(null, thread);
                    ChatSDK.events().source().accept(networkEvent);
                }

                NetworkEvent networkEvent = NetworkEvent.typingStateChanged(message, thread);
                ChatSDK.events().source().accept(networkEvent);

            }).ignoreElements().subscribe(ChatSDK.events());
//            realtime.addToReferenceManager();
        }

    }

    @Override
    public void typingOff(Thread thread) {
        DatabaseReference ref = ref(thread);
        RealtimeReferenceManager.shared().removeListeners(ref);
    }

    protected DatabaseReference ref(Thread thread) {
        return FirebasePaths.threadRef(thread.getEntityID())
                .child(FirebasePaths.TypingPath);
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
        }, FirebaseTypingIndicatorModule.config().typingTimeout);
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

    public String typingMessageForNames(Map<String, String> usersTyping) {
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
