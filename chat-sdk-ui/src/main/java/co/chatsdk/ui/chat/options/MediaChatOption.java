package co.chatsdk.ui.chat.options;

import android.widget.Toast;

import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.BiConsumer;
import timber.log.Timber;

/**
 * Created by ben on 10/11/17.
 */

public class MediaChatOption extends BaseChatOption {

    public MediaChatOption(String title, Integer iconResourceId, final MediaType type) {
        super(title, iconResourceId, null);
        action = (activity, thread) -> Observable.create((ObservableOnSubscribe<MessageSendProgress>) e -> {
            try {
                final MediaSelector mediaSelector = new MediaSelector();

                Timber.v("Selector Activity: " + activity.toString());

                dispose();

                disposableList.add(mediaSelector.startActivity(activity, type).subscribe((path, throwable) -> {
                    if (throwable == null) {
                        ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
                        if(type.is(MediaType.Photo)) {
                            connector.connect(ChatSDK.imageMessage().sendMessageWithImage(path, thread), e);
                        }
                        else if(type.is(MediaType.Video) && ChatSDK.videoMessage() != null) {
                            connector.connect(ChatSDK.videoMessage().sendMessageWithVideo(path, thread), e);
                        }
                        else {
                            e.onComplete();
                        }
                    } else {
                        Toast.makeText(activity, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        e.onError(throwable);
                    }
                    dispose();
                }));

            } catch (Exception ex) {
                ToastHelper.show(activity, ex.getLocalizedMessage());
            }
        });
    }

    public MediaChatOption(String title, MediaType type) {
        this(title, null, type);
    }
}
