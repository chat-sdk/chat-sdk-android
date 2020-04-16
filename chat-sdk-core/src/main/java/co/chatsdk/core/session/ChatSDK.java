package co.chatsdk.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.greenrobot.greendao.annotation.NotNull;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;

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
import co.chatsdk.core.handlers.Module;
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
import co.chatsdk.core.utils.TimeLog;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by ben on 9/5/17.
 */

public class ChatSDK {

    public static String Preferences = "chat_sdk_preferences";

    private static final ChatSDK instance = new ChatSDK();
    protected WeakReference<Context> context;
    public Config config;

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

    /**
     * You can override the network adapter and interface adapter classes here. If these values are provided, they will be used instead of any that could
     * be provided by a module. These values can be null but by the end of setup, the network adapter and interface adapter must both be set. Either
     * here or by a module.
     * @param networkAdapterClass
     * @param interfaceAdapterClass
     * @return
     */
    public static ConfigBuilder configure(@Nullable Class<? extends BaseNetworkAdapter> networkAdapterClass, @Nullable Class<? extends InterfaceAdapter> interfaceAdapterClass) {
        return new ConfigBuilder(networkAdapterClass, interfaceAdapterClass);
    }

    /**
     * Configure and let modules provide the interface and network adapters. We will loop over the modules and see if they provide each adapter,
     * the first that does will be used and any subsequent provider will be ignored.
     * @param configure
     * @return
     */
    public static ConfigBuilder configure(Configure<Config> configure) {
        return new ConfigBuilder(configure);
    }

    public static ChatSDK initialize(ConfigBuilder builder) throws Exception {

        shared().setContext(builder.context);
        shared().config = builder.config;

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
        }

        if (networkAdapter != null) {
            shared().setNetworkAdapter(networkAdapter.getConstructor().newInstance());
        } else {
            throw new Exception("The network adapter cannot be null. A network adapter must be defined using ChatSDK.configure(...) or by a module");
        }

        if (interfaceAdapter != null) {
            Constructor<? extends InterfaceAdapter> constructor = interfaceAdapter.getConstructor(Context.class);
            Object[] parameters = {builder.context};

            shared().setInterfaceAdapter(constructor.newInstance(parameters));
        } else {
            throw new Exception("The interface adapter cannot be null. An interface adapter must be defined using ChatSDK.configure(...) or by a module");
        }

        DaoCore.init(ctx());

        shared().storageManager = new StorageManager();
        shared().locationProvider = new LocationProvider();

        // Monitor the app so if it goes into the background we know
        AppBackgroundMonitor.shared().setEnabled(true);

        RxJavaPlugins.setErrorHandler(ChatSDK.events());

        shared().fileManager = new FileManager(builder.context);

        for (Module module: builder.modules) {
            module.activate(shared().context());
            Logger.info("Module " + module.getName() + " activated successfully");
        }

        return shared();
    }

    public static ChatSDK shared() {
        return instance;
    }

    public static Context ctx() {
        return shared().context();
    }

    public SharedPreferences getPreferences() {
//        return Single.defer(new Callable<SingleSource<? extends SharedPreferences>>() {
//            @Override
//            public SingleSource<? extends SharedPreferences> call() throws Exception {
//                return Single.just(context.get().getSharedPreferences(Preferences, Context.MODE_PRIVATE));
//            }
//        }).subscribeOn(Schedulers.io());

        TimeLog.startTimeLog(new Object(){}.getClass().getEnclosingMethod().getName());
        SharedPreferences preferences = context.get().getSharedPreferences(Preferences, Context.MODE_PRIVATE);
        TimeLog.endTimeLog();
        return preferences;
    }

    public String getString(@StringRes int stringId) {
        return context().getString(stringId);
    }

    public Context context() {
        return context.get();
    }

    public static Config config () {
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
