package co.chatsdk.ui.fragments;

import android.view.MenuItem;

import androidx.core.app.ActivityCompat;

import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.DialogUtils;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;

public class PrivateThreadsFragment extends ThreadsFragment {

    @Override
    protected Predicate<NetworkEvent> mainEventFilter() {
        return NetworkEvent.filterPrivateThreadsUpdated();
    }

    @Override
    public void initViews() {
        super.initViews();

        dm.add(getOnLongClickObservable().subscribe(thread -> DialogUtils.showToastDialog(getContext(), "", getResources().getString(R.string.alert_delete_thread), getResources().getString(R.string.delete),
                getResources().getString(R.string.cancel), null, () -> {
                    dm.add(ChatSDK.thread().deleteThread(thread)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                clearData();
                                reloadData();
                            }, throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));
                    return null;
                })));
    }


    @Override
    protected List<Thread> getThreads() {
        return ChatSDK.thread().getThreads(ThreadType.Private);
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
