/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BThreadDao;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.chat.ChatHelper;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.Activities.SearchActivity;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import co.chatsdk.ui.Adapters.UsersListAdapter;
import co.chatsdk.ui.Adapters.AbstractUsersListAdapter;
import co.chatsdk.core.dao.DaoCore;

import java.util.List;

import timber.log.Timber;

/**
 * Created by itzik on 6/17/2014.
 */
public class AbstractContactsFragment extends BaseFragment {

    private static final String TAG = AbstractContactsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ContactsFragment;

    /** Loading all the current user contacts.*/
    public static final int MODE_LOAD_CONTACTS = 1991;

    /** Loading all users for given thread id mode*/
    public static final int MODE_LOAD_THREAD_USERS = 1992;

    public static final int MODE_LOAD_FOLLOWERS = 1993;

    public static final int MODE_LOAD_FOLLOWS = 1994;

    /** Using the users that was given to the fragment in to initializer;*/
    public static final int MODE_USE_SOURCE = 1995;

    public static final int MODE_LOAD_CONTACT_THAT_NOT_IN_THREAD = 1996;

    /** When a user clicked he will be added to the current thread.*/
    public static final int CLICK_MODE_ADD_USER_TO_THREAD = 2991;
    /** Used for the share intent, When a user press on a user the attached bundle from the share intent will be sent to the selected user.*/
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

    protected AbstractUsersListAdapter adapter;
    protected ProgressBar progressBar;
    protected ListView listView;

    private ContactListListener contactListListener;

    /** Users that will be used to fill the adapter, This could be set manually or it will be filled when loading users for
     * {@link #loadingMode}*/
    protected List<BUser> sourceUsers = null;

    /** Used when the fragment is shown as a dialog*/
    protected String title = "";

    /** Determine which users will be loaded to this fragment.
     *
     * @see
     *  #MODE_LOAD_CONTACT_THAT_NOT_IN_THREAD,
     *  #MODE_LOAD_CONTACTS
     *  #MODE_LOAD_FOLLOWERS
     *  #MODE_LOAD_FOLLOWS
     *  #MODE_LOAD_THREAD_USERS
     *  #MODE_USE_SOURCE
     *  */
    protected int loadingMode;

    /** Determine what happen after a user is clicked.
     *
     * @see
     * #CLICK_MODE_ADD_USER_TO_THREAD
     * #CLICK_MODE_SHARE_CONTENT
     * #CLICK_MODE_SHOW_PROFILE */
    protected int clickMode;

    /** Extra bundle for the loading mode/ click mode, for example this is used as thread id/entityID for loading mode {@link #CLICK_MODE_ADD_USER_TO_THREAD}
     *  Look in {@link #loadSourceUsers()} or in {@link #setListClickMode()} for more examples. */
    protected Object extraData ="";

    /** This is passed to the {@link AbstractUsersListAdapter}, If true the list adapter will remove all duplicates.
     * @see AbstractUsersListAdapter */
    protected boolean removeDuplicates = true;

    /** This is passed to the list adapter, If true the list will be with headers.
     * @see AbstractUsersListAdapter */
    protected boolean withHeaders = false;

    /** If true the fragent will listen to users details change and updates.*/
    protected boolean withUpdates = true;

    /** Set to false if you dont want any menu item to be inflated for this fragment.
     *  This should be set before the fragment transaction,
     *  if you extends the fragment you can call it in {@link #onCreate(android.os.Bundle)}
     *  <B>see </B>{@link #setInflateMenu(boolean inflate)}*/
    protected boolean inflateMenu = true;

    private AbstractUsersListAdapter.ProfilePicClickListener profilePicClickListener;

    private AdapterView.OnItemClickListener onItemClickListener;

    /** When isDialog = true the dialog will always show the list of users given to him or pulled by the thread id.*/
    private boolean isDialog = false;

