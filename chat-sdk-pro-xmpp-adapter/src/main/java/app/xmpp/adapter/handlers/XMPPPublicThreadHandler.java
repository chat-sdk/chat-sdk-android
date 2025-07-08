package app.xmpp.adapter.handlers;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

import app.xmpp.adapter.XMPPManager;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import sdk.chat.core.base.AbstractPublicThreadHandler;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.RX;

public class XMPPPublicThreadHandler extends AbstractPublicThreadHandler {
    @Override
    public Single<ThreadX> createPublicThreadWithName(String name, String entityID, Map<String, Object> meta, String imageURL) {
        return Single.defer((Callable<SingleSource<ThreadX>>) () -> {

            if (entityID != null) {
                ThreadX t = ChatSDK.db().fetchThreadWithEntityID(entityID);
                if (t != null) {
                    return Single.just(t);
                }
            }
            return XMPPManager.shared().mucManager.createRoom(name, "", new ArrayList<>(), true);

        }).subscribeOn(RX.io());
    }
}
