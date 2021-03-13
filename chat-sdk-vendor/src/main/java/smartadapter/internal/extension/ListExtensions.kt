package smartadapter.internal.extension


/*
 * Created by Manne Öhlund on 2019-09-19.
 * Copyright (c) All rights reserved.
 */

import kotlin.reflect.full.allSupertypes

fun List<*>.isMutable() = this::class.allSupertypes.any { it.toString() == "kotlin.collections.MutableList<E>" }
