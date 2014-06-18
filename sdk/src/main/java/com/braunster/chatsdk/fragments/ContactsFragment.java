package com.braunster.chatsdk.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.activities.ChatActivity;
import com.braunster.chatsdk.adapter.ContactsExpandableListAdapter;
import com.braunster.chatsdk.adapter.VolleyContactsExpandableListAdapter;
import com.braunster.chatsdk.dao.BLinkedContact;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.tamplate.TestNetworkAdapter;

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

    private BUser user;
    private ExpandableListView expContacts;
    private ContactsExpandableListAdapter expandableListAdapter;
    private ArrayList<String> listDataHeader;
    private HashMap<String, List<BUser>> listDataChild;

    final List<BUser> onlineContacts = new ArrayList<BUser>();
    final List<BUser> offlineContacts = new ArrayList<BUser>();

    public static ContactsFragment newInstance() {
        ContactsFragment f = new ContactsFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_contacts, null);

        this.user = BNetworkManager.getInstance().currentUser();

        initViews();

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
                menu.add(Menu.NONE, R.id.action_add_chat_room, 10, "Add Chat");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(android.R.drawable.ic_menu_add);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_add_chat_room)
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

        if (mainView == null)
            return;

        listDataHeader = getHeaders();

        // Clearing the map.
        listDataChild = new HashMap<String, List<BUser>>();

        // Clearing the old contacts
        onlineContacts.clear();
        offlineContacts.clear();

        if (BNetworkManager.getInstance().getNetworkAdapter() != null)
            ((TestNetworkAdapter)BNetworkManager.getInstance().getNetworkAdapter())
                    .getContactListWithListener(new CompletionListenerWithData<List<BLinkedContact>>() {
                @Override
                public void onDone(List<BLinkedContact> contacts) {
                    if (DEBUG) Log.d(TAG, "Contacts list size: " + contacts.size());
                    for (BLinkedContact contact : contacts) {
                        if (contact.getContact().getOnline() != null && contact.getContact().getOnline())
                            onlineContacts.add(contact.getContact());
                        else offlineContacts.add(contact.getContact());

                        listDataChild.put(listDataHeader.get(0), onlineContacts);
                        listDataChild.put(listDataHeader.get(1), offlineContacts);
                    }

                    expandableListAdapter = new ContactsExpandableListAdapter(getActivity(), listDataHeader, listDataChild);
                    expContacts.setAdapter(expandableListAdapter);

                    expContacts.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                        @Override
                        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                            BNetworkManager.getInstance().createThreadWithUsers(new CompletionListenerWithData<String>() {
                                @Override
                                public void onDone(String id) {
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra(ChatActivity.THREAD_ID, id);
                                    startActivity(intent);
                                }

                                @Override
                                public void onDoneWithError() {
                                    Toast.makeText(getActivity(), "Failed to start chat.", Toast.LENGTH_SHORT).show();
                                }
                            }, expandableListAdapter.getChild(groupPosition, childPosition), BNetworkManager.getInstance().currentUser());
                            return false;
                        }
                    });
                    expContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        }
                    });
                }

                @Override
                public void onDoneWithError() {
                    Toast.makeText(getActivity(), "Failed to get contacs", Toast.LENGTH_SHORT).show();
                }
            });
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
