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
package tachyon.runtime.text.feed

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

class El(
        /**
         * @return the quantity
         */
        val quantity: Short, attrs: Array<Attr?>?, hasChildren: Boolean) {
    private val attrs: Array<Attr?>?

    /**
     * @return the hasChildren
     */
    val isHasChildren: Boolean

    @JvmOverloads
    constructor(quantity: Short, attrs: Array<Attr?>? = null as Array<Attr?>?) : this(quantity, attrs, false) {
    }

    constructor(quantity: Short, attr: Attr?, hasChildren: Boolean) : this(quantity, arrayOf<Attr?>(attr), hasChildren) {}
    constructor(quantity: Short, attr: Attr?) : this(quantity, arrayOf<Attr?>(attr)) {}
    constructor(quantity: Short, hasChildren: Boolean) : this(quantity, null as Array<Attr?>?, hasChildren) {}

    /**
     * @return the attrs
     */
    fun getAttrs(): Array<Attr?>? {
        return attrs
    }

    fun isQuantity(quantity: Short): Boolean {
        return this.quantity == quantity
    }

    companion object {
        var QUANTITY_0_1: Short = 0
        var QUANTITY_0_N: Short = 4
        var QUANTITY_1: Short = 8
        var QUANTITY_1_N: Short = 16
        val QUANTITY_AUTO = QUANTITY_0_1
    }

    init {
        this.attrs = attrs
        isHasChildren = hasChildren
    }
}