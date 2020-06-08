package sdk.chat.app.xmpp;

import android.content.Context;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.xmpp.adapter.module.XMPPModule;
import sdk.chat.core.module.Module;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.QuickStart;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;

public class ChatSDKXMPP extends QuickStart {

    /**
     * @param context
     * @param hostAddress XMPP host address
     * @param domain XMPP domain
     * @param port XMPP port
     * @param googleMapsKey Google static maps key.
     * @param drawerEnabled Whether to use drawer or tabs (Default)
     * @param modules Optional modules
     * @throws Exception
     */
    public static void quickStart(Context context, String hostAddress, String domain, int port, String googleMapsKey, boolean drawerEnabled, Module... modules) throws Exception {

        List<Module> newModules = Arrays.asList(

                XMPPModule.builder()
                        .setXMPP(hostAddress, domain, port)
                        .build(),

                UIModule.builder()
                        .setPublicRoomCreationEnabled(true)
                        .setPublicRoomsEnabled(true)
                        .setResetPasswordEnabled(false)
                        .build(),

                FirebaseUploadModule.shared(),

                FirebasePushModule.shared(),

                ExtrasModule.builder()
                        .setDrawerEnabled(drawerEnabled)
                        .build()

        );

        ChatSDK.builder()
                .setGoogleMaps(googleMapsKey)
                .setAnonymousLoginEnabled(false)
                .setClientPushEnabled(true)
                .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
                .setSendSystemMessageWhenRoleChanges(true)
                .build()

                .addModules(deduplicate(newModules, modules))

                // Activate
                .build()
                .activate(context);;
    }



}
