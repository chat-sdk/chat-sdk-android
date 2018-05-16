package co.chatsdk.android.app;

import android.content.Context;
import android.support.v4.app.Fragment;

import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import co.chatsdk.ui.threads.PublicThreadsFragment;

public class CustomInterfaceAdapter extends BaseInterfaceAdapter {

    public CustomInterfaceAdapter(Context context) {
        super(context);
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
