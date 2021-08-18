package sdk.chat.core.rigs;

import android.content.Context;

import io.reactivex.Completable;
import sdk.guru.common.DisposableMap;

public class MessageSender {

    protected Context context;
    protected DisposableMap dm = new DisposableMap();

    public MessageSender(Context context) {
        this.context = context;
    }

    /**
     * We can use this to fix an issue where the message sending stops if we leave the chat activity
     * @param rig
     * @return
     */
    public Completable run(MessageSendRig rig) {
        return Completable.create(emitter -> {
            dm.add(rig.doRun().subscribe(() -> {
                emitter.onComplete();
            }, throwable -> {
                emitter.onError(throwable);
            }));
        });
    }

}
