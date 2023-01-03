package smartadapter.viewevent.dragdrop

/*
 * Created by Manne Öhlund on 2019-08-15.
 * Copyright (c) All rights reserved.
 */

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import smartadapter.SmartRecyclerAdapter
import smartadapter.SmartViewHolderType
import smartadapter.extension.SmartViewHolderBinder
import smartadapter.listener.OnCreateViewHolderListener
import smartadapter.viewevent.model.ViewEvent
import smartadapter.viewholder.DraggableViewHolder
import smartadapter.viewholder.SmartAdapterHolder
import smartadapter.viewholder.SmartViewHolder
import java.util.Collections
import java.util.HashSet

/**
 * Automatically moves an item in [SmartRecyclerAdapter] dragged and dropped.
 *
 * @see BasicDragAndDropBinder
 * @see SmartAdapterHolder
 */
class AutoDragAndDropBinder(
    override val identifier: Any = AutoDragAndDropBinder::class,
    override var dragFlags: Int = 0,
    override var viewHolderTypes: List<SmartViewHolderType> = listOf(SmartViewHolder::class),
    override var longPressDragEnabled: Boolean = false,
    override var eventListener: (ViewEvent.OnItemMoved) -> Unit
) : BasicDragAndDropBinder(
    eventListener = eventListener
) {

    private val draggableViews = HashSet<RecyclerView.ViewHolder>()

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val moved = super.onMove(recyclerView, viewHolder, target)
        if (moved) {
            val oldPosition = viewHolder.adapterPosition
            val newPosition = target.adapterPosition
            with (smartRecyclerAdapter) {
                //val targetItem = getItems()[oldPosition]
                //getItems().removeAt(oldPosition)
                //getItems().add(newPosition, targetItem)
                Collections.swap(getItems(), oldPosition, newPosition)
                notifyItemMoved(oldPosition, newPosition)
            }
        }
        return moved
    }

    /**
     * If isLongPressDragEnabled returns false this extension will try to find [DraggableViewHolder]s
     * and set [android.view.View.OnTouchListener] on a draggable view that [DraggableViewHolder] returns via
     * [DraggableViewHolder.draggableView].
     *
     * @see DraggableViewHolder
     *
     * @param recyclerView target RecyclerView
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun setupDragAndDrop(recyclerView: RecyclerView) {
        super.setupDragAndDrop(recyclerView)

        if (!isLongPressDragEnabled) {
            smartRecyclerAdapter.add(object : DragDropViewHolderBinder(){
                override val identifier: Any
                    get() = DragDropViewHolderBinder::class

                override fun onCreateViewHolder(
                    adapter: SmartRecyclerAdapter,
                    viewHolder: SmartViewHolder<Any>
                ) {
                    if (viewHolder is DraggableViewHolder && !draggableViews.contains(viewHolder)) {
                        draggableViews.add(viewHolder)
                        (viewHolder as DraggableViewHolder).draggableView
                            .setOnTouchListener { _, event ->
                                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                                    touchHelper?.startDrag(viewHolder)
                                }
                                false
                            }
                    }
                }
            })
        }
    }
}

abstract class DragDropViewHolderBinder : SmartViewHolderBinder, OnCreateViewHolderListener
