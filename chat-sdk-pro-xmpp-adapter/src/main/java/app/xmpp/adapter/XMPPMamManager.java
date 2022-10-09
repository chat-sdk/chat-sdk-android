package app.xmpp.adapter;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.mam.element.MamElements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.xmpp.adapter.enums.ConnectionStatus;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public class XMPPMamManager implements AppBackgroundMonitor.StopListener {

    DisposableMap dm = new DisposableMap();

    public boolean loaded = false;

    Single<List<Message>> loadingPromise;

    private WeakReference<XMPPManager> manager;

    public XMPPMamManager(XMPPManager manager){
        this.manager = new WeakReference<>(manager);

        ChatSDK.appBackgroundMonitor().addListener(this);

        dm.add(manager.connectionManager().connectionStatus().subscribe(connectionStatus -> {
            if (connectionStatus.equals(ConnectionStatus.Disconnected)) {
                goOffline();
            }
        }));
    }

    public Single<List<Message>> getMessageArchive(String from, Date since, int max) {
        if (loadingPromise == null) {
            loadingPromise = Single.create((SingleOnSubscribe<List<Message>>) emitter -> {
                try {
                    List<Message> messages = new ArrayList<>();
                    if(manager.get().mamManager().isSupported()) {

                        Date serverDate = manager.get().clientToServerTime(since);

                        MamManager.MamQueryArgs args = MamManager.MamQueryArgs.builder()
                                .limitResultsSince(serverDate)
                                .setResultPageSizeTo(max)
                                .queryLastPage()
                                .build();

                        MamManager.MamQuery query = manager.get().mamManager().queryArchive(args);
                        messages.addAll(query.getMessages());
                        List<MamElements.MamResultExtension> extensions = query.getMamResultExtensions();
                        for (int i = 0; i <messages.size(); i++) {
                            Message m = messages.get(i);
                            MamElements.MamResultExtension extension = extensions.get(i);
                            DelayInformation delay = extension.getForwarded().getDelayInformation();
                            m.addExtension(delay);
                        }
                    }
                    emitter.onSuccess(messages);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }).doFinally(() -> {
                loaded = true;
                loadingPromise = null;
            }).subscribeOn(RX.io());
        }
        return loadingPromise;
    }

    @Override
    public void didStop() {
        goOffline();
    }

    public void goOffline() {
        loaded = false;
        loadingPromise = null;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isLoading() {
        return loadingPromise != null;
    }
}
