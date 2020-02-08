package co.chatsdk.ui.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.databinding.DataBindingUtil;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.databinding.ActivityProfileBinding;
import co.chatsdk.ui.fragments.ProfileFragment;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by ben on 8/23/17.
 */

public class ProfileActivity extends BaseActivity {

    protected User user;
    protected boolean startingChat = false;
    protected MenuItem chatMenuItem;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        String userEntityID = getIntent().getStringExtra(Keys.IntentKeyUserEntityID);

        if (userEntityID != null && !userEntityID.isEmpty()) {
            user =  ChatSDK.db().fetchUserWithEntityID(userEntityID);
            if (user != null) {
                ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.profileFragment);
                fragment.setUser(user);
                fragment.updateInterface();
                return;
            }
        }

        ToastHelper.show(this, R.string.user_entity_id_not_set);
        finish();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_profile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_profile_menu, menu);

        menu.findItem(R.id.action_chat).setIcon(Icons.get(Icons.shared().chat, R.color.app_bar_icon_color));
//        IconicsMenuInflaterUtil.inflate(inflater, this, R.menu.activity_profile_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat) {
            startChat();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startChat () {

        if (startingChat) {
            return;
        }

        startingChat = true;

        showProgressDialog(getString(R.string.creating_thread));


        dm.add(ChatSDK.thread().createThread("", user, ChatSDK.currentUser())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    dismissProgressDialog();
                    startingChat = false;
                })
                .subscribe(thread -> {
                    ChatSDK.ui().startChatActivityForID(getApplicationContext(), thread.getEntityID());
                }, throwable -> {
                    ToastHelper.show(getApplicationContext(), throwable.getLocalizedMessage());
                }));


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}
