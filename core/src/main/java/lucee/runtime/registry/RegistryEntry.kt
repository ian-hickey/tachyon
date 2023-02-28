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
package lucee.runtime.registry

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
 * represent a single Registry Entry
 */
class RegistryEntry(type: Short, key: String?, value: Object?) {
    private val type: Short
    private val key: String?
    private val value: Object?

    /**
     * @return Returns the key.
     */
    fun getKey(): String? {
        return key
    }

    /**
     * @return Returns the type.
     */
    fun getType(): Short {
        return type
    }

    /**
     * @return Returns the value.
     */
    fun getValue(): Object? {
        return value
    }

    @Override
    override fun toString(): String {
        return try {
            "Registry Entry: [" + key + " " + toStringType(type) + " " + value + "]"
        } catch (e: RegistryException) {
            "Registry Entry: [$key $value]"
        }
    }

    companion object {
        /**
         * Field `TYPE_STRING`
         */
        const val TYPE_STRING: Short = 0

        /**
         * Field `TYPE_DWORD`
         */
        const val TYPE_DWORD: Short = 1

        /**
         * Field `TYPE_ANY`
         */
        const val TYPE_ANY: Short = 2

        /**
         * Field `TYPE_KEY`
         */
        const val TYPE_KEY: Short = 3

        /**
         * Field `REGSTR_TOKEN`
         */
        val REGSTR_TOKEN: String? = "REG_SZ"

        /**
         * Field `REGKEY_TOKEN`
         */
        val REGKEY_TOKEN: String? = "REG_KEY"

        /**
         * Field `REGDWORD_TOKEN`
         */
        val REGDWORD_TOKEN: String? = "REG_DWORD"

        /**
         * cast a String type to a short Type
         *
         * @param strType
         * @return
         * @throws RegistryException
         */
        @Throws(RegistryException::class)
        fun toType(strType: String?): Short {
            if (strType!!.equals(REGDWORD_TOKEN)) return TYPE_DWORD else if (strType.equals(REGSTR_TOKEN)) return TYPE_STRING else if (strType.equals(REGKEY_TOKEN)) return TYPE_KEY
            throw RegistryException("$strType is not a valid Registry Type")
        }

        /**
         * cast a short type to a String Type
         *
         * @param type
         * @return Registry String Type Definition
         * @throws RegistryException
         */
        @Throws(RegistryException::class)
        fun toStringType(type: Short): String? {
            if (type == TYPE_DWORD) return REGDWORD_TOKEN else if (type == TYPE_STRING) return REGSTR_TOKEN else if (type == TYPE_KEY) return REGKEY_TOKEN
            throw RegistryException("invalid Registry Type definition")
        }

        /**
         * cast a short type to a String Type
         *
         * @param type
         * @return Registry String Type Definition
         * @throws RegistryException
         */
        @Throws(RegistryException::class)
        fun toCFStringType(type: Short): String? {
            if (type == TYPE_DWORD) return "DWORD" else if (type == TYPE_STRING) return "STRING" else if (type == TYPE_KEY) return "KEY"
            throw RegistryException("invalid Registry Type definition")
        }
    }

    /**
     * constructor of the class
     *
     * @param type (RegistryEntry.TYPE_DWORD, RegistryEntry.TYPE_STRING)
     * @param key
     * @param value
     * @throws RegistryException
     */
    init {
        if (type != TYPE_DWORD && type != TYPE_STRING && type != TYPE_KEY) throw RegistryException("invalid Registry Type definition")
        this.type = type
        this.key = key
        this.value = value
    }
}