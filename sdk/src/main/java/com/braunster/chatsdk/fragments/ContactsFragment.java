package com.braunster.chatsdk.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.activities.ChatActivity;
import com.braunster.chatsdk.activities.SearchActivity;
import com.braunster.chatsdk.adapter.UsersWithStatusListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.BatchedEvent;
import com.braunster.chatsdk.network.events.Event;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.Batcher;
import com.braunster.chatsdk.object.ChatSDKThreadPool;
import com.braunster.chatsdk.object.UIUpdater;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class ContactsFragment extends BaseFragment {

    private static final String TAG = ContactsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ContactsFragment;

    /** Loading all the current user contacts.*/
    public static final int MODE_LOAD_CONTACTS = 1991;
    /** Loading all users for given thread id mode*/
    public static final int MODE_LOAD_THREAD_USERS = 1992;

    /** When a user clicked he will be added to the current thread.*/
    public static final int CLICK_MODE_ADD_USER_TO_THREAD = 2991;
    /** Used for the share intent, When a user press on a user the attached data from the share intent will be sent to the selected user.*/
    public static final int CLICK_MODE_SHARE_CONTENT = 2992;

    public static final String LOADING_MODE = "Loading_Mode";
    public static final String CLICK_MODE = "Click_Mode";
    public static final String EVENT_TAG = "EventTag";
    public static final String IS_DIALOG = "is_dialog";

    private UsersWithStatusListAdapter adapter;
    private ProgressBar progressBar;
    private ListView listView;

    private List<BUser> sourceUsers = null;
    private String title = "";
    private int loadingMode, clickMode;
    private Object extraData ="";
    private String eventTAG;
    private UIUpdater uiUpdater;

    /** When isDialog = true the dialog will always show the list of users given to him or pulled by the thread id.*/
    private boolean isDialog = false;

    /* Initializers.*/
    public static ContactsFragment newInstance(String eventTAG) {
        ContactsFragment f = new ContactsFragment();
        f.setLoadingMode(MODE_LOAD_CONTACTS);
        f.setEventTAG(eventTAG);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ContactsFragment newInstance(int loadingMode, int clickMode, Object extraData) {
        ContactsFragment f = new ContactsFragment();
        f.setLoadingMode(loadingMode);
        f.setClickMode(clickMode);
        f.setExtraData(extraData);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ContactsFragment newDialogInstance(int mode, String extraData, String title) {
        ContactsFragment f = new ContactsFragment();
        f.setDialog();
        f.setTitle(title);
        f.setExtraData(extraData);
        f.setLoadingMode(mode);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    /** Creates a new contact dialog.
     * @param threadID - The id of the thread that his users is the want you want to show.
     * @param title - The title of the dialog.
     * @param withUpdates - the dialog will listen to user details changes.*/
    public static ContactsFragment newThreadUsersDialogInstance(String threadID, String title, boolean withUpdates) {
        ContactsFragment f = new ContactsFragment();
        f.setTitle(title);

        if (withUpdates)
            f.setEventTAG(DaoCore.generateEntity());

        f.setLoadingMode(MODE_LOAD_THREAD_USERS);
        f.setDialog();
        f.setExtraData(threadID);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ContactsFragment newDialogInstance(int loadingMode, int clickMode, String title, Object extraData) {
        ContactsFragment f = new ContactsFragment();
        f.setDialog();
        f.setLoadingMode(loadingMode);
        f.setExtraData(extraData);
        f.setClickMode(clickMode);
        f.setTitle(title);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }


    public void setEventTAG(String eventTAG) {
        this.eventTAG = eventTAG;
    }

    private void setDialog(){
        isDialog = true;
    }

    private void setTitle(String title){
        this.title = title;
    }

    private void setLoadingMode(int loadingMode){
        this.loadingMode = loadingMode;
    }

    private void setExtraData(Object extraData){
        this.extraData = extraData;
    }

    public void setClickMode(int clickMode) {
        this.clickMode = clickMode;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            loadingMode = savedInstanceState.getInt(LOADING_MODE);
            clickMode = savedInstanceState.getInt(CLICK_MODE);
            isDialog = savedInstanceState.getBoolean(IS_DIALOG);
            eventTAG = savedInstanceState.getString(EVENT_TAG);
        }

        if (!isDialog) {
            setHasOptionsMenu(true);
            setRetainInstance(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreateView");

        if (isDialog)
        {
            if(title.equals(""))
                getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            else getDialog().setTitle(title);
        }

        mainView = inflater.inflate(R.layout.chat_sdk_fragment_contacts, null);

        initViews();
        initToast();

        loadDataOnBackground();

        return mainView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LOADING_MODE, loadingMode);
        outState.putBoolean(IS_DIALOG, isDialog);
        outState.putString(EVENT_TAG, eventTAG);
    }

    @Override
    public void initViews(){
        listView = (ListView) mainView.findViewById(R.id.chat_sdk_list_contacts);

        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progressbar);

        adapter = new UsersWithStatusListAdapter(getActivity());
        listView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, "Add Chat");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_plus);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        // Each user that will be found in the search activity will be automatically added as a contact.
        if (id == R.id.action_chat_sdk_add)
        {
            Intent intent = new Intent(getActivity(), SearchActivity.class);

            startActivityForResult(intent, SearchActivity.GET_CONTACTS_ADDED_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loadData(){
        if (DEBUG) Log.v(TAG, "loadData");

        if (mainView == null || getActivity() == null)
            return;

        // If this is not a dialog we will load the contacts of the user.
        switch (loadingMode)
        {
            case MODE_LOAD_CONTACTS:
                if (DEBUG) Log.d(TAG, "Mode - Contacts");
                sourceUsers = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getContacts();
                break;

            case MODE_LOAD_THREAD_USERS:
                if (DEBUG) Log.d(TAG, "Mode - Thread Users");
                BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, extraData);

                // Remove the current user from the list.
                List<BUser> users = thread.getUsers();
                users.remove(BNetworkManager.sharedManager().getNetworkAdapter().currentUser());

                sourceUsers = users;
                break;
        }

        if (BNetworkManager.sharedManager().getNetworkAdapter() != null)
        {
            adapter.setListData(UsersWithStatusListAdapter.makeList(sourceUsers, true, true));
            setList();
        }
        else if (DEBUG) Log.e(TAG, "NetworkAdapter is null");
    }

    @Override
    public void loadDataOnBackground(){
        if (DEBUG) Log.v(TAG, "loadDataOnBackground");

        if (mainView == null || getActivity() == null)
        {
            if (DEBUG) Log.e(TAG, "main view or activity is null");
            return;
        }

        final boolean isFirst;
        if (uiUpdater != null)
        {
            isFirst = false;
            uiUpdater.setKilled(true);
            ChatSDKThreadPool.getInstance().removeSchedule(uiUpdater);
        }
        else
        {
            isFirst = true;
            if (adapter != null && adapter.getListData().size() < 2)
            {
                progressBar.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
            }
        }

        uiUpdater = new UIUpdater() {
            @Override
            public void run() {

                if (isKilled())
                {
                    if (DEBUG) Log.v(TAG, "Killed");
                    return;
                }

                // If this is not a dialog we will load the contacts of the user.
                switch (loadingMode) {
                    case MODE_LOAD_CONTACTS:
                        if (DEBUG) Log.d(TAG, "Mode - Contacts");
                        sourceUsers = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getContacts();
                        break;

                    case MODE_LOAD_THREAD_USERS:
                        if (DEBUG) Log.d(TAG, "Mode - Thread Users");
                        BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, extraData);

                        // Remove the current user from the list.
                        List<BUser> users = thread.getUsers();
                        users.remove(BNetworkManager.sharedManager().getNetworkAdapter().currentUser());

                        sourceUsers = users;
                        break;
                }

                Message message = new Message();
                message.what = 1;
                message.obj = UsersWithStatusListAdapter.makeList(sourceUsers, true, true);

                handler.sendMessage(message);

                uiUpdater = null;
            }
        };

        ChatSDKThreadPool.getInstance().scheduleExecute(uiUpdater, isFirst ? 0 : loadingMode == MODE_LOAD_CONTACTS ?  4 : 0);
    }

    @Override
    public void clearData() {
        if (DEBUG) Log.d(TAG, "ClearData");
        if (adapter != null)
        {
            if (uiUpdater != null)
                uiUpdater.setKilled(true);

            adapter.getListData().clear();
            adapter.notifyDataSetChanged();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case 1:
                    adapter.setListData(((List<UsersWithStatusListAdapter.UserListItem>) msg.obj));

                    if (progressBar.getVisibility() == View.VISIBLE)
                    {
                        progressBar.setVisibility(View.INVISIBLE);
                        listView.setVisibility(View.VISIBLE);
                    }
                    setList();
            }
        }
    };

    private void setList(){
        if (DEBUG) Log.v(TAG, "Set List, " + ( adapter == null ? "Adapter is null" : adapter.getListData().size()));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (adapter.getItem(position).getType() == UsersWithStatusListAdapter.TYPE_HEADER)
                    return;

                final BUser clickedUser = DaoCore.fetchEntityWithEntityID(BUser.class, adapter.getItem(position).getEntityID());

                switch (clickMode)
                {
                    case CLICK_MODE_ADD_USER_TO_THREAD:
                        BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, extraData);
                        BNetworkManager.sharedManager().getNetworkAdapter().addUsersToThread(thread, new RepetitiveCompletionListenerWithError<BUser, Object>() {
                            @Override
                            public boolean onItem(BUser user) {
                                showToast("User added to thread, Name: "  + clickedUser.getMetaName());
                                if (isDialog)
                                    getDialog().dismiss();
                                return false;
                            }

                            @Override
                            public void onDone() {

                            }

                            @Override
                            public void onItemError(BUser user, Object o) {

                            }
                        }, clickedUser);
                        break;

                    case CLICK_MODE_SHARE_CONTENT:
                        createAndOpenThreadWithUsers(clickedUser.getMetaName(), new CompletionListenerWithData<BThread>() {
                            @Override
                            public void onDone(BThread thread) {
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra(ChatActivity.THREAD_ID, thread.getId());

                                // Checking the kind of the instace data
                                // Uri is used for images
                                if (extraData instanceof Uri)
                                    intent.putExtra(ChatActivity.SHARED_FILE_URI, ((Uri) extraData));
                                    // String is for text.
                                else if (extraData instanceof  String)
                                    intent.putExtra(ChatActivity.SHARED_TEXT, ((String) extraData));
                                else
                                {
                                    showToast("Problem sharing contact, Unknown share type.");
                                    return;
                                }

                                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);
                            }

                            @Override
                            public void onDoneWithError(BError error) {

                            }
                        }, clickedUser, BNetworkManager.sharedManager().getNetworkAdapter().currentUser());
                        break;

                    default:
                        createAndOpenThreadWithUsers(clickedUser.getMetaName(), new CompletionListenerWithData<BThread>() {
                            @Override
                            public void onDone(BThread  thread) {
                                // This listener is used only because that if we dismiss the dialog before the thread creation has been done
                                // The contact dialog could not open the new chat activity because getActivity() will be null.
                                if (isDialog)
                                    getDialog().dismiss();
                            }

                            @Override
                            public void onDoneWithError(BError error) {

                            }
                        }, clickedUser, BNetworkManager.sharedManager().getNetworkAdapter().currentUser());
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getItem(position).getType() == UsersWithStatusListAdapter.TYPE_HEADER)
                    return true;

//                            showAlertDialog("", getResources().getString(R.string.alert_delete_contact), getResources().getString(R.string.delete),
//                            getResources().getString(R.string.cancel), null, new DeleteContact(adapter.getItem(position).getEntityID()));
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume, TAG: " + eventTAG);

        BatchedEvent userDetailsBatch = new BatchedEvent(eventTAG, "", Event.Type.UserEvent, handler);
        userDetailsBatch.setBatchedAction(Event.Type.UserEvent, 1000, new Batcher.BatchedAction<String>() {
            @Override
            public void triggered(List<String> list) {
                if (DEBUG) Log.d(TAG, "OnUserDetailsChanged");
                loadDataOnBackground();
            }
        });

        if (StringUtils.isNotEmpty(eventTAG))
        {
            EventManager.getInstance().addAppEvent(userDetailsBatch);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SearchActivity.GET_CONTACTS_ADDED_REQUEST)
            if (resultCode == Activity.RESULT_OK)
            {
                loadDataOnBackground();
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeEventByTag(eventTAG);
    }
}
