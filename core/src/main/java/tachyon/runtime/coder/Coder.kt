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
package tachyon.runtime.coder

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
 *
 */
object Coder {
    /**
     * Field `ENCODING_UU`
     */
    const val ENCODING_UU: Short = 0

    /**
     * Field `ENCODING_HEX`
     */
    const val ENCODING_HEX: Short = 1

    /**
     * Field `ENCODING_BASE64`
     */
    const val ENCODING_BASE64: Short = 2

    /**
     * @param type
     * @param value
     * @return
     * @throws CoderException
     */
    @Throws(CoderException::class)
    fun decode(type: String?, value: String?, precise: Boolean): ByteArray? {
        var type = type
        type = type.toLowerCase().trim()
        if (type.equals("hex")) return decode(ENCODING_HEX, value, precise)
        if (type.equals("uu")) return decode(ENCODING_UU, value, precise)
        if (type.equals("base64")) return decode(ENCODING_BASE64, value, precise)
        throw CoderException("Invalid encoding definition [$type]. Valid encodings are [hex, uu, base64].")
    }

    /**
     * @param type
     * @param value
     * @return
     * @throws CoderException
     */
    @Throws(CoderException::class)
    fun decode(type: Short, value: String?, precise: Boolean): ByteArray? {
        if (type == ENCODING_UU) return UUCoder.decode(value) else if (type == ENCODING_HEX) return HexCoder.decode(value) else if (type == ENCODING_BASE64) return Base64Coder.decode(value, precise)
        throw CoderException("Invalid encoding definition")
    }

    /**
     * @param type
     * @param value
     * @return
     * @throws CoderException
     */
    @Throws(CoderException::class)
    fun encode(type: String?, value: ByteArray?): String? {
        var type = type
        type = type.toLowerCase().trim()
        if (type.equals("hex")) return encode(ENCODING_HEX, value)
        if (type.equals("uu")) return encode(ENCODING_UU, value)
        if (type.equals("base64")) return encode(ENCODING_BASE64, value)
        throw CoderException("Invalid encoding definition [$type]. Valid encodings are [hex, uu, base64].")
    }

    /**
     * @param type
     * @param value
     * @return
     * @throws CoderException
     */
    @Throws(CoderException::class)
    fun encode(type: Short, value: ByteArray?): String? {
        if (type == ENCODING_UU) return UUCoder.encode(value) else if (type == ENCODING_HEX) return HexCoder.encode(value) else if (type == ENCODING_BASE64) return Base64Coder.encode(value)
        throw CoderException("Invalid encoding definition")
    }
}