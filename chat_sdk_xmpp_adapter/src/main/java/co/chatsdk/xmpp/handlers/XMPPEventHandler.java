package co.chatsdk.xmpp.handlers;

import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.EventHandler;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPEventHandler implements EventHandler {

    final private PublishSubject<NetworkEvent> eventSource = PublishSubject.create();

    @Override
    public PublishSubject<NetworkEvent> source() {
        return eventSource;
    }

    @Override
    public Observable<NetworkEvent> sourceOnMain() {
        return eventSource.observeOn(AndroidSchedulers.mainThread());
    }



}
