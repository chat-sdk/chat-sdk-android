package sdk.chat.ui.utils;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.fragments.BaseFragment;
import sdk.guru.common.DisposableMap;

public class FragmentLifecycleManager {

    List<BaseFragment> fragments = new ArrayList<>();
    DisposableMap dm = new DisposableMap();

    public FragmentLifecycleManager() {
        ChatSDK.shared().addOnActivateListener(() -> {
            // Clear data on logout
            dm.add(ChatSDK.events().sourceOnMain()
                    .filter(NetworkEvent.filterType(EventType.Logout))
                    .subscribe(networkEvent -> {
                        for (BaseFragment fragment: fragments) {
                            fragment.clearData();
                        }
                    }));
        });
    }

    public void add(BaseFragment fragment) {
        if (!fragments.contains(fragment)) {
            fragments.add(fragment);
        }
    }

    public void remove(BaseFragment fragment) {
        fragments.remove(fragment);
    }

}
