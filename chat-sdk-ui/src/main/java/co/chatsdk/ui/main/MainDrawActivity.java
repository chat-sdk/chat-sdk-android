package co.chatsdk.ui.main;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;

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
import co.chatsdk.ui.profile.ProfileFragment;

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
                .withOnAccountHeaderListener((view, profile, currentProfile) -> {
                    int j = 0;
                    for (Tab tab : ChatSDK.ui().defaultTabs()) {
                        if (tab.fragment instanceof ProfileFragment) {
                            j++;
                        }
                    }
                    setFragmentForPosition(j);
                    return false;
                })
                .build();

        View toolbarView = getLayoutInflater().inflate(R.layout.draw_toolbar, null);
        Toolbar toolbar = toolbarView.findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        Drawer d = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withDrawerItems(items)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    setFragmentForPosition(position);
                    return false;
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
