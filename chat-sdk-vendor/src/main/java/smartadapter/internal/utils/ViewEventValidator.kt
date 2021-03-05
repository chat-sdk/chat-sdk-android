package smartadapter.internal.utils

/*
 * Created by Manne Öhlund on 2019-06-11.
 * Copyright (c) All rights reserved.
 */

import io.github.manneohlund.smartrecycleradapter.R

/**
 * Checks if auto view events are of the predefined types provided by the SmartRecyclerAdapter library.
 */
object ViewEventValidator {

    private val autoViewEvents = listOf(
        R.id.undefined,
        R.id.event_on_click,
        R.id.event_on_long_click,
        R.id.event_on_item_selected
    )

    fun isViewEventIdValid(viewEventId: Int): Boolean {
        return autoViewEvents.contains(viewEventId)
    }
}
