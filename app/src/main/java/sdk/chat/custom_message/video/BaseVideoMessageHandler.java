package sdk.chat.custom_message.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

import co.chatsdk.android.app.R;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.handlers.VideoMessageHandler;
import co.chatsdk.core.rigs.BitmapUploadable;
import co.chatsdk.core.rigs.FileUploadable;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.rigs.Uploadable;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageType;
import io.reactivex.Completable;

/**
 * Created by ben on 10/6/17.
 */

public class BaseVideoMessageHandler implements VideoMessageHandler {

    public static String videoName = "video.mp4";
    public static String videoMimeType = "video/mp4";

    public static String imageName = "image.jpg";
    public static String imageMimeType = "image/jpeg";

    public Completable sendMessageWithVideo(final File videoFile, final Thread thread) {

        Bitmap preview = ThumbnailUtils.createVideoThumbnail(videoFile.getPath(),
                MediaStore.Images.Thumbnails.MINI_KIND);

        if (preview == null) {
            preview = BitmapFactory.decodeResource(ChatSDK.shared().context().getResources(), R.drawable.ic_300_preview);
        }
        final Bitmap thumb = preview;

        ArrayList<Uploadable> uploadables = new ArrayList<>();
        uploadables.add(new FileUploadable(videoFile, videoName, videoMimeType));
        uploadables.add(new BitmapUploadable(preview, imageName, imageMimeType));

        return new MessageSendRig(new MessageType(MessageType.Video), thread, message -> {
            message.setValueForKey(thumb.getWidth(), Keys.MessageImageWidth);
            message.setValueForKey(thumb.getHeight(), Keys.MessageImageHeight);
            message.setText(ChatSDK.shared().context().getString(R.string.video_message));
        }).setUploadables(uploadables, (message, result) -> {
            if(result.mimeType.equals(imageMimeType)) {
                message.setValueForKey(result.url, Keys.MessageImageURL);
            }
            if(result.mimeType.equals(videoMimeType)) {
                message.setValueForKey(result.url, Keys.MessageVideoURL);
            }
        }).run();
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageVideoURL);
    }
}
