package sdk.chat.ui.fragments;

import sdk.chat.core.dao.Thread;

public abstract class AbstractChatFragment extends BaseFragment {
    public abstract void onNewIntent(Thread thread);
}
