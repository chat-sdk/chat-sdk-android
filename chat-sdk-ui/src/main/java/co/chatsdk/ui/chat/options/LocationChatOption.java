package co.chatsdk.ui.chat.options;

import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.ui.chat.LocationSelector;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.BiConsumer;

/**
 * Created by ben on 10/11/17.
 */

public class LocationChatOption extends BaseChatOption {

    public LocationChatOption(String title, Integer iconResourceId) {
        super(title, iconResourceId, null);


        action = (activity, thread) -> Observable.create((ObservableOnSubscribe<MessageSendProgress>) e -> {
            try {
                final LocationSelector locationSelector = new LocationSelector();

                dispose();

                disposableList.add(locationSelector.startChooseLocationActivity(activity).subscribe(new BiConsumer<LocationSelector.Result, Throwable>() {
                    @Override
                    public void accept(LocationSelector.Result result, Throwable throwable) throws Exception {
                        ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
                        connector.connect(ChatSDK.locationMessage().sendMessageWithLocation(result.snapshotPath, result.latLng, thread),  e);
                        dispose();
                    }
                }));

            } catch (Exception ex) {
                ToastHelper.show(activity, ex.getLocalizedMessage());
            }
        });
    }

    public LocationChatOption(String title) {
        this(title, null);
    }
}