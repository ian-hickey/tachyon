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
package tachyon.commons.digest

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

object HashUtil {
    private val byteTable = createLookupTable()
    private const val HSTART = -0x44bf19b25dfa4f9cL
    private const val HMULT = 7664345821815920749L
    fun create64BitHash(cs: CharSequence): Long {
        var h = HSTART
        val hmult = HMULT
        val ht = byteTable
        val len: Int = cs.length()
        for (i in 0 until len) {
            val ch: Char = cs.charAt(i)
            h = h * hmult xor ht[ch.toInt() and 0xff]
            h = h * hmult xor ht[ch.toInt() ushr 8 and 0xff]
        }
        return if (h < 0) 0 - h else h
    }

    fun create64BitHashAsString(cs: CharSequence): String {
        return toString(create64BitHash(cs), Character.MAX_RADIX)
    }

    fun create64BitHashAsString(cs: CharSequence, radix: Int): String {
        return toString(create64BitHash(cs), radix)
    }

    private fun createLookupTable(): LongArray {
        val _byteTable = LongArray(256)
        var h = 0x544B2FBACAAF1684L
        for (i in 0..255) {
            for (j in 0..30) {
                h = h ushr 7 xor h
                h = h shl 11 xor h
                h = h ushr 10 xor h
            }
            _byteTable[i] = h
        }
        return _byteTable
    }
}