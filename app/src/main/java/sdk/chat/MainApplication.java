package sdk.chat;

import android.app.Application;

import org.pmw.tinylog.Level;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import sdk.chat.core.module.ImageMessageModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.location.LocationMessageModule;
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

        Disposable d3 = ChatSDK.events().errorSourceOnMain().subscribe(throwable -> {
            //
            throwable.printStackTrace();
        });
    }

    public void firebase() throws Exception {
        String rootPath = "pre_998";

        ChatSDK.builder()
                .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                .setAnonymousLoginEnabled(false)
                .setReuseDeleted1to1Threads(false)
                .setLogLevel(Level.DEBUG)
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
                .addModule(ImageMessageModule.shared())

                // Add the UI module
                .addModule(UIModule.builder()
                                .setPublicRoomCreationEnabled(false)
                                .setPublicRoomsEnabled(false)
                                .build()
                )

                // Add modules to handle file uploads, push notifications
                .addModule(FirebaseUploadModule.shared())
                .addModule(LocationMessageModule.shared())
                .addModule(FirebasePushModule.shared())

                .addModule(ExtrasModule.builder(config -> {
                    config.setDrawerEnabled(false);
                }))
                .build()
                .activateWithEmail(this, "team@sdk.chat");


    }

}
