package smartadapter.viewevent.viewholder

import smartadapter.viewevent.model.ViewEvent

/**
 * Smart adapter item view touch listener.
 */
interface OnItemTouchEventListener {

    fun onViewEvent(event: ViewEvent.OnTouchEvent)
}
