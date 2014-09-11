package com.braunster.chatsdk.adapter;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.astuetz.pagersslidingtabstrip.PagerSlidingTabStrip;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.fragments.ChatSDKBaseFragment;
import com.braunster.chatsdk.fragments.ChatSDKContactsFragment;
import com.braunster.chatsdk.fragments.ChatSDKConversationsFragment;
import com.braunster.chatsdk.fragments.ChatSDKProfileFragment;
import com.braunster.chatsdk.fragments.ChatSDKThreadsFragment;

/**
 * Created by itzik on 6/16/2014.
 */
public class PagerAdapterTabs extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

    private final String[] TITLES = {"Conversations", "Chat Rooms", "Contacts", "Profile"};

    private final ChatSDKBaseFragment[] FRAGMENTS = new ChatSDKBaseFragment[] {ChatSDKConversationsFragment.newInstance(), ChatSDKThreadsFragment.newInstance(), ChatSDKContactsFragment.newInstance("ConvFragmentPage"), ChatSDKProfileFragment.newInstance()};

    private final int[] ICONS = new int[] {R.drawable.ic_action_private, R.drawable.ic_action_public, R.drawable.ic_action_contacts, R.drawable.ic_action_user };

    public static final int Profile = 3, ChatRooms = 1, Contacts = 2, Conversations = 0;

    public PagerAdapterTabs(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public ChatSDKBaseFragment getItem(int position) {
        return FRAGMENTS[position];
    }

/*    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        super.instantiateItem(container, position);
        BaseFragment fragment = null;
        Log.e("AAAA", "POS:  " + position);
        switch (position)
        {
            case 0:
                fragment = ProfileFragment.newInstance();
                break;
            case 1:
                fragment = ThreadsFragment.newInstance();
                break;
            case 2:
                fragment = ContactsFragment.newInstance();
                break;
            case 3:
                fragment = ConversationsFragment.newInstance();
        }

        FRAGMENTS[position] = fragment;
        return fragment;
    }*/

    @Override
    public int getPageIconResId(int position) {
        return ICONS[position];
    }
}
