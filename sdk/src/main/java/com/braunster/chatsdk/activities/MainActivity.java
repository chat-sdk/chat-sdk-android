package com.braunster.chatsdk.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.NotificationUtils;
import com.braunster.chatsdk.adapter.PagerAdapterTabs;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.fragments.BaseFragment;
import com.braunster.chatsdk.fragments.ProfileFragment;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.AppEventListener;
import com.braunster.chatsdk.network.firebase.EventManager;

import java.io.File;
import java.util.concurrent.Callable;


public class MainActivity extends BaseActivity {

    // TODO add option to save up app in external storage. http://developer.android.com/guide/topics/data/install-location.html
    // TODO add option to the exit dialog to not show it again and exit every time the user press the back button.
    private static final String TAG = MainActivity.class.getSimpleName();
    private static boolean DEBUG = true;

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private PagerAdapterTabs adapter;

    private Menu menu;

    private static final String FIRST_TIME_IN_APP = "First_Time_In_App";
    private static final String PAGE_ADAPTER_POS = "page_adapter_pos";

    private int pageAdapterPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableFacebookIntegration(true);
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.v(TAG, "onCreate");
        setContentView(R.layout.chat_sdk_activity_view_pager);

        firstTimeInApp();
        initViews();
        initToast();

        enableCheckOnlineOnResumed(true);

        if (savedInstanceState != null)
        {
            if (DEBUG) Log.v(TAG, "Saved Instance is not null, "  + savedInstanceState.getInt(PAGE_ADAPTER_POS));
            pager.setCurrentItem(savedInstanceState.getInt(PAGE_ADAPTER_POS));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DEBUG) Log.v(TAG, "onPause");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume");

        EventManager.getInstance().removeEventByTag(appEventListener.getTag());
        EventManager.getInstance().addEventIfNotExist(appEventListener);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int lastPage = 0;
            private int refreshContactsInterval = 4000;
            private long lastContactsRefresh = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                if (DEBUG) Log.v(TAG, "onPageScrolled");
            }

            @Override
            public void onPageSelected(int position) {
                if (DEBUG) Log.v(TAG, "onPageSelected, Pos: " + position + ", Last: " + lastPage);
//                adapter.getItem(position).refresh();
                // If the user leaves the profile page check tell the fragment to update index and metadata if needed.
                if (lastPage == 0)
//                    ((ProfileFragment) adapter.getItem(0)).updateProfileIfNeeded();
                    ((ProfileFragment) getFragment(0)).updateProfileIfNeeded();

             /*   if (position == PagerAdapterTabs.Contacts)
                {
                    if (System.currentTimeMillis() - lastContactsRefresh > refreshContactsInterval)
                    {
                        adapter.getItem(position).refreshOnBackground();
                        lastContactsRefresh = System.currentTimeMillis();
                    }
                }*/

                pageAdapterPos = position;

                lastPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                if (DEBUG) Log.v(TAG, "onPageScrollStateChanged");
            }
        });

        //region Trying to obtain invitable friends. NEED GAME PREMISSIONS!
