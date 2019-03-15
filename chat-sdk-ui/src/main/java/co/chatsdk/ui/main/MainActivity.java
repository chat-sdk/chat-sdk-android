/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.R;


public abstract class MainActivity extends BaseActivity {

    protected DisposableList disposableList = new DisposableList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed context
            finish();
            return;
        }
        initViews();
        launchFromPush(getIntent().getExtras());
    }

    public void launchFromPush (Bundle bundle) {
        if (bundle != null) {
            String threadID = bundle.getString(Keys.THREAD_ENTITY_ID);
            if (threadID != null && !threadID.isEmpty()) {
                ChatSDK.ui().startChatActivityForID(getBaseContext(), threadID);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        disposableList.dispose();

        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.Logout))
                .subscribe(networkEvent -> clearData()));

        updateLocalNotificationsForTab();
        reloadData();

    }

    protected abstract void reloadData();
    protected abstract void initViews();
    protected abstract void clearData();
    protected abstract void updateLocalNotificationsForTab();
    protected abstract int activityLayout();

        @Override
    protected void onPause () {
        super.onPause();
        disposableList.dispose();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        launchFromPush(intent.getExtras());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
