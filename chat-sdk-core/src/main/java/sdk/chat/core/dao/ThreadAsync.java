package sdk.chat.core.dao;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import sdk.guru.common.RX;

public class ThreadAsync {
    public static Single<List<Message>> getMessagesWithOrderAsync(Thread thread, int order, int limit) {
        return Single.defer(() -> Single.just(thread.getMessagesWithOrder(order, limit))).subscribeOn(RX.db());
    }
    public static Completable markRead(Thread thread) {
        return Completable.create(emitter -> {
            thread.markRead();
            emitter.onComplete();
        }).subscribeOn(RX.db());
    }

}
