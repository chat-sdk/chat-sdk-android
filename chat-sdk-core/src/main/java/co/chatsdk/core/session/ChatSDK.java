package co.chatsdk.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.StringRes;

import org.greenrobot.greendao.annotation.NotNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import co.chatsdk.core.base.LocationProvider;
import co.chatsdk.core.base.BaseNetworkAdapter;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.AudioMessageHandler;
import co.chatsdk.core.handlers.AuthenticationHandler;
import co.chatsdk.core.handlers.BlockingHandler;
import co.chatsdk.core.handlers.ContactHandler;
import co.chatsdk.core.handlers.ContactMessageHandler;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.EncryptionHandler;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.handlers.FileMessageHandler;
import co.chatsdk.core.handlers.HookHandler;
import co.chatsdk.core.handlers.ImageMessageHandler;
import co.chatsdk.core.handlers.LastOnlineHandler;
import co.chatsdk.core.handlers.LocationMessageHandler;
import co.chatsdk.core.handlers.ProfilePicturesHandler;
import co.chatsdk.core.handlers.PublicThreadHandler;
import co.chatsdk.core.handlers.PushHandler;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.handlers.SearchHandler;
import co.chatsdk.core.handlers.StickerMessageHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.handlers.TypingIndicatorHandler;
import co.chatsdk.core.handlers.UploadHandler;
import co.chatsdk.core.handlers.VideoMessageHandler;
import co.chatsdk.core.interfaces.InterfaceAdapter;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.notifications.NotificationDisplayHandler;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.core.utils.AppBackgroundMonitor;

import co.chatsdk.core.storage.FileManager;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;


/**
 * Created by ben on 9/5/17.
 */

public class ChatSDK {

    public static String Preferences = "chat_sdk_preferences";

    private static final ChatSDK instance = new ChatSDK();
    protected WeakReference<Context> context;
    public Configuration config;
    public Disposable localNotificationDisposable;

    protected InterfaceAdapter interfaceAdapter;
    protected StorageManager storageManager;
    protected BaseNetworkAdapter networkAdapter;

    protected LocationProvider locationProvider;
    protected FileManager fileManager;

    protected ChatSDK () {
    }

    private void setContext (Context context) {
        this.context = new WeakReference<>(context);
    }

    public static ChatSDK initialize (Context context, Configuration config, @NotNull Class<? extends BaseNetworkAdapter> networkAdapterClass, @NotNull Class<? extends InterfaceAdapter> interfaceAdapterClass) throws Exception {
        shared().setContext(context);
        shared().config = config;

        shared().setNetworkAdapter(networkAdapterClass.getConstructor().newInstance());

        Constructor<? extends InterfaceAdapter> constructor = interfaceAdapterClass.getConstructor(Context.class);
        Object[] parameters = {context};

        shared().setInterfaceAdapter(constructor.newInstance(parameters));

        DaoCore.init(shared().context());

        shared().storageManager = new StorageManager();

        shared().locationProvider = new LocationProvider();

        shared().handleLocalNotifications();
        // Monitor the app so if it goes into the background we know
        AppBackgroundMonitor.shared().setEnabled(true);

        RxJavaPlugins.setErrorHandler(ChatSDK.events());

        shared().fileManager = new FileManager(context);

        return shared();
    }

    public void handleLocalNotifications () {

        if (localNotificationDisposable != null) {
            localNotificationDisposable.dispose();
        }

        // TODO: Check this
        localNotificationDisposable = ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .subscribe(new Consumer<NetworkEvent>() {
                               @Override
                               public void accept(NetworkEvent networkEvent) throws Exception {
                                   Message message = networkEvent.message;
                                   Thread thread = networkEvent.thread;
                                   if (message != null && !AppBackgroundMonitor.shared().inBackground() && thread.isMuted()) {
                                       if (thread.typeIs(ThreadType.Private) || (thread.typeIs(ThreadType.Public) && ChatSDK.config().localPushNotificationsForPublicChatRoomsEnabled)) {
                                           if (!message.getSender().isMe() && !message.isDelivered() && ChatSDK.ui().showLocalNotifications(message.getThread()) || NotificationDisplayHandler.connectedToAuto(context())) {
                                               ReadStatus status = message.readStatusForUser(ChatSDK.currentUser());
                                               if (!message.isRead() && !status.is(ReadStatus.delivered()) && !status.is(ReadStatus.read())) {
                                                   // Only show the alert if we'recyclerView not on the private threads tab
                                                   ChatSDK.ui().notificationDisplayHandler().createMessageNotification(message);
                                               }
                                           }
                                       }
                                   }
                               }
                           });
    }

    public static ChatSDK shared () {
        return instance;
    }

    public SharedPreferences getPreferences () {
        return context.get().getSharedPreferences(Preferences, Context.MODE_PRIVATE);
    }

    public String getString(@StringRes int stringId) {
        return context().getString(stringId);
    }

    public Context context () {
        return context.get();
    }

    public static Configuration config () {
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

    public static UploadHandler upload () {
        return a().upload;
    }

    public static EventHandler events () {
        return a().events;
    }

    public static User currentUser () {
        return ChatSDK.core().currentUser();
    }

    public static String currentUserID() {
        if (ChatSDK.core().currentUser() != null) {
            return ChatSDK.core().currentUser().getEntityID();
        } else {
            return null;
        }
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

    public static EncryptionHandler encryption () { return a().encryption; }

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

    public static LocationProvider locationProvider () {
        return shared().locationProvider;
    }

    public static BaseNetworkAdapter a() {
        return shared().networkAdapter;
    }

    public static StorageManager db () {
        return shared().storageManager;
    }


}
