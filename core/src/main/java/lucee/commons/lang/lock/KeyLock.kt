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
package lucee.commons.lang.lock

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

class KeyLock {
    private val token = Token()
    private var listener: KeyLockListener

    constructor() {
        listener = NullKeyLockListener.getInstance()
    }

    constructor(listener: KeyLockListener) {
        this.listener = listener
    }

    fun start(key: String) {
        while (true) {
            // nobody inside
            synchronized(token) {
                if (token.value == null) {
                    token.value = key
                    token.count++
                    listener.onStart(token.value, true)
                    return
                }
                if (key.equalsIgnoreCase(token.value)) {
                    token.count++
                    listener.onStart(token.value, false)
                    return
                }
                try {
                    token.wait()
                } catch (e: InterruptedException) {
                }
            }
        }
    }

    fun end() {
        synchronized(token) {
            if (--token.count <= 0) {
                listener.onEnd(token.value, true)
                if (token.count < 0) token.count = 0
                token.value = null
            } else listener.onEnd(token.value, false)
            token.notify()
        }
    }

    fun setListener(listener: KeyLockListener) {
        this.listener = listener
    }
}

internal class Token {
    var count = 0
    var value: String? = null
}