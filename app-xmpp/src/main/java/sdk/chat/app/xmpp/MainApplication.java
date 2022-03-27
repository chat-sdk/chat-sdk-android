package sdk.chat.app.xmpp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.util.TLSUtils;
import org.pmw.tinylog.Logger;

import java.lang.reflect.Field;

import app.xmpp.adapter.XMPPManager;
import app.xmpp.adapter.module.XMPPModule;
import io.reactivex.disposables.Disposable;
import sdk.chat.app.xmpp.utils.SecureKeyStore;
import sdk.chat.core.dao.Message;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.module.ImageMessageModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.file.FileMessageModule;
import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;

public class MainApplication extends Application {

    public SecureKeyStore store;

    @Override
    public void onCreate() {
        super.onCreate();
        xmpp();
    }

    public void xmpp() {
        try {

            store = new SecureKeyStore(this);

            ChatSDK.builder()

                    // Configure the library
                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .setAnonymousLoginEnabled(false)
                    .setDebugModeEnabled(false)
                    .setThreadDestructionEnabled(false)
                    .setClientPushEnabled(true)
                    .setAllowUserToRejoinGroup(true)
                    .build()

                    // Add modules to handle file uploads, push notifications
                    .addModule(FirebaseUploadModule.shared())
                    .addModule(FirebasePushModule.shared())

                    .addModule(XMPPModule.builder()
//                            .setXMPP("we-connect-dev.com", "we-connect-dev.com")
                            .setXMPP("xmpp.app", "xmpp.app")
                            .setAllowServerConfiguration(false)
//                            .setSecurityMode("required")
//                            .setSecurityMode("ifpossible")
//                            .setSecurityMode("ifpossible")
                            .setPingInterval(5)

                            .setDebugEnabled(true)
                            .setSecurityMode("ifpossible")
                            .build())

                    .addModule(AudioMessageModule.shared())
                    .addModule(LocationMessageModule.shared())
                    .addModule(ImageMessageModule.shared())
                    .addModule(VideoMessageModule.shared())
                    .addModule(FileMessageModule.shared())
                    .addModule(StickerMessageModule.builder()
                            .build())

                    .addModule(UIModule.builder()
                            .setRequestPermissionsOnStartup(false)
                            .setMessageSelectionEnabled(true)
                            .setUsernameHint("JID")
                            .setMessageForwardingEnabled(true)
                            .setMessageReplyEnabled(true)
                            .setResetPasswordEnabled(false)
                            .setPublicRoomCreationEnabled(true)
                            .setPublicRoomsEnabled(false)
                            .build())

//                    .addModule(XMPPReadReceiptsModule.shared())
                    .addModule(ExtrasModule.builder()
                            .setQrCodesEnabled(true)
                            .setDrawerEnabled(false)
                            .build())

                    .build().activateWithEmail(this, "ben@sdk.chat");

//            ChatSDK.config().setDebugUsername(Device.honor() ? "a3": "a4");
//            ChatSDK.config().setDebugPassword("123");

//            ChatSDK.ui().setThreadDetailsActivity(ThreadDetailsActivity.class);

//            chatsdkAuth(Device.honor() ? "xxx1" : "xxx2", "123", "test@conference.xmpp.app");

        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getLocalizedMessage());
            assert(false);
        }

        XMPPModule.config().setConnectionConfigProvider(builder -> {
            try {
                builder.setCustomX509TrustManager(new TLSUtils.AcceptAllTrustManager());
                builder.setCompressionEnabled(false);
            } catch(Exception e) {
                Logger.debug(e.getLocalizedMessage());
            }
        });

        Disposable d = ChatSDK.events().sourceOnMain().subscribe(networkEvent -> {
            networkEvent.debug();
        });

        d = ChatSDK.events().errorSourceOnMain().subscribe(throwable -> {
            // Catch errors
            throwable.printStackTrace();
        });

        ChatSDK.hook().addHook(Hook.sync(data -> {
            Object message = data.get(HookEvent.Message);
            if (message instanceof Message) {
                Logger.info(message);
            }
        }), HookEvent.MessageReceived);

        ChatSDK.hook().addHook(Hook.sync(data -> {
            Object entryObject = data.get(XMPPManager.xmppRosterEntry);
            if (entryObject instanceof RosterEntry) {
                RosterEntry entry = (RosterEntry) entryObject;
                Logger.debug(entry);
            }
        }), XMPPManager.xmppRosterItemUpdated, XMPPManager.xmppRosterItemAdded, XMPPManager.xmppRosterItemRemoved);

        // Use encrypted shared preferences
        try {

            Field field = ChatSDK.class.getDeclaredField("keyStorage");
            field.setAccessible(true);
            field.set(ChatSDK.shared(), new SecureKeyStore(this));
            System.out.println("Ok");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Navbar title black
        // Chat screen ... rather than +
        // Message selected buttons not showing i nav bar
        // Chat attachment button is camera???

    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (name.equals(ChatSDK.Preferences) && mode == Context.MODE_PRIVATE) {
            return store.pref();
        }
        return super.getSharedPreferences(name, mode);
    }


//    private void chatsdkAuth(String username, String password, String threadId) {
//        if (ChatSDK.auth().isAuthenticatedThisSession()) {
//            chatsdkGroup(threadId);
//        } else {
//            ChatSDK.auth().authenticate(AccountDetails.username(username, password)).subscribe(() -> {
//                chatsdkGroup(threadId);
//            }, throwable -> {
//                ChatSDK.auth().authenticate(AccountDetails.signUp(username, password)).subscribe(() -> {
//                    chatsdkGroup(threadId);
//                }, throwable1 -> {
////                    view.displayFailure("Gagal register group");
//                });
//            });
//        }
//    }
//
//    private void chatsdkGroup(String threadId){
//        Thread thread = getThread(threadId);
//        if(thread != null) {
////            if(ChatSDK.thread().hasVoice(thread, ChatSDK.currentUser())){
//                ChatSDK.thread().joinThread(thread).observeOn(RX.main()).subscribe(() -> {
//                    startChatActivity(thread);
//                }, throwable -> {
////                    view.displayFailure("Gagal join group");
//                });
//        }
//    }
//
//    private Thread getThread(String threadId){
//        Thread thread =  ChatSDK.db().fetchOrCreateThreadWithEntityID(threadId);
//        thread.setType(ThreadType.PrivateGroup);
//        return thread;
//    }
//
//    private void startChatActivity(Thread thread){
//        ChatSDK.ui().startChatActivityForID(this, thread.getEntityID(), Intent.FLAG_ACTIVITY_NEW_TASK);
//    }
}