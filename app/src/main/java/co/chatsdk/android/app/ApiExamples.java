package co.chatsdk.android.app;

import android.content.Context;
import android.content.Intent;

import java.util.HashMap;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.BroadcastHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
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

    /**
     * Push notifications are handled by the broadcast receiver. For custom handling,
     * you can register a custom hander. Make sure to do this after activating the
     * push module.
     */
    public void customPushNotificationHandling () {
        ChatSDK.push().setBroadcastHandler(new BroadcastHandler() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Handle push notifications here
            }
        });
    }

    /**
     * How to get the unread message count for a thread
     * @param thread
     */
    public void getUnreadMessageCount (Thread thread) {
        int count  = thread.getUnreadMessagesCount();
    }

    /**
     * How to determine when a message has been sent / uploaded etc...
     * @param thread
     */
    public void getNotificationWhenFileUploaded (Thread thread) {
        Disposable d = ChatSDK.imageMessage().sendMessageWithImage("file-path", thread).subscribe(messageSendProgress -> {
            if (messageSendProgress.getStatus() == MessageSendStatus.Uploading) {
                // Message is uploading
            }
            if (messageSendProgress.getStatus() == MessageSendStatus.Sent) {
                // Message has finished uploading
            }
        });
    }

    /**
     * How to detect when a new message has been received
     */
    public void getMessageReceived () {
        ChatSDK.hook().addHook(new Hook(data -> Completable.create(emitter -> {

            // Get the payload from the notification
            if (data.get(HookEvent.Message) instanceof Message) {

                // Cast it as a message
                Message message = (Message) data.get(HookEvent.Message);

                // Check the message type
                if (message.getMessageType().is(MessageType.Image)) {

                }
            }

            // Hooks return a completable which allows them to be asynchronous. When you've
            // finished you need to register complete
            emitter.onComplete();
        })), HookEvent.MessageReceived);
    }

    /**
     * To find out if a read receipt has been updated
     */
    public void listenForReadReceiptUpdated () {
        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {
            if (networkEvent.type == EventType.ThreadReadReceiptUpdated) {
                // Code here
            }
        });
    }

    /**
     * Get a user with a given entity ID
     * @param entityID
     */
    public void getUserWithEntityID(String entityID) {
        User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityID);
        Disposable d = ChatSDK.core().userOn(user).subscribe(() -> {
            // User object has now been populated and is ready to use

        }, throwable -> {

        });

    }

}
