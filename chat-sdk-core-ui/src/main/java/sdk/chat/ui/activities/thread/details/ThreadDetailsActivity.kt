package sdk.chat.ui.activities.thread.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_thread_details.*
import kotlinx.android.synthetic.main.fragment_profile.appbar
import kotlinx.android.synthetic.main.fragment_profile.avatarImageView
import kotlinx.android.synthetic.main.fragment_profile.headerImageView
import kotlinx.android.synthetic.main.fragment_profile.onlineIndicator
import sdk.chat.core.dao.Keys
import sdk.chat.core.dao.Thread
import sdk.chat.core.dao.User
import sdk.chat.core.events.EventType
import sdk.chat.core.events.NetworkEvent
import sdk.chat.core.interfaces.ThreadType
import sdk.chat.core.session.ChatSDK
import sdk.chat.core.utils.Dimen
import sdk.chat.core.utils.Strings
import sdk.chat.ui.R
import sdk.chat.ui.activities.ImagePreviewActivity
import sdk.chat.ui.fragments.ProfileViewOffsetChangeListener
import sdk.chat.ui.icons.Icons
import sdk.chat.ui.module.UIModule
import sdk.chat.ui.recycler.*
import sdk.chat.ui.utils.ThreadImageBuilder
import sdk.chat.ui.utils.ToastHelper
import sdk.chat.ui.utils.UserSorter
import sdk.guru.common.RX
import smartadapter.SmartRecyclerAdapter
import smartadapter.viewevent.listener.OnClickEventListener
import smartadapter.viewevent.listener.OnLongClickEventListener
import java.util.*
import kotlin.math.abs

/**
 * Created by Ben Smiley on 24/11/14.
 */
open class ThreadDetailsActivity: ImagePreviewActivity() {

    open lateinit var thread: Thread
    open lateinit var adapter: SmartRecyclerAdapter

    open var onClickRelay = PublishRelay.create<User>()
    open var onLongClickRelay = PublishRelay.create<User>()
    open var threadUsers: MutableMap<User, ThreadUser> = HashMap()

    open lateinit var leaveButton: ButtonViewModel
    open lateinit var destroyButton: ButtonViewModel
    open lateinit var editButton: ButtonViewModel
    open lateinit var joinButton: ButtonViewModel

    open var items: MutableList<Any> = ArrayList()

    @LayoutRes
    override fun getLayout(): Int {
        return R.layout.activity_thread_details
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var thread: Thread? = null
        if (savedInstanceState != null) {
            thread = getDataFromBundle(savedInstanceState)
        } else {
            if (intent.extras != null) {
                thread = getDataFromBundle(intent.extras)
            } else {
                finish()
            }
        }
        if (thread == null) {
            ToastHelper.show(this, R.string.error_thread_not_found)
            finish()
        } else {
            this.thread = thread
        }
        initViews()
    }

    override fun initViews() {
        super.initViews()

        appbar.addOnOffsetChangedListener(ProfileViewOffsetChangeListener(avatarImageView))
        appbar.addOnOffsetChangedListener(ProfileViewOffsetChangeListener(onlineIndicator))

        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener() { appBarLayout: AppBarLayout, i: Int ->
            val fraction: Float = abs(i / appBarLayout.totalScrollRange.toFloat())
            val p = recyclerView.layoutParams
            if (p is ViewGroup.MarginLayoutParams) {
                p.topMargin = (45 * (1 - fraction)).toInt()
                recyclerView.requestLayout()
            }
        })

        leaveButton = ButtonViewModel(getString(R.string.leave_chat), resources.getColor(R.color.red), object : ButtonRunnable {
            override fun run(value: Activity) {
                ChatSDK.thread().leaveThread(thread).doOnComplete { }.subscribe(this@ThreadDetailsActivity)
            }
        })

        destroyButton = ButtonViewModel(getString(R.string.destroy), resources.getColor(R.color.red), object : ButtonRunnable {
            override fun run(value: Activity) {
                ChatSDK.thread().destroy(thread).subscribe()
            }
        })

