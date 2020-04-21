package sdk.chat.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;

import io.reactivex.functions.Consumer;
import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.base.LocationProvider;
import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.AudioMessageHandler;
import sdk.chat.core.handlers.AuthenticationHandler;
import sdk.chat.core.handlers.BlockingHandler;
import sdk.chat.core.handlers.ContactHandler;
import sdk.chat.core.handlers.ContactMessageHandler;
import sdk.chat.core.handlers.CoreHandler;
import sdk.chat.core.handlers.EncryptionHandler;
import sdk.chat.core.handlers.EventHandler;
import sdk.chat.core.handlers.FileMessageHandler;
import sdk.chat.core.handlers.HookHandler;
import sdk.chat.core.handlers.ImageMessageHandler;
import sdk.chat.core.handlers.LastOnlineHandler;
import sdk.chat.core.handlers.LocationMessageHandler;
import sdk.chat.core.handlers.Module;
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
import sdk.chat.core.interfaces.InterfaceAdapter;
import sdk.chat.core.storage.FileManager;
import sdk.chat.core.utils.AppBackgroundMonitor;
import io.reactivex.plugins.RxJavaPlugins;
import sdk.guru.common.BiAction;
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

    protected LocationProvider locationProvider;
    protected FileManager fileManager;

    protected ConfigBuilder<ChatSDK> builder;

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

    /**
     * Configure and let modules provide the interface and network adapters. We will loop over the modules and see if they provide each adapter,
     * the first that does will be used and any subsequent provider will be ignored.
     * @return
     */
    public static ConfigBuilder<ChatSDK> builder() {
         shared().builder = new ConfigBuilder<>(shared());
        return shared().builder;
    }

    public void activate(Context context) throws Exception {
        setContext(context);

        config = builder.config();

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
            setNetworkAdapter(networkAdapter.getConstructor().newInstance());
        } else {
            throw new Exception("The network adapter cannot be null. A network adapter must be defined using ChatSDK.configure(...) or by a module");
        }

        if (interfaceAdapter != null) {
            Constructor<? extends InterfaceAdapter> constructor = interfaceAdapter.getConstructor(Context.class);
            Object[] parameters = {context};

            setInterfaceAdapter(constructor.newInstance(parameters));
        } else {
            throw new Exception("The interface adapter cannot be null. An interface adapter must be defined using ChatSDK.configure(...) or by a module");
        }

        DaoCore.init(ctx());

        storageManager = new StorageManager();
        locationProvider = new LocationProvider();

        // Monitor the app so if it goes into the background we know
        AppBackgroundMonitor.shared().setEnabled(true);

        RxJavaPlugins.setErrorHandler(ChatSDK.events());

        fileManager = new FileManager(context);

        for (Module module: builder.modules) {
            module.activate(context);
            Logger.info("Module " + module.getName() + " activated successfully");
        }

    }

    public static ChatSDK initialize(ConfigBuilder builder) throws Exception {


        return shared();
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
