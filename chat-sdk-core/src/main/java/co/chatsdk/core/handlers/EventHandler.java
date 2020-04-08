package co.chatsdk.core.handlers;

import co.chatsdk.core.events.NetworkEvent;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public interface EventHandler extends Consumer<Throwable>, CompletableObserver {

    PublishSubject<NetworkEvent> source ();
    Observable<NetworkEvent> sourceOnMain ();
    Observable<Throwable> errorSourceOnMain ();

    void impl_currentUserOn (String userEntityID);
    void impl_currentUserOff (String userEntityID);

    void disposeOnLogout(Disposable d);

}
