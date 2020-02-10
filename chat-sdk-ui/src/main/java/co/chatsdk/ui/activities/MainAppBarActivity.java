package co.chatsdk.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import butterknife.BindView;
import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.databinding.ActivityViewPagerBinding;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.adapters.PagerAdapterTabs;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.interfaces.SearchSupported;

public class MainAppBarActivity extends MainActivity {

    protected PagerAdapterTabs adapter;

    protected ActivityViewPagerBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());

        initViews();
    }

    @Override
    protected boolean searchEnabled() {
        return currentTab().fragment instanceof SearchSupported;
    }

    @Override
    protected void search(String text) {
        Fragment fragment = currentTab().fragment;
        if (fragment instanceof SearchSupported) {
            ((SearchSupported) fragment).filter(text);
        }
    }

    @Override
    protected MaterialSearchView searchView() {
        return b.searchView;
    }

    protected @LayoutRes
    int getLayout() {
        return R.layout.activity_view_pager;
    }

    protected void initViews() {
        super.initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(Icons.get(Icons.shared().user, R.color.white));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        b.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        // Only creates the adapter if it wasn't initiated already
        if (adapter == null) {
            adapter = new PagerAdapterTabs(getSupportFragmentManager());
        }

        final List<Tab> tabs = adapter.getTabs();
        for (Tab tab : tabs) {
            b.tabLayout.addTab(b.tabLayout.newTab().setText(tab.title));
        }

//        ((BaseFragment) tabs.get(0).fragment).setTabVisibility(true);

        b.viewPager.setAdapter(adapter);

        b.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(b.tabLayout));
        b.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        b.viewPager.setOffscreenPageLimit(3);

        TabLayout.Tab tab = b.tabLayout.getTabAt(0);
        if (tab != null) {
            tabSelected(tab);
        }
    }

    public void tabSelected(TabLayout.Tab tab) {

        int index = tab.getPosition();

        b.viewPager.setCurrentItem(index);

        final List<Tab> tabs = adapter.getTabs();

//        Fragment currentFragment = adapter.getTabs().get(index).fragment;
//        if (getSupportActionBar() != null) {
//            if (currentFragment instanceof HasAppbar) {
//                getSupportActionBar().hide();
//            } else {
//                getSupportActionBar().show();
//            }
//        }

        updateLocalNotificationsForTab();

        // We mark the tab as visible. This lets us be more efficient with updates
        // because we only
        for(int i = 0; i < tabs.size(); i++) {
            Fragment fragment = tabs.get(i).fragment;
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) tabs.get(i).fragment).setTabVisibility(i == tab.getPosition());
            }
        }
    }

    public Tab currentTab() {
        return adapter.getTabs().get(b.viewPager.getCurrentItem());
    }

    public void updateLocalNotificationsForTab () {
        TabLayout.Tab tab = b.tabLayout.getTabAt(b.tabLayout.getSelectedTabPosition());
        ChatSDK.ui().setLocalNotificationHandler(thread -> showLocalNotificationsForTab(tab, thread));
    }

    public boolean showLocalNotificationsForTab (TabLayout.Tab tab, Thread thread) {
        // Don't show notifications on the threads tabs
        Tab t = adapter.getTabs().get(tab.getPosition());

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
        for(Tab t : adapter.getTabs()) {
            if(t.fragment instanceof BaseFragment) {
                ((BaseFragment) t.fragment).clearData();
            }
        }
    }

    public void reloadData () {
        for(Tab t : adapter.getTabs()) {
            if(t.fragment instanceof BaseFragment) {
                ((BaseFragment) t.fragment).safeReloadData();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                ChatSDK.ui().startProfileActivity(this, ChatSDK.currentUserID());
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
