package sdk.chat.app.firestream;

import android.content.Context;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.chatsdk.firebase.file_storage.FirebaseUploadModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.firestream.FireStreamModule;
import co.chatsdk.ui.module.DefaultUIModule;
import sdk.chat.core.module.Module;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Config;
import sdk.chat.core.utils.QuickStart;
import sdk.chat.ui.extras.ExtrasModule;

public class ChatSDKFireStream extends QuickStart {

    /**
     * @param context
     * @param rootPath Firebase base path (can be any string, cannot contain special characters)
     * @param googleMapsKey Google static maps key.
     * @param drawerEnabled Whether to use drawer or tabs (Default)
     * @param modules Optional modules
     * @throws Exception
     */
    public static void quickStart(Context context, String rootPath, String googleMapsKey, boolean drawerEnabled, Module... modules) throws Exception {

        List<Module> newModules = Arrays.asList(
                FireStreamModule.builder()
                        .setRoot(rootPath)
                        .build(),

                DefaultUIModule.builder()
                        .setPublicRoomCreationEnabled(true)
                        .setPublicRoomsEnabled(true)
                        .build(),

                FirebaseUploadModule.shared(),

                FirebasePushModule.shared(),

                ExtrasModule.builder(config -> {
                    config.setDrawerEnabled(drawerEnabled);
                }),

                FirebaseUIModule.builder()
                        .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
                        .build()
        );

        ChatSDK.builder()
                .setGoogleMaps(googleMapsKey)
                .setAnonymousLoginEnabled(false)

                .setRemoteConfigEnabled(false)
                .setIdenticonType(Config.IdenticonType.RoboHash)
                .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
                .setSendSystemMessageWhenRoleChanges(true)
                .build()

                .addModules(deduplicate(newModules, modules))

                // Activate
                .build()
                .activate(context);;
    }


}
