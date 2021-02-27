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
import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;

public class ChatSDKXMPP extends QuickStart {

    public static void quickStartWithPatreon(Context context, String hostAddress, String domain, int port, String googleMapsKey, boolean drawerEnabled, String patreonId, Module... modules) throws Exception {
        quickStart(context, hostAddress, domain, port, googleMapsKey, drawerEnabled, patreon(patreonId), modules);
    }

    public static void quickStartWithEmail(Context context, String hostAddress, String domain, int port, String googleMapsKey, boolean drawerEnabled, String email, Module... modules) throws Exception {
        quickStart(context, hostAddress, domain, port, googleMapsKey, drawerEnabled, email(email), modules);
    }

    public static void quickStartWithGithubSponsors(Context context, String hostAddress, String domain, int port, String googleMapsKey, boolean drawerEnabled, String githubId, Module... modules) throws Exception {
        quickStart(context, hostAddress, domain, port, googleMapsKey, drawerEnabled, github(githubId), modules);
    }

    public static void quickStart(Context context, String hostAddress, String domain, int port, String googleMapsKey, boolean drawerEnabled, Module... modules) throws Exception {
        quickStart(context, hostAddress, domain, port, googleMapsKey, drawerEnabled, null, modules);
    }

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
    public static void quickStart(Context context, String hostAddress, String domain, int port, String googleMapsKey, boolean drawerEnabled, String identifier, Module... modules) throws Exception {

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

                LocationMessageModule.shared(),

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
                .activate(context, identifier);
    }



}
