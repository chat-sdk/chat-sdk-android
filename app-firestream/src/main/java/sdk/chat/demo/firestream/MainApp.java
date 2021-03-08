package sdk.chat.demo;

import android.app.Application;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import org.pmw.tinylog.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import sdk.chat.core.module.Module;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.ui.FirebaseUIModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.firestream.adapter.FireStreamModule;
import sdk.chat.firestream.adapter.FirebaseServiceType;

import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {

            List<Module> newModules = Arrays.asList(
                    FireStreamModule.builder(FirebaseServiceType.Realtime)
                            .setRoot("pre_1")
                            .setSandbox("firestream")
                            .build(),

                    UIModule.builder()
                            .setPublicRoomCreationEnabled(true)
                            .setPublicRoomsEnabled(true)
                            .build(),

                    LocationMessageModule.shared(),
                    FirebaseUploadModule.shared(),
                    FirebasePushModule.shared(),

                    ExtrasModule.builder(config -> {
                        config.setDrawerEnabled(false);
                    }),

                    FirebaseUIModule.builder()
                            .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
                            .build()
            );

            ChatSDK.builder()
                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .setAnonymousLoginEnabled(false)

                    .setRemoteConfigEnabled(false)
                    .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
                    .setSendSystemMessageWhenRoleChanges(true)
                    .build()

                    .addModules(newModules)

                    // Activate
                    .build()
                    .activateWithEmail(this, "ben@sdk.chat");

            ChatSDK.events().sourceOnMain().subscribe(event -> {
                Logger.debug(event);
            });

            ChatSDK.events().errorSourceOnMain().subscribe(event -> {
                Logger.debug(event);
                event.printStackTrace();
            });

        } catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }
}
