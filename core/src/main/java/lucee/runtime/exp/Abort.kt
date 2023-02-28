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
package lucee.runtime.exp

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

/**
 * This Exception will be thrown, when page Excecution is aborted (tag abort).
 */
class Abort : AbortException {
    var scope: Int
        private set

    /**
     * Constructor of the Class
     */
    constructor(scope: Int) : super("Page request is aborted") {
        this.scope = scope
    }

    protected constructor(scope: Int, msg: String?) : super(msg) {
        this.scope = scope
    }

    companion object {
        const val SCOPE_PAGE = 0
        const val SCOPE_REQUEST = 1
        fun newInstance(scope: Int): Abort? {
            return Abort(scope)
        }

        fun isSilentAbort(t: Throwable?): Boolean {
            return if (t is PageExceptionBox) {
                isSilentAbort((t as PageExceptionBox?).getPageException())
            } else t is Abort && t !is RequestTimeoutException
        }

        fun isAbort(t: Throwable?): Boolean {
            if (t is Abort) return true
            return if (t is PageExceptionBox) {
                (t as PageExceptionBox?).getPageException() is Abort
            } else false
        }

        fun isAbort(t: Throwable?, scope: Int): Boolean {
            return if (t is PageExceptionBox) {
                isAbort((t as PageExceptionBox?).getPageException(), scope)
            } else t is Abort && (t as Abort?)!!.scope == scope
        }
    }
}