package sdk.chat.ui.provider;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.icons.Icons;

public class MenuItemProvider {

    public static int AddItemId = 900;
    public static int SearchItemId = 901;

    public MenuItem addAddItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(AddItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, AddItemId, order, R.string.add)
                    .setIcon(Icons.get(context, ChatSDKUI.icons().add, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return item;
    }

    public MenuItem addSearchItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(SearchItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, SearchItemId, order, R.string.search)
                    .setIcon(Icons.get(context, ChatSDKUI.icons().search, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return item;
    }

}
