package smartadapter.viewholder

/*
 * Created by Manne Öhlund on 2019-08-16.
 * Copyright (c) All rights reserved.
 */

import android.view.View

/**
 * Provides target view for draggable purposes.
 * Lets ItemTouchHelper handler to bind [androidx.recyclerview.widget.ItemTouchHelper.startDrag].
 */
interface DraggableViewHolder {

    /**
     * Target draggable view
     */
    val draggableView: View
}
