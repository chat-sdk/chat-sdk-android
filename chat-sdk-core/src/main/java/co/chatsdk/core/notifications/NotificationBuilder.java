package co.chatsdk.core.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.core.app.NotificationCompat;

import java.lang.ref.WeakReference;

import co.chatsdk.core.R;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.MessageDisplayHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ImageBuilder;
import co.chatsdk.core.utils.ImageUtils;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

public class NotificationBuilder {

    public static String ChatSDKMessageChannel = "sdk.chat.notification.Message";

    protected WeakReference<Context> context;
    protected NotificationManager notificationManager;

    protected int smallIcon = -1;
    protected Bitmap largeIcon = null;
    protected Intent intent;
    protected boolean vibrationEnabled = false;
    protected String title = null;
    protected String text = null;
    protected Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    protected int number = -1;
    protected String ticker = null;
    protected int color = -1;
    protected String channelName = null;
    protected String channelDescription = null;
    protected String largeIconUrl = null;

    protected Thread messageReplyActionThread = null;

    public NotificationBuilder(Context context) {
        this.context = new WeakReference<>(context);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public NotificationBuilder setSmallIcon(int smallIcon) {
        this.smallIcon = smallIcon;
        return this;
    }

    public NotificationBuilder setLargeIcon(Bitmap largeIcon) {
        this.largeIcon = largeIcon;
        return this;
    }

    public NotificationBuilder setIntent(Intent intent) {
        this.intent = intent;
        return this;
    }

    public NotificationBuilder useDefaultIcons() {
        int pushIcon = ChatSDK.config().pushNotificationImageDefaultResourceId;
        if(pushIcon <= 0) {
            pushIcon = R.drawable.icn_72_push_mask;
        }
        return setSmallIcon(pushIcon)
                .setLargeIcon(BitmapFactory.decodeResource(context.get().getResources(), pushIcon));
    }

    public NotificationBuilder useDefaultColor() {
        return setColor(ChatSDK.config().pushNotificationColor);
    }

    public NotificationBuilder setVibrationEnabled(boolean enabled) {
        vibrationEnabled = enabled;
        return this;
    }

    public NotificationBuilder addOpenChatIntentForMessage(Message message) {

        Context context = ChatSDK.shared().context();
        String threadID = message.getThread().getEntityID();

        Intent openChatIntent = new Intent(context, ChatSDK.ui().getChatActivity());
        openChatIntent.putExtra(Keys.IntentKeyThreadEntityID, threadID);
        openChatIntent.setAction(threadID);
        openChatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent = openChatIntent;

        return this;
    }

    public NotificationBuilder addTitleAndTextForMessage(Message message) {
        MessageDisplayHandler displayHandler = ChatSDK.ui().getMessageHandler(message.getMessageType());
        return setTitle(message.getSender().getName()).setText(displayHandler.displayName(message));
    }

    public NotificationBuilder addIconForUser(User user) {
        if (user != null) {
            largeIconUrl = user.getAvatarURL();
        }
        return this;
    }

    public NotificationBuilder addIconForUserEntityID(String userEntityID) {
        if (userEntityID != null && !userEntityID.isEmpty()) {
            User user = ChatSDK.db().fetchUserWithEntityID(userEntityID);
            if (user != null) {
                return addIconForUser(user);
            }
        }
        return this;
    }

    public NotificationBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public NotificationBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public NotificationBuilder setSoundUri(Uri uri) {
        this.soundUri = uri;
        return this;
    }

    public NotificationBuilder setNumber(int number) {
        this.number = number;
        return this;
    }

    public NotificationBuilder setChannelName(String name) {
        this.channelName = name;
        return this;
    }

    public NotificationBuilder setChannelDescription(String description) {
        this.channelDescription = description;
        return this;
    }

    public NotificationBuilder useDefaultChannel() {
        return setChannelName(context.get().getString(R.string.app_name))
                .setChannelDescription(context.get().getString(R.string.push_channel_name));
    }

    public NotificationBuilder setColor(@ColorInt int color) {
        this.color = color;
        return this;
    }

    public Single<NotificationCompat.Builder> build() {
        return Single.create((SingleOnSubscribe<NotificationCompat.Builder>) emitter -> {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context.get())
                            .setPriority(Notification.PRIORITY_HIGH);

            if (title != null) {
                builder.setContentTitle(title);
            }

            if (text != null) {
                builder.setContentText(text);
            }

            if (smallIcon != -1) {
                builder.setSmallIcon(smallIcon);
            }

            if (vibrationEnabled) {
                builder.setVibrate(new long[]{0, 250, 100, 250});
            }

            if (soundUri != null) {
                builder.setSound(soundUri);
            }

            if (number != -1) {
                builder.setNumber(number);
            }

            if (intent != null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(context.get(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pendingIntent);
            }

            if (ticker != null) {
                builder.setTicker(ticker);
            } else if (title != null && text != null) {
                builder.setTicker(title + ": " + text);
            }

            if (largeIcon != null) {
                builder.setLargeIcon(scaleLargeIcon(largeIcon));
            }

            if (color != -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setColor(ChatSDK.config().pushNotificationColor);
            }

            if (channelName != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                builder.setChannelId(ChatSDKMessageChannel);

                NotificationChannel channel = new NotificationChannel(ChatSDKMessageChannel, channelName, NotificationManager.IMPORTANCE_HIGH);
                channel.enableVibration(vibrationEnabled);

                if (channelDescription != null) {
                    channel.setDescription(channelDescription);
                }

                notificationManager.createNotificationChannel(channel);
            }

            if (messageReplyActionThread != null) {
                new MessageActionBuilder().addActions(builder, context.get(), messageReplyActionThread);
            }

            emitter.onSuccess(builder);
        }).flatMap(this::loadLargeIconFromURL);
    }

    public Single<NotificationCompat.Builder> loadLargeIconFromURL(final NotificationCompat.Builder builder) {
        if (largeIconUrl != null) {
            return ImageBuilder.bitmapForURL(context.get(), largeIconUrl).map(bitmap -> {
                if (bitmap != null) {
                    builder.setLargeIcon(scaleLargeIcon(bitmap));
                }
                return builder;
            });
        }
        return Single.just(builder);
    }

    public Bitmap scaleLargeIcon(Bitmap bitmap) {
        return ImageUtils.scaleImage(bitmap, (int) (context.get().getResources().getDisplayMetrics().density * 48));
    }

    public NotificationBuilder useDefault() {
        return this.useDefaultIcons()
                .useDefaultColor()
                .setVibrationEnabled(true)
                .useDefaultChannel();
    }

    public NotificationBuilder forMessage(Message message) {
        NotificationBuilder builder = useDefault()
                .addOpenChatIntentForMessage(message)
                .addTitleAndTextForMessage(message)
                .addIconForUser(message.getSender());
        if (ChatSDK.config().replyFromNotificationEnabled) {
            builder.addMessageReplyActionsForThread(message.getThread());
        }
        return builder;
    }

    public NotificationBuilder forMessageAuto(Message message) {
        NotificationBuilder builder = useDefaultIcons()
                .setVibrationEnabled(true)
                .useDefaultChannel()
                .addTitleAndTextForMessage(message)
                .addMessageReplyActionsForThread(message.getThread());
        return builder;
    }

    public NotificationBuilder forAuto(String title, String text, Thread thread) {
        NotificationBuilder builder = useDefaultIcons()
                .setVibrationEnabled(true)
                .useDefaultChannel()
                .setTitle(title)
                .setText(text)
                .addMessageReplyActionsForThread(thread);
        return builder;
    }

    public NotificationBuilder addMessageReplyActionsForThread(Thread thread) {
        messageReplyActionThread = thread;
        return this;
    }

}
