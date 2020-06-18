package sdk.chat.ui.fragments;

import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Predicate;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;
import sdk.chat.ui.module.UIModule;
import sdk.guru.common.RX;

public class PublicThreadsFragment extends ThreadsFragment {

    @Override
    public Predicate<NetworkEvent> mainEventFilter() {
        return NetworkEvent.filterPublicThreadsUpdated();
    }

    @Override
    public void initViews() {
        super.initViews();

//        dm.add(getOnLongClickObservable().subscribe(thread -> DialogUtils.showToastDialog(getContext(), 0, R.string.alert_delete_thread, R.string.delete,
//                R.string.cancel, () -> {
//                    dm.add(ChatSDK.thread().deleteThread(thread)
//                            .observeOn(RX.main())
//                            .subscribe(() -> {
//                                clearData();
//                                reloadData();
//                            }, throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));
//                }, null)));
    }


    @Override
    protected Single<List<Thread>> getThreads() {
        return Single.create((SingleOnSubscribe<List<Thread>>) emitter -> {
            List<Thread> threads =  ChatSDK.thread().getThreads(ThreadType.Public);

            if (ChatSDK.config().publicChatRoomLifetimeMinutes == 0) {
                emitter.onSuccess(threads);
            } else {
                // Do we need to filter the list to remove old chat rooms?
                long now = new Date().getTime();
                List<Thread> filtered = new ArrayList<>();
                for (Thread t : threads) {
                    if (t.getCreationDate() == null || now - t.getCreationDate().getTime() < TimeUnit.MINUTES.toMillis(ChatSDK.config().publicChatRoomLifetimeMinutes)) {
                        filtered.add(t);
                    }
                }
                emitter.onSuccess(filtered);
            }
        }).subscribeOn(RX.single());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_add) {
            ChatSDK.ui().startEditThreadActivity(getContext(), null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean allowThreadCreation () {
        return UIModule.config().publicRoomCreationEnabled;
    }
}
