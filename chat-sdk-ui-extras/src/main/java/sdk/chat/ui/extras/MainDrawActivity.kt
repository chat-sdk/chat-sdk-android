package sdk.chat.ui.extras

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.ui.activities.MainActivity
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
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
    private var lastFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        slider.apply {
            for (tab in ChatSDK.ui().tabs()) {
                itemAdapter.add(PrimaryDrawerItem().withName(tab.title).withIcon(tab.icon))
            }
            onDrawerItemClickListener = { v, drawerItem, position ->
                setFragmentForPosition(position - 1)
                false
            }
            setSavedInstance(savedInstanceState)
        }

        setFragmentForPosition(0);
    }

    protected fun setFragmentForPosition(position: Int) {
        val tab = ChatSDK.ui().tabs().get(position)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, tab.fragment).commit()
        supportActionBar?.setTitle(tab.title)
//        if(lastFragment != null) {
//            transaction.remove(lastFragment!!)
//        }
//
//
//        transaction.add(R.id.fragment_container, tab.fragment).disallowAddToBackStack().commit()
        lastFragment = tab.fragment
//        invalidateOptionsMenu()
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
            addProfiles(
                    profile,
                    //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
                    ProfileSettingDrawerItem().apply {
                        name = StringHolder(R.string.logout)
                        icon = ImageHolder(IconicsDrawable(context, GoogleMaterial.Icon.gmd_exit_to_app).apply {
                            colorInt = Color.RED
                        })
                    }
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
//        if (lastFragment != null) {
//            lastFragment!!.onCreateOptionsMenu(menu, inflater)
//        }
//        return super.onCreateOptionsMenu(menu)
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
