package sdk.chat.demo.examples;

import android.app.Activity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import co.chatsdk.contact.ContactBookModule;
import co.chatsdk.encryption.EncryptionModule;
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
import sdk.chat.audio.AudioMessageModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Config;
import sdk.chat.location.FirebaseNearbyUsersModule;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.realtime.R;
import sdk.chat.ui.extras.ExtrasModule;

public class AddFirebaseModules extends Activity {

    public void add() throws Exception {

        ChatSDK.builder()
                .setDebugUsername(null)
                .setDebugPassword(null)
                .setGoogleMaps(null)
                .setReplyFromNotificationEnabled(true)
                .setDisconnectFromFirebaseWhenInBackground(true)
                .setAnonymousLoginEnabled(true)
                .setPushNotificationAction(null)
                .setShowEmptyChats(true)
                .setInboundPushHandlingEnabled(true)
                .setReuseDeleted1to1Threads(true)
                .setLocalPushNotificationsForPublicChatRoomsEnabled(false)
                .setRemoteConfigEnabled(false)
                .setMessageDeletionListenerLimit(30)
                .setUserSearchLimit(20)
                .setMessagesToLoadPerBatch(30)
                .setClientPushEnabled(false)
                .setMaxImageWidth(1920)
                .setMaxImageHeight(2560)
                .setMaxThumbnailDimensions(400)
                .setDefaultNamePrefix("ChatSDK")
                .setDefaultName(null)
                .setLogoDrawableResourceID(R.drawable.ic_launcher_big)
                .setDefaultUserAvatarUrl(null)
                .setPushNotificationImageDefaultResourceId(0)
                .setOnlySendPushToOfflineUsers(false)
                .setPushNotificationSound("")
                .setPushNotificationColor("#ff33b5e5")
                .setLocationURLRepresentation("https://www.google.com/maps/search/?api=1&query=%f,%f")
                .setPublicChatAutoSubscriptionEnabled(false)
                .setStorageDirectory("ChatSDK")
                .setIdenticonBaseURL(null)
                .setIdenticonType(Config.IdenticonType.RoboHash)
                .setPublicChatRoomLifetimeMinutes(TimeUnit.DAYS.toMinutes(7))
                .setDisablePresence(false)
                .setSendSystemMessageWhenRoleChanges(true)
                .setRolesEnabled(true)
                .build()

                // Firebase Module
                .addModule(
                        FirebaseModule.builder()
                                .setFirebaseRootPath("pre_1")
                                .setFirebaseDatabaseURL(null)
                                .setFirebaseApp(null)
                                .setDisableClientProfileUpdate(false)
                                .setDevelopmentModeEnabled(true)
                                .setDisablePublicThreads(false)
                                .setEnableCompatibilityWithV4(true)
                                .setEnableWebCompatibility(false)
                                .build()
                )

                // UI Module
                .addModule(DefaultUIModule.builder()
                        .setTheme(R.style.ChatTheme)
                        .setImageCroppingEnabled(true)
                        .setResetPasswordEnabled(true)
                        .setPublicRoomCreationEnabled(true)
                        .setPublicRoomsEnabled(true)
                        .setImageMessagesEnabled(true)
                        .setLocationMessagesEnabled(true)
                        .setGroupsEnabled(true)
                        .setThreadDetailsEnabled(true)
                        .setSaveImagesToDirectoryEnabled(true)
                        .setDefaultProfileImage(co.chatsdk.ui.R.drawable.icn_100_profile)
                        .setProfileHeaderImage(co.chatsdk.ui.R.drawable.header2)
                        .setUsernameHint("Email")
                        .setAllowBackPressFromMainActivity(false)
                        .build()
                )

                // Firebase Upload
                .addModule(FirebaseUploadModule.builder()
                        .setFirebaseStorageURL(null)
                        .build())

                // Firebase Push Notifications
                .addModule(FirebasePushModule.builder()
                        .setFirebaseFunctionsRegion(null)
                        .build())

                // Profile pictures module
                .addModule(ProfilePicturesModule.shared())

                // Contact book module
                .addModule(ContactBookModule.builder()
                        .contactBook("Email subject", "Email body", "SMS body")
                        .build())

                // Encryption module
                .addModule(EncryptionModule.shared())

                // File messages module
                .addModule(FileMessageModule.shared())

                // Audio messages module
                .addModule(AudioMessageModule.builder()
                        .setCompressionEnabled(true)
                        .build())

                // Sticker messages module
                .addModule(StickerMessageModule.shared())

                // Video messages module
                .addModule(VideoMessageModule.shared())

                // Firebase blocking module
                .addModule(FirebaseBlockingModule.shared())

                // Last online modulle
                .addModule(FirebaseLastOnlineModule.shared())

                // Nearby users module
                .addModule(FirebaseNearbyUsersModule.builder()
                        .setMaxDistance(50000)
                        .setMinimumDisplayResolution(100)
                        .setMinRefreshDistance(50)
                        .setMinRefreshTime(1)
                        .build())

                // Firebase Read receipts module
                .addModule(FirebaseReadReceiptsModule.builder()
                        .setMaxAge(TimeUnit.DAYS.toMillis(7))
                        .setMaxMessagesPerThread(20)
                        .build())

                // Firebase Typing indicator
                .addModule(FirebaseTypingIndicatorModule.builder()
                        .setTypingTimeout(3000)
                        .build())

                // UI extras - drawer
                .addModule(ExtrasModule.builder()
                        .setDrawerEnabled(true)
                        .setDrawerHeaderImage(sdk.chat.ui.extras.R.drawable.header2)
                        .build())

                // Firebase UI
                .addModule(FirebaseUIModule.builder()
                        .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
                        .build()
                )

                // Activate
                .build()
                .activate(this);

    }

}
