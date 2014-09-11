/*
package com.braunster.chatsdk.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.adapter.ContactsExpandableListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

*/
/**
 * Created by itzik on 6/17/2014.
 *//*

public class ExpandableContactsFragment extends BaseFragment {

    // TODO show user profile when pressing on contacts
    
    private static final String TAG = ExpandableContactsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ExpandableContactsFragment;

    public static final int MODE_LOAD_CONTACS = 1991;
    public static final int MODE_LOAD_THREAD_USERS = 1992;

    private BUser user;
    private ExpandableListView expContacts;
    private ContactsExpandableListAdapter expandableListAdapter;
    private ArrayList<String> listDataHeader;
    private HashMap<String, List<BUser>> listDataChild;

    private final List<BUser> onlineContacts = new ArrayList<BUser>();
    private final List<BUser> offlineContacts = new ArrayList<BUser>();
    private List<BUser> sourceUsers = null;
    private String title = "";
    private int mode;
    private String extraData ="";

    */
/** When isDialog = true the dialog will always show the list of users given to him or pulled by the thread id.*//*

    private boolean isDialog = false;

    public static ExpandableContactsFragment newInstance() {
        ExpandableContactsFragment f = new ExpandableContactsFragment();
        f.setMode(MODE_LOAD_CONTACS);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ExpandableContactsFragment newDialogInstance(int mode, String extraData, String title) {
        ExpandableContactsFragment f = new ExpandableContactsFragment();
        f.setDialog();
        f.setTitle(title);
        f.setExtraData(extraData);
        f.setMode(mode);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ExpandableContactsFragment newThreadUsersDialogInstance(String threadID, String title) {
        ExpandableContactsFragment f = new ExpandableContactsFragment();
        f.setTitle(title);
        f.setMode(MODE_LOAD_THREAD_USERS);
        f.setDialog();
        f.setExtraData(threadID);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ExpandableContactsFragment newDialogInstance(int mode, String title) {
        ExpandableContactsFragment f = new ExpandableContactsFragment();
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
        if (!isDialog)
            setHasOptionsMenu(true);
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

        mainView = inflater.inflate(R.layout.chat_sdk_fragment_expandable_contacts, null);

        this.user = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

        initViews();

        loadData();

        return mainView;
    }

    @Override
    public void initViews(){
        expContacts = (ExpandableListView) mainView.findViewById(R.id.exp_list_contacts);
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

        */
/* Cant use switch in the library*//*

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

        listDataHeader = getHeaders();

        // Clearing the map.
        listDataChild = new HashMap<String, List<BUser>>();

        // Clearing the old contacts
        onlineContacts.clear();
        offlineContacts.clear();

        if (BNetworkManager.sharedManager().getNetworkAdapter() != null)
        {
            for (BUser contact : sourceUsers) {

                if (contact.getOnline() != null && contact.getOnline())
                    onlineContacts.add(contact);
                else offlineContacts.add(contact);

                listDataChild.put(listDataHeader.get(0), onlineContacts);
                listDataChild.put(listDataHeader.get(1), offlineContacts);
            }

            expandableListAdapter = new ContactsExpandableListAdapter(getActivity(), listDataHeader, listDataChild);
            expContacts.setAdapter(expandableListAdapter);

            expContacts.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    BUser clickedUser = expandableListAdapter.getChild(groupPosition, childPosition);

                    createAndOpenThreadWithUsers(clickedUser.getMetaName(), clickedUser, BNetworkManager.sharedManager().getNetworkAdapter().currentUser());

                    return false;
                }
            });

            expContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
        }
        else if (DEBUG) Log.e(TAG, "NetworkAdapter is null");
    }

    private ArrayList<String> getHeaders(){
        ArrayList<String> listDataHeader = new ArrayList<String>();

        // Adding child data
        listDataHeader.add("Online");
        listDataHeader.add("Offline");

        return listDataHeader;
    }

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
*/
