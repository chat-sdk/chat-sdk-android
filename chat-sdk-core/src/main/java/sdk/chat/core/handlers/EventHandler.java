package sdk.chat.core.handlers;

import com.jakewharton.rxrelay2.PublishRelay;

import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.chat.core.events.NetworkEvent;


/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public interface EventHandler extends Consumer<Throwable>, CompletableObserver {

    PublishRelay<NetworkEvent> source();
    Observable<NetworkEvent> sourceOnMain();
    Observable<NetworkEvent> sourceOnBackground();
    Observable<Throwable> errorSourceOnMain();

    void impl_currentUserOn (String userEntityID);
    void impl_currentUserOff (String userEntityID);

    void disposeOnLogout(Disposable d);
    void stop();
}
