package smartadapter.viewevent.dragdrop

/*
 * Created by Manne Öhlund on 2019-08-15.
 * Copyright (c) All rights reserved.
 */

import android.annotation.SuppressLint
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import smartadapter.SmartRecyclerAdapter
import smartadapter.SmartViewHolderType
import smartadapter.viewevent.model.ViewEvent
import smartadapter.viewevent.swipe.Direction
import smartadapter.viewholder.SmartViewHolder

/**
 * The basic implementation of [DragAndDropEventBinder].
 *
 * @see DragAndDropEventBinder
 */
open class BasicDragAndDropBinder(
    override val identifier: Any = BasicDragAndDropBinder::class,
    override var dragFlags: Int = 0,
    override var touchHelper: ItemTouchHelper? = null,
    override var viewHolderTypes: List<SmartViewHolderType> = listOf(SmartViewHolder::class),
    override var longPressDragEnabled: Boolean = false,
    override var eventListener: (ViewEvent.OnItemMoved) -> Unit
) : DragAndDropEventBinder() {

    override lateinit var smartRecyclerAdapter: SmartRecyclerAdapter

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        for (viewHolderType in viewHolderTypes) {
            if (viewHolderType.java.isAssignableFrom(target.javaClass) && viewHolder.javaClass == target.javaClass) {
                eventListener.invoke(
                    ViewEvent.OnItemMoved(
                        smartRecyclerAdapter,
                        viewHolder as SmartViewHolder<*>,
                        viewHolder.adapterPosition,
                        viewHolder.itemView,
                        target as SmartViewHolder<*>
                    )
                )
                return true
            }
        }
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Direction) {
        // Noop
    }

    override fun isLongPressDragEnabled(): Boolean {
        return longPressDragEnabled
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupDragAndDrop(recyclerView: RecyclerView) {
        if (dragFlags == 0) {
            dragFlags = if (recyclerView.layoutManager is GridLayoutManager) {
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
            } else {
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (linearLayoutManager.orientation == LinearLayoutManager.HORIZONTAL) {
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                } else {
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                }
            }
        }
    }
}
