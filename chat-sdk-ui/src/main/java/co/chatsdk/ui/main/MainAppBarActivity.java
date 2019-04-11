package co.chatsdk.ui.main;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.ViewHelper;

public class MainAppBarActivity extends MainActivity {

    protected TabLayout tabLayout;
    protected ViewPager viewPager;
    protected PagerAdapter adapter;
    protected List<Tab> tabs;

    protected @LayoutRes
    int activityLayout() {
        return R.layout.activity_view_pager;
    }

    protected PagerAdapter initAdapter() {
        // Only creates the adapter if it wasn't initiated already
        if (adapter == null) {
            adapter = new PagerAdapterTabs(getSupportFragmentManager());
        }

        tabs = ((PagerAdapterTabs)adapter).getTabs();
        if (tabLayout != null) {
            for (Tab tab : tabs) {
                tabLayout.addTab(tabLayout.newTab().setText(tab.title));
            }
        }

        Fragment firstFragment = tabs.get(0).fragment;
        if (firstFragment instanceof BaseFragment) {
            ((BaseFragment) firstFragment).setTabVisibility(true);
        }
        return adapter;
    }

    protected void initViews() {
        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab_layout);

        if (viewPager != null) {
            viewPager.setAdapter(initAdapter());
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            viewPager.setOffscreenPageLimit(3);
        }

        if (tabLayout != null) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    ViewHelper.setCurrentItem(viewPager, tab.getPosition());

                    updateLocalNotificationsForTab();

                    // We mark the tab as visible. This lets us be more efficient with updates
                    // because we only
                    if (tabs == null) return;
                    for (int i = 0; i < tabs.size(); i++) {
                        ((BaseFragment) tabs.get(i).fragment).setTabVisibility(i == tab.getPosition());
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
    }

    public void updateLocalNotificationsForTab () {
        if (tabLayout == null) return;
        TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
        ChatSDK.ui().setLocalNotificationHandler(thread -> showLocalNotificationsForTab(tab, thread));
    }

    public boolean showLocalNotificationsForTab (TabLayout.Tab tab, Thread thread) {
        // Don't show notifications on the threads tabs
        if (tabs == null) return false;
        Tab t = tabs.get(tab.getPosition());

        if (thread.typeIs(ThreadType.Private)) {
            Class privateThreadsFragmentClass = ChatSDK.ui().privateThreadsFragment().getClass();
            return !t.fragment.getClass().isAssignableFrom(privateThreadsFragmentClass);
        }
        if (thread.typeIs(ThreadType.Public)) {
            Class publicThreadsFragmentClass = ChatSDK.ui().publicThreadsFragment().getClass();
            return !t.fragment.getClass().isAssignableFrom(publicThreadsFragmentClass);
        }
        return true;
    }

    public void clearData () {
        if (tabs == null) return;
        for (Tab t : tabs) {
            if (t.fragment instanceof BaseFragment) {
                ((BaseFragment) t.fragment).clearData();
            }
        }
    }

    public void reloadData () {
        if (tabs == null) return;
        for (Tab t : tabs) {
            if (t.fragment instanceof BaseFragment) {
                ((BaseFragment) t.fragment).safeReloadData();
            }
        }
    }

}
