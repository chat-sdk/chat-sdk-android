package sdk.chat.ui.extras

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.miguelcatalan.materialsearchview.MaterialSearchView
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
import io.reactivex.Single
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_main_drawer.*
import sdk.chat.core.events.EventType
import sdk.chat.core.events.NetworkEvent
import sdk.chat.core.hook.Executor
import sdk.chat.core.hook.Hook
import sdk.chat.core.hook.HookEvent
import sdk.chat.core.interfaces.LocalNotificationHandler
import sdk.chat.core.session.ChatSDK
import sdk.chat.ui.activities.MainActivity
import sdk.chat.ui.fragments.BaseFragment
import sdk.chat.ui.icons.Icons
import sdk.chat.ui.interfaces.SearchSupported
import sdk.chat.ui.module.UIModule
import sdk.guru.common.RX

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

        initViews()

        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.MessageReadReceiptUpdated, EventType.MessageAdded)).subscribe(Consumer {
            // Refresh the read count
            dm.add(privateTabName().subscribe(Consumer {
                slider.updateName(privateThreadItem.identifier, it)
            }))
        }))

        // Update the user details
        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserMetaUpdated)).filter(NetworkEvent.filterCurrentUser()).subscribe(Consumer {
            headerView.post {
                updateProfile()
                headerView.updateProfile(profile)
                updateHeaderBackground()
            }
        }))

        ChatSDK.hook().addHook(Hook.sync(Executor {
            headerView.post {
                updateProfile()
                headerView.updateProfile(profile)
                updateHeaderBackground()
            }
        }), HookEvent.DidAuthenticate);

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

        buildHeader(false, savedInstanceState)

        val logoutItem = PrimaryDrawerItem().withName(R.string.logout).withIcon(Icons.get(this, Icons.choose().logout, R.color.logout_button_color))
        logoutItem.isSelectable = false

        val profileItem = PrimaryDrawerItem().withName(R.string.profile).withIcon(Icons.get(this, Icons.choose().user, R.color.profile_icon_color))
        profileItem.isSelectable = false

        slider.apply {
            for (tab in ChatSDK.ui().tabs()) {
                val item = PrimaryDrawerItem().withName(tab.title).withIcon(tab.icon)
                itemAdapter.add(item)
                if(tab.fragment === ChatSDK.ui().privateThreadsFragment()) {
                    privateThreadItem = item

                    dm.add(privateTabName().subscribe(Consumer {
                        slider.updateName(privateThreadItem.identifier, it)
                    }))
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
                    ChatSDK.auth().logout().observeOn(RX.main()).doOnComplete(Action { ChatSDK.ui().startSplashScreenActivity(this@MainDrawActivity) }).subscribe(this@MainDrawActivity)
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

    protected fun privateTabName(): Single<StringHolder> {
        return KotlinHelper.privateTabName().observeOn(RX.main())
    }

    protected fun updateProfile() {
        val user = ChatSDK.currentUser()

        profile = ProfileDrawerItem().apply {
            identifier = 1
            name = StringHolder(user.name)
            description = StringHolder(user.status)
            if(user.avatarURL != null) {
                icon = ImageHolder(user.avatarURL)
            } else {
                icon = ImageHolder(UIModule.config().defaultProfilePlaceholder)
            }
        }
        // Create the AccountHeader
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

        updateProfile()

        // Create the AccountHeader
        headerView = AccountHeaderView(this, compact = compact).apply {
            attachToSliderView(slider)
            addProfiles(
                    profile
                    //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
//                    ProfileSettingDrawerItem().apply {
//                        name = StringHolder(R.string.logout)
//                        icon = ImageHolder(Icons.get(this, Icons.choose().logout, R.color.logout_button_color))
//                    }
            )
            selectionListEnabledForSingleProfile = false

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
//        headerView.currentProfileView.setBackgroundColor(Color.WHITE)
        headerView.currentProfileView.setBackgroundDrawable(resources.getDrawable(R.drawable.shape_circle))

        updateHeaderBackground()
    }

    fun updateHeaderBackground() {
        val user = ChatSDK.currentUser()
        if(user.headerURL != null) {
//            headerView.headerBackground = ImageHolder(user.headerURL)
            Glide.with(this).load(user.headerURL).into(headerView.accountHeaderBackground)
        } else {
            headerView.headerBackground = ImageHolder(ExtrasModule.config().drawerHeaderImage)
        }
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

    override fun searchView(): MaterialSearchView {
        return searchView
    }

    override fun reloadData() {
        for (tab in ChatSDK.ui().tabs()) {
            (tab.fragment as BaseFragment).reloadData()
        }
    }

//    override fun initViews() {
//    }

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
