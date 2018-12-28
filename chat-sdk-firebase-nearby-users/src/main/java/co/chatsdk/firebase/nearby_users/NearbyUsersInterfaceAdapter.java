package co.chatsdk.firebase.nearby_users;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import co.chatsdk.core.Tab;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;

/**
 * Created by pepe on 15.03.18.
 */

public class NearbyUsersInterfaceAdapter extends BaseInterfaceAdapter {

    public NearbyUsersInterfaceAdapter(Context context) {
        super(context);
    }

    @Override
    public List<Tab> defaultTabs() {
        ArrayList<Tab> tabs = new ArrayList<>();

        tabs.add(privateThreadsTab());
        tabs.add(publicThreadsTab());
        tabs.add(nearbyUsersTab());
        tabs.add(contactsTab());
        tabs.add(profileTab());

        return tabs;
    }

    public Class getNearbyUsersActivity() {
        return NearbyUsersFragment.class;
    }

    public Tab nearbyUsersTab() {
        return new Tab("Nearby Users", R.drawable.nearby_users, nearbyUsersFragment());
    }

    public Fragment nearbyUsersFragment() {
        return NearbyUsersFragment.newInstance();
    }

}
