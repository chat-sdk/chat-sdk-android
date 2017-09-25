/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;

import co.chatsdk.core.InterfaceManager;
import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.BaseInterfaceAdapter;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.activities.SearchActivity;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import co.chatsdk.core.dao.DaoCore;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by itzik on 6/17/2014.
 */
public class ContactsFragment extends BaseFragment {

    private static boolean DEBUG = Debug.ContactsFragment;

    /** Loading all the current user contacts.*/
    public static final int MODE_LOAD_CONTACTS = 1991;

    /** Loading all users for given thread id mode*/
    public static final int MODE_LOAD_THREAD_USERS = 1992;

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
    public static final String IS_DIALOG = "is_dialog";

    /** The text color that the adapter will use, Use -1 to set adapter to default color.*/
    protected int textColor = -1991;

    protected UsersListAdapter adapter;
    protected ProgressBar progressBar;
    protected ListView listView;

    private DisposableList disposables = new DisposableList();

    /** Users that will be used to fill the adapter, This could be set manually or it will be filled when loading users for
     * {@link #loadingMode}*/
    protected List<User> sourceUsers = new ArrayList<>();

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
     * #CLICK_MODE_SHOW_PROFILEs */
    protected int clickMode;

    protected Object extraData ="";

    /** If true the fragment will listen to users details change and updates.*/
    protected boolean withUpdates = true;

    /** Set to false if you dont want any menu item to be inflated for this fragment.
     *  This should be set before the fragment transaction,
     *  if you extends the fragment you can call it in {@link #onCreate(android.os.Bundle)}
     *  <B>see </B>{@link #setInflateMenu(boolean inflate)}*/
    protected boolean inflateMenu = true;

    /** When isDialog = true the dialog will always show the list of users given to him or pulled by the thread id.*/
    private boolean isDialog = false;

