package co.chatsdk.ui.chat.options;

import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.ChatOptionType;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.ui.chat.LocationSelector;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by ben on 10/11/17.
 */

public class LocationChatOption extends BaseChatOption {

    public LocationChatOption(String title, Integer iconResourceId) {
        super(title, iconResourceId, null, ChatOptionType.SendMessage);


        action = (activity, result, thread) -> Observable.create((ObservableOnSubscribe<MessageSendProgress>) e -> {
            try {
                final LocationSelector locationSelector = new LocationSelector();

                dispose();

                activityResultDisposable = result.subscribe(result1 -> locationSelector.handleResult(activity, result1.requestCode, result1.resultCode, result1.data));

                LocationSelector.Result locationResult = (snapshotPath, latLng) -> {

                    dispose();

                    ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
                    connector.connect(NM.locationMessage().sendMessageWithLocation(snapshotPath, latLng, thread),  e);
                };

                locationSelector.startChooseLocationActivity(activity, locationResult);

            } catch (Exception ex) {
                ToastHelper.show(activity, ex.getLocalizedMessage());
            }
        });
    }

    public LocationChatOption(String title) {
        this(title, null);
    }
}