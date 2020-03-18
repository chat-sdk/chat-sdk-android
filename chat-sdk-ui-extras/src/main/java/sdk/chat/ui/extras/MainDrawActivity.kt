package sdk.chat.ui.extras

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import co.chatsdk.core.events.EventType
import co.chatsdk.core.events.NetworkEvent
import co.chatsdk.core.interfaces.LocalNotificationHandler
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.ui.activities.MainActivity
import co.chatsdk.ui.fragments.BaseFragment
import co.chatsdk.ui.icons.Icons
import co.chatsdk.ui.interfaces.SearchSupported
import com.bumptech.glide.Glide
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mikepenz.materialdrawer.model.interfaces.withIcon
import com.mikepenz.materialdrawer.model.interfaces.withName
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.updateName
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_main_drawer.*

class MainDrawActivity : MainActivity() {

    override fun getLayout(): Int {
        return R.layout.activity_main_drawer
    }

    private lateinit var headerView: AccountHeaderView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private lateinit var profile: IProfile
    private var currentFragment: Fragment? = null

    private lateinit var privateThreadItem: PrimaryDrawerItem
    private lateinit var publicThreadItem: PrimaryDrawerItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)

        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.MessageReadReceiptUpdated, EventType.MessageAdded)).subscribe(Consumer {
            // Refresh the read count
            slider.updateName(privateThreadItem.identifier, privateTabName())
        }))

        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
                Glide.with(this@MainDrawActivity)
                        .load(uri)
                        .dontAnimate()
                        .placeholder(placeholder)
                        .into(imageView)
            }

            override fun cancel(imageView: ImageView) {
                Glide.with(this@MainDrawActivity).clear(imageView)
            }
        })

        // Handle Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, root, toolbar, R.string.material_drawer_open, R.string.material_drawer_close)

        val user = ChatSDK.currentUser()

        profile = ProfileDrawerItem().apply {
            name = StringHolder(user.name)
            description = StringHolder(user.email)
            icon = ImageHolder(user.avatarURL)
        }

        // Create the AccountHeader
        buildHeader(false, savedInstanceState)

        val logoutItem = PrimaryDrawerItem().withName(R.string.logout).withIcon(Icons.get(Icons.choose().logout, R.color.logout_button_color))
        logoutItem.isSelectable = false

        val profileItem = PrimaryDrawerItem().withName(R.string.profile).withIcon(Icons.get(Icons.choose().user, R.color.profile_icon_color))
        profileItem.isSelectable = false

        slider.apply {
            for (tab in ChatSDK.ui().tabs()) {
                val item = PrimaryDrawerItem().withName(tab.title).withIcon(tab.icon)
                itemAdapter.add(item)
                if(tab.fragment === ChatSDK.ui().privateThreadsFragment()) {
                    privateThreadItem = item
                    slider.updateName(privateThreadItem.identifier, privateTabName())
                }
                if(tab.fragment === ChatSDK.ui().publicThreadsFragment()) {
                    publicThreadItem = item
                }
            }
            itemAdapter.add(DividerDrawerItem())
            itemAdapter.add(profileItem)
            itemAdapter.add(logoutItem)
            onDrawerItemClickListener = { v, drawerItem, position ->
                // Logout item
                if(drawerItem  === logoutItem) {
                    ChatSDK.auth().logout().subscribe(this@MainDrawActivity)
                } else if(drawerItem  === profileItem) {
                    ChatSDK.ui().startProfileActivity(context, ChatSDK.currentUserID())
                } else {
                    setFragmentForPosition(position - 1)
                }
                false
            }
            setSavedInstance(savedInstanceState)
        }

        setFragmentForPosition(0);
    }

    protected fun privateTabName(): StringHolder {
        val unread = ChatSDK.thread().getUnreadMessagesAmount(false);
        val tab = ChatSDK.ui().privateThreadsTab();
        if (unread == 0) {
            return StringHolder(tab.title)
        } else {
            return StringHolder(String.format(tab.title, " (" + unread + ")"))
        }
    }

    protected fun setFragmentForPosition(position: Int) {
        val tabs = ChatSDK.ui().tabs()
        val tab = tabs.get(position)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, tab.fragment).commit()
        supportActionBar?.setTitle(tab.title)
        currentFragment = tab.fragment
        updateLocalNotificationsForTab()

        // We mark the tab as visible. This lets us be more efficient with updates
        // because we only
        for (i in tabs.indices) {
            val fragment: Fragment = tabs.get(i).fragment
            if (fragment is BaseFragment) {
                (tabs.get(i).fragment as BaseFragment).setTabVisibility(fragment === currentFragment)
            }
        }

    }

    /**
     * small helper method to reuse the logic to build the AccountHeader
     * this will be used to replace the header of the drawer with a compact/normal header
     *
     * @param compact
     * @param savedInstanceState
     */
    private fun buildHeader(compact: Boolean, savedInstanceState: Bundle?) {
        // Create the AccountHeader
        headerView = AccountHeaderView(this, compact = compact).apply {
            attachToSliderView(slider)
            addProfiles(
                    profile
                    //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
//                    ProfileSettingDrawerItem().apply {
//                        name = StringHolder(R.string.logout)
//                        icon = ImageHolder(Icons.get(Icons.choose().logout, R.color.logout_button_color))
//                    }
            )
            selectionListEnabledForSingleProfile = false
            headerBackground = ImageHolder(ChatSDK.config().drawerHeaderImage)
            withSavedInstance(savedInstanceState)
            onAccountHeaderListener = { view, profile, current ->
                ChatSDK.ui().startProfileActivity(context, ChatSDK.currentUserID())
                false
            }
//            onAccountHeaderSelectionViewClickListener = { view, profile, current ->
//                ChatSDK.ui().startProfileActivity(context, ChatSDK.currentUserID())
//                false
//            }
        }
        headerView.currentProfileName.setTextColor(ContextCompat.getColor(this, R.color.app_bar_text_color))
        headerView.currentProfileEmail.setTextColor(ContextCompat.getColor(this, R.color.app_bar_text_color))
        headerView.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the drawer to the bundle
        outState = slider.saveInstanceState(outState)

        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerView.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (root.isDrawerOpen(slider)) {
            root.closeDrawer(slider)
        } else {
            super.onBackPressed()
        }
    }

    override fun searchEnabled(): Boolean {
        return currentFragment is SearchSupported
    }

    override fun search(text: String?) {
        (currentFragment as SearchSupported).filter(text)
    }

    override fun searchView(): com.miguelcatalan.materialsearchview.MaterialSearchView {
        return searchView
    }

    override fun reloadData() {
        for (tab in ChatSDK.ui().tabs()) {
            (tab.fragment as BaseFragment).reloadData()
        }
    }

    override fun initViews() {
    }

    override fun clearData() {
        for (tab in ChatSDK.ui().tabs()) {
            (tab.fragment as BaseFragment).clearData()
        }
    }

    override fun updateLocalNotificationsForTab() {
        ChatSDK.ui().setLocalNotificationHandler(LocalNotificationHandler {
            showLocalNotificationsForTab(currentFragment, it)
        })
    }


}
