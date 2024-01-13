package sdk.chat.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ProcessLifecycleOwner;

import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import feather.Feather;
import io.reactivex.Completable;
import io.reactivex.plugins.RxJavaPlugins;
import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.AudioMessageHandler;
import sdk.chat.core.handlers.AuthenticationHandler;
import sdk.chat.core.handlers.BlockingHandler;
import sdk.chat.core.handlers.CallHandler;
import sdk.chat.core.handlers.ContactHandler;
import sdk.chat.core.handlers.ContactMessageHandler;
import sdk.chat.core.handlers.CoreHandler;
import sdk.chat.core.handlers.EventHandler;
import sdk.chat.core.handlers.FileMessageHandler;
import sdk.chat.core.handlers.HookHandler;
import sdk.chat.core.handlers.IEncryptionHandler;
import sdk.chat.core.handlers.ImageMessageHandler;
import sdk.chat.core.handlers.LastOnlineHandler;
import sdk.chat.core.handlers.LocationMessageHandler;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.handlers.ProfilePicturesHandler;
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
import sdk.chat.core.interfaces.IKeyStorage;
import sdk.chat.core.interfaces.InterfaceAdapter;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.manager.TextMessagePayload;
import sdk.chat.core.module.Module;
import sdk.chat.core.notifications.NotificationDisplayHandler;
import sdk.chat.core.push.BroadcastHandler;
import sdk.chat.core.push.PushQueue;
import sdk.chat.core.rigs.DownloadManager;
import sdk.chat.core.rigs.MessageSender;
import sdk.chat.core.storage.FileManager;
import sdk.chat.core.storage.UploadManager;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.chat.core.utils.ConnectionStateMonitor;
import sdk.chat.core.utils.KeyStorage;
import sdk.guru.common.RX;


/**
 * Created by ben on 9/5/17.
 */

public class ChatSDK {

    public static ChatSDK shared() {
        return instance;
    }
    private static final ChatSDK instance = new ChatSDK();

    public static String Preferences = "chat_sdk_preferences";

    protected WeakReference<Context> context;
    public Config<ChatSDK> config = new Config<>(this);

    protected InterfaceAdapter interfaceAdapter;
    protected StorageManager storageManager;
    protected BaseNetworkAdapter networkAdapter;
    protected AppBackgroundMonitor appBackgroundMonitor = new AppBackgroundMonitor();

    protected FileManager fileManager;

    protected List<String> requiredPermissions = new ArrayList<>();

    protected ConfigBuilder<ChatSDK> builder;
    protected boolean isActive = false;
    protected String licenseIdentifier;
    protected IKeyStorage keyStorage;
    protected DownloadManager downloadManager;
    protected MessageSender messageSender;
    protected UploadManager uploadManager = new UploadManager();
    protected ConnectionStateMonitor connectionStateMonitor;

    protected List<Runnable> onActivateListeners = new ArrayList<>();
    protected List<Runnable> onPermissionsRequestedListeners = new ArrayList<>();

    protected List<BroadcastHandler> broadcastHandlers = new ArrayList<>();
    protected PushQueue pushQueue = new PushQueue();

    protected Feather feather = Feather.with();
    protected List<MessageHandler> messageHandlers = new ArrayList<>();

    protected ChatSDK () {
    }

    private void setContext (Context context) {
        this.context = new WeakReference<>(context);
    }

    /**
     * You can override the network adapter and interface adapter classes here. If these values are provided, they will be used instead of any that could
     * be provided by a module. These values can be null but by the end of setup, the network adapter and interface adapter must both be set. Either
     * here or by a module.
     * @param networkAdapterClass
     * @param interfaceAdapterClass
     * @return
     */
    public static ConfigBuilder<ChatSDK> configure(@Nullable Class<? extends BaseNetworkAdapter> networkAdapterClass, @Nullable Class<? extends InterfaceAdapter> interfaceAdapterClass) {
        shared().builder = new ConfigBuilder<>(shared());
        return shared().builder.setNetworkAdapter(networkAdapterClass).setInterfaceAdapter(interfaceAdapterClass);
    }

    public static ConfigBuilder<ChatSDK> configure() {
        shared().builder = new ConfigBuilder<>(shared());
        return shared().builder;
    }

    /**
     * Configure and let modules provide the interface and network adapters. We will loop over the modules and see if they provide each adapter,
     * the first that does will be used and any subsequent provider will be ignored.
     * @return
     */
    public static Config<ConfigBuilder<ChatSDK>> builder() {
         shared().builder = new ConfigBuilder<>(shared());
        return shared().builder.builder();
    }

