package sdk.chat.ui.provider;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.icons.Icons;

public class MenuItemProvider {

    public static int addItemId = 900;
    public static int searchItemId = 901;
    public static int copyItemId = 902;
    public static int deleteItemId = 903;
    public static int forwardItemId = 904;
    public static int replyItemId = 905;
    public static int callItemId = 906;
    public static int saveItemId = 907;

    public MenuItem addAddItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(addItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, addItemId, order, R.string.add)
                    .setIcon(ChatSDKUI.icons().get(context, ChatSDKUI.icons().add, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return item;
    }

    public MenuItem addSearchItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(searchItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, searchItemId, order, R.string.search)
                    .setIcon(ChatSDKUI.icons().get(context, ChatSDKUI.icons().search, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return item;
    }

    public MenuItem addCopyItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(copyItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, copyItemId, order, R.string.copy)
                    .setIcon(ChatSDKUI.icons().get(context, ChatSDKUI.icons().copy, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return item;
    }

    public MenuItem addDeleteItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(deleteItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, deleteItemId, order, R.string.delete)
                    .setIcon(ChatSDKUI.icons().get(context, ChatSDKUI.icons().delete, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return item;
    }

    public MenuItem addForwardItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(forwardItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, forwardItemId, order, R.string.forward_message)
                    .setIcon(ChatSDKUI.icons().get(context, ChatSDKUI.icons().forward, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return item;
    }

    public MenuItem addReplyItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(replyItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, replyItemId, order, R.string.reply)
                    .setIcon(ChatSDKUI.icons().get(context, ChatSDKUI.icons().reply, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return item;
    }

    public MenuItem addCallItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(callItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, callItemId, order, R.string.call)
                    .setIcon(ChatSDKUI.icons().get(context, ChatSDKUI.icons().call, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return item;
    }

    public MenuItem addSaveItem(Context context, Menu menu, int order) {
        MenuItem item = menu.findItem(saveItemId);
        if (item == null) {
            item = menu.add(Menu.NONE, saveItemId, order, R.string.save)
                    .setIcon(ChatSDKUI.icons().get(context, ChatSDKUI.icons().save, ChatSDKUI.icons().actionBarIconColor));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return item;
    }

}
