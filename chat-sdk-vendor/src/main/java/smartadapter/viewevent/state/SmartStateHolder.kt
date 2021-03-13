package smartadapter.viewevent.state

import smartadapter.Position
import java.util.TreeSet

/*
 * Created by Manne Öhlund on 2019-08-09.
 * Copyright (c) All rights reserved.
 */

/**
 * Defines the SortedSet for enabled adapter positions.
 */
interface SmartStateHolder {

    /**
     * Provides sorted set of selected positions.
     */
    var selectedItems: TreeSet<Int>

    /**
     * Adds the position to the data set.
     * @param position the adapter position
     */
    fun enable(position: Position)

    /**
     * Adds the position to the data set for all target items.
     */
    fun enableAll()

    /**
     * Removes the position from the data set.
     * @param position the adapter position
     */
    fun disable(position: Position)

    /**
     * Removes all the positions from the data set.
     */
    fun disableAll()

    /**
     * Enables or disables the position in the data set.
     * @param position the adapter position
     */
    fun toggle(position: Position)

    /**
     * Clears all the stored states.
     */
    fun clear()
}
