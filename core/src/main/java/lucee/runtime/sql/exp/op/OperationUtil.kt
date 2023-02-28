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
package lucee.runtime.sql.exp.op

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

object OperationUtil {
    fun toString(operator: Int): String? {
        when (operator) {
            Operation.OPERATION2_DIVIDE -> return "/"
            Operation.OPERATION2_MINUS -> return "-"
            Operation.OPERATION2_MULTIPLY -> return "*"
            Operation.OPERATION2_PLUS -> return "+"
            Operation.OPERATION2_BITWISE -> return "^"
            Operation.OPERATION2_MOD -> return "%"
            Operation.OPERATION2_AND -> return "and"
            Operation.OPERATION2_OR -> return "or"
            Operation.OPERATION2_XOR -> return "xor"
            Operation.OPERATION2_EQ -> return "="
            Operation.OPERATION2_GT -> return ">"
            Operation.OPERATION2_GTE -> return ">="
            Operation.OPERATION2_LT -> return "<"
            Operation.OPERATION2_LTE -> return "<="
            Operation.OPERATION2_LTGT -> return "<>"
            Operation.OPERATION2_NEQ -> return "!="
            Operation.OPERATION2_NOT_LIKE -> return "not like"
            Operation.OPERATION2_LIKE -> return "like"
            Operation.OPERATION1_PLUS -> return "+"
            Operation.OPERATION1_MINUS -> return "-"
            Operation.OPERATION1_NOT -> return "not"
            Operation.OPERATION1_IS_NOT_NULL -> return "is not null"
            Operation.OPERATION1_IS_NULL -> return "is null"
        }
        return null
    }
}