package co.chatsdk.ui.chat.options;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Toast;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.ChatOptionType;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.utils.ActivityResult;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
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
        action = (activity, result, thread) -> Observable.create((ObservableOnSubscribe<MessageSendProgress>) e -> {
            try {
                final MediaSelector mediaSelector = new MediaSelector();

                Timber.v("Selector Activity: " + activity.toString());

                dispose();

                activityResultDisposable = result.subscribe(result12 -> mediaSelector.handleResult(activity, result12.requestCode, result12.resultCode, result12.data), throwable -> {
                    if(!StringChecker.isNullOrEmpty(throwable.getLocalizedMessage())) {
                        Toast.makeText(activity, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                MediaSelector.Result handleResult = result1 -> {

                    dispose();

                    ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
                    if(type == Type.TakePhoto || type == Type.ChoosePhoto) {
                        connector.connect(NM.imageMessage().sendMessageWithImage(result1, thread), e);
                    }
                    else if((type == Type.TakeVideo || type == Type.ChooseVideo) && NM.videoMessage() != null) {
                        connector.connect(NM.videoMessage().sendMessageWithVideo(result1, thread), e);
                    }
                    else {
                        e.onComplete();
                    }
                };

                if(type == Type.TakePhoto) {
                    mediaSelector.startTakePhotoActivity(activity, handleResult);
                }
                if(type == Type.ChoosePhoto) {
                    mediaSelector.startChooseImageActivity(activity, handleResult);
                }
                if(type == Type.TakeVideo) {
                    mediaSelector.startTakeVideoActivity(activity, handleResult);
                }
                if(type == Type.ChooseVideo) {
                    mediaSelector.startChooseVideoActivity(activity, handleResult);
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
