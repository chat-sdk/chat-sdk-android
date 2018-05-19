package co.chatsdk.android.app;

import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.threads.PublicThreadsFragment;

public class CustomPublicThreadsFragment extends PublicThreadsFragment {

    protected EditText searchTextView;
    protected ImageView searchImageView;

    @Override
    public void initViews(LayoutInflater inflater) {
        mainView = inflater.inflate(R.layout.custom_activity_search_threads, null);
        searchTextView = mainView.findViewById(R.id.list_threads_search_input);
        searchImageView = mainView.findViewById(R.id.list_threads_search_btn);
        listThreads = mainView.findViewById(co.chatsdk.ui.R.id.list_threads);

        adapter = new CustomThreadsListAdapter(getActivity());

        listThreads.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        listThreads.setAdapter(adapter);

        adapter.onClickObservable().subscribe(thread -> InterfaceManager.shared().a.startChatActivityForID(getContext(), thread.getEntityID()));

    }

    @Override
    public void onResume() {
        super.onResume();



        searchImageView.setOnClickListener(v -> {
            ((CustomThreadsListAdapter)adapter).filterThreads(searchTextView.getText().toString());
        });

        searchTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchImageView.callOnClick();
            }
            return false;
        });
    }

    protected View.OnClickListener searchOnClickListener = v -> {
        reloadData();
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
        if (ChatSDK.config().publicRoomCreationEnabled && NM.currentUser().metaBooleanForKey("admin")) {
            MenuItem item =
                    menu.add(Menu.NONE, co.chatsdk.ui.R.id.action_chat_sdk_add, 10, getString(co.chatsdk.ui.R.string.public_thread_fragment_add_item_text));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            item.setIcon(co.chatsdk.ui.R.drawable.ic_plus);
        }
    }

}
