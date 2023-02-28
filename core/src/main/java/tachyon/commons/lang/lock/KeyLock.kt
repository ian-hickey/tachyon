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
package tachyon.commons.lang.lock

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