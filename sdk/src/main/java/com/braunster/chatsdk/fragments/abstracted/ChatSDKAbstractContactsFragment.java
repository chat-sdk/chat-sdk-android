/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.fragments.abstracted;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
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
import com.braunster.chatsdk.Utils.helper.ChatSDKChatHelper;
import com.braunster.chatsdk.activities.ChatSDKChatActivity;
import com.braunster.chatsdk.activities.ChatSDKSearchActivity;
import com.braunster.chatsdk.adapter.ChatSDKUsersListAdapter;
import com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractUsersListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.fragments.ChatSDKBaseFragment;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.BatchedEvent;
import com.braunster.chatsdk.network.events.Event;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.Batcher;
import com.braunster.chatsdk.object.ChatSDKThreadPool;
import com.braunster.chatsdk.object.UIUpdater;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatSDKAbstractContactsFragment extends ChatSDKBaseFragment {

    private static final String TAG = ChatSDKAbstractContactsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ContactsFragment;

    /** Loading all the current user contacts.*/
    public static final int MODE_LOAD_FRIENDS = 1991;

    /** Loading all users for given thread id mode*/
    public static final int MODE_LOAD_THREAD_USERS = 1992;

    public static final int MODE_LOAD_FOLLOWERS = 1993;

    public static final int MODE_LOAD_FOLLOWS = 1994;

    /** Using the users that was given to the fragment in to initializer;*/
    public static final int MODE_USE_SOURCE = 1995;

    public static final int MODE_LOAD_FRIENDS_THAT_NOT_IN_THREAD = 1996;

    public static final int MODE_LOAD_ONLINE = 1997;

    /** When a user clicked he will be added to the current thread.*/
    public static final int CLICK_MODE_ADD_USER_TO_THREAD = 2991;
    /** Used for the share intent, When a user press on a user the attached data from the share intent will be sent to the selected user.*/
    public static final int CLICK_MODE_SHARE_CONTENT = 2992;
    /** Open profile activity when user is clicked.*/
    public static final int CLICK_MODE_SHOW_PROFILE = 2993;
    /** Nothing happen on list item click.*/
    public static final int CLICK_MODE_NONE = 2994;

    public static final String LOADING_MODE = "Loading_Mode";
    public static final String CLICK_MODE = "Click_Mode";
    public static final String EVENT_TAG = "EventTag";
    public static final String IS_DIALOG = "is_dialog";

    /** The text color that the adapter will use, Use -1 to set adapter to default color.*/
    protected int textColor = -1991;

    protected ChatSDKAbstractUsersListAdapter adapter;
    protected ProgressBar progressBar;
    protected ListView listView;

    private ContactListListener contactListListener;

    /** Users that will be used to fill the adapter, This could be set manually or it will be filled when loading users for
     * {@link #loadingMode}*/
    protected List<BUser> sourceUsers = null;

    /** Used when the fragment is shown as a dialog*/
    protected String title = "";

    @IntDef({MODE_LOAD_FRIENDS_THAT_NOT_IN_THREAD, MODE_LOAD_FRIENDS,
            MODE_LOAD_FOLLOWERS, MODE_LOAD_FOLLOWS, MODE_LOAD_ONLINE,
            MODE_LOAD_THREAD_USERS, MODE_USE_SOURCE})
    @Retention(RetentionPolicy.SOURCE)
    @interface LoadingMode{}

    @IntDef({CLICK_MODE_ADD_USER_TO_THREAD, CLICK_MODE_NONE, CLICK_MODE_SHARE_CONTENT, CLICK_MODE_SHOW_PROFILE})
    @Retention(RetentionPolicy.SOURCE)
    @interface ClickMode{}

    /** Determine which users will be loaded to this fragment.
     *
     * @see
     *  #MODE_LOAD_FRIENDS_THAT_NOT_IN_THREAD ,
     *  #MODE_LOAD_FRIENDS
     *  #MODE_LOAD_FOLLOWERS
     *  #MODE_LOAD_FOLLOWS
     *  #MODE_LOAD_THREAD_USERS
     *  #MODE_USE_SOURCE
     *  */
    protected @LoadingMode int loadingMode;

    /** Determine what happen after a user is clicked.
     *
     * @see
     * #CLICK_MODE_ADD_USER_TO_THREAD
     * #CLICK_MODE_SHARE_CONTENT
     * #CLICK_MODE_SHOW_PROFILE */
    protected @ClickMode int clickMode;

    /** Extra data for the loading mode/ click mode, for example this is used as thread id/entityID for loading mode {@link #CLICK_MODE_ADD_USER_TO_THREAD}
     *  Look in {@link #loadSourceUsers()} or in {@link #setListClickMode()} for more examples. */
    protected Object extraData ="";

    /** This is passed to the {@link com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractUsersListAdapter}, If true the list adapter will remove all duplicates.
     * @see com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractUsersListAdapter*/
    protected boolean removeDuplicates = true;

    /** This is passed to the list adapter, If true the list will be with headers.
     * @see com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractUsersListAdapter*/
    protected boolean withHeaders = false;

    /** If true the fragent will listen to users details change and updates.*/
    protected boolean withUpdates = true;

    /** Set to false if you dont want any menu item to be inflated for this fragment.
     *  This should be set before the fragment transaction,
     *  if you extends the fragment you can call it in {@link #onCreate(android.os.Bundle)}
     *  <B>see </B>{@link #setInflateMenu(boolean inflate)}*/
    protected boolean inflateMenu = true;

    protected String eventTAG;

    protected UIUpdater uiUpdater;

    private ChatSDKAbstractUsersListAdapter.ProfilePicClickListener profilePicClickListener;

    private AdapterView.OnItemClickListener onItemClickListener;

    /** When isDialog = true the dialog will always show the list of users given to him or pulled by the thread id.*/
    private boolean isDialog = false;

    public void setEventTAG(String eventTAG) {
        this.eventTAG = eventTAG;
    }

    public void setDialog(){
        this.isDialog = true;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setLoadingMode(@LoadingMode  int loadingMode){
        this.loadingMode = loadingMode;
    }

    public void setExtraData(Object extraData){
        this.extraData = extraData;
    }

    public void setClickMode(@ClickMode int clickMode) {
        this.clickMode = clickMode;
    }

    public void setSourceUsers(List<BUser> sourceUsers) {
        this.sourceUsers = sourceUsers;
    }

    @SuppressWarnings("ResourceType")
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
        if (isDialog)
        {
            if(title.equals(""))
                getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            else getDialog().setTitle(title);
        }

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

        // Create the adpater only if null,
        // This is here so we wont override the adapter given from the extended class with setAdapter.
        if (adapter == null)
            adapter = new ChatSDKUsersListAdapter(getActivity());

        adapter.setProfilePicClickListener(profilePicClickListener);

        setTextColor(textColor);

        listView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (!inflateMenu)
            return;

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
            Intent intent = new Intent(getActivity(), chatSDKUiHelper.searchActivity);

            startActivityForResult(intent, ChatSDKSearchActivity.GET_CONTACTS_ADDED_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loadData(){
        if (DEBUG) Timber.v("loadData");

        if (mainView == null || getActivity() == null)
            return;

        loadSourceUsers();

        if (BNetworkManager.sharedManager().getNetworkAdapter() != null)
        {
            adapter.setUserItems(adapter.makeList(sourceUsers, withHeaders, removeDuplicates));

            setListClickMode();
        }

    }

    @Override
    public void loadDataOnBackground(){

        if (DEBUG) Timber.v("loadDataOnBackground, eventTag: %s", eventTAG);

        if (mainView == null || getActivity() == null)
        {
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
            if (adapter != null && adapter.getUserItems().size() < 2)
            {
                showLoading();
            }
        }

        uiUpdater = new UIUpdater() {
            @Override
            public void run() {

                if (isKilled())
                {
                    return;
                }

                loadSourceUsers();

                Message message = new Message();
                message.what = 1;
                List<ChatSDKAbstractUsersListAdapter.AbstractUserListItem> items = adapter.makeList(sourceUsers, withHeaders, removeDuplicates);

                message.obj = items;

                handler.sendMessage(message);

                uiUpdater = null;
            }
        };

        ChatSDKThreadPool.getInstance().scheduleExecute(uiUpdater, isFirst ? 0 : loadingMode == MODE_LOAD_FRIENDS ?  4 : 0);
    }

    @Override
    public void clearData() {
        if (adapter != null)
        {
            if (uiUpdater != null)
                uiUpdater.setKilled(true);

            adapter.getUserItems().clear();
            adapter.notifyDataSetChanged();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (DEBUG) Timber.v("handleMessage");

            switch (msg.what)
            {
                case 1:
                    adapter.setUserItems(((List) msg.obj));

                    if (progressBar.getVisibility() == View.VISIBLE)
                    {
                        hideLoading();
                    }

                    setListClickMode();
            }
        }
    };

    private void setListClickMode(){

        if (onItemClickListener!=null)
        {
            listView.setOnItemClickListener(onItemClickListener);
        }
        else
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (adapter.getItem(position).getType() == ChatSDKUsersListAdapter.TYPE_HEADER)
                        return;

                    final BUser clickedUser = DaoCore.fetchEntityWithEntityID(BUser.class, adapter.getItem(position).getEntityID());

                    switch (clickMode)
                    {
                        case CLICK_MODE_ADD_USER_TO_THREAD:
                            BThread thread;

                            if (extraData instanceof Long)
                                thread = DaoCore.fetchEntityWithProperty(BThread.class, BThreadDao.Properties.Id, extraData);
                            else
                                thread = DaoCore.fetchEntityWithEntityID(BThread.class, extraData);

                            getNetworkAdapter().addUsersToThread(thread, clickedUser)
                                    .done(new DoneCallback<BThread>() {
                                        @Override
                                        public void onDone(BThread thread) {
                                            showToast( getString(R.string.abstract_contact_fragment_user_added_to_thread_toast_success)   + clickedUser.getName());

                                            if (contactListListener!= null)
                                                contactListListener.onContactClicked(clickedUser);

                                            if (isDialog)
                                            {
                                                getDialog().dismiss();
                                            }
                                        }
                                    })
                                    .fail(new FailCallback<BError>() {
                                        @Override
                                        public void onFail(BError error) {
                                            chatSDKUiHelper.showAlertToast( getString(R.string.abstract_contact_fragment_user_added_to_thread_toast_fail) );
                                        }
                                    });
                            break;

                        case CLICK_MODE_SHARE_CONTENT:
                            createAndOpenThreadWithUsers(clickedUser.getName(), clickedUser, getNetworkAdapter().currentUserModel())
                                    .done(new DoneCallback<BThread>() {
                                        @Override
                                        public void onDone(BThread thread) {
                                            Intent intent = new Intent(getActivity(), chatSDKUiHelper.chatActivity);
                                            intent.putExtra(ChatSDKChatActivity.THREAD_ID, thread.getId());

                                            // Checking the kind of the instace data
                                            // Uri is used for images
                                            if (extraData instanceof Uri)
                                                intent.putExtra(ChatSDKChatHelper.SHARED_FILE_URI, ((Uri) extraData));
                                                // String is for text.
                                            else if (extraData instanceof  String)
                                                intent.putExtra(ChatSDKChatHelper.SHARED_TEXT, ((String) extraData));
                                            else
                                            {
                                                showToast(getString(R.string.abstract_contact_fragment_share_with_contact_toast_fail_unknown_type));
                                                return;
                                            }

                                            if (contactListListener!= null)
                                                contactListListener.onContactClicked(clickedUser);

                                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            startActivity(intent);
                                        }
                                    })
                                    .fail(new FailCallback<BError>() {
                                        @Override
                                        public void onFail(BError error) {

                                        }
                                    });
                            break;

                        case CLICK_MODE_SHOW_PROFILE:
                            chatSDKUiHelper.startProfileActivity(clickedUser.getId());
                            break;

                        case CLICK_MODE_NONE:

                            break;

                        default:
                            createAndOpenThreadWithUsers(clickedUser.getName(), clickedUser, getNetworkAdapter().currentUserModel())
                                .done(new DoneCallback<BThread>() {
                                    @Override
                                    public void onDone(BThread thread) {
                                        if (contactListListener != null)
                                            contactListListener.onContactClicked(clickedUser);

                                        // This listener is used only because that if we dismiss the dialog before the thread creation has been done
                                        // The contact dialog could not open the new chat activity because getActivity() will be null.
                                        if (isDialog)
                                            getDialog().dismiss();
                                    }
                                })
                                .fail(new FailCallback<BError>() {
                                    @Override
                                    public void onFail(BError error) {

                                    }
                                });
                    }
                }
            });

    }

    public void filterListStartWith(String filter){
        adapter.filterStartWith(filter);
    }

    private void loadSourceUsers(){


        if (DEBUG) Timber.v("loadSourceUsers, Loading mode: %s", loadingMode);

        if (loadingMode!=MODE_USE_SOURCE)
            // If this is not a dialog we will load the contacts of the user.
            switch (loadingMode) {


                case MODE_LOAD_FRIENDS:
                    sourceUsers = getNetworkAdapter().friends();
                    break;

                case MODE_LOAD_THREAD_USERS:
                    if (DEBUG) Timber.d("Mode - Thread Users");
                    BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, extraData);

                    // Remove the current user from the list.
                    List<BUser> users = thread.getUsers();
                    users.remove(getNetworkAdapter().currentUserModel());

                    sourceUsers = users;
                    break;

                case MODE_LOAD_FRIENDS_THAT_NOT_IN_THREAD:
                    List<BUser> users1 = getNetworkAdapter().friends();
                    thread = DaoCore.fetchEntityWithProperty(BThread.class, BThreadDao.Properties.Id, extraData);

                    if (thread == null)
                    {
                        sourceUsers = new ArrayList<>();

                        Timber.e("MODE_LOAD_FRIENDS_THAT_NOT_IN_THREAD, Thread is null");
                    }
                    else
                    {
                        List<BUser> threadUser = thread.getUsers();
                        users1.removeAll(threadUser);
                        sourceUsers = users1;
                    }
                    break;

                case MODE_LOAD_ONLINE:

                    sourceUsers = getNetworkAdapter().onlineUsers();

                    sourceUsers.remove(getNetworkAdapter().currentUserModel());

                    break;

                case MODE_LOAD_FOLLOWERS:

                    sourceUsers = getNetworkAdapter().followers();

                    sourceUsers.remove(getNetworkAdapter().currentUserModel());

                    break;
            }
    }

    public void showLoading(){
        if (DEBUG) Timber.v("showLoading, tag: %s", eventTAG);
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.INVISIBLE);
    }

    public void hideLoading(){
        if (DEBUG) Timber.v("hideLoading, tag: %s", eventTAG);
        progressBar.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (withUpdates)
        {
            BatchedEvent userDetailsBatch = new BatchedEvent(eventTAG, "", handler);
            userDetailsBatch.setBatchedAction(Event.Type.UserDetailsEvent, 1000, new Batcher.BatchedAction<String>() {
                @Override
                public void triggered(List<String> list) {
                    loadDataOnBackground();
                }
            });

            userDetailsBatch.setBatchedAction(Event.Type.OnlineChangeEvent, 1000, new Batcher.BatchedAction<String>() {
                @Override
                public void triggered(List<String> list) {
                    Timber.i("online users updated.");
                    loadDataOnBackground();
                }
            });

            if (loadingMode == MODE_LOAD_FRIENDS){

                userDetailsBatch.setBatchedAction(Event.Type.FriendsChangeEvent, 100, new Batcher.BatchedAction<String>() {
                    @Override
                    public void triggered(List<String> list) {
                        Timber.i("friends updated.");
                        loadDataOnBackground();
                    }
                });
            }

            if (StringUtils.isNotEmpty(eventTAG))
            {
                getNetworkAdapter().getEventManager().removeEventByTag(eventTAG);
                getNetworkAdapter().getEventManager().addEvent(userDetailsBatch);
            }
            else
                if (DEBUG) Timber.e("Event tag is empty");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ChatSDKSearchActivity.GET_CONTACTS_ADDED_REQUEST)
            if (resultCode == Activity.RESULT_OK)
            {
                loadDataOnBackground();
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getNetworkAdapter().getEventManager().removeEventByTag(eventTAG);
    }

    public void setAdapter(ChatSDKAbstractUsersListAdapter adapter) {
        this.adapter = adapter;
    }













    public void setContactListListener(ContactListListener contactListListener) {
        this.contactListListener = contactListListener;
    }

    public interface ContactListListener{
        public void onContactClicked(BUser user);
    }

    public void setRemoveDuplicates(boolean removeDuplicates) {
        this.removeDuplicates = removeDuplicates;
    }

    public void setWithHeaders(boolean withHeaders) {
        this.withHeaders = withHeaders;
    }

    public void setInflateMenu(boolean inflateMenu) {
        this.inflateMenu = inflateMenu;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;

        if (adapter!=null)
        {
            adapter.setTextColor(textColor);
            adapter.notifyDataSetChanged();
        }
    }

    public ChatSDKAbstractUsersListAdapter getAdapter() {
        return adapter;
    }

    public void setProfilePicClickListener(ChatSDKAbstractUsersListAdapter.ProfilePicClickListener profilePicClickListener){
        this.profilePicClickListener = profilePicClickListener;
        if (adapter!=null)
        {
            adapter.setProfilePicClickListener(profilePicClickListener);
            adapter.notifyDataSetChanged();
        }
    }

    public void withUpdates(boolean withUpdates) {
        this.withUpdates = withUpdates;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
