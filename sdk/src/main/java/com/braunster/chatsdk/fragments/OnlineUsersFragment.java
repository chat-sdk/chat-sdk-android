package com.braunster.chatsdk.fragments;

import com.braunster.chatsdk.fragments.abstracted.ChatSDKAbstractContactsFragment;

/**
 * Created by braunster on 08.06.15.
 */
public class OnlineUsersFragment extends ChatSDKAbstractContactsFragment {

    public static ChatSDKContactsFragment newInstance(String eventTAG) {
        ChatSDKContactsFragment f = new ChatSDKContactsFragment();
        f.setLoadingMode(MODE_LOAD_ONLINE);
        f.setClickMode(CLICK_MODE_SHOW_PROFILE);
        f.setEventTAG(eventTAG);
        return f;
    }
}
