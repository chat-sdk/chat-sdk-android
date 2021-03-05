package sdk.chat;

import android.app.Application;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import sdk.chat.android.live.R;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Device;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.ui.FirebaseUIModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.profile.pictures.ProfilePicturesModule;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;

/**
 * Created by Ben Smiley on 6/8/2014.
 */
//public class MainApplication extends MultiDexApplication {
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            firebase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void firebase() throws Exception {
        String rootPath = "pre_2";

        ChatSDK.builder()
                .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                .setAnonymousLoginEnabled(false)

//                .setDebugModeEnabled(true)
                .setRemoteConfigEnabled(false)
                .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
                .setSendSystemMessageWhenRoleChanges(true)
                .setRemoteConfigEnabled(true)
                .build()

                // Add the network adapter module
                .addModule(
                        FirebaseModule.builder()
                                .setFirebaseRootPath(rootPath)
                                .setDisableClientProfileUpdate(false)
                                .setDevelopmentModeEnabled(true)
                                .build()
                )

                // Add the UI module
                .addModule(UIModule.builder()
                        .setPublicRoomCreationEnabled(true)
                        .setPublicRoomsEnabled(true)
                        .setTheme(R.style.GGTheme)
                        .build()
                )

                // Add modules to handle file uploads, push notifications
                .addModule(FirebaseUploadModule.shared())
                .addModule(FirebasePushModule.shared())
                .addModule(ProfilePicturesModule.shared())

                .addModule(ExtrasModule.builder(config -> {
                    if (Device.honor(this)) {
                        config.setDrawerEnabled(false);
                    }
                }))

                .addModule(FirebaseUIModule.builder()
                        .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
                        .build()
                )

                // Activate
                .build()
                .activate(this);


        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {
            networkEvent.debug();
        });

        d = ChatSDK.events().errorSourceOnMain().subscribe(t -> {
            t.printStackTrace();
        });


    }



}
