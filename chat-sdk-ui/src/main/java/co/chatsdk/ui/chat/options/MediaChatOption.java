package co.chatsdk.ui.chat.options;

import android.widget.Toast;

import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ChatOptionType;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * Created by ben on 10/11/17.
 */

public class MediaChatOption extends BaseChatOption {

    public enum Type {
        TakePhoto,
        ChoosePhoto,
        TakeVideo,
        ChooseVideo,
    }

    public MediaChatOption(String title, Integer iconResourceId, final Type type) {
        super(title, iconResourceId, null, ChatOptionType.SendMessage);
        action = (activity, thread) -> Observable.create((ObservableOnSubscribe<MessageSendProgress>) e -> {
            try {
                final MediaSelector mediaSelector = new MediaSelector();

                Timber.v("Selector Activity: " + activity.toString());

                dispose();

                activityResultDisposable = ActivityResultPushSubjectHolder.shared().subscribe(result12 -> {
                    mediaSelector.handleResult(activity, result12.requestCode, result12.resultCode, result12.data);
                }, throwable -> {
                    if(!StringChecker.isNullOrEmpty(throwable.getLocalizedMessage())) {
                        Toast.makeText(activity, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                MediaSelector.Result handleResult = result1 -> {

                    dispose();

                    ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
                    if(type == Type.TakePhoto || type == Type.ChoosePhoto) {
                        connector.connect(ChatSDK.imageMessage().sendMessageWithImage(result1, thread), e);
                    }
                    else if((type == Type.TakeVideo || type == Type.ChooseVideo) && ChatSDK.videoMessage() != null) {
                        connector.connect(ChatSDK.videoMessage().sendMessageWithVideo(result1, thread), e);
                    }
                    else {
                        e.onComplete();
                    }
                };

                Disposable d = null;

                Consumer<? super Throwable> consumer = (Consumer<Throwable>) throwable -> {
                    ToastHelper.show(activity, throwable.getLocalizedMessage());
                };

                if(type == Type.TakePhoto) {
                    d = PermissionRequestHandler.shared().requestCameraAccess(activity).concatWith(PermissionRequestHandler.shared().requestWriteExternalStorage(activity)).subscribe(() -> {
                            mediaSelector.startTakePhotoActivity(activity, handleResult);
                        }, consumer);
                }
                if(type == Type.ChoosePhoto) {
                    d = PermissionRequestHandler.shared().requestReadExternalStorage(activity).subscribe(() -> {
                        mediaSelector.startChooseImageActivity(activity, MediaSelector.CropType.Rectangle, handleResult);
                    }, consumer);
                }
                if(type == Type.TakeVideo) {
                    d = PermissionRequestHandler.shared().requestCameraAccess(activity).concatWith(PermissionRequestHandler.shared().requestWriteExternalStorage(activity)).subscribe(() -> {
                        mediaSelector.startTakeVideoActivity(activity, handleResult);
                    }, consumer);
                }
                if(type == Type.ChooseVideo) {
                    d = PermissionRequestHandler.shared().requestReadExternalStorage(activity).subscribe(() -> {
                        mediaSelector.startChooseVideoActivity(activity, handleResult);
                    }, consumer);
                }
            } catch (Exception ex) {
                ToastHelper.show(activity, ex.getLocalizedMessage());
            }
        });
    }

    public MediaChatOption(String title, Type type) {
        this(title, null, type);
    }
}
