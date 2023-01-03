package smartadapter.viewevent.listener

import androidx.annotation.IdRes
import io.github.manneohlund.smartrecycleradapter.R
import smartadapter.SmartRecyclerAdapter
import smartadapter.SmartViewHolderType
import smartadapter.ViewId
import smartadapter.extension.SmartViewHolderBinder
import smartadapter.extension.findView
import smartadapter.listener.OnCreateViewHolderListener
import smartadapter.viewevent.model.ViewEvent
import smartadapter.viewevent.viewholder.OnItemClickEventListener
import smartadapter.viewholder.SmartViewHolder

/**
 * Contains the logic for the multi view holder views click for recycler adapter positions.
 */
open class OnClickEventListener(
    override val viewHolderType: SmartViewHolderType = SmartViewHolder::class,
    @IdRes
    override vararg val viewIds: ViewId = intArrayOf(R.id.undefined),
    override val identifier: Any = OnClickEventListener::class,
    override var eventListener: (ViewEvent.OnClick) -> Unit
) : OnViewEventListener<ViewEvent.OnClick>,
    SmartViewHolderBinder,
    OnCreateViewHolderListener {

    override fun onCreateViewHolder(
        adapter: SmartRecyclerAdapter,
        viewHolder: SmartViewHolder<Any>
    ) {
        viewIds.forEach {
            with(findView(it, viewHolder)) {
                setOnClickListener { view ->
                    val event = ViewEvent.OnClick(
                        adapter,
                        viewHolder,
                        viewHolder.adapterPosition,
                        view
                    )
                    (viewHolder as? OnItemClickEventListener)?.let {
                        viewHolder.onViewEvent(event)
                    }
                    eventListener.invoke(event)
                }
            }
        }
    }
}
