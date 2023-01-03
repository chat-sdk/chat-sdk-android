package smartadapter.viewevent.dragdrop

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
 * Defines basic functionality of the DragAndDropExtension.
 */
abstract class DragAndDropEventBinder : ItemTouchHelper.Callback(),
    OnViewEventListener<ViewEvent.OnItemMoved>,
    SmartViewHolderBinder,
    ItemTouchBinder<DragAndDropEventBinder> {

    /**
     * The target [SmartRecyclerAdapter].
     */
    abstract var smartRecyclerAdapter: SmartRecyclerAdapter

    /**
     * Sets target drag flags.
     * @see ItemTouchHelper.LEFT
     * @see ItemTouchHelper.RIGHT
     * @see ItemTouchHelper.UP
     * @see ItemTouchHelper.DOWN
     */
    abstract var dragFlags: Int

    /**
     * Defines [ItemTouchHelper] for custom item view touch handling.
     */
    abstract var touchHelper: ItemTouchHelper?

    /**
     * Defines target view holder types that should be draggable.
     */
    abstract var viewHolderTypes: List<SmartViewHolderType>

    /**
     * Defines if item should be draggable after long press.
     */
    abstract var longPressDragEnabled: Boolean

    /**
     * Defines the draggable flags or binds touch listener to target drag view.
     */
    abstract fun setupDragAndDrop(recyclerView: RecyclerView)

    /**
     * Builds and binds the drag and drop mechanism to target recycler view
     */
    override fun bind(
        smartRecyclerAdapter: SmartRecyclerAdapter,
        recyclerView: RecyclerView
    ): DragAndDropEventBinder {
        this.smartRecyclerAdapter = smartRecyclerAdapter
        this.touchHelper = ItemTouchHelper(this).apply {
            attachToRecyclerView(recyclerView)
        }
        setupDragAndDrop(recyclerView)
        return this
    }
}
