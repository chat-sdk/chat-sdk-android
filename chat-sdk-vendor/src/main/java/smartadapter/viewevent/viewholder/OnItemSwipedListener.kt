package smartadapter.viewevent.viewholder

import smartadapter.viewevent.model.ViewEvent
import smartadapter.viewevent.swipe.SwipeEventBinder

interface OnItemSwipedListener {

    /**
     * Called when event bound view triggers an [ViewEvent.OnItemSwiped] event.
     *
     * @see [SwipeEventBinder]
     */
    fun onItemSwiped(event: ViewEvent.OnItemSwiped)
}
