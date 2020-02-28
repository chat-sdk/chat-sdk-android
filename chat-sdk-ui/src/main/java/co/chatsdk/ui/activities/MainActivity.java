/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.pmw.tinylog.Logger;

import butterknife.BindView;
import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ToastHelper;


public abstract class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Check this
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed context
            finish();
            return;
        }
        launchFromPush(getIntent().getExtras());

    }

    protected void initViews() {
        super.initViews();
        if (searchView() != null) {
            searchView().setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    search(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    search(newText);
                    return false;
                }
            });
        }
        requestPermissions();
    }

    protected void requestPermissions() {

    }

    protected abstract boolean searchEnabled();
    protected abstract void search(String text);
    protected abstract MaterialSearchView searchView();

    public void launchFromPush (@Nullable Bundle bundle) {
        if (bundle != null) {
            String threadID = bundle.getString(Keys.IntentKeyThreadEntityID);
            if (threadID != null && !threadID.isEmpty()) {
                ChatSDK.ui().startChatActivityForID(getBaseContext(), threadID);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.Logout))
                .subscribe(networkEvent -> clearData()));

        updateLocalNotificationsForTab();
        reloadData();

    }

    protected abstract void reloadData();
    protected abstract void clearData();
    protected abstract void updateLocalNotificationsForTab();
    protected abstract int getLayout();

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        launchFromPush(intent.getExtras());
    }

    @Override
    protected void onSaveInstanceState(@Nullable Bundle outState) {
        super.onSaveInstanceState(outState);
        // Add the filter button

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean value = super.onCreateOptionsMenu(menu);

        if (searchEnabled()) {
            getMenuInflater().inflate(R.menu.activity_search_menu, menu);
            MenuItem item = menu.findItem(R.id.action_search);
            item.setIcon(Icons.get(Icons.choose().search, R.color.app_bar_icon_color));
            searchView().setMenuItem(item);
        }

        return value;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Fixes an issue where if we press back the whole app goes blank
    }

    public boolean showLocalNotificationsForTab (Fragment fragment, Thread thread) {
        // Don't show notifications on the threads tabs
        if (thread.typeIs(ThreadType.Private)) {
            Class privateThreadsFragmentClass = ChatSDK.ui().privateThreadsFragment().getClass();
            return !fragment.getClass().isAssignableFrom(privateThreadsFragmentClass);
        }
        if (thread.typeIs(ThreadType.Public)) {
            Class publicThreadsFragmentClass = ChatSDK.ui().publicThreadsFragment().getClass();
            return !fragment.getClass().isAssignableFrom(publicThreadsFragmentClass);
        }
        return true;
    }

}
