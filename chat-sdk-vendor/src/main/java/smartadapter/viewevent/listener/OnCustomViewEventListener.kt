package smartadapter.viewevent.listener

import smartadapter.SmartRecyclerAdapter
import smartadapter.extension.SmartViewHolderBinder
import smartadapter.listener.OnCreateViewHolderListener
import smartadapter.viewevent.model.ViewEvent
import smartadapter.viewevent.viewholder.CustomViewEventListenerHolder
import smartadapter.viewholder.SmartViewHolder

/**
 * Contains the logic for passing itself to a [SmartViewHolder]
 * via [CustomViewEventListenerHolder] interface to enable posting of custom [ViewEvent].
 */
open class OnCustomViewEventListener(
    override val identifier: Any = OnCustomViewEventListener::class,
    override var eventListener: (ViewEvent) -> Unit
) : OnViewEventListener<ViewEvent>,
    SmartViewHolderBinder,
    OnCreateViewHolderListener {

    override fun onCreateViewHolder(
        adapter: SmartRecyclerAdapter,
        viewHolder: SmartViewHolder<Any>
    ) {
        if (viewHolder is CustomViewEventListenerHolder) {
            viewHolder.customViewEventListener = eventListener
        }
    }
}
