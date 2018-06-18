package co.patchat.android.app;

import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;

import co.chatsdk.core.session.NM;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.threads.PublicThreadsFragment;
import co.chatsdk.ui.threads.ThreadsListAdapter;

public class CustomPublicThreadsFragment extends PublicThreadsFragment {

    protected boolean isAdmin = false;

    @Override
    public void initViews(LayoutInflater inflater) {
        mainView = inflater.inflate(co.chatsdk.ui.R.layout.chat_sdk_activity_public_threads, null);
        searchField = mainView.findViewById(co.chatsdk.ui.R.id.search_field);
        listThreads = mainView.findViewById(co.chatsdk.ui.R.id.list_threads);

        adapter = new CustomThreadsListAdapter(getActivity());

        listThreads.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        listThreads.setAdapter(adapter);

        adapter.onClickObservable().subscribe(thread -> InterfaceManager.shared().a.startChatActivityForID(getContext(), thread.getEntityID()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (settingsItem != null) {
            settingsItem.setEnabled(isAdmin);
            settingsItem.setVisible(isAdmin);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isAdmin = NM.currentUser().metaBooleanForKey("admin");
        if (settingsItem != null) {
            settingsItem.setEnabled(isAdmin);
            settingsItem.setVisible(isAdmin);
        }
    }

}
