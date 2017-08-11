/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;

import co.chatsdk.core.NM;

import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.Defines;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.fragments.FragmentIDs;

import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import co.chatsdk.ui.helpers.ExitHelper;
import co.chatsdk.ui.helpers.NotificationUtils;
import co.chatsdk.ui.utils.Utils;
import co.chatsdk.ui.helpers.OpenFromPushChecker;
import co.chatsdk.ui.adapters.PagerAdapterTabs;

import org.apache.commons.lang3.StringUtils;


public class MainActivity extends BaseActivity {

    private ExitHelper exitHelper;
    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    protected PagerAdapterTabs adapter;

    private static final String FIRST_TIME_IN_APP = "First_Time_In_App";
    public static final String PAGE_ADAPTER_POS = "page_adapter_pos";

    public static final String Action_Contacts_Added = "com.braunster.androidchatsdk.action.contact_added";
    public static final String Action_clear_data = "com.braunster.androidchatsdk.action.logged_out";
    public static final String Action_Refresh_Fragment = "com.braunster.androidchatsdk.action.refresh_fragment";

    private int pageAdapterPos = -1;

    private OpenFromPushChecker mOpenFromPushChecker;

    Disposable messageAddedDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        exitHelper = new ExitHelper(this);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }

        setContentView(R.layout.chat_sdk_activity_view_pager);

        firstTimeInApp();
        initViews();

        enableCheckOnlineOnResumed(true);

        if (!fromLoginActivity && savedInstanceState != null)
        {
            pager.setCurrentItem(savedInstanceState.getInt(PAGE_ADAPTER_POS));
        }

        mOpenFromPushChecker = new OpenFromPushChecker();
        if(mOpenFromPushChecker.checkOnCreate(getIntent(), savedInstanceState))
        {
            startChatActivityForID(getIntent().getExtras().getLong(ChatActivity.THREAD_ID));
            return;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(messageAddedDisposable != null) {
            messageAddedDisposable.dispose();
        }

        // TODO: Check this
        messageAddedDisposable = NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .filter(NetworkEvent.filterThreadType(ThreadType.Private))
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(NetworkEvent networkEvent) throws Exception {
                        if(networkEvent.message != null) {
                            if(!networkEvent.message.getSender().isMe()) {
                                NotificationUtils.createMessageNotification(MainActivity.this, networkEvent.message);
                            }
                        }
                    }
                });


        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //pageAdapterPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    @Override
    protected void onPause () {
        super.onPause();
        if(messageAddedDisposable != null) {
            messageAddedDisposable.dispose();
            messageAddedDisposable = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        if (mOpenFromPushChecker == null)
            mOpenFromPushChecker = new OpenFromPushChecker();
        
        if (mOpenFromPushChecker.checkOnNewIntent(intent))
        {
            startChatActivityForID(intent.getExtras().getLong(ChatActivity.THREAD_ID));
            return;
        }

        if (adapter != null)
        {
            BaseFragment profile = getFragment(FragmentIDs.Profile);
            BaseFragment conversations = getFragment(FragmentIDs.Conversations);

            if (conversations!=null)
                conversations.loadData();

            if (profile != null)
                profile.refresh();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt(PAGE_ADAPTER_POS, pageAdapterPos);
        mOpenFromPushChecker.onSaveInstanceState(outState);
    }

    private void initViews(){
        pager = (ViewPager) findViewById(R.id.pager);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        // Only creates the adapter if it wasn't initiated already
        if (adapter == null)
            adapter = new PagerAdapterTabs(getSupportFragmentManager());

        pager.setAdapter(adapter);

        tabs.setViewPager(pager);

        pager.setOffscreenPageLimit(3);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_chat_sdk, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.android_settings) {

            // FIXME Clearing the cache, Just for debug.
            /*final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            VolleyUtils.getBitmapCache().resize(1);
            VolleyUtils.getBitmapCache().resize(maxMemory / 8);*/
            return true;
        }
        else   if (item.getItemId() == R.id.contact_developer) {
            if(StringUtils.isNotEmpty(Defines.ContactDeveloper_Email))
            {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", Defines.ContactDeveloper_Email, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, Defines.ContactDeveloper_Subject);
                startActivity(Intent.createChooser(emailIntent, Defines.ContactDeveloper_DialogTitle));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If the register did not had a chance to register due to orientation change.
        try{
            unregisterReceiver(mainReceiver);
        }catch (IllegalArgumentException e){
            // No need to handle the exception.
        }
    }

    private void firstTimeInApp(){
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FIRST_TIME_IN_APP, true))
        {
            // Creating the images directory if not exist.
            Utils.ImageSaver.getAlbumStorageDir(this, Defines.ImageDirName);

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(FIRST_TIME_IN_APP, false).apply();
        }
    }

    /** Refresh the contacts fragment when a contact added action is received.
     *  Clear Fragments bundle when logged out.
     *  Refresh Fragment when wanted.*/
    private BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Action_Contacts_Added))
            {
                BaseFragment contacts = getFragment(FragmentIDs.Contacts);

                if (contacts != null)
                    contacts.loadData();

                // TODO: This should be handled by the list already no?
//                if (intent.getExtras().containsKey(SearchActivity.USER_IDS_LIST))
//                {
//                    String[] ids = intent.getStringArrayExtra(SearchActivity.USER_IDS_LIST);
//                    for (String id : ids) {
//
//                        new UserWrapper(id);
//                    }
//
//                        NetworkManager.getCoreInterface().getEventManager().userMetaOn(id, null);
//                }
            }
            else if (intent.getAction().equals(Action_clear_data))
            {
                clearData();
            }
            else if (intent.getAction().equals(Action_Refresh_Fragment))
            {
                if (intent.getExtras() == null)
                    return;

                if (!intent.getExtras().containsKey(PAGE_ADAPTER_POS))
                    return;

                int fragment = intent.getExtras().getInt(PAGE_ADAPTER_POS);

                BaseFragment frag = getFragment(fragment);

                if (frag!= null)
                    frag.refresh();

            }
            else if (intent.getAction().equals(ChatActivity.ACTION_CHAT_CLOSED))
            {
                getFragment(FragmentIDs.Conversations).loadData();
            }
        }
    };

    private void clearData(){
        BaseFragment contacts = getFragment(FragmentIDs.Contacts);

        if (contacts != null)
            contacts.clearData();

        BaseFragment conv = getFragment(FragmentIDs.Conversations);

        if (conv != null)
            conv.clearData();

        BaseFragment pro = getFragment(FragmentIDs.Profile);

        if (pro != null)
            pro.clearData();
    }

    /* Exit Stuff*/
    @Override
    public void onBackPressed() {
        exitHelper.triggerExit();
    }

    /** After screen orientation chage the getItem from the fragment page adapter is no null but it is not visible to the user
     *  so we have to use this workaround so when we call any method on the wanted fragment the fragment will respond.
     *  http://stackoverflow.com/a/7393477/2568492*/
    private BaseFragment getFragment(int index){
        return ((BaseFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + index));
    }
}
