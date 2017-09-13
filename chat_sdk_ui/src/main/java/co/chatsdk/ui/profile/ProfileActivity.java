package co.chatsdk.ui.profile;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import co.chatsdk.core.InterfaceManager;
import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.ui.BaseInterfaceAdapter;
import co.chatsdk.ui.R;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 8/23/17.
 */

public class ProfileActivity extends FragmentActivity {

    private User user;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_profile_activity);

        String userEntityID = getIntent().getStringExtra(BaseInterfaceAdapter.USER_ENTITY_ID);

        if(userEntityID != null && !userEntityID.isEmpty()) {
            user =  StorageManager.shared().fetchUserWithEntityID(userEntityID);
            if(user != null) {
                ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.profile_fragment);
                fragment.setUser(user);
                return;
            }
        }


        ToastHelper.show(R.string.user_entity_id_not_set);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_chat, 1, getString(R.string.action_chat));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.icn_24_chat);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_chat) {
            startChat();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startChat () {

        NM.thread().createThread(user.getName(), user, NM.currentUser()).doOnSuccess(new Consumer<Thread>() {
            @Override
            public void accept(Thread thread) throws Exception {
                if (thread != null) {
                    InterfaceManager.shared().a.startChatActivityForID(thread.getEntityID());
                }
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                ToastHelper.show(R.string.create_thread_with_users_fail_toast);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
