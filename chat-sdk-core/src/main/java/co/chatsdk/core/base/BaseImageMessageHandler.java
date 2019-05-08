package co.chatsdk.core.base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import co.chatsdk.core.R;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.ImageMessageHandler;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.utils.StringChecker;
import id.zelory.compressor.Compressor;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ben on 10/24/17.
 */

public class BaseImageMessageHandler implements ImageMessageHandler {

    @Override
    public Completable sendMessageWithImage(final File imageFile, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Image), thread, message -> {
            // Get the image and set the image message dimensions
            final Bitmap image = BitmapFactory.decodeFile(imageFile.getPath(), null);

            message.setValueForKey(image.getWidth(), Keys.MessageImageWidth);
            message.setValueForKey(image.getHeight(), Keys.MessageImageHeight);

        }).setFile(imageFile, "image.jpg", "image/jpeg", (message, result) -> {
            // When the file has uploaded, set the image URL
            message.setValueForKey(result.url, Keys.MessageImageURL);

        }).setFileCompressAction(file -> {
            return new Compressor(ChatSDK.shared().context())
                    .setMaxHeight(ChatSDK.config().imageMaxHeight)
                    .setMaxWidth(ChatSDK.config().imageMaxWidth)
                    .compressToFile(file);
        }).run();
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageImageURL);
    }
}
