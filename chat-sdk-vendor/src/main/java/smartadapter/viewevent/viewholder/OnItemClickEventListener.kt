package smartadapter.viewevent.viewholder

import smartadapter.viewevent.model.ViewEvent

/**
 * Smart adapter item view click listener.
 */
interface OnItemClickEventListener {

    fun onViewEvent(event: ViewEvent.OnClick)
}