    public void activate(Context context) throws Exception {
        activate(context, null);
    }

    public void activateWithPatreon(Context context, @Nullable String patreonId) throws Exception {
        activate(context, "Patreon:" + patreonId);
    }

    public void activateWithEmail(Context context, @Nullable String email) throws Exception {
        activate(context, "Email:" + email);
    }

    public Completable activateWithEmailAsync(Context context, @Nullable String email) {
        return activateAsync(context, "Email:" + email);
    }


    public void activateWithGithubSponsors(Context context, @Nullable String githubSponsorsId) throws Exception {
        activate(context, "Github:" + githubSponsorsId);
    }

    public Completable activateAsync(Context context, @Nullable String identifier) {
        return Completable.create(emitter -> {
            activate(context, identifier);
            emitter.onComplete();
        }).subscribeOn(RX.computation());
    }

    public void activate(Context context, @Nullable String identifier) throws Exception {

        if (isActive) {
            throw new Exception("Chat SDK is already active. It is not recommended to call activate twice. If you must do this, make sure to call stop() first.");
        }


        ProcessLifecycleOwner.get().getLifecycle().addObserver(appBackgroundMonitor);

        setContext(context);
        keyStorage = new KeyStorage(context);

        config = builder.config();

        RX.db().scheduleDirect(() -> {
            Logger.getConfiguration().level(config.logLevel).activate();
            Logger.debug("Test");
            Logger.debug("A");
        });

        downloadManager = new DownloadManager(context);
        messageSender = new MessageSender(context);

        Class<? extends BaseNetworkAdapter> networkAdapter = builder.networkAdapter;
        if (builder.networkAdapter != null) {
            Logger.info("Network adapter provided by ChatSDK.configure call");
        }

        Class<? extends InterfaceAdapter> interfaceAdapter = builder.interfaceAdapter;
        if (builder.networkAdapter != null) {
            Logger.info("Interface adapter provided by ChatSDK.configure call");
        }

        for (Module module: builder.modules) {
            if (networkAdapter == null) {
                if(module instanceof NetworkAdapterProvider) {
                    NetworkAdapterProvider provider = (NetworkAdapterProvider) module;
                    if (provider.getNetworkAdapter() != null) {
                        networkAdapter = provider.getNetworkAdapter();
                        Logger.info("Module: " + module.getName() + " provided network adapter");
                    }
                }
            }
            if (interfaceAdapter == null) {
                if(module instanceof InterfaceAdapterProvider) {
                    InterfaceAdapterProvider provider = (InterfaceAdapterProvider) module;
                    if (provider.getInterfaceAdapter() != null) {
                        interfaceAdapter = provider.getInterfaceAdapter();
                        Logger.info("Module: " + module.getName() + " provided interface adapter");
                    }
                }
            }
            for (String permission: module.requiredPermissions()) {
                if (!requiredPermissions.contains(permission)) {
                    requiredPermissions.add(permission);
                }
            }
            if (module.isPremium() && (identifier == null || identifier.isEmpty())) {
                System.out.println("<<");
                System.out.println(">>");
                System.out.println("<<");
                System.out.println(">>");
                System.out.println("To use premium modules you must include either your email, Patreon ID or Github Sponsors ID");
                System.out.println("ChatSDK.builder()....build().activateWith...");
                System.out.println("<<");
                System.out.println(">>");
                System.out.println("<<");
                System.out.println(">>");


                throw new Exception("To use premium modules you must include either your email, Patreon ID or Github Sponsors ID");
            }
        }

        // Make a list of providers
        List<Object> providers = new ArrayList<>();
        providers.addAll(config.providers);
        providers.addAll(builder.modules);

        feather = Feather.with(providers);

        if (networkAdapter != null) {
            setNetworkAdapter(networkAdapter.getConstructor().newInstance());
        } else {
            throw new Exception("The network adapter cannot be null. A network adapter must be defined using ChatSDK.configure(...) or by a module");
        }

        if (interfaceAdapter != null) {
            InterfaceAdapter adapter = interfaceAdapter.newInstance();
            adapter.initialize(context);
            setInterfaceAdapter(adapter);


//            Constructor<? extends InterfaceAdapter> constructor = interfaceAdapter.getConstructor(Context.class);
//            Object[] parameters = {context};
//
//            setInterfaceAdapter(constructor.newInstance(parameters));
        } else {
            throw new Exception("The interface adapter cannot be null. An interface adapter must be defined using ChatSDK.configure(...) or by a module");
        }

        storageManager = new StorageManager(context);

        // Monitor the app so if it goes into the background we know
        ChatSDK.appBackgroundMonitor().setEnabled(true);

        RxJavaPlugins.setErrorHandler(ChatSDK.events());

        fileManager = new FileManager(context);

        for (Module module: builder.modules) {
            module.activate(context);
            Logger.info("Module " + module.getName() + " activated successfully");
        }

        connectionStateMonitor = new ConnectionStateMonitor();
        connectionStateMonitor.enable(context);

        // Local notifications
        hook().addHook(Hook.sync(data -> {
            // If we are using remote notifications even when online, we disable local notifications
            // completely
            if (ChatSDK.config().disablePushHandlingWhenOnline) {
                Object messageObject = data.get(HookEvent.Message);
                Object threadObject = data.get(HookEvent.Thread);
                if (messageObject instanceof Message && threadObject instanceof Thread) {
                    Message message = (Message) messageObject;
                    Thread thread = (Thread) threadObject;

                    if (!thread.isMuted() && !thread.isDeleted()) {
                        if (thread.typeIs(ThreadType.Private) || ChatSDK.config().localPushNotificationsForPublicChatRoomsEnabled) {
                            if (!message.isDelivered()) {

                                boolean inBackground = ChatSDK.appBackgroundMonitor().inBackground();
                                boolean connectedToAuto = NotificationDisplayHandler.connectedToAuto(context);
                                if (inBackground || connectedToAuto || (ChatSDK.ui().showLocalNotifications(thread))) {
                                    RX.onMain(() -> ChatSDK.ui().notificationDisplayHandler().createMessageNotification(message));
                                }
                            }
                        }
                    }
                }
            }

        }), HookEvent.MessageReceived);

        for (Runnable r: onActivateListeners) {
            r.run();
        }

        licenseIdentifier = identifier;
        isActive = true;

    }