/*        BFacebookManager.getInvitableFriendsList(new CompletionListenerWithData() {
            @Override
            public void onDone(Object o) {
                ArrayList<JSONObject> list = (ArrayList<JSONObject>) o;
                if (list != null)
                {
                    if (DEBUG) Log.d(TAG, "invitable friends list size: " + list.size());
                }
                else if (DEBUG) Log.e(TAG, "invitable Friends list is null");
            }

            @Override
            public void onDoneWithError() {

            }
        }); */
        //endregion
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (adapter != null)
        {
            BaseFragment pro = getFragment(0), conv = getFragment(3);

            if (conv!=null)
                conv.loadDataOnBackground();

            if (pro != null)
                pro.refresh();

//            adapter.getItem(PagerAdapterTabs.Conversations).refreshOnBackground();
//            adapter.getItem(PagerAdapterTabs.Profile).refresh();
        }
    }

    private AppEventListener appEventListener = new AppEventListener("MainActivity") {
        private final int uiUpdateDelay = 3000;
        private UIUpdater uiUpdaterDetailsChanged, uiUpdaterThreadDetailsChanged, uiUpdaterMessages;

        @Override
        public boolean onThreadDetailsChanged(final String threadId) {
            super.onThreadDetailsChanged(threadId);

            if (DEBUG) Log.v(TAG, "onThreadDetailsChanged");

            if (uiUpdaterThreadDetailsChanged != null)
                uiUpdaterThreadDetailsChanged.setKilled(true);

            handler.removeCallbacks(uiUpdaterThreadDetailsChanged);

            uiUpdaterThreadDetailsChanged = new UIUpdater(){

                @Override
                public void run() {
                    if (!isKilled())
                    {
                        BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, threadId);
                        if (DEBUG) Log.d(TAG, "Type: " + thread.getType());
                        DaoCore.printEntity(thread);

                        BaseFragment fragment;
                        if (thread.getType() == BThread.Type.Private)
                            fragment = getFragment(3);
//                            adapter.getItem(PagerAdapterTabs.Conversations).refreshOnBackground();
                        else
                            fragment = getFragment(1);
//                            adapter.getItem(PagerAdapterTabs.ChatRooms).refreshOnBackground();

                        if (fragment != null)
                            fragment.loadDataOnBackground();
                    }
                }
            };

            handler.postDelayed(uiUpdaterThreadDetailsChanged, uiUpdateDelay);

            return false;
        }

        @Override
        public boolean onUserAddedToThread(String threadId, String userId) {
            if (DEBUG) Log.v(TAG, "onUserAddedToThread");

//            BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, threadId);
//            if (DEBUG) Log.d(TAG, "Type: " + thread.getType());
//            DaoCore.printEntity(thread);
            return false;
        }

        @Override
        public boolean onUserDetailsChange(BUser user) {
            if (DEBUG) Log.v(TAG, "onUserDetailsChange");

            if (uiUpdaterDetailsChanged != null)
                uiUpdaterDetailsChanged.setKilled(true);

            handler.removeCallbacks(uiUpdaterDetailsChanged);

            uiUpdaterDetailsChanged = new UIUpdater(){
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "Run");
                    if (!isKilled())
                    {
                        if (DEBUG) Log.d(TAG, "Not killed");
                        BaseFragment contacs, conv;
                        contacs = getFragment(2);
                        conv = getFragment(3);

                        if (contacs!=null)
                            contacs.loadDataOnBackground();

                        if (conv!= null)
                            conv.loadDataOnBackground();
//                        adapter.getItem(PagerAdapterTabs.Contacts).refreshOnBackground();
//                        adapter.getItem(PagerAdapterTabs.Conversations).refreshOnBackground();
                    }
                }
            };

            handler.postDelayed(uiUpdaterDetailsChanged, uiUpdateDelay);

            return false;
        }

        @Override
        public boolean onMessageReceived(final BMessage message) {
            if (DEBUG) Log.v(TAG, "onMessageReceived");

            // Only notify for private threads.
            if (message.getBThreadOwner().getType() == BThread.Type.Public)
                return true;

            // Make sure the message that incoming is not the user message.
            if (message.getBUserSender().getEntityID().equals(
                    BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID()))
                return false;

            if (uiUpdaterMessages != null)
                uiUpdaterMessages.setKilled(true);

            handler.removeCallbacks(uiUpdaterMessages);

            uiUpdaterMessages = new UIUpdater(){

                @Override
                public void run() {
                    if (!isKilled())
                    {
                        // We check to see that the ChatActivity is not listening to this messages so we wont alert twice.
                        if (!EventManager.getInstance().isEventTagExist(ChatActivity.MessageListenerTAG + message.getOwnerThread())) {
/*
                            PendingIntent pendingIntent = NotificationUtils.existAlarm(MainActivity.this, PushUtils.MESSAGE_NOTIFICATION_ID);

                          if (pendingIntent != null)
                            {
                                if (DEBUG) Log.e(TAG, "Pending intent exist" + pendingIntent.toString());
                                pendingIntent.getIntentSender().
                            }*/

                            NotificationUtils.createMessageNotification(MainActivity.this, message);
                        }
                    }
                }
            };

            handler.postDelayed(uiUpdaterMessages, uiUpdateDelay);

            return false;
        }
    };

    abstract class UIUpdater implements Runnable{

        private boolean killed = false;

        public void setKilled(boolean killed) {
            this.killed = killed;
        }

        public boolean isKilled() {
            return killed;
        }
    }

    Handler handler = new Handler();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_ADAPTER_POS, pageAdapterPos);
    }

    private void initViews(){
        pager = (ViewPager) findViewById(R.id.pager);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        adapter = new PagerAdapterTabs(getSupportFragmentManager());

        pager.setAdapter(adapter);

        tabs.setViewPager(pager);

        pager.setOffscreenPageLimit(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_sdk, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.android_settings) {
//            DaoCore.printUsersData();
            EventManager.getInstance().printDataReport();
//            EventManager.getInstance().removeAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void firstTimeInApp(){
        //TODO handle no SDCARD!
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FIRST_TIME_IN_APP, true))
        {
            if (DEBUG) Log.d(TAG, "First time in app");
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                if (DEBUG) Log.d(TAG, "No SDCARD");
            } else {
                File directory = new File(Environment.getExternalStorageDirectory()+ File.separator+"AndroidChatSDK");
                if (DEBUG) Log.d(TAG, "Creating app directory");
                    directory.mkdirs();
            }

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(FIRST_TIME_IN_APP, false).apply();
        }
    }

    /*Facebook Stuff*/
/*    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };*/

/*    private void onSessionStateChange(Session session, final SessionState state, Exception exception){
        BFacebookManager.onSessionStateChange(session, state, exception, new CompletionListener() {
            @Override
            public void onDone() {
                if (DEBUG) Log.i(TAG, "onDone");
            }

            @Override
            public void onDoneWithError() {
                if (DEBUG) Log.e(TAG, "onDoneWithError");
                // Facebook session is closed so we need to disconnect from firebase.
                BNetworkManager.sharedManager().getNetworkAdapter().logout();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra(LoginActivity.FLAG_LOGGED_OUT, true);
                startActivity(intent);
            }
        });
    }*/

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(DEBUG) Log.v(TAG, "onActivityResult");
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }*/

    /* Exit Stuff*/
    @Override
    public void onBackPressed() {
        // Show alert dialog, Positive response is just dismiss the dialog, Negative will close the app.
        showAlertDialog("", getResources().getString(R.string.alert_exit), getResources().getString(R.string.exit),
                getResources().getString(R.string.stay), null, new CloseApp());
    }

    private void showAlertDialog(String title, String alert, String p, String n, final Callable neg, final Callable pos){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title if not null
        if (title != null && !title.equals(""))
            alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(alert)
                .setCancelable(false)
                .setPositiveButton(p, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (pos != null)
                            try {
                                pos.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(n, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        if (neg != null)
                            try {
                                neg.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    class CloseApp implements Callable{
        @Override
        public Object call() throws Exception {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return null;
        }
    }


    /** After screen orientation chage the getItem from the fragment page adapter is no null but it is not visible to the user
     *  so we have to use this workaround so when we call any method on the wanted fragment the fragment will respond.
     *  http://stackoverflow.com/a/7393477/2568492*/
    private BaseFragment getFragment(int index){
        return ((BaseFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + index));
    }
}
