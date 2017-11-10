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
        action = new Action() {
            @Override
            public Observable<?> execute(final Activity activity, final PublishSubject<ActivityResult> result, final Thread thread) {
                return Observable.create(new ObservableOnSubscribe<MessageSendProgress>() {
                    @Override
                    public void subscribe(final ObservableEmitter<MessageSendProgress> e) throws Exception {
                        try {
                            final MediaSelector mediaSelector = new MediaSelector();

                            Timber.v("Selector Activity: " + activity.toString());

                            dispose();

                            activityResultDisposable = result.subscribe(new Consumer<ActivityResult>() {
                                @Override
                                public void accept(@NonNull ActivityResult result) throws Exception {
                                    mediaSelector.handleResult(activity, result.requestCode, result.resultCode, result.data);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    if(!StringChecker.isNullOrEmpty(throwable.getLocalizedMessage())) {
                                        Toast.makeText(activity, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            MediaSelector.Result handleResult = new MediaSelector.Result() {
                                public void result(String result) {

                                    dispose();

                                    ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
                                    if(type == Type.TakePhoto || type == Type.ChoosePhoto) {
                                        connector.connect(NM.imageMessage().sendMessageWithImage(result, thread), e);
                                    }
                                    else if((type == Type.TakeVideo || type == Type.ChooseVideo) && NM.videoMessage() != null) {
                                        connector.connect(NM.videoMessage().sendMessageWithVideo(result, thread), e);
                                    }
                                    else {
                                        e.onComplete();
                                    }
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
                    }
                });
            }
        };
    }

    public MediaChatOption(String title, Type type) {
        this(title, null, type);
    }
}
