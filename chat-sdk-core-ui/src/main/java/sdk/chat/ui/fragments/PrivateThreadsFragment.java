package sdk.chat.ui.fragments;

import android.view.MenuItem;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;
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

        dm.add(getOnLongClickObservable().subscribe(thread -> DialogUtils.showToastDialog(getContext(), 0, R.string.alert_delete_thread, R.string.delete,
                R.string.cancel,  () -> {
                    dm.add(ChatSDK.thread().deleteThread(thread)
                            .observeOn(RX.main())
                            .subscribe(() -> {
                                clearData();
                                reloadData();
                            }, throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));
                }, null)));
    }


    @Override
    protected Single<List<Thread>> getThreads() {
        return Single.defer(() -> Single.just(ChatSDK.thread().getThreads(ThreadType.Private))).subscribeOn(RX.single());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_add) {
            ChatSDK.ui().startCreateThreadActivity(getContext());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
