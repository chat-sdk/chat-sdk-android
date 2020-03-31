package co.chatsdk.xmpp.ui;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.Tab;
import co.chatsdk.ui.BaseInterfaceAdapter;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class XMPPInterfaceAdapter extends BaseInterfaceAdapter {

    public XMPPInterfaceAdapter(Context context) {
        super(context);
//        searchActivity = XMPPSearchActivity.class;
    }

    @Override
    public List<Tab> defaultTabs() {

        ArrayList<Tab> tabs = new ArrayList<>();

        tabs.add(privateThreadsTab());
        tabs.add(contactsTab());

        return tabs;
    }
}