    public void stop() {
        context = null;
        config = new Config<>(this);
        if (networkAdapter != null) {
            networkAdapter = null;
        }
        if (interfaceAdapter != null) {
            interfaceAdapter = null;
        }
        requiredPermissions.clear();
        ChatSDK.appBackgroundMonitor().stop();

        if (builder != null) {
            for (Module module: builder.modules) {
                module.stop();
            }
        }
        isActive = false;
    }

    public static Context ctx() {
        return shared().context();
    }

    public SharedPreferences getPreferences() {
        return  context.get().getSharedPreferences(Preferences, Context.MODE_PRIVATE);
    }

    public static String getString(@StringRes int stringId) {
        return ctx().getString(stringId);
    }

    public static Exception getException(@StringRes int stringId) {
        return new Exception(ctx().getString(stringId));
    }

    public Context context() {
        return context.get();
    }

    public static Config config() {
        return shared().config;
    }

    public FileManager fileManager() {
        return fileManager;
    }

    /**
     * Shortcut to return the interface adapter
     * @return InterfaceAdapter
     */
    public static InterfaceAdapter ui () {
        return shared().interfaceAdapter;
    }

    public void setInterfaceAdapter (InterfaceAdapter interfaceAdapter) {
        shared().interfaceAdapter = interfaceAdapter;
    }

    public void setNetworkAdapter (BaseNetworkAdapter networkAdapter) {
        shared().networkAdapter = networkAdapter;
    }

    public static CoreHandler core () {
        return a().core;
    }

    public static AuthenticationHandler auth () {
        return a().auth;
    }

    public static ThreadHandler thread () {
        return a().thread;
    }

    public static PublicThreadHandler publicThread () {
        return a().publicThread;
    }

    public static PushHandler push () {
        return a().push;
    }

    public static CallHandler call() {
        return a().call;
    }

    public static UploadHandler upload () {
        return a().upload;
    }

    public static EventHandler events () {
        return a().events;
    }

    public static User currentUser () {
        return auth().currentUser();
    }

    public static String currentUserID() {
        return auth().getCurrentUserEntityID();
    }

    public static SearchHandler search () {
        return a().search;
    }

    public static ContactHandler contact () {
        return a().contact;
    }

    public static BlockingHandler blocking () {
        return a().blocking;
    }

    public static IEncryptionHandler encryption () { return a().encryption; }

    public static LastOnlineHandler lastOnline () {
        return a().lastOnline;
    }

    public static AudioMessageHandler audioMessage () {
        return a().audioMessage;
    }

    public static VideoMessageHandler videoMessage () {
        return a().videoMessage;
    }

    public static HookHandler hook () {
        return a().hook;
    }

    public static StickerMessageHandler stickerMessage () {
        return a().stickerMessage;
    }

    public static ContactMessageHandler contactMessage () {
        return a().contactMessage;
    }

