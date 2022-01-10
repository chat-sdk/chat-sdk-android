package sdk.chat.message.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.VideoMessageHandler;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.rigs.FileUploadable;
import sdk.chat.core.rigs.JPEGUploadable;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.rigs.Uploadable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;

/**
 * Created by ben on 10/6/17.
 */

public class BaseVideoMessageHandler implements VideoMessageHandler {

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
                Bitmap preview = ThumbnailUtils.createVideoThumbnail(videoFile.getPath(),
                        MediaStore.Images.Thumbnails.MINI_KIND);

                if (preview == null) {
                    preview = BitmapFactory.decodeResource(ChatSDK.shared().context().getResources(), R.drawable.icn_200_image_message_placeholder);
                }
                final Bitmap thumb = preview;

                final File thumbFile = ImageUtils.saveBitmapToFile(thumb);

                ArrayList<Uploadable> uploadables = new ArrayList<>();
                uploadables.add(new FileUploadable(videoFile, videoName, videoMimeType));
                uploadables.add(new JPEGUploadable(thumbFile, imageName).setReportProgress(false));

                return new MessageSendRig(new MessageType(MessageType.Video), thread, message -> {
                    message.setValueForKey(thumb.getWidth(), Keys.MessageImageWidth);
                    message.setValueForKey(thumb.getHeight(), Keys.MessageImageHeight);
                    message.setText(ChatSDK.getString(R.string.video_message));
                }).setUploadables(uploadables, (message, result) -> {
                    if(result.mimeType.equals(imageMimeType)) {
                        message.setValueForKey(result.url, Keys.MessageImageURL);
                    }
                    if(result.mimeType.equals(videoMimeType)) {
                        message.setValueForKey(result.url, Keys.MessageVideoURL);
                    }
                }).run();
            }
        });
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageVideoURL);
    }

    @Override
    public String toString(Message message) {
        return ChatSDK.getString(R.string.video_message);
    }

    @Override
    public String getImageURL(Message message) {
        if (message.getMessageType().is(MessageType.Video) || message.getReplyType().is(MessageType.Video)) {
            return message.getImageURL();
        }
        return null;
    }
}
