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
package tachyon.runtime.sql.exp.op

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