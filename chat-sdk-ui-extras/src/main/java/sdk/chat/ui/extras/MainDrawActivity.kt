package sdk.chat.ui.extras

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.ui.main.MainActivity
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.actionBar
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.iconics.withIcon
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import kotlinx.android.synthetic.main.activity_main_drawer.*

class MainDrawActivity : MainActivity() {

    override fun reloadData() {
    }

    override fun initViews() {
    }

    override fun clearData() {
    }

    override fun updateLocalNotificationsForTab() {
    }

    override fun activityLayout(): Int {
        return R.layout.activity_main_drawer
    }

    private lateinit var headerView: AccountHeaderView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private lateinit var profile: IProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityLayout())

        // Handle Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setTitle("Test Title")

        actionBarDrawerToggle = ActionBarDrawerToggle(this, root, toolbar, R.string.material_drawer_open, R.string.material_drawer_close)

        var user = ChatSDK.currentUser()
        profile = ProfileDrawerItem().withName(user.name).withEmail(user.email).withIcon(user.avatarURL)

        // Create the AccountHeader
        buildHeader(false, savedInstanceState)


        slider.apply {
            for (tab in ChatSDK.ui().tabs()) {
                itemAdapter.add(PrimaryDrawerItem().withName(tab.title).withIcon(tab.icon))
            }
            onDrawerItemClickListener = { v, drawerItem, position ->
                setFragmentForPosition(position)
                true
            }
            setSavedInstance(savedInstanceState)
        }
    }

    protected fun setFragmentForPosition(position: Int) {
        var tab = ChatSDK.ui().defaultTabs().get(position)
        supportFragmentManager.beginTransaction().add(R.id.content, tab.fragment).disallowAddToBackStack().commit()
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
            selectionListEnabledForSingleProfile = false
            headerBackground = ImageHolder(R.drawable.header)
            // TODO withCompactStyle(compact)
            addProfiles(
                    profile,
                    //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
                    ProfileSettingDrawerItem().withName("Add Account").withDescription("Add new GitHub Account").withIcon(IconicsDrawable(this@MainDrawActivity, GoogleMaterial.Icon.gmd_add).apply { actionBar(); paddingDp = 5 }).withIdentifier(PROFILE_SETTING.toLong()),
                    ProfileSettingDrawerItem().withName("Manage Account").withIcon(GoogleMaterial.Icon.gmd_settings)
            )
            withSavedInstance(savedInstanceState)
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

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val inflater = menuInflater
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return super.onOptionsItemSelected(item)
//    }

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

    companion object {
        private const val PROFILE_SETTING = 1
    }
}
