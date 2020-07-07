package sdk.chat.demo;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.contact.ContactBookModule;
import sdk.chat.core.module.Module;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.firbase.online.FirebaseLastOnlineModule;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.blocking.FirebaseBlockingModule;
import sdk.chat.firebase.location.FirebaseNearbyUsersModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.receipts.FirebaseReadReceiptsModule;
import sdk.chat.firebase.typing.FirebaseTypingIndicatorModule;
import sdk.chat.firebase.ui.FirebaseUIModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.firestream.adapter.FireStreamModule;
import sdk.chat.firestream.adapter.FirebaseServiceType;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.file.FileMessageModule;
import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.profile.pictures.ProfilePicturesModule;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIConfig;
import sdk.chat.ui.module.UIModule;

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

    public PublishRelay<Updated> updated = PublishRelay.create();

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

    protected Backend backend = Backend.Firebase;
    protected Style style = Style.Tabs;
    protected LoginStyle loginStyle = LoginStyle.FirebaseUI;
    protected Database database;

    protected boolean configured = false;

    public DemoConfigBuilder setBackend(Backend backend) {
        if (this.backend != backend) {
            this.backend = backend;
            updated.accept(Updated.Backend);
        }
        return this;
    }

    public DemoConfigBuilder setStyle(Style style) {
        if (this.style != style) {
            this.style = style;
            updated.accept(Updated.Style);
        }
        return this;
    }

    public DemoConfigBuilder setLoginStyle(LoginStyle style) {
        if (this.loginStyle != style) {
            this.loginStyle = style;
            updated.accept(Updated.LoginStyle);
        }
        return this;
    }

    public DemoConfigBuilder setDatabase(Database database) {
        if (this.database != database) {
            this.database = database;
            updated.accept(Updated.Database);
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
        if (backend != null) {
            editor.putString("backend", backend.toString());
        }
        if (style != null) {
            editor.putString("style", style.toString());
        }
        if (loginStyle != null) {
            editor.putString("loginStyle", loginStyle.toString());
        }
        if (database != null) {
            editor.putString("database", database.toString());
        }
        if (isValid()) {
            editor.putBoolean("chat-sdk-configured", true);
        }
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
        configured = prefs.getBoolean("chat-sdk-configured", false);
    }

    public SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("chat-sdk-demo-config", Context.MODE_PRIVATE);
    }

    public boolean isValid() {
        return backend != null && style != null && loginStyle != null; // && database != null;
    }

    public boolean isConfigured() {
        return configured;
    }

    @Deprecated
    public boolean isConfiguredForFirebase() {
        return isConfigured();
//        return backend != null && style != null && loginStyle != null;
    }

    public void setupChatSDK(Context context) throws Exception {

        if (ChatSDK.shared().isValid() || !isValid()) {
            return;
        }

        List<Module> modules = new ArrayList<>();

        // Backend module
        if (backend == Backend.FireStream) {
            modules.add(FireStreamModule.builder(
                    database == Database.Realtime ? FirebaseServiceType.Realtime : FirebaseServiceType.Firestore,
                    config -> config
                    .setRoot(database == Database.Realtime ? "firestream_realtime" : "firestream_firestore")
                    .setSandbox("firestream")
                    .setDeleteMessagesOnReceiptEnabled(false)
                    .setDeliveryReceiptsEnabled(false)
            ));
//            modules.add(FirestreamBlockingModule.shared());
            modules.add(FirebaseNearbyUsersModule.shared());
//            modules.add(FireStreamReadReceiptsModule.shared());
//            modules.add(FirestreamTypingIndicatorModule.shared());

        }
        if (backend == Backend.Firebase) {
            modules.add(FirebaseModule.builder()
                    .setFirebaseRootPath("firebase")
                    .setEnableCompatibilityWithV4(false)
                    .build());

            modules.add(FirebaseBlockingModule.shared());
            modules.add(FirebaseLastOnlineModule.shared());
            modules.add(FirebaseNearbyUsersModule.shared());
            modules.add(FirebaseReadReceiptsModule.shared());
            modules.add(FirebaseTypingIndicatorModule.shared());

        }

        Configure<UIConfig> uiConfigConfigure = config -> {
            config.setPublicRoomCreationEnabled(true);
//            config.setTheme(R.style.ChatSDKTheme);
        };

        if (backend == Backend.XMPP) {
//                modules.add(Testing.myOpenFire(XMPPModule.builder()).build().configureUI(uiConfigConfigure));
//                modules.add(XMPPReadReceiptsModule.shared());
        } else {
            modules.add(UIModule.builder(uiConfigConfigure));
        }

        if (loginStyle == LoginStyle.FirebaseUI) {
            modules.add(FirebaseUIModule.builder(config -> config
                        .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
            ));
        }
        modules.add(ExtrasModule.builder(config -> {
            config.setDrawerEnabled(style == Style.Drawer);
        }));

        ChatSDK.builder()

                // Configure the library
                .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                .setPublicChatRoomLifetimeMinutes(60 * 24)
                .setAnonymousLoginEnabled(false)
                .setDebugModeEnabled(false)
                .setRemoteConfigEnabled(true)
                // We are handling this ourselves
                .setInboundPushHandlingEnabled(false)
                .build()

                .addModules(modules)

                // Add modules to handle file uploads, push notifications
                .addModule(FirebaseUploadModule.shared())
                .addModule(FirebasePushModule.shared())
                .addModule(ProfilePicturesModule.shared())
                .addModule(LocationMessageModule.shared())

                .addModule(ContactBookModule.shared())
//                    .addModule(EncryptionModule.shared())
                .addModule(FileMessageModule.shared())
                .addModule(AudioMessageModule.shared())
                .addModule(StickerMessageModule.shared())
                .addModule(VideoMessageModule.shared())

                // Activate
                .build()
                .activate(context, "team@sdk.chat");


    }

}
