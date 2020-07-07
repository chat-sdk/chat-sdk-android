package sdk.chat.demo.examples;

import android.app.Activity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import sdk.chat.contact.ContactBookModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.R;
import sdk.chat.firebase.location.FirebaseNearbyUsersModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.ui.FirebaseUIModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.firestream.adapter.FireStreamModule;
import sdk.chat.firestream.adapter.FirebaseServiceType;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.file.FileMessageModule;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.profile.pictures.ProfilePicturesModule;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;

public class AddFireStreamModules extends Activity {

    public void add() throws Exception {

        ChatSDK.builder()
                .setDebugUsername(null)
                .setDebugPassword(null)
                .setGoogleMaps(null)
                .setReplyFromNotificationEnabled(true)
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
                .setPublicChatRoomLifetimeMinutes(TimeUnit.DAYS.toMinutes(7))
                .setDisablePresence(false)
                .setSendSystemMessageWhenRoleChanges(true)
                .setRolesEnabled(true)
                .build()

                // Firebase Module
                .addModule(
                        FireStreamModule.builder(FirebaseServiceType.Firestore)
                                .setRoot("pre_1")
                                .setSandbox("firestream")
                                .setDeliveryReceiptsEnabled(true)
                                .setDeleteMessagesOnReceiptEnabled(false)
                                .setAutoAcceptChatInviteEnabled(true)
                                .setAutoMarkReceivedEnabled(true)
                                .setDebugEnabled(false)
                                .build()
                )

                // UI Module
                .addModule(UIModule.builder()
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
                        .setDefaultProfilePlaceholder(R.drawable.icn_100_profile)
                        .setProfileHeaderImage(R.drawable.header)
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
//                .addModule(EncryptionModule.shared())

                // File messages module
                .addModule(FileMessageModule.shared())

                // Audio messages module
                .addModule(AudioMessageModule.builder()
                        .build())

                // Sticker messages module
                .addModule(StickerMessageModule.shared())

                // Video messages module
                .addModule(VideoMessageModule.shared())

                // Nearby users module
                .addModule(FirebaseNearbyUsersModule.builder()
                        .setMaxDistance(50000)
                        .setMinimumDisplayResolution(100)
                        .setMinRefreshDistance(50)
                        .setMinRefreshTime(1)
                        .build())

                // UI extras - drawer
                .addModule(ExtrasModule.builder()
                        .setDrawerEnabled(true)
                        .setDrawerHeaderImage(sdk.chat.ui.extras.R.drawable.header)
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