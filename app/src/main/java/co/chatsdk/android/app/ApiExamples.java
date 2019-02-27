package co.chatsdk.android.app;

import java.util.HashMap;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * This class contains a list of example API calls for reference
 */
public class ApiExamples {

    /**
     * Example of how to send an image message to a thread
     * @param filePath - Local path to image file
     * @param thread - The thread to send the message to
     */
    public void sendImageMessage (String filePath, Thread thread) {
        Disposable d = ChatSDK.imageMessage().sendMessageWithImage(filePath, thread).subscribe(messageSendProgress -> {

        }, throwable -> {

        });
    }

    /**
     * Example of how to listen for when a message is received
     */
    public void listenForReceivedMessage () {
        ChatSDK.hook().addHook(new Hook(data -> Completable.create(emitter -> {
            Message message = (Message) data.get(HookEvent.Message);
            emitter.onComplete();
        })), HookEvent.MessageReceived);
    }

    /**
     * An example of how to create a thread between the current user an another user
     * @param name - Thread name
     * @param user - User to chat with
     */
    public void createThread (String name, User user) {
        Disposable d = ChatSDK.thread().createThread(name, user, ChatSDK.currentUser())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    // Runs when process completed with error or success
                })
                .subscribe(thread -> {
                    // When the thread is created
                }, throwable -> {
                    // If there is an error
                });

    }

    /**
     * If you already have a Firebase log in for your app you can setup the
     * Chat SDK by calling the following after you user has authenticated.
     * Calling this method will perform all the necessary setup for the Chat SDK
     */
    public void authenticateWithCurrentFirebaseLogin () {
        Disposable d = ChatSDK.auth().authenticateWithCachedToken().subscribe(() -> {

        }, throwable -> {

        });
    }

    /**
     * An example of how to retrieve a remote user from Firebase using the search API
     * @param userID
     */
    public void getUserFromFirebase (String userID) {
        Disposable d = ChatSDK.search().usersForIndex(userID, 1).subscribe(user -> {

        }, throwable -> {

        });
    }


}
