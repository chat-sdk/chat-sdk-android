package smartadapter.internal.utils

/*
 * Created by Manne Öhlund on 24/03/17.
 * Copyright © 2017 All rights reserved.
 */

import smartadapter.internal.exception.ConstructorNotFoundException
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

object ReflectionUtils {

    @Throws(ConstructorNotFoundException::class)
    fun getConstructor(clazz: KClass<*>, vararg validConstructorClasses: KClass<*>): KFunction<Any> {
        if (validConstructorClasses.isEmpty()) {
            throw IllegalArgumentException("No validConstructorClasses passed")
        }

        for (constructor in clazz.constructors) {
            val targetParameterIndex = if (isInnerClass(clazz)) 1 else 0
            if (constructor.parameters.size == 1 + targetParameterIndex) {
                val constructorParameter = constructor.parameters[targetParameterIndex]
                for (validConstructorClass in validConstructorClasses) {
                    if (validConstructorClass == constructorParameter.type.jvmErasure) {
                        return constructor
                    }
                }
            }
        }

        throw ConstructorNotFoundException(clazz::class.java)
    }

    @Throws(Exception::class)
    fun invokeConstructor(constructor: KFunction<Any>, vararg args: Any?): Any {
        return constructor.call(*args)
    }

    fun isStatic(clazz: Class<*>): Boolean {
        return Modifier.isStatic(clazz.modifiers)
    }

    fun isInnerClass(clazz: KClass<*>): Boolean {
        return clazz.isInner
    }
}
