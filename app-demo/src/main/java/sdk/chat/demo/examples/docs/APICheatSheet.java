package sdk.chat.demo.examples.docs;

import org.pmw.tinylog.Logger;

import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.AudioMessageHandler;
import sdk.chat.core.handlers.AuthenticationHandler;
import sdk.chat.core.handlers.BlockingHandler;
import sdk.chat.core.handlers.ContactHandler;
import sdk.chat.core.handlers.CoreHandler;
import sdk.chat.core.handlers.IEncryptionHandler;
import sdk.chat.core.handlers.FileMessageHandler;
import sdk.chat.core.handlers.ImageMessageHandler;
import sdk.chat.core.handlers.LastOnlineHandler;
import sdk.chat.core.handlers.LocationMessageHandler;
import sdk.chat.core.handlers.NearbyUsersHandler;
import sdk.chat.core.handlers.PublicThreadHandler;
import sdk.chat.core.handlers.PushHandler;
import sdk.chat.core.handlers.ReadReceiptHandler;
import sdk.chat.core.handlers.SearchHandler;
import sdk.chat.core.handlers.StickerMessageHandler;
import sdk.chat.core.handlers.ThreadHandler;
import sdk.chat.core.handlers.TypingIndicatorHandler;
import sdk.chat.core.handlers.UploadHandler;
import sdk.chat.core.handlers.VideoMessageHandler;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.InterfaceAdapter;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Config;
import sdk.chat.core.session.StorageManager;
import sdk.chat.core.types.AccountDetails;
import sdk.chat.demo.examples.CustomChatActivity;
import sdk.chat.demo.examples.activities.AProfileFragment;
import sdk.chat.ui.fragments.ProfileFragment;

public class APICheatSheet {

    public void configuration() {
        Config config = ChatSDK.config();

        // Example
        config.setGoogleMaps("Google Maps API Key");
    }

    public void messagingServerAPI() {
        BaseNetworkAdapter networkAdapter = ChatSDK.shared().a();

        // Core Methods
        CoreHandler core = networkAdapter.core;
        AuthenticationHandler auth = networkAdapter.auth;
        ThreadHandler thread = networkAdapter.thread;
        ImageMessageHandler imageMessage = networkAdapter.imageMessage;
        LocationMessageHandler locationMessage = networkAdapter.locationMessage;
        ContactHandler contact = networkAdapter.contact;
        SearchHandler search = networkAdapter.search;
        PublicThreadHandler publicThread = networkAdapter.publicThread;

        // Free modules
        PushHandler push = networkAdapter.push;
        UploadHandler upload = networkAdapter.upload;

        // Paid modules
        VideoMessageHandler videoMessage = networkAdapter.videoMessage;
        AudioMessageHandler audioMessage = networkAdapter.audioMessage;
        TypingIndicatorHandler typingIndicator = networkAdapter.typingIndicator;
        LastOnlineHandler lastOnline = networkAdapter.lastOnline;
        BlockingHandler blocking = networkAdapter.blocking;
        NearbyUsersHandler nearbyUsers = networkAdapter.nearbyUsers;
        ReadReceiptHandler readReceipts = networkAdapter.readReceipts;
        StickerMessageHandler stickerMessage = networkAdapter.stickerMessage;
        FileMessageHandler fileMessage = networkAdapter.fileMessage;
        IEncryptionHandler encryption = networkAdapter.encryption;

        // Push the user's profile data to the server
        ChatSDK.core().pushUser().subscribe(() -> {

        }, throwable -> {

        });

        // Get the current user
        User user = ChatSDK.core().currentUser();

        // Get a user and update their proifle from the server
        ChatSDK.core().getUserForEntityID("User Entity ID").subscribe(otherUser -> {

        }, throwable -> {

        });

        // Login
        ChatSDK.auth().authenticate(AccountDetails.username("Username", "Password")).subscribe(() -> {

        }, throwable -> {

        });

        // Check if user is logged in
        boolean isLoggedIn = ChatSDK.auth().isAuthenticated();

        // Log out
        ChatSDK.auth().logout().subscribe();

        // Create thread
        User otherUser = ChatSDK.core().getUserNowForEntityID("EntityID");
        ChatSDK.thread().createThread("Name", Collections.singletonList(otherUser)).subscribe(thread1 -> {

            // Send a message
            ChatSDK.thread().sendMessageWithText("Hi", thread1).subscribe();

        });

        // Get a list of public threads
        List<Thread> threads = ChatSDK.thread().getThreads(ThreadType.Private1to1);

    }

    public void messagingServerNotifications() {

        Predicate<NetworkEvent> filter = NetworkEvent.filterType(EventType.MessageAdded, EventType.MessageRemoved);

        Disposable d = ChatSDK.events()
                .source()
                .filter(filter)
                .subscribe(networkEvent -> {

                    // Handle Event Here
                    if (networkEvent.getMessage() != null) {
                        Logger.debug(networkEvent.getMessage().getText());
                    }

                });

        // Stop listening
        d.dispose();

        // Hooks
        ChatSDK.hook().addHook(Hook.sync(data -> {
            Message message = (Message) data.get(HookEvent.Message);
        }), HookEvent.MessageReceived);

        // Asynchronous code
        ChatSDK.hook().addHook(Hook.async(data -> Completable.create(emitter -> {
            // ... Async code here
            emitter.onComplete();
        })), HookEvent.MessageReceived);

    }

    public void uiService() {

        InterfaceAdapter interfaceAdapter = ChatSDK.ui();

        // Override an activity
        ChatSDK.ui().setChatActivity(CustomChatActivity.class);

        // Override a fragment
        ChatSDK.ui().setProfileFragmentProvider(user -> {
            ProfileFragment fragment =  new AProfileFragment();
            fragment.setUser(user);
            return fragment;
        });

    }

    public void localDatabase() {



        StorageManager storageManager = ChatSDK.db();

        // Create entity
        User user = ChatSDK.db().createEntity(User.class);

        // Fetch an entity with a given ID
        Thread thread = ChatSDK.db().fetchThreadWithEntityID("threadEntityID");

        // Fetch or create an entity with a given ID
        User otherUser = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, "userEntityID'");

        user.setName("Test");
        user.setAvatarURL("http://something.png");

        List<User> users = thread.getUsers();
        List<Message> messages = thread.getMessages();

        // etc...
    }

}
