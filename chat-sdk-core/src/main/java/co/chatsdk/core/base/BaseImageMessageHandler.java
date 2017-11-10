package co.chatsdk.core.base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.handlers.ImageMessageHandler;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.core.utils.StringChecker;
import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import co.chatsdk.core.dao.Thread;

/**
 * Created by ben on 10/24/17.
 */

public class BaseImageMessageHandler implements ImageMessageHandler {
        @Override
        public Observable<MessageSendProgress> sendMessageWithImage(final String filePath, final Thread thread) {
            return Observable.create(new ObservableOnSubscribe<MessageSendProgress>() {
                @Override
                public void subscribe(final ObservableEmitter<MessageSendProgress> e) throws Exception {

                    final Message message = AbstractThreadHandler.newMessage(MessageType.Image, thread);

                    // First pass back an empty result so that we add the cell to the table view
                    message.setMessageStatus(MessageSendStatus.Uploading);
                    message.update();
                    e.onNext(new MessageSendProgress(message));

                    File compress = new Compressor(ChatSDK.shared().context())
                            .setMaxHeight(ChatSDK.config().imageMaxHeight)
                            .setMaxWidth(ChatSDK.config().imageMaxWidth)
                            .compressToFile(new File(filePath));

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    final Bitmap image = BitmapFactory.decodeFile(compress.getPath(), options);

                    if(image == null) {
                        // TODO: Localize
                        e.onError(new Throwable("Unable to save image to disk"));
                        return;
                    }

                    NM.upload().uploadImage(image).subscribe(new Observer<FileUploadResult>() {
                        @Override
                        public void onSubscribe(Disposable d) {}

                        @Override
                        public void onNext(FileUploadResult result) {
                            if(!StringChecker.isNullOrEmpty(result.url))  {

                                message.setTextString(result.url + Defines.DIVIDER + result.url + Defines.DIVIDER + ImageUtils.getDimensionAsString(image));

                                message.setValueForKey(image.getWidth(), Keys.MessageImageWidth);
                                message.setValueForKey(image.getHeight(), Keys.MessageImageHeight);
                                message.setValueForKey(result.url, Keys.MessageImageURL);
                                message.setValueForKey(result.url, Keys.MessageThumbnailURL);

                                message.update();

                                Timber.v("ProgressListener: " + result.progress.asFraction());

                            }

                            e.onNext(new MessageSendProgress(message, result.progress));

                        }

                        @Override
                        public void onError(Throwable ex) {
                            e.onError(ex);
                        }

                        @Override
                        public void onComplete() {

                            message.setMessageStatus(MessageSendStatus.Sending);
                            message.update();

                            e.onNext(new MessageSendProgress(message));

                            ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
                            connector.connect(NM.thread().sendMessage(message), e);

                        }
                    });
                }
            }).subscribeOn(Schedulers.single());

        }
}
