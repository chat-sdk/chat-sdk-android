package sdk.chat.ui.recycler

import android.os.Bundle
import androidx.activity.viewModels
import kotlinx.android.synthetic.main.activity_moderation.*
import sdk.chat.core.dao.Keys
import sdk.chat.core.dao.Thread
import sdk.chat.core.dao.User
import sdk.chat.core.session.ChatSDK
import sdk.chat.ui.R
import sdk.chat.ui.activities.BaseActivity
import sdk.chat.ui.utils.ToastHelper
import smartadapter.SmartRecyclerAdapter
import smartadapter.viewevent.listener.OnClickEventListener
import smartadapter.viewevent.listener.OnMultiItemCheckListener
import smartadapter.viewevent.listener.OnSingleItemCheckListener
import smartadapter.viewevent.model.ViewEvent
import smartadapter.viewevent.viewmodel.ViewEventViewModel

open class ModerationActivity: BaseActivity() {

    class SingleItemCheckedViewModel : ViewEventViewModel<ViewEvent, OnSingleItemCheckListener>(
            OnSingleItemCheckListener(viewId = R.id.radioButton)
    )

    protected var user: User? = null
    protected var thread: Thread? = null

    protected lateinit var smartRecyclerAdapter: SmartRecyclerAdapter
    private val singleItemCheckedViewModel: SingleItemCheckedViewModel by viewModels()

    override fun getLayout(): Int {
        return R.layout.activity_moderation
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()

        val userEntityID = intent.getStringExtra(Keys.IntentKeyUserEntityID)
        if (userEntityID != null && userEntityID.isNotEmpty()) {
            user = ChatSDK.db().fetchUserWithEntityID(userEntityID)
        }

        val threadEntityID = intent.getStringExtra(Keys.IntentKeyThreadEntityID)
        if (threadEntityID != null && threadEntityID.isNotEmpty()) {
            thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID)
        }

        if (user == null || thread === null) {
            ToastHelper.show(this, R.string.user_entity_id_not_set)
            finish()
            return
        }

        val items = (0..100).toMutableList()

        smartRecyclerAdapter = SmartRecyclerAdapter
                .items(items)
                .map(Integer::class, SimpleSelectableRadioButtonViewHolder::class)
                .add(singleItemCheckedViewModel.observe(this) {
                    handleCheckEvent(it)
                })
                .add(OnClickEventListener {
                    showToast("onClick ${it.position}")
                })
                .into(recyclerView)

    }

    private fun handleCheckEvent(it: ViewEvent) {
        showToast("Item click ${it.position}\n" +
                "${singleItemCheckedViewModel.viewEventListener.selectedItemsCount} of " +
                "${smartRecyclerAdapter.itemCount} selected items")
    }

}