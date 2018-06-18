package co.patchat.android.app;

import android.content.Context;
import android.support.v4.app.Fragment;

import co.chatsdk.core.Tab;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import co.chatsdk.ui.threads.PublicThreadsFragment;

public class CustomInterfaceAdapter extends BaseInterfaceAdapter {

    public CustomInterfaceAdapter(Context context) {
        super(context);
    }

    @Override
    public Class getChatActivity() {
        return CustomChatActivity.class;
    }

    @Override
    public Fragment privateThreadsFragment() {
        return CustomPrivateThreadsFragment.newInstance();
    }

    @Override
    public Tab publicThreadsTab() {
        return new Tab(R.string.analyzers, R.drawable.ic_action_public, publicThreadsFragment());
    }

    @Override
    public Fragment publicThreadsFragment() {
        return new CustomPublicThreadsFragment();
    }

    @Override
    public Class getThreadDetailsActivity() {
        return CustomThreadDetailsActivity.class;
    }

    @Override
    public Class getPublicThreadEditDetailsActivity() {
        return CustomPublicThreadEditDetailsActivity.class;
    }

}
