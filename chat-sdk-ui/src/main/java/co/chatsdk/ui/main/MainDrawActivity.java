package co.chatsdk.ui.main;

import android.os.Bundle;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;

public class MainDrawActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<IDrawerItem> items = new ArrayList<>();

        int i = 0;
        for (Tab tab : ChatSDK.ui().defaultTabs()) {
            items.add(new PrimaryDrawerItem()
                    .withIdentifier(i++)
                    .withName(tab.title)
                    .withIcon(tab.icon));
        }

        User currentUser = ChatSDK.currentUser();
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
//                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(currentUser.getName())
                                .withEmail(currentUser.getEmail())
                                .withIcon(currentUser.getAvatarURL())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        Drawer d = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(false)
                .withDrawerItems(items)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        setFragmentForPosition(position);
                        return false;
                    }
                })
                .build();
        d.openDrawer();
        setFragmentForPosition(0);
    }

    protected void setFragmentForPosition (int position) {
        Tab tab = ChatSDK.ui().defaultTabs().get(position);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content, tab.fragment)
                .disallowAddToBackStack()
                .commit();
    }

        @Override
    protected void reloadData() {

    }

    @Override
    protected void initViews() {
        setContentView(activityLayout());
        getSupportActionBar().hide();
    }

    @Override
    protected void clearData() {

    }

    @Override
    protected void updateLocalNotificationsForTab() {

    }

    @Override
    protected int activityLayout() {
        return R.layout.activity_draw;
    }
}
