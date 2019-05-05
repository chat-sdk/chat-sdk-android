package co.chatsdk.core.base;

import com.google.android.gms.maps.model.LatLng;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.LocationMessageHandler;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.utils.GoogleUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ben on 10/24/17.
 */

public class BaseLocationMessageHandler implements LocationMessageHandler {

    public Completable sendMessageWithLocation(final String filePath, final LatLng location, final Thread thread) {
        return Single.create((SingleOnSubscribe<Message>) emitter -> {

            final Message message = AbstractThreadHandler.newMessage(MessageType.Location, thread);

            int maxSize = ChatSDK.config().imageMaxThumbnailDimension;
            String imageURL = GoogleUtils.getMapImageURL(location, maxSize, maxSize);

            message.setValueForKey(location.longitude, Keys.MessageLongitude);
            message.setValueForKey(location.latitude, Keys.MessageLatitude);
            message.setValueForKey(maxSize, Keys.MessageImageWidth);
            message.setValueForKey(maxSize, Keys.MessageImageHeight);
            message.setValueForKey(imageURL, Keys.MessageImageURL);
            message.setValueForKey(imageURL, Keys.MessageThumbnailURL);

            ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

            emitter.onSuccess(message);
        }).flatMapCompletable(ChatSDK.thread()::sendMessage).subscribeOn(Schedulers.single());
    }

    @Override
    public String textRepresentation(Message message) {
        return String.format(ChatSDK.config().locationURLRepresentation, message.doubleForKey(Keys.MessageLatitude), message.doubleForKey(Keys.MessageLongitude));
    }
}
