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
package lucee.runtime.text.feed

import kotlin.Throws
import kotlin.jvm.Synchronized
import lucee.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import lucee.commons.collection.LongKeyList.Pair
import lucee.commons.collection.AbstractCollection
import lucee.runtime.type.Array
import java.sql.Array
import lucee.commons.lang.Pair
import lucee.runtime.exp.CatchBlockImpl.Pair
import lucee.runtime.type.util.ListIteratorImpl
import lucee.runtime.type.Lambda
import java.util.Random
import lucee.runtime.config.Constants
import lucee.runtime.engine.Request
import lucee.runtime.engine.ExecutionLogSupport.Pair
import lucee.runtime.functions.other.NullValue
import lucee.runtime.functions.string.Val
import lucee.runtime.reflection.Reflector.JavaAnnotation
import lucee.transformer.cfml.evaluator.impl.Output
import lucee.transformer.cfml.evaluator.impl.Property
import lucee.transformer.bytecode.statement.Condition.Pair

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