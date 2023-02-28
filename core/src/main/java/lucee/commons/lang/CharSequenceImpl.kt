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
package lucee.commons.lang

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

class CharSequenceImpl(chars: CharArray) : CharSequence {
    private val chars: CharArray
    private val str: String
    private var lcStr: String

    /**
     * Constructor of the class
     *
     * @param str
     */
    constructor(str: String) : this(str.toCharArray()) {}

    @Override
    fun charAt(index: Int): Char {
        return chars[index]
    }

    @Override
    fun length(): Int {
        return chars.size
    }

    @Override
    override fun subSequence(start: Int, end: Int): CharSequence {
        val dest = CharArray(end - start)
        System.arraycopy(chars, start, dest, 0, end - start)
        return CharSequenceImpl(dest)
    }

    @Override
    override fun toString(): String {
        return str
    }

    fun toLowerCaseString(): String {
        return lcStr
    }

    /**
     * Constructor of the class
     *
     * @param chars
     */
    init {
        str = String(chars)
        this.chars = chars
        var c: Char
        for (i in chars.indices) {
            c = chars[i]
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9')) {
                lcStr = str.toLowerCase()
                return
            }
        }
        lcStr = str
    }
}