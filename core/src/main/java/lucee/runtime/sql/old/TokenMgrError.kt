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
package lucee.runtime.sql.old

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

class TokenMgrError : Error {
    @get:Override
    val message: String?
        get() = super.getMessage()

    constructor() {}
    constructor(s: String?, i: Int) : super(s) {
        errorCode = i
    }

    constructor(flag: Boolean, i: Int, j: Int, k: Int, s: String?, c: Char, l: Int) : this(LexicalError(flag, i, j, k, s, c), l) {}

    var errorCode = 0

    companion object {
        protected fun addEscapes(s: String?): String? {
            val stringbuffer = StringBuffer()
            for (i in 0 until s!!.length()) {
                var c: Char
                when (s.charAt(i)) {
                    0 -> {
                    }
                    8 -> stringbuffer.append("\\b")
                    9 -> stringbuffer.append("\\t")
                    10 -> stringbuffer.append("\\n")
                    12 -> stringbuffer.append("\\f")
                    13 -> stringbuffer.append("\\r")
                    34 -> stringbuffer.append("\\\"")
                    39 -> stringbuffer.append("\\'")
                    92 -> stringbuffer.append("\\\\")
                    else -> if (s.charAt(i).also { c = it } < ' ' || c > '~') {
                        val s1 = "0000" + Integer.toString(c, 16)
                        stringbuffer.append("\\u" + s1.substring(s1.length() - 4, s1.length()))
                    } else {
                        stringbuffer.append(c)
                    }
                }
            }
            return stringbuffer.toString()
        }

        private fun LexicalError(flag: Boolean, i: Int, j: Int, k: Int, s: String?, c: Char): String? {
            return ("Lexical error at line " + j + ", column " + k + ".  Encountered: " + (if (flag) "<EOF> " else "\"" + addEscapes(String.valueOf(c)) + "\"" + " (" + c.toInt() + "), ")
                    + "after : \"" + addEscapes(s) + "\"")
        }

        const val LEXICAL_ERROR = 0
        const val STATIC_LEXER_ERROR = 1
        const val INVALID_LEXICAL_STATE = 2
        const val LOOP_DETECTED = 3
    }
}