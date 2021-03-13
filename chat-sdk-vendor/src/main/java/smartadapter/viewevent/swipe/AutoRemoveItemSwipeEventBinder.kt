package smartadapter.viewevent.swipe

/*
 * Created by Manne Öhlund on 2019-08-17.
 * Copyright (c) All rights reserved.
 */

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

import smartadapter.SmartRecyclerAdapter
import smartadapter.SmartViewHolderType
import smartadapter.viewevent.model.ViewEvent
import smartadapter.viewholder.SmartAdapterHolder
import smartadapter.viewholder.SmartViewHolder

/**
 * Automatically removes an item in [SmartRecyclerAdapter] when swiped.
 *
 * @see BasicSwipeEventBinder
 *
 * @see SmartAdapterHolder
 */
class AutoRemoveItemSwipeEventBinder(
    override val identifier: Any = AutoRemoveItemSwipeEventBinder::class,
    override var swipeFlags: SwipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    override var viewHolderTypes: List<SmartViewHolderType> = listOf(SmartViewHolder::class),
    override var longPressDragEnabled: Boolean = false,
    override var eventListener: (ViewEvent.OnItemSwiped) -> Unit
) : BasicSwipeEventBinder(
    eventListener = eventListener
) {

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        super.onSwiped(viewHolder, direction)
        smartRecyclerAdapter.removeItem(viewHolder.adapterPosition)
    }
}
