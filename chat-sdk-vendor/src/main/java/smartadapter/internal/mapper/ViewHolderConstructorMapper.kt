package smartadapter.internal.mapper

/*
 * Created by Manne Öhlund on 2019-07-16.
 * Copyright (c) All rights reserved.
 */

import android.view.View
import android.view.ViewGroup
import smartadapter.SmartViewHolderType
import smartadapter.internal.utils.ReflectionUtils
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class ViewHolderConstructorMapper {

    private val viewHolderConstructorMapper = HashMap<KClass<*>, KFunction<Any>>()

    fun add(smartViewHolderClasses: Collection<SmartViewHolderType>) {
        for (smartViewHolderClass in smartViewHolderClasses) {
            add(smartViewHolderClass)
        }
    }

    fun add(smartViewHolderClass: SmartViewHolderType) {
        if (!viewHolderConstructorMapper.containsKey(smartViewHolderClass)) {
            viewHolderConstructorMapper[smartViewHolderClass] = ReflectionUtils.getConstructor(smartViewHolderClass, View::class, ViewGroup::class)
        }
    }

    fun getConstructor(smartViewHolderClass: SmartViewHolderType): KFunction<Any>? {
        return viewHolderConstructorMapper[smartViewHolderClass]
    }
}
