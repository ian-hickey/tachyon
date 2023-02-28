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
package tachyon.runtime.timer

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
 * Implementation of a simple Stopwatch
 */
class Stopwatch(unit: Int) {
    // public static final int UNIT_MICRO=4;
    private var start: Long = 0
    private var count = 0
    private var total: Long = 0
    var isRunning = false
    private val useNano: Boolean

    /**
     * start the watch
     */
    fun start() {
        isRunning = true
        start = _time()
    }

    private fun _time(): Long {
        return if (useNano) System.nanoTime() else System.currentTimeMillis()
    }

    /**
     * stops the watch
     *
     * @return returns the current time or 0 if watch not was running
     */
    fun stop(): Long {
        if (isRunning) {
            val time = _time() - start
            total += time
            count++
            isRunning = false
            return time
        }
        return 0
    }

    /**
     * @return returns the current time or 0 if watch is not running
     */
    fun time(): Long {
        return if (isRunning) _time() - start else 0
    }

    /**
     * @return returns the total elapsed time
     */
    fun totalTime(): Long {
        return total + time()
    }

    /**
     * @return returns how many start and stop was making
     */
    fun count(): Int {
        return count
    }

    /**
     * resets the stopwatch
     */
    fun reset() {
        start = 0
        count = 0
        total = 0
        isRunning = false
    }

    companion object {
        const val UNIT_MILLI = 1
        const val UNIT_NANO = 2
    }

    init {
        useNano = unit == UNIT_NANO
    }
}