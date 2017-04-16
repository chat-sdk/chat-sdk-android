/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package wanderingdevelopment.tk.sdkbaseui.adapter;


import android.support.v4.app.FragmentManager;

import wanderingdevelopment.tk.sdkbaseui.pagersslidingtabstrip.PagerSlidingTabStrip;
import wanderingdevelopment.tk.sdkbaseui.R;

import wanderingdevelopment.tk.sdkbaseui.FragmentTemplates.ChatSDKBaseFragment;
import wanderingdevelopment.tk.sdkbaseui.FragmentTemplates.ChatSDKContactsFragment;
import wanderingdevelopment.tk.sdkbaseui.FragmentTemplates.ChatSDKConversationsFragment;
import wanderingdevelopment.tk.sdkbaseui.FragmentTemplates.ChatSDKProfileFragment;
import wanderingdevelopment.tk.sdkbaseui.FragmentTemplates.ChatSDKThreadsFragment;

/**
 * Created by itzik on 6/16/2014.
 */
public class PagerAdapterTabs extends AbstractChatSDKTabsAdapter implements PagerSlidingTabStrip.IconTabProvider {

    public PagerAdapterTabs(FragmentManager fm) {
        super(fm);

        fragments = new ChatSDKBaseFragment[] {ChatSDKConversationsFragment.newInstance(),
                ChatSDKThreadsFragment.newInstance(),
                ChatSDKContactsFragment.newInstance("ConvFragmentPage"),
                ChatSDKProfileFragment.newInstance()};

        icnns = new int[] {R.drawable.ic_action_private, R.drawable.ic_action_public, R.drawable.ic_action_contacts, R.drawable.ic_action_user };
    }

}
