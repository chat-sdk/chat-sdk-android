package com.braunster.chatsdk.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
import com.braunster.chatsdk.events.MessageEventListener;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.events.AppEventListener;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

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
    private UiLifecycleHelper uiHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_view_pager);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        firstTimeInApp();
        initViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();

//        EventManager.getInstance().addEventIfNotExist(new AppEventListener("MainActivity") {
//            private final int interval = 1000;
//            private long lastThreadDetailUpdate = 0;
//
//            @Override
//            public boolean onThreadAdded(String threadId) {
//                super.onThreadAdded(threadId);
//                if (System.currentTimeMillis() - lastThreadDetailUpdate > interval)
//                {
//                    lastThreadDetailUpdate = System.currentTimeMillis();
//                    return false;
//                }
//
//                BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, threadId);
//                if (DEBUG) Log.d(TAG, "Thread is added.");
//                if (thread.getType() == BThread.Type.Private)
//                    adapter.getItem(PagerAdapterTabs.Conversations).refresh();
//                else adapter.getItem(PagerAdapterTabs.ChatRooms).refresh();
//
//                threadId = null;
//                return false;
//            }
//
//            @Override
//            public boolean onUserDetailsChange(BUser user) {
//                super.onUserDetailsChange(user);
//
//                adapter.getItem(PagerAdapterTabs.Contacts).refresh();
//
//                user = null;
//                return false;
//            }
//
//            @Override
//            public boolean onMessageReceived(BMessage message) {
//                super.onMessageReceived(message);
//
//                // Make sure the message that incoming is not the user message.
//                if (message.getBUserSender().getEntityID().equals(
//                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getEntityID()))
//                    return false;
//
//                // We check to see that the ChatActivity is not listening to this messages so we wont alert twice.
//                if (!EventManager.getInstance().isTagExist(ChatActivity.MessageListenerTAG + message.getOwnerThread())) {
//                    String msgContent = message.getType() == BMessage.Type.bText.ordinal() ? message.getText() : message.getType() == BMessage.Type.bImage.ordinal() ? "Image" : "Location";
//
//                    Intent resultIntent = new Intent(MainActivity.this, ChatActivity.class);
//                    resultIntent.putExtra(ChatActivity.THREAD_ID, message.getOwnerThread());
//                    NotificationUtils.createAlertNotification(MainActivity.this, 2000, resultIntent,
//                            NotificationUtils.getDataBundle(message.getBUserSender().getMetaName() != null ? message.getBUserSender().getMetaName() : " ", "New message from " + message.getBUserSender().getMetaName(), msgContent));
//                }
//
//                message = null;
//                return false;
//            }
//        });

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
        });*/
        //endregion
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private void initViews(){
//        pager = (ViewPager) findViewById(R.id.pager);
//        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
//
//        adapter = new PagerAdapterTabs(getSupportFragmentManager());
//
//        pager.setAdapter(adapter);
//
//        tabs.setViewPager(pager);
//
//        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
////                if (DEBUG) Log.v(TAG, "onPageScrolled");
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                if (DEBUG) Log.v(TAG, "onPageSelected, Pos: " + position);
////                adapter.getItem(position).refresh();
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
////                if (DEBUG) Log.v(TAG, "onPageScrollStateChanged");
//            }
//        });
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
            DaoCore.printUsersData();
//            EventManager.getInstance().removeAllEvents();
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

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(FIRST_TIME_IN_APP, true).apply();
        }
    }

    /*Facebook Stuff*/
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, final SessionState state, Exception exception){
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
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(DEBUG) Log.v(TAG, "onActivityResult");
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

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
}
