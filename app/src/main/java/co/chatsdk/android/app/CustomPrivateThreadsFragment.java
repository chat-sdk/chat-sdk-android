package co.chatsdk.android.app;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import co.chatsdk.core.session.NM;
import co.chatsdk.ui.helpers.DialogUtils;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.threads.PrivateThreadsFragment;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class CustomPrivateThreadsFragment extends PrivateThreadsFragment {

    public static CustomPrivateThreadsFragment newInstance() {
        CustomPrivateThreadsFragment f = new CustomPrivateThreadsFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    protected void initList() {
        adapter = new CustomThreadsListAdapter(getActivity());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setClickable(true);

        adapter.onClickObservable().subscribe(thread -> InterfaceManager.shared().a.startChatActivityForID(getContext(), thread.getEntityID()));

        adapter.onLongClickObservable().subscribe(thread -> DialogUtils.showToastDialog(getContext(), "", getResources().getString(co.chatsdk.ui.R.string.alert_delete_thread), getResources().getString(co.chatsdk.ui.R.string.delete),
                getResources().getString(co.chatsdk.ui.R.string.cancel), null, () -> {
                    NM.thread().deleteThread(thread)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                }

                                @Override
                                public void onComplete() {
                                    adapter.clearData();
                                    reloadData();
                                    ToastHelper.show(getContext(), getString(co.chatsdk.ui.R.string.delete_thread_success_toast));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    ToastHelper.show(getContext(), getString(co.chatsdk.ui.R.string.delete_thread_fail_toast));
                                }
                            });
                    return null;
                }));
    }

}
