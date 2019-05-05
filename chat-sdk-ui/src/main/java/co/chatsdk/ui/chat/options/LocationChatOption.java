package co.chatsdk.ui.chat.options;

import android.app.Activity;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.ui.chat.LocationSelector;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;

/**
 * Created by ben on 10/11/17.
 */

public class LocationChatOption extends BaseChatOption {

    public LocationChatOption(String title, Integer iconResourceId) {
        super(title, iconResourceId, (activity, thread) -> {
            return new LocationSelector().startChooseLocationActivity(activity).flatMapCompletable(result -> ChatSDK.locationMessage().sendMessageWithLocation(result.snapshotPath, result.latLng, thread));
        });
    }

    public LocationChatOption(String title) {
        this(title, null);
    }
}