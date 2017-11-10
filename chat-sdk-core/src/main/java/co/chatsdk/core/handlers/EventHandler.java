package co.chatsdk.core.handlers;

import co.chatsdk.core.events.NetworkEvent;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public interface EventHandler {

    PublishSubject<NetworkEvent> source ();
    Observable<NetworkEvent> sourceOnMain ();

}
