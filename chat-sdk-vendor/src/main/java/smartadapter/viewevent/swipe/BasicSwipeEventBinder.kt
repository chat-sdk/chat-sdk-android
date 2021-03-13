package smartadapter.viewevent.swipe

/*
 * Created by Manne Öhlund on 2019-08-15.
 * Copyright (c) All rights reserved.
 */

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import smartadapter.SmartRecyclerAdapter
import smartadapter.SmartViewHolderType
import smartadapter.viewevent.model.ViewEvent
import smartadapter.viewevent.viewholder.OnItemSwipedListener
import smartadapter.viewholder.SmartViewHolder
import kotlin.math.abs

/**
 * The basic implementation of [SwipeEventBinder].
 *
 * @see SwipeEventBinder
 */
open class BasicSwipeEventBinder(
    override val identifier: Any = BasicSwipeEventBinder::class,
    override var longPressDragEnabled: Boolean = false,
    override var swipeFlags: SwipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    override var viewHolderTypes: List<SmartViewHolderType> = listOf(SmartViewHolder::class),
    override var eventListener: (ViewEvent.OnItemSwiped) -> Unit
) : SwipeEventBinder() {

    override lateinit var smartRecyclerAdapter: SmartRecyclerAdapter

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        var swipeFlags = 0
        for (viewHolderType in viewHolderTypes) {
            if (viewHolderType.java.isAssignableFrom(viewHolder.javaClass)) {
                swipeFlags = this.swipeFlags
                break
            }
        }
        return makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is OnItemSwipedListener) {
            viewHolder.onItemSwiped(
                ViewEvent.OnItemSwiped(
                    smartRecyclerAdapter,
                    viewHolder as SmartViewHolder<*>,
                    viewHolder.adapterPosition,
                    viewHolder.itemView,
                    direction
                )
            )
        }
        eventListener.invoke(
            ViewEvent.OnItemSwiped(
                smartRecyclerAdapter,
                viewHolder as SmartViewHolder<*>,
                viewHolder.adapterPosition,
                viewHolder.itemView,
                direction
            )
        )
    }

    override fun isLongPressDragEnabled(): Boolean = longPressDragEnabled

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val alpha = 1 - abs(dX) / recyclerView.width
            viewHolder.itemView.alpha = alpha
        }
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun bind(
        smartRecyclerAdapter: SmartRecyclerAdapter,
        recyclerView: RecyclerView
    ): BasicSwipeEventBinder {
        this.smartRecyclerAdapter = smartRecyclerAdapter
        val touchHelper = ItemTouchHelper(this)
        touchHelper.attachToRecyclerView(recyclerView)
        return this
    }
}
