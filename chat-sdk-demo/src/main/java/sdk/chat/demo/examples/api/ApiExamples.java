package sdk.chat.demo.examples.api;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.push.BroadcastHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.ui.ProfileFragmentProvider;
import sdk.chat.demo.examples.activities.AProfileFragment;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.wrappers.UserWrapper;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.ui.activities.LoginActivity;
import sdk.guru.common.RX;

/**
 * This class contains a list of example API calls for reference
 */
public class ApiExamples {


    public void customizeUI () {

        // You could define a custom LoginActivity subclass here
        ChatSDK.ui().setLoginActivity(LoginActivity.class);

        // Or customise the profile fragment
        ChatSDK.ui().setProfileFragmentProvider(new ProfileFragmentProvider() {
            @Override
            public Fragment profileFragment(User user) {
                AProfileFragment fragment = new AProfileFragment();
                fragment.setUser(user);
                return fragment;
            }
        });

    }

    /**
     * Example of how to send an image text to a thread
     * @param filePath - Local path to image file
     * @param thread - The thread to send the text to
     */
    public void sendImageMessage (File filePath, ThreadX thread) {
        Disposable d = ChatSDK.imageMessage().sendMessageWithImage(filePath, thread).subscribe(() -> {
            // Handle Success
        }, (Consumer<Throwable>) throwable -> {
            // Handle failure
        });
    }

    public void sendTextMessage (String message, ThreadX thread) {
        Disposable d = ChatSDK.thread().sendMessageWithText(message, thread).subscribe(() -> {
            // Handle Success
        }, throwable -> {
            // Handle failure
        });
    }

    /**
     * Example of how to listen for when a text type received
     */
    public void listenForReceivedMessage () {

        // Synchronous code
        ChatSDK.hook().addHook(Hook.sync(data -> {
            Message message = (Message) data.get(HookEvent.Message);
        }), HookEvent.MessageReceived);

        // Asynchronous code
        ChatSDK.hook().addHook(Hook.async(data -> Completable.create(emitter -> {
            // ... Async code here
            emitter.onComplete();
        })), HookEvent.MessageReceived);
    }

    /**
     * An example of how to create a thread between the current user an another user
     * @param name - Thread name
     * @param user - User to chat from
     */
    public void createThread (String name, User user) {
        Disposable d = ChatSDK.thread().createThread(name, user, ChatSDK.currentUser())
                .observeOn(RX.main())
                .doFinally(() -> {
                    // Runs when process completed from error or success
                })
                .subscribe(thread -> {
                    // When the thread type created
                }, throwable -> {
                    // If there type an error
                });

    }

    public void openChatActivityWithThread (Context context, ThreadX thread) {
        ChatSDK.ui().startChatActivityForID(context, thread.getEntityID());
    }


    /**
     * If you already have a Firebase log in for your app you can setup the
     * Chat SDK by calling the following after you user has authenticated.
     * Calling this method will perform all the necessary setup for the Chat SDK
     */
    public void authenticateWithCurrentFirebaseLogin () {
        Disposable d = ChatSDK.auth().authenticate().subscribe(() -> {

        }, throwable -> {

        });
    }

    /**
     * An example of how to retrieve a remote user from Firebase using the filter API
     * @param name
     */
    public void getUserFromFirebase (String name) {
        // You could also use Keys.Email or Keys.Phone
        Disposable d = ChatSDK.search().usersForIndex(name, 1, Keys.Name).subscribe(user -> {

        }, throwable -> {

        });
    }

    /**
     * Push notifications are handled by the broadcast receiver. For custom handling,
     * you can register a custom hander. Make sure to do this after activating the
     * push module.
     */
    public void customPushNotificationHandling () {

        BroadcastHandler handler = new BroadcastHandler() {
            @Override
            public boolean onReceive(Context context, Intent intent) {
                return false;
            }

            @Override
            public boolean canHandle(Intent intent) {
                return false;
            }
        };

        // During Chat SDK initialization
        FirebasePushModule.builder().setBroadcastHandler(handler).build();

        // Add an extra receiver
        ChatSDK.shared().addBroadcastHandler(handler);

    }

    /**
     * How to get the unread text count for a thread
     * @param thread
     */
    public int getUnreadMessageCount (ThreadX thread) {
        return thread.getUnreadMessagesCount();
    }

    /**
     * How to detect when a new text has been received
     */
    public void getMessageReceived () {
        ChatSDK.hook().addHook(Hook.sync(data -> {

            // Get the body from the notification
            if (data.get(HookEvent.Message) instanceof Message) {

                // Cast it as a text
                Message message = (Message) data.get(HookEvent.Message);

                // Check the text getTypingStateType
                if (message.getMessageType().is(MessageType.Image)) {

                }
            }
        }), HookEvent.MessageReceived);
    }

    /**
     * To find out if a read receipt has been modified
     */
    public void listenForReadReceiptUpdated () {
        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {
            if (networkEvent.type == EventType.MessageReadReceiptUpdated) {
                // Code here
            }
        });
    }

    /**
     * Get a user from a given entity ID
     * @param entityID
     */
    public void getUserWithEntityID(String entityID) {
        User user = ChatSDK.core().getUserNowForEntityID(entityID);
        Disposable d = ChatSDK.core().userOn(user).subscribe(() -> {
            // User object has now been populated and type ready to use

        }, throwable -> {

        });
    }

    /**
     * Add extra hashMapStringObject data to a text
     */
    public void addMetaDataToMessage (Message message) {
        message.setValueForKey("Value", "Key");
    }

    public void deleteMessage (Message message) {
        ChatSDK.thread().deleteMessage(message).subscribe(() -> {
            // BaseMessage has been deleted
        });

    }

    // These fragments can be embedded in your views to display lists of chats
    public void conversationFragmentExample () {
        Fragment publicChatsFragment = ChatSDK.ui().publicThreadsFragment();
        Fragment privateChatsFragment = ChatSDK.ui().privateThreadsFragment();
    }

    public void listContacts () {
        List<User> contacts = ChatSDK.contact().contacts();
    }

    /**
     * And example of how to get all the registered users. Using this is generally a bad idea to use this
     * for anything other than testing because if you have a large number of users, it's extremely
     * inefficient.
     * @return
     */
    public static Single<List<User>> getAllRegisteredUsers() {
        return Single.create(emitter -> {
            DatabaseReference ref = FirebasePaths.usersRef();
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<User> users = new ArrayList<>();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            UserWrapper uw = FirebaseModule.config().provider.userWrapper(child);
                            users.add(uw.getModel());
                        }
                    }
                    emitter.onSuccess(users);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    emitter.onError(databaseError.toException());
                }
            });
        });
    }
}
