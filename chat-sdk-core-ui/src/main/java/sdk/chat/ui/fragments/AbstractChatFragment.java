package sdk.chat.ui.fragments;

import sdk.chat.core.dao.ThreadX;

public abstract class AbstractChatFragment extends BaseFragment {
    public abstract void onNewIntent(ThreadX thread);
    public abstract boolean onBackPressed();
}
