package smartadapter.internal.exception

/*
 * Created by Manne Öhlund on 2019-07-17.
 * Copyright (c) All rights reserved.
 */

import android.view.View
import android.view.ViewGroup

/**
 * Exception indicates that the constructor of a target class with specified params was not found.
 */
class ConstructorNotFoundException(viewHolderClass: Class<*>) : RuntimeException(
        "Constructor for '$viewHolderClass' " +
        "with only one valid parameter '${View::class.java.name}' " +
        "or '${ViewGroup::class.java.name}' " +
        "not found")
