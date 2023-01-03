package smartadapter.viewevent.swipe

/*
 * Created by Manne Öhlund on 2019-08-17.
 * Copyright (c) All rights reserved.
 */

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import smartadapter.SmartRecyclerAdapter
import smartadapter.SmartViewHolderType
import smartadapter.extension.ItemTouchBinder
import smartadapter.extension.SmartViewHolderBinder
import smartadapter.viewevent.listener.OnViewEventListener
import smartadapter.viewevent.model.ViewEvent

/**
 * Type alias for swipe direction integer value.
 */
typealias Direction = Int

/**
 * Type alias for swipe flags integer.
 */
typealias SwipeFlags = Int

/**
 * Defines basic functionality of the DragAndDropExtension.
 */
abstract class SwipeEventBinder : ItemTouchHelper.Callback(),
    OnViewEventListener<ViewEvent.OnItemSwiped>,
    SmartViewHolderBinder,
    ItemTouchBinder<SwipeEventBinder> {

    /**
     * The target [SmartRecyclerAdapter].
     */
    abstract var smartRecyclerAdapter: SmartRecyclerAdapter

    /**
     * Sets target swipe flags.
     * @see ItemTouchHelper.LEFT
     *
     * @see ItemTouchHelper.RIGHT
     */
    abstract var swipeFlags: SwipeFlags

    /**
     * Defines if item should be draggable after long press.
     */
    abstract var longPressDragEnabled: Boolean

    /**
     * Sets target view holder types that should be draggable.
     */
    abstract var viewHolderTypes: List<SmartViewHolderType>

    /**
     * Builds and binds the drag and drop mechanism to target recycler view
     */
    abstract override fun bind(
        smartRecyclerAdapter: SmartRecyclerAdapter,
        recyclerView: RecyclerView
    ) : SwipeEventBinder
}
