package co.chatsdk.android.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.error.ChatSDKException;
import co.chatsdk.core.handlers.VideoMessageHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.session.NetworkManager;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;
import co.chatsdk.ui.chat.options.MediaChatOption;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import io.reactivex.Observable;

/**
 * Created by itzik on 6/8/2014.
 */
public class AppObj extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        Configuration.Builder config = new Configuration.Builder(context);
//        builder.firebaseRootPath("firebase_v4_web_new_4");
        config.firebaseRootPath("18_08_test_4");
        config.googleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE");
        config.firebaseCloudMessagingServerKey("AAAA_WvJyeI:APA91bFIDYoxnbFTub61SKCh8-RZrElzdkZpzyV3paGFlRWonMzq33zQmQW3ub5hDXLuRaipwtoHSoDKXkZlN5DRb_EYdrxtaDptmvZKCYBPKI-4RqTK9wVLOJvgc5X3bVWLfpNSJO_tLK2pnmhfpHDw2Zs-5L2yug");
        config.publicRoomCreationEnabled(false);
        config.pushNotificationSound("default");

        try {
            ChatSDK.initialize(config.build(), new BaseInterfaceAdapter(context), new FirebaseNetworkAdapter());
        }
        catch (ChatSDKException e) {

        }

        NetworkManager.shared().a.videoMessage = new VideoMessageHandler() {
            @Override
            public Observable<MessageSendProgress> sendMessageWithVideo(String videoPath, Thread thread) {
                return null;
            }

            @Override
            public void updateMessageCellView(Message message, Object viewHolder, Context context) {

            }
        };

        InterfaceManager.shared().a.addChatOption(new MediaChatOption(ChatSDK.shared().context().getString(R.string.take_video), MediaChatOption.Type.TakeVideo));
        InterfaceManager.shared().a.addChatOption(new MediaChatOption(ChatSDK.shared().context().getString(R.string.choose_video), MediaChatOption.Type.ChooseVideo));


        FirebaseFileStorageModule.activate();
        FirebasePushModule.activateForFirebase();
//        FirebaseUIModule.activate(context, EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID);

    }

    @Override
    protected void attachBaseContext (Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