    public static ContactsFragment newInstance() {
        ContactsFragment f = new ContactsFragment();
        f.setLoadingMode(MODE_LOAD_CONTACTS);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ContactsFragment newInstance(int loadingMode, int clickMode, Object extraData) {
        ContactsFragment f = new ContactsFragment();
        f.setLoadingMode(loadingMode);
        f.setClickMode(clickMode);
        f.setExtraData(extraData);
        return f;
    }

    /** Creates a new contact dialog.
     * @param threadID - The id of the thread that his users is the want you want to show.
     * @param title - The title of the dialog.
     * @param withUpdates - the dialog will listen to user details changes.*/
    public static ContactsFragment newThreadUsersDialogInstance(String threadID, String title, boolean withUpdates) {
        ContactsFragment f = new ContactsFragment();
        f.setTitle(title);

        f.setLoadingMode(MODE_LOAD_THREAD_USERS);
        f.setDialog();
        f.setExtraData(threadID);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

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

        disposables.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.filterContactsChanged())
                .subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                loadData(false);
                Timber.v("Contacts Notification");
            }
        }));

        disposables.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                        loadData(true);
                    }
                }));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (isDialog) {
            if(title.equals("")) {
                getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
            else {
                getDialog().setTitle(title);
            }
        }

        mainView = inflater.inflate(R.layout.chat_sdk_fragment_contacts, null);

        initViews();

        loadData(true);

        return mainView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LOADING_MODE, loadingMode);
        outState.putBoolean(IS_DIALOG, isDialog);
    }

    public void initViews(){
        listView = (ListView) mainView.findViewById(R.id.chat_sdk_list_contacts);

        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progressbar);

        // Create the adapter only if null this is here so we wont
        // override the adapter given from the extended class with setAdapter.
        if (adapter == null) {
            adapter = new UsersListAdapter((AppCompatActivity) getActivity());
        }

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
        if (id == R.id.action_chat_sdk_add) {
            Intent intent = new Intent(getActivity(), InterfaceManager.shared().a.getSearchActivity());
            startActivityForResult(intent, BaseInterfaceAdapter.REQUEST_CODE_GET_CONTACTS);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadData (final boolean force) {

        final ArrayList<User> originalUserList = new ArrayList<>();
        originalUserList.addAll(sourceUsers);

        reloadUsers().subscribe(new Action() {
            @Override
            public void run() throws Exception {
                if (!originalUserList.equals(sourceUsers) || force) {
                    adapter.setUsers(sourceUsers, true);
                    Timber.v("Update Contact List");
                }
                setupListClickMode();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });
    }

    public void clearData() {
        if (adapter != null) {
            adapter.getUserItems().clear();
            adapter.notifyDataSetChanged();
        }
    }

    private void setupListClickMode() {
        if(adapter.getRowClickListener() == null) {
            adapter.setRowClickListener(new UsersListAdapter.RowClickListener() {
                @Override
                public void click(int position) {
                    final User clickedUser = DaoCore.fetchEntityWithEntityID(User.class, adapter.getItem(position).getEntityID());

                    switch (clickMode) {
                        case CLICK_MODE_ADD_USER_TO_THREAD:

                            Thread thread = null;
                            if (extraData instanceof Long) {
                                thread = StorageManager.shared().fetchThreadWithID((Long) extraData);
                            }
                            else if (extraData instanceof String) {
                                thread = StorageManager.shared().fetchThreadWithEntityID((String) extraData);
                            }

                            if(thread != null) {
                                NM.thread().addUsersToThread(thread, clickedUser).doOnError(new Consumer<Throwable>() {
                                    @Override
                                    public void accept(@NonNull Throwable throwable) throws Exception {
                                        throwable.printStackTrace();
                                        ToastHelper.show(getString(R.string.abstract_contact_fragment_user_added_to_thread_toast_fail));
                                    }
                                }).subscribe(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        ToastHelper.show(getString(R.string.abstract_contact_fragment_user_added_to_thread_toast_success) + clickedUser.getName());
                                        if (isDialog) {
                                            getDialog().dismiss();
                                        }
                                    }
                                });
                            }
                            break;

                        case CLICK_MODE_NONE:
                            break;
                        default:
                            InterfaceManager.shared().a.startProfileActivity(clickedUser.getEntityID());
                    }
                }
            });
        }
    }

    private Completable reloadUsers () {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                if (loadingMode != MODE_USE_SOURCE) {

                    sourceUsers.clear();
                   // If this is not a dialog we will load the contacts of the user.
                    switch (loadingMode) {
                        case MODE_LOAD_CONTACTS:
                            if (DEBUG) Timber.d("Mode - Contacts");
                            sourceUsers.addAll(NM.contact().contacts());
                            Timber.d("Contacts: " + sourceUsers.size());
                            break;

                        case MODE_LOAD_THREAD_USERS:
                            if (DEBUG) Timber.d("Mode - CoreThread Users");
                            Thread thread = DaoCore.fetchEntityWithEntityID(Thread.class, extraData);

                            // Remove the current user from the list.
                            List<User> users = thread.getUsers();
                            users.remove(NM.currentUser());

                            sourceUsers.addAll(users);
                            break;

                        case MODE_LOAD_CONTACT_THAT_NOT_IN_THREAD:
                            List<User> users1 = NM.contact().contacts();
                            thread = StorageManager.shared().fetchThreadWithID((Long) extraData);
                            List<User> threadUser = thread.getUsers();
                            users1.removeAll(threadUser);
                            sourceUsers.addAll(users1);
                            break;
                    }
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void setInflateMenu(boolean inflateMenu) {
        this.inflateMenu = inflateMenu;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;

        if (adapter!=null)
        {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.dispose();
    }


    public void withUpdates(boolean withUpdates) {
        this.withUpdates = withUpdates;
    }

//    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
//        listView.setOnItemClickListener(onItemClickListener);
//    }
}
