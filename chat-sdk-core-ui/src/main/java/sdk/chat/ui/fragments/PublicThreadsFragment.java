package sdk.chat.ui.fragments;

import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Predicate;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.provider.MenuItemProvider;
import sdk.guru.common.RX;

public class PublicThreadsFragment extends ThreadsFragment {

    @Override
    public Predicate<NetworkEvent> mainEventFilter() {
        return NetworkEvent.filterPublicThreadsUpdated();
    }

    @Override
    public void initViews() {
        super.initViews();


//        dm.add(getOnLongClickObservable().subscribe(thread -> DialogUtils.showToastDialog(getContext(), 0, sdk.chat.core.R.string.alert_delete_thread, sdk.chat.core.R.string.delete,
//                sdk.chat.core.R.string.cancel, () -> {
//                    dm.add(ChatSDK.thread().deleteThread(thread)
//                            .observeOn(RX.main())
//                            .subscribe(() -> {
//                                clearData();
//                                reloadData();
//                            }, throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));
//                }, null)));
    }


    @Override
    protected Single<List<ThreadX>> getThreads() {
        return Single.create((SingleOnSubscribe<List<ThreadX>>) emitter -> {
            List<ThreadX> threads =  ChatSDK.thread().getThreads(ThreadType.Public);

            if (ChatSDK.config().publicChatRoomLifetimeMinutes == 0) {
                emitter.onSuccess(threads);
            } else {
                // Do we need to filter the list to remove old chat rooms?
                long now = new Date().getTime();
                List<ThreadX> filtered = new ArrayList<>();
                for (ThreadX t : threads) {
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

        if (id == MenuItemProvider.addItemId) {
            ChatSDK.ui().startEditThreadActivity(getContext(), null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean allowThreadCreation() {
        return UIModule.config().publicRoomCreationEnabled;
    }
}
