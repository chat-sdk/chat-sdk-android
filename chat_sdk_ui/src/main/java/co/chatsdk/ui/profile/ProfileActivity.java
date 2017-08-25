package co.chatsdk.ui.profile;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.ui.R;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.ui.helpers.UIHelper;
import io.reactivex.functions.BiConsumer;

/**
 * Created by ben on 8/23/17.
 */

public class ProfileActivity extends BaseActivity {

    private User user;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_profile_activity);

        String userEntityID = getIntent().getStringExtra(UIHelper.USER_ENTITY_ID);

        if(userEntityID != null && !userEntityID.isEmpty()) {
            user =  StorageManager.shared().fetchUserWithEntityID(userEntityID);
            if(user != null) {
                ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.profile_fragment);
                fragment.setUser(user);
                return;
            }
        }


        UIHelper.shared().showToast(getString(R.string.user_entity_id_not_set));
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
        UIHelper.shared().createAndOpenThreadWithUsers(this, user.getName(), user, NM.currentUser()).subscribe(new BiConsumer<Thread, Throwable>() {
            @Override
            public void accept(Thread thread, Throwable throwable) throws Exception {
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
