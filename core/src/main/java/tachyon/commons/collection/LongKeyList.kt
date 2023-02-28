/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.commons.collection

import kotlin.Throws
import kotlin.jvm.Synchronized
import tachyon.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import tachyon.commons.collection.LongKeyList.Pair
import tachyon.commons.collection.AbstractCollection
import tachyon.runtime.type.Array
import java.sql.Array
import tachyon.commons.lang.Pair
import tachyon.runtime.exp.CatchBlockImpl.Pair
import tachyon.runtime.type.util.ListIteratorImpl
import tachyon.runtime.type.Lambda
import java.util.Random
import tachyon.runtime.config.Constants
import tachyon.runtime.engine.Request
import tachyon.runtime.engine.ExecutionLogSupport.Pair
import tachyon.runtime.functions.other.NullValue
import tachyon.runtime.functions.string.Val
import tachyon.runtime.reflection.Reflector.JavaAnnotation
import tachyon.transformer.cfml.evaluator.impl.Output
import tachyon.transformer.cfml.evaluator.impl.Property
import tachyon.transformer.bytecode.statement.Condition.Pair

/**
 * class to fill objects, objects will be sorted by long key.
 */
class LongKeyList {
    private val root: Pair

    /**
     * adds a new object to the stack
     *
     * @param key key as long
     * @param value object to fill
     */
    fun add(key: Long, value: Object) {
        add(key, value, root)
    }

    /**
     * @param key
     * @param value
     * @param parent
     */
    private fun add(key: Long, value: Object, parent: Pair?) {
        if (parent!!.value == null) parent.setData(key, value) else if (key < parent.key) add(key, value, parent.left) else add(key, value, parent.right)
    }

    /**
     * @return returns the first object in stack
     */
    fun shift(): Object? {
        var oldest: Pair? = root
        while (oldest!!.left != null && oldest.left!!.value != null) oldest = oldest.left
        val rtn: Object? = oldest.value
        oldest.copy(oldest.right)
        return rtn
    }

    /**
     * @return returns the last object in Stack
     */
    fun pop(): Object? {
        var oldest: Pair? = root
        while (oldest!!.right != null && oldest.right!!.value != null) oldest = oldest.right
        val rtn: Object? = oldest.value
        oldest.copy(oldest.left)
        return rtn
    }

    /**
     * @param key key to value
     * @return returns the value to the key
     */
    operator fun get(key: Long): Object? {
        var current: Pair? = root
        while (true) {
            if (current == null || current.key == 0L) {
                return null
            } else if (current.key == key) return current.value else if (current.key < key) current = current.right else if (current.key > key) current = current.left
        }
    }

    internal inner class Pair {
        /**
         * key for value
         */
        var key: Long = 0

        /**
         * value object
         */
        var value: Object? = null

        /**
         * left side
         */
        var left: Pair? = null

        /**
         * right side
         */
        var right: Pair? = null

        /**
         * sets data to Pair
         *
         * @param key
         * @param value
         */
        fun setData(key: Long, value: Object?) {
            this.key = key
            this.value = value
            left = Pair()
            right = Pair()
        }

        /**
         * @param pair
         */
        fun copy(pair: Pair?) {
            if (pair != null) {
                left = pair.left
                right = pair.right
                value = pair.value
                key = pair.key
            } else {
                left = null
                right = null
                value = null
                key = 0
            }
        }
    }

    /**
     * constructor of the class
     */
    init {
        root = Pair()
    }
}