    public static FileMessageHandler fileMessage () {
        return a().fileMessage;
    }

    public static ImageMessageHandler imageMessage () {
        return a().imageMessage;
    }

    public static LocationMessageHandler locationMessage () {
        return a().locationMessage;
    }

    public static ReadReceiptHandler readReceipts () {
        return a().readReceipts;
    }

    public static TypingIndicatorHandler typingIndicator () {
        return a().typingIndicator;
    }

    public static ProfilePicturesHandler profilePictures () {
        return a().profilePictures;
    }

    public static BaseNetworkAdapter a() {
        return shared().networkAdapter;
    }

    public static StorageManager db () {
        return shared().storageManager;
    }

//    @Deprecated
//    public static String getMessageImageURL(Message message) {
//        return messageHandlerManager().getMessageImageURL(message);
//
//    }
//
//    @Deprecated
//    public static String getMessageText(Message message) {
//        return messageHandlerManager().getMessageText(message);
//    }

    public static List<MessageHandler> getMessageHandlers() {
        List<MessageHandler> handlers = new ArrayList<>();
        for (Module module: shared().builder.modules) {
            if (module.getMessageHandler() != null) {
                handlers.add(module.getMessageHandler());
            }
        }
        for (MessageHandler handler: shared().messageHandlers) {
            handlers.add(handler);
        }
        return handlers;
    }

    public static MessageHandler getMessageHandler(MessageType type) {
        for (MessageHandler handler: getMessageHandlers()) {
            if (handler.isFor(type)) {
                return handler;
            }
        }
        return null;
    }

    public static MessagePayload getMessagePayload(Message message) {

        if (message.typeIs(MessageType.Text)) {

            // Is this a reply?
            MessagePayload reply = null;
            if (message.isReply()) {
                MessageType replyType = message.getReplyType();
                if (replyType != null && replyType.is(MessageType.Text)) {
                    reply = new TextMessagePayload(message);
                } else {
                    MessageHandler handler = getMessageHandler(message.getReplyType());
                    if (handler != null) {
                        reply = handler.payloadFor(message);
                    }
                }
            }

            return new TextMessagePayload(message, reply);
        } else {
            MessageHandler handler = getMessageHandler(message.getMessageType());
            if (handler != null) {
                return handler.payloadFor(message);
            }
        }
        return null;
    }

    public List<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isValid() {
        return isActive && (context != null && context.get() != null && networkAdapter != null && interfaceAdapter != null);
    }

    public void addOnActivateListener(Runnable runnable) {
        onActivateListeners.add(runnable);
    }

    public void addOnPermissionRequestedListener(Runnable runnable) {
        onPermissionsRequestedListeners.add(runnable);
    }

    public void permissionsRequested() {
        for (Runnable r: onPermissionsRequestedListeners) {
            r.run();
        }
    }

    public String getLicenseIdentifier() {
        return licenseIdentifier;
    }

    public IKeyStorage getKeyStorage() {
        return keyStorage;
    }

    public void setKeyStorage(IKeyStorage storage) {
        this.keyStorage = storage;
    }

    public static DownloadManager downloadManager() {
        return shared().downloadManager;
    }

    public static MessageSender messageSender() {
        return shared().messageSender;
    }

    public static UploadManager uploadManager() {
        return shared().uploadManager;
    }

    public void addBroadcastHandler(BroadcastHandler handler) {
        broadcastHandlers.add(handler);
    }
    public void addBroadcastHandler(BroadcastHandler handler, int index) {
        broadcastHandlers.add(index, handler);
    }

    public void removeBroadcastHandler(BroadcastHandler handler) {
        broadcastHandlers.remove(handler);
    }

    public List<BroadcastHandler> broadcastHandlers() {
        return broadcastHandlers;
    }

    public void clearBroadcastHandlers() {
        broadcastHandlers.clear();
    }

    public static PushQueue pushQueue() {
        return shared().pushQueue;
    }

    public static AppBackgroundMonitor appBackgroundMonitor() {
        return shared().appBackgroundMonitor;
    }

    public static Feather feather() {
        return shared().feather;
    }

    public static AppBackgroundMonitor backgroundMonitor() {
        return shared().appBackgroundMonitor;
    }

    public static ConnectionStateMonitor connectionStateMonitor() {
        return shared().connectionStateMonitor;
    }

    public static void addMessageHandler(MessageHandler handler) {
        shared().messageHandlers.add(handler);
    }

    public static void removeMessageHandler(MessageHandler handler) {
        shared().messageHandlers.remove(handler);
    }

}

