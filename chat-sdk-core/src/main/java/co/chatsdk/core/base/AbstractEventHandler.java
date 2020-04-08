package co.chatsdk.core.base;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.notifications.NotificationDisplayHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.core.utils.AppBackgroundMonitor;
import sdk.guru.common.DisposableMap;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public abstract class AbstractEventHandler implements EventHandler {

    final protected PublishSubject<NetworkEvent> eventSource = PublishSubject.create();
    final protected PublishSubject<Throwable> errorSource = PublishSubject.create();

    final protected DisposableMap dm = new DisposableMap();

    public Disposable localNotificationDisposable;

    public AbstractEventHandler() {
        dm.add(source().filter(NetworkEvent.filterType(EventType.Logout)).subscribe(networkEvent -> {
            dm.dispose();
        }, this));

        if (localNotificationDisposable != null) {
            localNotificationDisposable.dispose();
        }

        localNotificationDisposable = sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .subscribe(networkEvent -> {
                    Message message = networkEvent.getMessage();
                    Thread thread = networkEvent.getThread();
                    if (message != null && !AppBackgroundMonitor.shared().inBackground() && thread.isMuted()) {
                        if (thread.typeIs(ThreadType.Private) || (thread.typeIs(ThreadType.Public) && ChatSDK.config().localPushNotificationsForPublicChatRoomsEnabled)) {
                            if (!message.getSender().isMe() && !message.isDelivered() && ChatSDK.ui().showLocalNotifications(message.getThread()) || NotificationDisplayHandler.connectedToAuto(ChatSDK.ctx())) {
                                ReadStatus status = message.readStatusForUser(ChatSDK.currentUser());
                                if (!message.isRead() && !status.is(ReadStatus.delivered()) && !status.is(ReadStatus.read())) {
                                    // Only show the alert if we'recyclerView not on the private threads tab
                                    ChatSDK.ui().notificationDisplayHandler().createMessageNotification(message);
                                }
                            }
                        }
                    }
                });

    }

    public PublishSubject<NetworkEvent> source () {
        return eventSource;
    }

    public Observable<NetworkEvent> sourceOnMain () {
        return source().hide().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Throwable> errorSourceOnMain() {
        return errorSource.hide().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void accept(Throwable t) throws Exception {
        onError(t);
    };

    public void disposeOnLogout(Disposable d) {
        dm.add(d);
    }

    public void onSubscribe(@NonNull Disposable d) {
        dm.add(d);
    }

    public void onComplete() {}

    public void onError(@NonNull Throwable e) {
        errorSource.onNext(e);
    }


}
