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
package tachyon.runtime.exp

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