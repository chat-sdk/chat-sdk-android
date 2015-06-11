/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.adapter;


import android.app.FragmentManager;

import com.astuetz.pagersslidingtabstrip.PagerSlidingTabStrip;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.fragments.ChatSDKBaseFragment;
import com.braunster.chatsdk.fragments.ChatSDKContactsFragment;
import com.braunster.chatsdk.fragments.ChatSDKConversationsFragment;
import com.braunster.chatsdk.fragments.ChatSDKThreadsFragment;
import com.braunster.chatsdk.fragments.ChatcatProfileFragment;
import com.braunster.chatsdk.fragments.abstracted.ChatSDKAbstractContactsFragment;

/**
 * Created by itzik on 6/16/2014.
 */
public class PagerAdapterTabs extends BaseChatSDKTabsAdapter implements PagerSlidingTabStrip.IconTabProvider {

    public PagerAdapterTabs(FragmentManager fm) {
        super(fm);

        fragments = new ChatSDKBaseFragment[] {ChatSDKConversationsFragment.newInstance(),
                ChatSDKThreadsFragment.newInstance(),
                ChatSDKContactsFragment.newInstance(ChatSDKAbstractContactsFragment.MODE_LOAD_ONLINE, ChatSDKAbstractContactsFragment.CLICK_MODE_SHOW_PROFILE, null, "Tab_Online"),
                ChatSDKContactsFragment.friendsFragment("Tab_Friends"),
                ChatcatProfileFragment.newInstance()};

        icnns = new int[] {R.drawable.ic_action_private, R.drawable.ic_action_public, R.drawable.ic_online, R.drawable.ic_action_contacts, R.drawable.ic_action_user };

    }

}
