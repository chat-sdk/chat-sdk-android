package com.braunster.chatsdk.fragments;

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

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.adapter.UsersWithStatusListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class ContactsFragment extends BaseFragment {

    // TODO show user profile when pressing on contacts
    
    private static final String TAG = ContactsFragment.class.getSimpleName();
    private static boolean DEBUG = true;

    public static final int MODE_LOAD_CONTACS = 1991;
    public static final int MODE_LOAD_THREAD_USERS = 1992;
    public static final String MODE = "Mode";
    public static final String IS_DIALOG = "is_dialog";

    private BUser user;
    private UsersWithStatusListAdapter adapter;
    private ListView listView;
    private ArrayList<String> listDataHeader;
    private HashMap<String, List<BUser>> listDataChild;

    private final List<BUser> onlineContacts = new ArrayList<BUser>();
    private final List<BUser> offlineContacts = new ArrayList<BUser>();
    private List<BUser> sourceUsers = null;
    private String title = "";
    private int mode;
    private String extraData ="";

    /** When isDialog = true the dialog will always show the list of users given to him or pulled by the thread id.*/
    private boolean isDialog = false;

    public static ContactsFragment newInstance() {
        ContactsFragment f = new ContactsFragment();
        f.setMode(MODE_LOAD_CONTACS);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ContactsFragment newDialogInstance(int mode, String extraData, String title) {
        ContactsFragment f = new ContactsFragment();
        f.setDialog();
        f.setTitle(title);
        f.setExtraData(extraData);
        f.setMode(mode);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ContactsFragment newThreadUsersDialogInstance(String threadID, String title) {
        ContactsFragment f = new ContactsFragment();
        f.setTitle(title);
        f.setMode(MODE_LOAD_THREAD_USERS);
        f.setDialog();
        f.setExtraData(threadID);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ContactsFragment newDialogInstance(int mode, String title) {
        ContactsFragment f = new ContactsFragment();
        f.setDialog();
        f.setMode(mode);
        f.setTitle(title);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    private void setDialog(){
        isDialog = true;
    }

    private void setTitle(String title){
        this.title = title;
    }

    private void setMode(int mode){
        this.mode = mode;
    }

    private void setExtraData(String extraData){
        this.extraData = extraData;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            mode = savedInstanceState.getInt(MODE);
            isDialog = savedInstanceState.getBoolean(IS_DIALOG);
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

        this.user = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

        initViews();

        loadData();

        return mainView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MODE, mode);
        outState.putBoolean(IS_DIALOG, isDialog);
    }

    @Override
    public void initViews(){
        listView = (ListView) mainView.findViewById(R.id.list_contacts);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, "Add Chat");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(android.R.drawable.ic_menu_add);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        // ASK what the add button do in this class
        if (id == R.id.action_chat_sdk_add)
        {
//            Intent intent = new Intent(getActivity(), PickFriendsActivity.class);
//
//            startActivity(intent);
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
        switch (mode)
        {
            case MODE_LOAD_CONTACS:
                sourceUsers = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getContacts();
                break;

            case MODE_LOAD_THREAD_USERS:
                BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, extraData);
                sourceUsers = thread.getUsers();
                break;
        }

        if (BNetworkManager.sharedManager().getNetworkAdapter() != null)
        {
            adapter = new UsersWithStatusListAdapter(getActivity(), UsersWithStatusListAdapter.makeList(sourceUsers, true));
            listView.setAdapter(adapter);

           listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (adapter.getItem(position).getType() == UsersWithStatusListAdapter.TYPE_HEADER)
                        return;

                    BUser clickedUser = DaoCore.fetchEntityWithEntityID(BUser.class, adapter.getItem(position).getEntityID());

                    createAndOpenThreadWithUsers(clickedUser.getMetaName(), clickedUser, BNetworkManager.sharedManager().getNetworkAdapter().currentUser());
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (adapter.getItem(position).getType() == UsersWithStatusListAdapter.TYPE_HEADER)
                        return true;

//                    showAlertDialog("", getResources().getString(R.string.alert_delete_contact), getResources().getString(R.string.delete),
//                            getResources().getString(R.string.cancel), null, new DeleteContact(adapter.getItem(position).getEntityID()));

                    return true;
                }
            });

            //region Multi select
/*listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    mode.setTitle(adapter.getSelectedCount() + " selected.");
                    adapter.toggleSelection(position);
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.multi_select_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    adapter.clearSelection();
                }
            });*/
            //endregion

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

        if (BNetworkManager.sharedManager().getNetworkAdapter() == null)
        {
            if (DEBUG) Log.e(TAG, "network adapter is null");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                // If this is not a dialog we will load the contacts of the user.
                switch (mode)
                {
                    case MODE_LOAD_CONTACS:
                        if (DEBUG) Log.d(TAG, "Mode - Contacts");
                        sourceUsers = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getContacts();
                        break;

                    case MODE_LOAD_THREAD_USERS:
                        if (DEBUG) Log.d(TAG, "Mode - Thread Users");
                        BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, extraData);
                        sourceUsers = thread.getUsers();
                        break;
                }

                if (BNetworkManager.sharedManager().getNetworkAdapter() != null) {
                    adapter = new UsersWithStatusListAdapter(getActivity(), UsersWithStatusListAdapter.makeList(sourceUsers, true));
                }

                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case 1:

                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            if (adapter.getItem(position).getType() == UsersWithStatusListAdapter.TYPE_HEADER)
                                return;

                            BUser clickedUser = DaoCore.fetchEntityWithEntityID(BUser.class, adapter.getItem(position).getEntityID());

                            createAndOpenThreadWithUsers(clickedUser.getMetaName(), clickedUser, BNetworkManager.sharedManager().getNetworkAdapter().currentUser());
                        }
                    });

                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            if (adapter.getItem(position).getType() == UsersWithStatusListAdapter.TYPE_HEADER)
                                return true;

//                    showAlertDialog("", getResources().getString(R.string.alert_delete_contact), getResources().getString(R.string.delete),
//                            getResources().getString(R.string.cancel), null, new DeleteContact(adapter.getItem(position).getEntityID()));

                            return true;
                        }
                    });

                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.v(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
