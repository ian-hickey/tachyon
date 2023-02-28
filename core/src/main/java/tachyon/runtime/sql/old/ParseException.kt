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
package tachyon.runtime.sql.old

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

class ParseException : Exception {
    constructor(token: Token?, ai: Array<IntArray?>?, `as`: Array<String?>?) : super("") {
        eol = System.getProperty("line.separator", "\n")
        specialConstructor = true
        currentToken = token
        expectedTokenSequences = ai
        tokenImage = `as`
    }

    constructor() {
        eol = System.getProperty("line.separator", "\n")
        specialConstructor = false
    }

    constructor(s: String?) : super(s) {
        eol = System.getProperty("line.separator", "\n")
        specialConstructor = false
    }

    @get:Override
    val message: String?
        get() {
            if (!specialConstructor) return super.getMessage()
            var s = ""
            var i = 0
            for (j in expectedTokenSequences.indices) {
                if (i < expectedTokenSequences!![j].length) i = expectedTokenSequences!![j].length
                for (k in 0 until expectedTokenSequences!![j].length) s = s + tokenImage!![expectedTokenSequences!![j]!![k]] + " "
                if (expectedTokenSequences!![j]!![expectedTokenSequences!![j].length - 1] != 0) s = "$s..."
                s = "$s$eol    "
            }
            var s1 = "Encountered \""
            var token: Token = currentToken!!.next
            for (l in 0 until i) {
                if (l != 0) s1 = "$s1 "
                if (token!!.kind === 0) {
                    s1 = s1 + tokenImage!![0]
                    break
                }
                s1 = s1 + add_escapes(token!!.image)
                token = token!!.next
            }
            s1 = s1 + "\" at line " + currentToken!!.next!!.beginLine + ", column " + currentToken!!.next!!.beginColumn
            s1 = "$s1.$eol"
            s1 = if (expectedTokenSequences!!.size == 1) s1 + "Was expecting:" + eol + "    " else s1 + "Was expecting one of:" + eol + "    "
            s1 = s1 + s
            return s1
        }

    protected fun add_escapes(s: String?): String? {
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

    protected var specialConstructor: Boolean
    var currentToken: Token? = null
    var expectedTokenSequences: Array<IntArray?>?
    var tokenImage: Array<String?>?
    protected var eol: String?
}