package sdk.chat.demo;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.contact.ContactBookModule;
import co.chatsdk.firestream.FireStreamModule;
import co.chatsdk.xmpp.module.XMPPModule;
import co.chatsdk.xmpp.read_receipt.XMPPReadReceiptsModule;
import firestream.chat.FirestreamConfig;
import sdk.chat.core.module.Module;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Config;

import sdk.chat.core.session.Configure;
import co.chatsdk.firebase.blocking.FirebaseBlockingModule;
import co.chatsdk.firebase.file_storage.FirebaseUploadModule;
import co.chatsdk.firebase.module.FirebaseModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.last_online.FirebaseLastOnlineModule;
import co.chatsdk.message.file.FileMessageModule;
import co.chatsdk.message.sticker.module.StickerMessageModule;
import co.chatsdk.profile.pictures.ProfilePicturesModule;
import co.chatsdk.read_receipts.FirebaseReadReceiptsModule;
import co.chatsdk.typing_indicator.FirebaseTypingIndicatorModule;
import co.chatsdk.ui.module.DefaultUIModule;
import co.chatsdk.ui.module.UIConfig;
import io.reactivex.subjects.PublishSubject;
import sdk.chat.audio.AudioMessageModule;
import sdk.chat.demo.testing.Testing;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.ui.extras.ExtrasModule;

public class DemoConfigBuilder {

    public enum Updated {
        Backend,
        Style,
        LoginStyle,
        Database,
        All,
    }

    protected static final DemoConfigBuilder instance = new DemoConfigBuilder();

    public static DemoConfigBuilder shared() {
        return instance;
    }

    public PublishSubject<Updated> updated = PublishSubject.create();

    public enum Backend {
        Firebase,
        FireStream,
        XMPP
    }

    public enum Style {
        Drawer,
        Tabs
    }

    public enum LoginStyle {
        FirebaseUI,
        Custom
    }

    public enum Database {
        Realtime,
        Firestore,
        OpenFire,
        Custom
    }

    protected Backend backend = null;
    protected Style style = null;
    protected LoginStyle loginStyle = null;
    protected Database database = null;


    public DemoConfigBuilder setBackend(Backend backend) {
        if (this.backend != backend) {
            this.backend = backend;
            updated.onNext(Updated.Backend);
        }
        return this;
    }

    public DemoConfigBuilder setStyle(Style style) {
        if (this.style != style) {
            this.style = style;
            updated.onNext(Updated.Style);
        }
        return this;
    }

    public DemoConfigBuilder setLoginStyle(LoginStyle style) {
        if (this.loginStyle != style) {
            this.loginStyle = style;
            updated.onNext(Updated.LoginStyle);
        }
        return this;
    }

    public DemoConfigBuilder setDatabase(Database database) {
        if (this.database != database) {
            this.database = database;
            updated.onNext(Updated.Database);
        }
        return this;
    }

    public Backend getBackend() {
        return backend;
    }

    public Style getStyle() {
        return style;
    }

    public LoginStyle getLoginStyle() {
        return loginStyle;
    }

    public Database getDatabase() {
        return database;
    }

    public void save(Context context) {
        SharedPreferences.Editor editor = prefs(context).edit();
        editor.putString("backend", backend.toString());
        editor.putString("style", style.toString());
        editor.putString("loginStyle", loginStyle.toString());
        editor.putString("database", database.toString());
        editor.apply();
    }

    public void load(Context context) {
        SharedPreferences prefs = prefs(context);
        String backend = prefs.getString("backend", null);
        if (backend != null) {
            this.backend = Backend.valueOf(backend);
        }
        String style = prefs.getString("style", null);
        if (style != null) {
            this.style = Style.valueOf(style);
        }
        String loginStyle = prefs.getString("loginStyle", null);
        if (loginStyle != null) {
            this.loginStyle = LoginStyle.valueOf(loginStyle);
        }
        String database = prefs.getString("database", null);
        if (database != null) {
            this.database = Database.valueOf(database);
        }
    }

    public SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("chat-sdk-demo-config", Context.MODE_PRIVATE);
    }

    public boolean isConfigured() {
        return backend != null && style != null && loginStyle != null && database != null;
    }

    public void setupChatSDK(Context context) {
        List<Module> modules = new ArrayList<>();

        // Backend module
        if (backend == Backend.FireStream) {
            modules.add(FireStreamModule.builder(config -> config
                    .setRoot(database == Database.Realtime ? "live_firestream_realtime" : "live_firestream_firestore")
                    .setSandbox("firestream")
                    .setStartListeningFromLastSentMessageDateEnabled(false)
                    .setListenToMessagesWithTimeAgo(FirestreamConfig.TimePeriod.days(7))
                    .setDatabaseType(database == Database.Realtime ? FirestreamConfig.DatabaseType.Realtime : FirestreamConfig.DatabaseType.Firestore)
                    .setDeleteMessagesOnReceiptEnabled(false)
                    .setDeliveryReceiptsEnabled(false)
            ));
        }
        if (backend == Backend.Firebase) {
            FirebaseModule fb = FirebaseModule.builder()
                    .setFirebaseRootPath("live_firebase").build();

            modules.add(fb);
            modules.add(FirebaseBlockingModule.shared());
            modules.add(FirebaseLastOnlineModule.shared());
//                modules.add(FirebaseNearbyUsersModule.shared());
            modules.add(FirebaseReadReceiptsModule.shared());
            modules.add(FirebaseTypingIndicatorModule.shared());

        }

        try {

            Configure<UIConfig> uiConfigConfigure = config -> {
                config.setPublicRoomCreationEnabled(true);
            };

            if (backend == Backend.XMPP) {
                modules.add(Testing.myOpenFire(XMPPModule.builder()).build().configureUI(uiConfigConfigure));
                modules.add(XMPPReadReceiptsModule.shared());
            } else {
                modules.add(DefaultUIModule.builder(uiConfigConfigure));
            }

            if (loginStyle == LoginStyle.FirebaseUI) {
                modules.add(FirebaseUIModule.builder(config -> config
                            .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
                ));
            }
            if (style == Style.Drawer) {
                modules.add(ExtrasModule.shared());
            }

            ChatSDK.builder()

                    // Configure the library
                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .setPublicChatRoomLifetimeMinutes(60 * 24)
                    .setAnonymousLoginEnabled(false)
                    .setDebugModeEnabled(true)
                    .setRemoteConfigEnabled(true)
                    .setIdenticonType(Config.IdenticonType.Gravatar)
                    .build()

                    .addModules(modules)

                    // Add modules to handle file uploads, push notifications
                    .addModule(FirebaseUploadModule.shared())
                    .addModule(FirebasePushModule.shared())
                    .addModule(ProfilePicturesModule.shared())

                    .addModule(ContactBookModule.shared())
//                    .addModule(EncryptionModule.shared())
                    .addModule(FileMessageModule.shared())
                    .addModule(AudioMessageModule.shared())
                    .addModule(StickerMessageModule.shared())
                    .addModule(VideoMessageModule.shared())

                    // Activate
                    .build()
                    .activate(context);

        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.debug("Error");
            assert(false);
        }
    }

}
