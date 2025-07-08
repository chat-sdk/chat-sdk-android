package sdk.chat.ui.fragments;

import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;
import sdk.chat.ui.provider.MenuItemProvider;
import sdk.chat.ui.utils.DialogUtils;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.RX;

public class PrivateThreadsFragment extends ThreadsFragment {

    @Override
    protected Predicate<NetworkEvent> mainEventFilter() {
        return NetworkEvent.filterPrivateThreadsUpdated();
    }

    @Override
    public void initViews() {
        super.initViews();
    }

    @Override public boolean addListeners() {
        if(super.addListeners()) {

            dm.add(getOnLongClickObservable().subscribe(thread -> {
                showLongPressDialog(thread);
            }));

            return true;
        }
        return false;
    }

    public void showLongPressDialog(ThreadX thread) {
        DialogUtils.showToastDialog(getContext(), 0, sdk.chat.core.R.string.alert_delete_thread, sdk.chat.core.R.string.delete,
                sdk.chat.core.R.string.cancel,  () -> {
                    dm.add(ChatSDK.thread().deleteThread(thread)
                            .observeOn(RX.main())
                            .subscribe(() -> {
                                removeThread(thread);
//                                clearData();
//                                loadData();
                            }, throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));
                }, null);

    }

    @Override
    protected Single<List<ThreadX>> getThreads() {
        return Single.defer(() -> {

            List<ThreadX> threads = ChatSDK.thread().getThreads(ThreadType.Private);

            if (ChatSDK.config().privateChatRoomLifetimeMinutes == 0) {
                return Single.just(threads);
            } else {
                // Do we need to filter the list to remove old chat rooms?
                long now = new Date().getTime();
                List<ThreadX> filtered = new ArrayList<>();
                for (ThreadX t : threads) {
                    if (t.getCreationDate() == null || now - t.getCreationDate().getTime() < TimeUnit.MINUTES.toMillis(ChatSDK.config().privateChatRoomLifetimeMinutes)) {
                        filtered.add(t);
                    }
                }
                return Single.just(filtered);
            }
        }).subscribeOn(RX.single());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == MenuItemProvider.addItemId) {
            ChatSDK.ui().startCreateThreadActivity(getContext());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
