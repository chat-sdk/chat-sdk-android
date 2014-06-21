package com.braunster.chatsdk.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.adapter.PagerAdapterTabs;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BFacebookManager;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
        pager = (ViewPager) findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        adapter = new PagerAdapterTabs(getSupportFragmentManager());

        pager.setAdapter(adapter);

        tabs.setViewPager(pager);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                if (DEBUG) Log.v(TAG, "onPageScrolled");
            }

            @Override
            public void onPageSelected(int position) {
                if (DEBUG) Log.v(TAG, "onPageSelected, Pos: " + position);
                adapter.getItem(position).onRefresh();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                if (DEBUG) Log.v(TAG, "onPageScrollStateChanged");
            }
        });
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
            DaoCore.clearTestData();
            DaoCore.createTestData();
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