    public void setDialog(){
        this.isDialog = true;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setLoadingMode(int loadingMode){
        this.loadingMode = loadingMode;
    }

    public void setExtraData(Object extraData){
        this.extraData = extraData;
    }

    public void setClickMode(int clickMode) {
        this.clickMode = clickMode;
    }

    public void setSourceUsers(List<BUser> sourceUsers) {
        this.sourceUsers = sourceUsers;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            loadingMode = savedInstanceState.getInt(LOADING_MODE);
            clickMode = savedInstanceState.getInt(CLICK_MODE);
            isDialog = savedInstanceState.getBoolean(IS_DIALOG);
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
    }

    @Override
    public void initViews(){
        listView = (ListView) mainView.findViewById(R.id.chat_sdk_list_contacts);

        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progressbar);

        // Create the adpater only if null,
        // This is here so we wont override the adapter given from the extended class with setAdapter.
        if (adapter == null)
            adapter = new UsersListAdapter((AppCompatActivity) getActivity());

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
            Intent intent = new Intent(getActivity(), chatSDKUiHelper.getSearchActivity());

            startActivityForResult(intent, SearchActivity.GET_CONTACTS_ADDED_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loadData(){

        if (mainView == null || getActivity() == null)
            return;

        loadSourceUsers();

        if (NM.currentUser() != null)
        {
            adapter.setUserItems(adapter.makeList(sourceUsers, withHeaders, removeDuplicates));

            setListClickMode();
        }

    }

    @Override
    public void clearData() {
        if (adapter != null)
        {
            adapter.getUserItems().clear();
            adapter.notifyDataSetChanged();
        }
    }

    private void setListClickMode () {

        if (onItemClickListener!=null)
        {
            listView.setOnItemClickListener(onItemClickListener);
        }
        else {

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (adapter.getItem(position).getType() == UsersListAdapter.TYPE_HEADER)
                        return;

                    final BUser clickedUser = DaoCore.fetchEntityWithEntityID(BUser.class, adapter.getItem(position).getEntityID());
                    final BUser currentUser = NM.currentUser();

                    switch (clickMode) {
                        case CLICK_MODE_ADD_USER_TO_THREAD:
                            BThread thread;

                            if (extraData instanceof Long) {
                                thread = DaoCore.fetchEntityWithProperty(BThread.class, BThreadDao.Properties.Id, extraData);
                            } else {
                                thread = DaoCore.fetchEntityWithEntityID(BThread.class, extraData);
                            }

                            NM.thread().addUsersToThread(thread, clickedUser)
                                    .doOnComplete(new Action() {
                                        @Override
                                        public void run() throws Exception {
                                            showToast(getString(R.string.abstract_contact_fragment_user_added_to_thread_toast_success) + clickedUser.getMetaName());

                                            if (contactListListener != null)
                                                contactListListener.onContactClicked(clickedUser);

                                            if (isDialog) {
                                                getDialog().dismiss();
                                            }

                                        }
                                    })
                                    .doOnError(new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            chatSDKUiHelper.showToast(getString(R.string.abstract_contact_fragment_user_added_to_thread_toast_fail));
                                        }
                                    }).subscribe();

                            break;

                        case CLICK_MODE_SHARE_CONTENT:
                            createAndOpenThreadWithUsers(clickedUser.getMetaName(), clickedUser, currentUser)
                                    .doOnSuccess(new Consumer<BThread>() {
                                        @Override
                                        public void accept(final BThread thread) throws Exception {
                                            Intent intent = new Intent(getActivity(), chatSDKUiHelper.getChatActivity());
                                            intent.putExtra(ChatActivity.THREAD_ID, thread.getId());

                                            // Checking the kind of the instace bundle
                                            // Uri is used for images
                                            if (extraData instanceof Uri)
                                                intent.putExtra(ChatHelper.SHARED_FILE_URI, ((Uri) extraData));
                                                // String is for text.
                                            else if (extraData instanceof String)
                                                intent.putExtra(ChatHelper.SHARED_TEXT, ((String) extraData));
                                            else {
                                                showToast(getString(R.string.abstract_contact_fragment_share_with_contact_toast_fail_unknown_type));
                                                return;
                                            }

                                            if (contactListListener != null)
                                                contactListListener.onContactClicked(clickedUser);

                                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            startActivity(intent);
                                        }
                                    }).subscribe();
                            break;

                        case CLICK_MODE_NONE:

                            break;

                        default:
                            createAndOpenThreadWithUsers(clickedUser.getMetaName(), clickedUser, currentUser)
                                    .doOnSuccess(new Consumer<BThread>() {
                                        @Override
                                        public void accept(BThread bThread) throws Exception {
                                            if (contactListListener != null)
                                                contactListListener.onContactClicked(clickedUser);

                                            // This listener is used only because that if we dismiss the dialog before the thread creation has been done
                                            // The contact dialog could not open the new chat activity because getActivity() will be null.
                                            if (isDialog)
                                                getDialog().dismiss();

                                        }
                                    }).subscribe();
                    }
                }
            });
        }

    }

    public void filterListStartWith(String filter){
        adapter.filterStartWith(filter);
    }

    private void loadSourceUsers () {
        if (loadingMode != MODE_USE_SOURCE)
            // If this is not a dialog we will load the contacts of the user.
            switch (loadingMode) {
                case MODE_LOAD_CONTACTS:
                    if (DEBUG) Timber.d("Mode - Contacts");
                    sourceUsers = NM.currentUser().getContacts();
                    break;

                case MODE_LOAD_THREAD_USERS:
                    if (DEBUG) Timber.d("Mode - CoreThread Users");
                    BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, extraData);

                    // Remove the current user from the list.
                    List<BUser> users = thread.getUsers();
                    users.remove(NM.currentUser());

                    sourceUsers = users;
                    break;

                case MODE_LOAD_CONTACT_THAT_NOT_IN_THREAD:
                    List<BUser> users1 = NM.currentUser().getContacts();
                    thread = DaoCore.fetchEntityWithProperty(BThread.class, BThreadDao.Properties.Id, extraData);
                    List<BUser> threadUser = thread.getUsers();
                    users1.removeAll(threadUser);
                    sourceUsers = users1;
                    break;

//                case MODE_LOAD_FOLLOWERS:
//                    if (extraData instanceof String && StringUtils.isNotEmpty((String) extraData))
//                    {
//                        sourceUsers = new ArrayList<BUser>();
//                        showLoading();
//                        BNetworkManager.getCoreInterface().getFollowers((String) extraData)
//                                .subscribe(new Observer<BUser>() {
//                                    @Override
//                                    public void onSubscribe(Disposable d) {
//                                    }
//
//                                    @Override
//                                    public void onNext(BUser value) {
//                                        if(!value.equals(NM.currentUser())) {
//                                            sourceUsers.add(value);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onError(Throwable e) {
//
//                                    }
//
//                                    @Override
//                                    public void onComplete() {
//                                        hideLoading();
//                                        adapter.addBUsersRows(sourceUsers);
//                                        adapter.notifyDataSetChanged();
//                                    }
//                                });
//
//                    }
//                    else
//                        sourceUsers = NM.currentUser().getFollowers();
//                    break;
//
//                case MODE_LOAD_FOLLOWS:
//                    if (extraData instanceof String && StringUtils.isNotEmpty((String) extraData))
//                    {
//
//                        sourceUsers = new ArrayList<BUser>();
//                        showLoading();
//                        BNetworkManager.getCoreInterface().getFollows((String) extraData).subscribe(new Observer<BUser>() {
//                            @Override
//                            public void onSubscribe(Disposable d) {
//                            }
//
//                            @Override
//                            public void onNext(BUser value) {
//                                if(!value.equals(NM.currentUser())) {
//                                    sourceUsers.add(value);
//                                }
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                            }
//
//                            @Override
//                            public void onComplete() {
//                                hideLoading();
//                                adapter.addBUsersRows(sourceUsers);
//                                adapter.notifyDataSetChanged();
//                            }
//                        });
//                    }
//                    else
//                        sourceUsers = NM.currentUser().getFollows();
//                    break;
            }
    }

    public void showLoading(){
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.INVISIBLE);
    }

    public void hideLoading(){
        progressBar.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SearchActivity.GET_CONTACTS_ADDED_REQUEST)
            if (resultCode == AppCompatActivity.RESULT_OK)
            {
                loadData();
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setAdapter(AbstractUsersListAdapter adapter) {
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

    public AbstractUsersListAdapter getAdapter() {
        return adapter;
    }

    public void setProfilePicClickListener(AbstractUsersListAdapter.ProfilePicClickListener profilePicClickListener){
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
