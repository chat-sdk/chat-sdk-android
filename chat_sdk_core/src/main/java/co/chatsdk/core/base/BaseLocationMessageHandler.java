package co.chatsdk.core.base;

import com.google.android.gms.maps.model.LatLng;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.handlers.LocationMessageHandler;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.utils.GoogleUtils;
import co.chatsdk.core.utils.ImageUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import co.chatsdk.core.dao.Thread;

/**
 * Created by ben on 10/24/17.
 */

public class BaseLocationMessageHandler implements LocationMessageHandler {

    public Observable<MessageSendProgress> sendMessageWithLocation(final String filePath, final LatLng location, final Thread thread) {
        return Observable.create(new ObservableOnSubscribe<MessageSendProgress>() {
            @Override
            public void subscribe(ObservableEmitter<MessageSendProgress> e) throws Exception {
                final Message message = AbstractThreadHandler.newMessage(MessageType.Location, thread);

                int maxSize = ChatSDK.config().imageMaxThumbnailDimension;
                String imageURL = GoogleUtils.getMapImageURL(location, maxSize, maxSize);

                // Add the LatLng data to the message and the image url and thumbnail url
                // TODO: Deprecated
                message.setTextString(String.valueOf(location.latitude)
                        + Defines.DIVIDER
                        + String.valueOf(location.longitude)
                        + Defines.DIVIDER + imageURL
                        + Defines.DIVIDER + imageURL
                        + Defines.DIVIDER + ImageUtils.getDimensionAsString(maxSize, maxSize));

                message.setValueForKey(location.longitude, Keys.MessageLongitude);
                message.setValueForKey(location.latitude, Keys.MessageLatitude);
                message.setValueForKey(maxSize, Keys.MessageImageWidth);
                message.setValueForKey(maxSize, Keys.MessageImageHeight);
                message.setValueForKey(imageURL, Keys.MessageImageURL);
                message.setValueForKey(imageURL, Keys.MessageThumbnailURL);

                e.onNext(new MessageSendProgress(message));

                ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
                connector.connect(NM.thread().sendMessage(message), e);

            }
        }).subscribeOn(Schedulers.single());
    }

}
