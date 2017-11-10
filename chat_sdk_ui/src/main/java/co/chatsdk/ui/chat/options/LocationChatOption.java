package co.chatsdk.ui.chat.options;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.ChatOptionType;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.utils.ActivityResult;
import co.chatsdk.ui.chat.LocationSelector;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ben on 10/11/17.
 */

public class LocationChatOption extends BaseChatOption {

    public LocationChatOption(String title, Integer iconResourceId) {
        super(title, iconResourceId, null, ChatOptionType.SendMessage);
        action = new Action() {
            @Override
            public Observable<MessageSendProgress> execute(final Activity activity, final PublishSubject<ActivityResult> result, final Thread thread) {
                return Observable.create(new ObservableOnSubscribe<MessageSendProgress>() {
                    @Override
                    public void subscribe(final ObservableEmitter<MessageSendProgress> e) throws Exception {
                        try {
                            final LocationSelector locationSelector = new LocationSelector();

                            dispose();

                            activityResultDisposable = result.subscribe(new Consumer<ActivityResult>() {
                                @Override
                                public void accept(@NonNull ActivityResult result) throws Exception {
                                    locationSelector.handleResult(activity, result.requestCode, result.resultCode, result.data);
                                }
                            });

                            LocationSelector.Result result = new LocationSelector.Result() {
                                @Override
                                public void result(String snapshotPath, LatLng latLng) {

                                    dispose();

                                    ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
                                    connector.connect(NM.locationMessage().sendMessageWithLocation(snapshotPath, latLng, thread), e);
                                }
                            };

                            locationSelector.startChooseLocationActivity(activity, result);

                        } catch (Exception ex) {
                            ToastHelper.show(activity, ex.getLocalizedMessage());
                        }
                    }
                });
            }
        };
    }

    public LocationChatOption(String title) {
        this(title, null);
    }
}