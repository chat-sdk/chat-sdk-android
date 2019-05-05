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

        protected Disposable imageUploadDisposable;

        @Override
        public Completable sendMessageWithImage(final String filePath, final Thread thread) {
            return Completable.create(emitter -> {

                final Message message = AbstractThreadHandler.newMessage(MessageType.Image, thread);

                // First pass back an empty result so that we add the cell to the table view
                message.setMessageStatus(MessageSendStatus.Uploading);
                message.update();

                ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

                File compress = new Compressor(ChatSDK.shared().context())
                        .setMaxHeight(ChatSDK.config().imageMaxHeight)
                        .setMaxWidth(ChatSDK.config().imageMaxWidth)
                        .compressToFile(new File(filePath));

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                final Bitmap image = BitmapFactory.decodeFile(compress.getPath(), options);

                if(image != null) {

                    imageUploadDisposable = ChatSDK.upload().uploadImage(image).flatMapMaybe((Function<FileUploadResult, MaybeSource<Message>>) result -> {

                        ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

                        if (result.urlValid()) {

                            message.setValueForKey(image.getWidth(), Keys.MessageImageWidth);
                            message.setValueForKey(image.getHeight(), Keys.MessageImageHeight);
                            message.setValueForKey(result.url, Keys.MessageImageURL);
                            message.setValueForKey(result.url, Keys.MessageThumbnailURL);

                            message.update();

                            return Maybe.just(message);
                        } else {
                            return Maybe.empty();
                        }
                    }).firstElement().toSingle().flatMapCompletable(message1 -> {
                        message1.setMessageStatus(MessageSendStatus.Sending);
                        message1.update();

                        ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message1)));

                        return ChatSDK.thread().sendMessage(message1);
                    }).subscribe(emitter::onComplete, emitter::onError);

                } else {
                    emitter.onError(new Throwable(ChatSDK.shared().context().getString(R.string.unable_to_save_image_to_disk)));
                }
            }).subscribeOn(Schedulers.single()).doOnDispose(() -> {
                if (imageUploadDisposable != null) {
                    imageUploadDisposable.dispose();
                }
            });

        }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageImageURL);
    }
}