        editButton = ButtonViewModel(getString(R.string.edit), resources.getColor(R.color.blue), object : ButtonRunnable {
            override fun run(value: Activity) {
                ChatSDK.ui().startEditThreadActivity(this@ThreadDetailsActivity, thread.entityID)
            }
        })

        joinButton = ButtonViewModel(getString(R.string.join), resources.getColor(R.color.blue), object : ButtonRunnable {
            override fun run(value: Activity) {
                ChatSDK.thread().joinThread(thread).doOnError(this@ThreadDetailsActivity).subscribe()
            }
        })

        if (thread.typeIs(ThreadType.Group)) {
            onlineIndicator.visibility = View.INVISIBLE
        } else {
            onlineIndicator.visibility = View.VISIBLE
        }

        adapter = SmartRecyclerAdapter
                .items(items)
                .map(ThreadUser::class, ThreadUserViewHolder::class)
                .map(SectionViewModel::class, SectionViewHolder::class)
                .map(DividerViewModel::class, DividerViewHolder::class)
                .map(ButtonViewModel::class, ButtonViewHolder::class)
                .add(OnClickEventListener {
                    val item = items[it.position]
                    if (item is ThreadUser) {
                        onClickRelay.accept(item.user)
                        // Refresh the roles in case another user's role changes
                        ChatSDK.thread().refreshRoles(thread).observeOn(RX.main()).doOnComplete(Action {
                            ChatSDK.ui().startModerationActivity(this, thread.entityID, item.user.entityID)
                        }).subscribe()
                    }
                    if (item is ButtonViewModel) {
                        item.click(this)
                    }
                })
                .add(OnLongClickEventListener {
                    val item = items[it.position]
                    if (item is ThreadUser) {
                        onLongClickRelay.accept(item.user)
                    }
                })
                .into(recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadUserAdded))
                .filter(NetworkEvent.filterThreadEntityID(thread.entityID))
                .subscribe(Consumer { networkEvent: NetworkEvent ->
                    reloadData()
                }, this))

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadUserRemoved))
                .filter(NetworkEvent.filterThreadEntityID(thread.entityID))
                .subscribe(Consumer { networkEvent: NetworkEvent ->
//                    if (networkEvent.user.isMe) {
                        reloadData()
//                    } else {
//                        remove(networkEvent.user)
//                    }
                }, this))

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadMetaUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.entityID))
                .subscribe(Consumer { networkEvent: NetworkEvent? ->
                    reloadInterface()
                }, this))

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterRoleUpdated(thread))
                .subscribe(Consumer { networkEvent: NetworkEvent ->
                    if (networkEvent.user.isMe) {
                        reloadButtons()
                    }
                    reloadData()
                }, this))

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterPresence(thread))
                .subscribe { networkEvent: NetworkEvent ->
                    update(networkEvent.user)
                })

        addUsersFab.setOnClickListener { v: View? ->
            addUsersFab.isEnabled = false
            ChatSDK.ui().startAddUsersToThreadActivity(this, thread.entityID)
        }

        refreshFab.setOnClickListener { v: View? ->
            ChatSDK.thread().refreshRoles(thread).subscribe()
        }

        val profileHeader = UIModule.config().profileHeaderImage
        headerImageView.setImageResource(profileHeader)

        reloadData()
        reloadInterface()
        reloadButtons()
    }

    override fun onStart() {
        super.onStart()
        addUsersFab.isEnabled = true
    }

    open fun reloadInterface() {

        val name = Strings.nameForThread(thread)

        supportActionBar?.title = name
        supportActionBar?.setHomeButtonEnabled(true)

        avatarImageView.setOnClickListener { v: View? -> zoomImageFromThumbnail(avatarImageView, thread.imageUrl) }
        ThreadImageBuilder.load(avatarImageView, thread, Dimen.from(this, R.dimen.large_avatar_width))
    }

    open fun reloadButtons() {
        if (ChatSDK.thread().canAddUsersToThread(thread)) {
            addUsersFab.setImageDrawable(Icons.get(this, Icons.choose().add, R.color.white))
            addUsersFab.visibility = View.VISIBLE
        } else {
            addUsersFab.visibility = View.INVISIBLE
        }
        if (ChatSDK.thread().canRefreshRoles(thread)) {
            refreshFab.setImageDrawable(Icons.get(this, Icons.choose().refresh, R.color.white))
            refreshFab.visibility = View.VISIBLE
        } else {
            refreshFab.visibility = View.INVISIBLE
        }
    }

    open fun reloadData() {

        items.clear()

        val isGroup = thread.typeIs(ThreadType.Group);

        // If this is not a dialog we will load the contacts of the user.
        var threadUsers = mutableListOf<ThreadUser>()
        for (user in thread.members) {
            threadUsers.add(getThreadUser(user))
        }

        if (isGroup) {
            items.add(SectionViewModel(getString(R.string.me)).hideBorders(true))
            items.add(getThreadUser(ChatSDK.currentUser()))
            if (threadUsers.isNotEmpty()) {
                items.add(SectionViewModel(getString(R.string.participants)))
            }
        } else {
            items.add(SectionViewModel(""))
        }

        UserSorter.sortThreadUsers(threadUsers)

        items.addAll(threadUsers)

        // Only the creator can modify the group. Also, private 1-to-1 chats can't be edited
        if (ChatSDK.thread().canEditThreadDetails(thread)) {
            items.add(DividerViewModel())
            items.add(editButton)
        }

        if (ChatSDK.thread().canLeaveThread(thread)) {
            items.add(DividerViewModel())
            items.add(leaveButton)
        }

        if (ChatSDK.thread().canJoinThread(thread)) {
            items.add(DividerViewModel())
            items.add(joinButton)
        }

        if (ChatSDK.thread().canDestroy(thread)) {
            items.add(DividerViewModel())
            items.add(destroyButton)
        }

        items.add(SectionViewModel("").hideBorders(bottom = true))

        adapter.setItems(items)
    }

    open fun getThreadUser(user: User): ThreadUser {
        val tu = threadUsers[user] ?: ThreadUser(thread, user)
        threadUsers[user] = tu
        return tu
    }

    open fun update(user: User) {
        adapter.notifyItemChanged(items.indexOf(getThreadUser(user)))
    }

    open fun remove(user: User) {
        val index = items.indexOf(getThreadUser(user))
        if (index >= 0) {
            adapter.removeItem(index, true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (ChatSDK.thread().canRefreshRoles(thread)) {
            ChatSDK.thread().refreshRoles(thread).subscribe()
        }
        //        reloadData(false);
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish() // Finish needs to be called before animate exit
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val thread = getDataFromBundle(intent.extras)
        if (thread != null) {
            this.thread = thread
        }
    }

    open fun getDataFromBundle(bundle: Bundle?): Thread? {
        if (bundle == null) {
            return null
        }
        val threadEntityID = bundle.getString(Keys.IntentKeyThreadEntityID)
        if (threadEntityID != null && threadEntityID.isNotEmpty()) {
            return ChatSDK.db().fetchThreadWithEntityID(threadEntityID)
        } else {
            finish()
        }
        return null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Keys.IntentKeyThreadEntityID, thread.entityID)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_thread_details_menu, menu)
        if (!ChatSDK.thread().muteEnabled(thread)) {
            menu.removeItem(R.id.action_mute)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        if (item.itemId == R.id.action_mute) {
            if (thread.isMuted) {
                ChatSDK.thread().unmute(thread).subscribe(this)
            } else {
                ChatSDK.thread().mute(thread).subscribe(this)
            }
            invalidateOptionsMenu()
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_mute)
        if (item != null) {
            if (thread.isMuted) {
                item.title = getString(R.string.unmute_notifications)
            } else {
                item.title = getString(R.string.mute_notifications)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

}