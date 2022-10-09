 package sdk.chat.message.video;

 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.media.ThumbnailUtils;
 import android.provider.MediaStore;

 import java.io.File;
 import java.util.ArrayList;

 import io.reactivex.Completable;
 import sdk.chat.core.base.AbstractMessageHandler;
 import sdk.chat.core.dao.Keys;
 import sdk.chat.core.dao.Message;
 import sdk.chat.core.dao.Thread;
 import sdk.chat.core.handlers.VideoMessageHandler;
 import sdk.chat.core.image.ImageUtils;
 import sdk.chat.core.manager.MessagePayload;
 import sdk.chat.core.rigs.FileUploadable;
 import sdk.chat.core.rigs.JPEGUploadable;
 import sdk.chat.core.rigs.MessageSendRig;
 import sdk.chat.core.rigs.Uploadable;
 import sdk.chat.core.session.ChatSDK;
 import sdk.chat.core.types.MessageType;
 import sdk.chat.core.utils.Base64ImageUtils;
 import sdk.guru.common.RX;

 /**
 * Created by ben on 10/6/17.
 */

public class BaseVideoMessageHandler extends AbstractMessageHandler implements VideoMessageHandler {

    public static String videoName = "video.mp4";
    public static String videoMimeType = "video/mp4";

    public static String imageName = "image.jpg";
    public static String imageMimeType = "image/jpeg";

    public Completable sendMessageWithVideo(final File videoFile, final Thread thread) {
        return Completable.defer(() -> {
            // Check the file size.. and if it's bigger than max, report error

            if (videoFile.length() > VideoMessageModule.shared().config.maxFileSizeInBytes()) {
                return Completable.error(new Throwable(String.format(ChatSDK.getString(R.string.file_too_large_max_size__), VideoMessageModule.shared().config.maxFileSizeInMB)));
            } else {

                // Generate thumbnail
//                final Bitmap placeholder = ThumbnailUtils.createVideoThumbnail(videoFile.getPath(),
//                        MediaStore.Images.Thumbnails.MINI_KIND);

                final Bitmap placeholder = ThumbnailUtils.createVideoThumbnail(videoFile.getPath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

                final File placeholderFile = ImageUtils.saveBitmapToFile(placeholder);

                // Make uploadables
                Uploadable videoUploadable = new FileUploadable(videoFile, videoName, videoMimeType, Keys.MessageVideoURL);
                Uploadable placeholderUploadable = new JPEGUploadable(placeholderFile, imageName, Keys.MessageImageURL).setReportProgress(false);

                ArrayList<Uploadable> uploadables = new ArrayList<>();
                uploadables.add(videoUploadable);
                uploadables.add(placeholderUploadable);

                return new MessageSendRig(new MessageType(MessageType.Video), thread, message -> {

                    message.setValueForKey(placeholder.getWidth(), Keys.MessageImageWidth);
                    message.setValueForKey(placeholder.getHeight(), Keys.MessageImageHeight);
                    message.setText(ChatSDK.getString(R.string.video_message));
                    message.setValueForKey(videoFile.length(), Keys.MessageSize);

                    message.setFilePath(videoFile.getPath());
                    message.setPlaceholderPath(placeholderFile.getPath());

                    // Add a base64 preview
                    if (ChatSDK.config().sendBase64ImagePreview) {
                        String base64 = Base64ImageUtils.toBase64(placeholder, ChatSDK.config().imagePreviewMaxSize, ChatSDK.config().imagePreviewQuality);
                        message.setValueForKey(base64, Keys.MessageImagePreview);
                    }

                }).setUploadables(uploadables, null).run();
            }
        }).subscribeOn(RX.computation());
    }

     @Override
     public void startPlayVideoActivity(Activity activity, String videoURL) {
         if(videoURL != null) {
             Intent intent = new Intent(activity, VideoMessageModule.config().getVideoPlayerActivity());
             intent.putExtra(Keys.IntentKeyFilePath, videoURL);
             activity.startActivity(intent);
         }
     }

     @Override
    public MessagePayload payloadFor(Message message) {
        return new VideoMessagePayload(message);
    }

    @Override
    public boolean isFor(MessageType type) {
        return type != null && type.is(MessageType.Video);
    }

 }
