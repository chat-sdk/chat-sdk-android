package sdk.chat.demo.examples;

import android.app.Activity;

import java.util.concurrent.TimeUnit;

import app.xmpp.adapter.module.XMPPModule;
import app.xmpp.receipts.XMPPReadReceiptsModule;
import sdk.chat.contact.ContactBookModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.R;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.file.FileMessageModule;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.profile.pictures.ProfilePicturesModule;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;

public class AddXMPPModules extends Activity {

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
                        XMPPModule.builder()
                                .setXMPP("host", "domain", 5222, null)
                                .setMucMessageHistoryDownloadLimit(20)
                                .setCompressionEnabled(true)
                                .setSecurityMode("disabled")
                                .setDebugEnabled(false)
                                .setAllowServerConfiguration(true)
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

                // UI extras - drawer
                .addModule(ExtrasModule.builder()
                        .setDrawerEnabled(true)
                        .setDrawerHeaderImage(sdk.chat.ui.extras.R.drawable.header)
                        .build())

                // XMPP Read receipts
                .addModules(XMPPReadReceiptsModule.shared())

                // Activate
                .build()
                .activate(this);

    }

}
