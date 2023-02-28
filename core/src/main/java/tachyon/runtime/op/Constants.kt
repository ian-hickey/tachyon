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
package tachyon.runtime.op

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
 * Constant Values
 */
object Constants {
    val EMPTY_OBJECT_ARRAY: Array<Object?>? = arrayOfNulls<Object?>(0)

    /**
     * Field `INTEGER_ZERO` equals Integer.valueOf(0)
     */
    val INTEGER_0: Integer? = Integer.valueOf(0)

    /**
     * Field `INTEGER_ONE` equals Integer.valueOf(1)
     */
    val INTEGER_1: Integer? = Integer.valueOf(1)
    val INTEGER_MINUS_ONE: Integer? = Integer.valueOf(-1)

    /**
     * Field `INTEGER_TWO` equals Integer.valueOf(8)
     */
    val INTEGER_2: Integer? = Integer.valueOf(2)

    /**
     * Field `INTEGER_THREE` equals Integer.valueOf(3)
     */
    val INTEGER_3: Integer? = Integer.valueOf(3)

    /**
     * Field `INTEGER_FOUR` equals Integer.valueOf(4)
     */
    val INTEGER_4: Integer? = Integer.valueOf(4)

    /**
     * Field `INTEGER_FIVE` equals Integer.valueOf(5)
     */
    val INTEGER_5: Integer? = Integer.valueOf(5)

    /**
     * Field `INTEGER_SIX` equals Integer.valueOf(6)
     */
    val INTEGER_6: Integer? = Integer.valueOf(6)

    /**
     * Field `INTEGER_SEVEN` equals Integer.valueOf(7)
     */
    val INTEGER_7: Integer? = Integer.valueOf(7)

    /**
     * Field `INTEGER_EIGHT` equals Integer.valueOf(8)
     */
    val INTEGER_8: Integer? = Integer.valueOf(8)

    /**
     * Field `INTEGER_NINE` equals Integer.valueOf(9)
     */
    val INTEGER_9: Integer? = Integer.valueOf(9)

    /**
     * Field `INTEGER_NINE` equals Integer.valueOf(9)
     */
    val INTEGER_10: Integer? = Integer.valueOf(10)
    val INTEGER_11: Integer? = Integer.valueOf(11)
    val INTEGER_12: Integer? = Integer.valueOf(12)
    const val SHORT_VALUE_ZERO = 0.toShort()
    val SHORT_ZERO: Short? = Short.valueOf(0.toShort())
    val LONG_ZERO: Long? = Long.valueOf(0)
    val DOUBLE_ZERO: Double? = Double.valueOf(0)

    /**
     * return an Integer object with same value
     *
     * @param i
     * @return Integer Object
     */
    @Deprecated
    @Deprecated("use Integer.valueOf() instead")
    fun Integer(i: Int): Integer? {
        // if(i>-1 && i<100) return INTEGER[i];
        return Integer.valueOf(i)
    }

    /**
     * return a Boolean object with same value
     *
     * @param b
     * @return Boolean Object
     */
    @Deprecated
    @Deprecated("use Boolean.valueOf() instead")
    fun Boolean(b: Boolean): Boolean? {
        return if (b) Boolean.TRUE else Boolean.FALSE
    }
}