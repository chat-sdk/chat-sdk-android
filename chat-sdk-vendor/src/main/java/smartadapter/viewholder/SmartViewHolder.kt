package smartadapter.viewholder

/*
 * Created by Manne Öhlund on 2019-06-10.
 * Copyright (c) All rights reserved.
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

import androidx.recyclerview.widget.RecyclerView

/**
 * Extension of [RecyclerView.ViewHolder] containing data item binding method.
 * @param <T> Data item type
 */
abstract class SmartViewHolder<T : Any>(view: View) : RecyclerView.ViewHolder(view), BindableViewHolder<T> {
    constructor(parentView: ViewGroup, @LayoutRes layout: Int) : this(
        LayoutInflater.from(parentView.context).inflate(layout, parentView, false)
    )
}