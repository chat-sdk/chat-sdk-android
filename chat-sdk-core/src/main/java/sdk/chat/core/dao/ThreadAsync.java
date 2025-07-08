package sdk.chat.core.dao;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.guru.common.RX;

public class ThreadAsync {
    public static Single<List<Message>> getMessagesWithOrderAsync(ThreadX thread, int order, int limit) {
        return Single.defer(() -> Single.just(thread.getMessagesWithOrder(order, limit))).subscribeOn(RX.db());
    }
    public static Completable markRead(ThreadX thread) {
        return Completable.create(emitter -> {
            thread.markRead();
            emitter.onComplete();
        }).subscribeOn(RX.db());
    }

}
