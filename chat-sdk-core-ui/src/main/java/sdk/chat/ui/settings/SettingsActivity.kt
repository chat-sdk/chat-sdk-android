package sdk.chat.ui.settings

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_smart_recycler.*
import sdk.chat.ui.ChatSDKUI
import sdk.chat.ui.R
import sdk.chat.ui.activities.BaseActivity
import sdk.chat.ui.recycler.*
import smartadapter.SmartRecyclerAdapter
import smartadapter.viewevent.listener.OnClickEventListener

open class SettingsActivity: BaseActivity() {
    open lateinit var smartRecyclerAdapter: SmartRecyclerAdapter

    override fun getLayout(): Int {
        return R.layout.activity_smart_recycler
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()

        // Setup the items
        val items = ChatSDKUI.shared().settingsItems

        smartRecyclerAdapter = SmartRecyclerAdapter
                .items(items)
                .map(SectionViewModel::class, SectionViewHolder::class)
                .map(NavigationViewModel::class, NavigationViewHolder::class)
                .map(RadioViewModel::class, RadioViewHolder::class)
                .map(ButtonViewModel::class, ButtonViewHolder::class)
                .map(DividerViewModel::class, DividerViewHolder::class)
                .map(ToggleViewModel::class, ToggleViewHolder::class)
                .add(OnClickEventListener {

                    it.view.clearAnimation()

                    var model = smartRecyclerAdapter.getItem(it.position)
                    if (model is NavigationViewModel) {
                        model.click()
                    }
                    if (model is ButtonViewModel) {
                        model.click(this)
                    }
                    if (model is RadioViewModel) {
                        if (!model.checked) {
                            for (i in 0 until smartRecyclerAdapter.itemCount) {
                                var item = smartRecyclerAdapter.getItem(i)
                                if (item is RadioViewModel) {
                                    if (item.group == model.group) {
                                        item.checked = item == model
                                    }
                                }
                                smartRecyclerAdapter.notifyItemChanged(i)
                            }
                            model.click()
                        }
                    }
                })

                .into(recyclerView)

    }
}