package app.xmpp.adapter.ui;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import app.xmpp.adapter.activities.XMPPLoginActivity;
import sdk.chat.ui.BaseInterfaceAdapter;
import sdk.chat.core.Tab;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class XMPPInterfaceAdapter extends BaseInterfaceAdapter {

    public XMPPInterfaceAdapter(Context context) {
        super(context);
//        searchActivity = XMPPSearchActivity.class;
        loginActivity = XMPPLoginActivity.class;
    }

    @Override
    public List<Tab> defaultTabs() {

        ArrayList<Tab> tabs = new ArrayList<>();

        tabs.add(privateThreadsTab());
        tabs.add(contactsTab());

        return tabs;
    }


}
