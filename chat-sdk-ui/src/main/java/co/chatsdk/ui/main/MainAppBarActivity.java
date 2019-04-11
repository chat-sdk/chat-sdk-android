package co.chatsdk.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import co.chatsdk.core.Tab;
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
        return R.layout.chat_sdk_activity_view_pager;
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
        ChatSDK.ui().setLocalNotificationHandler(thread -> showLocalNotificationsForTab(tab));
    }

    public boolean showLocalNotificationsForTab (TabLayout.Tab tab) {
        // Don't show notifications on the threads tabs
        if (tabs == null) return false;
        Tab t = tabs.get(tab.getPosition());

        Class privateThreadsFragmentClass = ChatSDK.ui().privateThreadsFragment().getClass();

        return !t.fragment.getClass().isAssignableFrom(privateThreadsFragmentClass);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.contact_developer) {

            String emailAddress = ChatSDK.config().contactDeveloperEmailAddress;
            String subject = ChatSDK.config().contactDeveloperEmailSubject;
            String dialogTitle = ChatSDK.config().contactDeveloperDialogTitle;

            if(StringUtils.isNotEmpty(emailAddress))
            {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", emailAddress, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                startActivity(Intent.createChooser(emailIntent, dialogTitle));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
