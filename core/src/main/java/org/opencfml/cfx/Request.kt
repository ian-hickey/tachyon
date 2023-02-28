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
package org.opencfml.cfx

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
 * Alternative Implementation of Jeremy Allaire's Request Interface
 */
interface Request {
    /**
     * checks if attribute with this key exists
     *
     * @param key key to check
     * @return has key or not
     */
    fun attributeExists(key: String?): Boolean

    /**
     * @return if tags has set [debug] attribute
     */
    fun debug(): Boolean

    /**
     * returns attribute matching key
     *
     * @param key key to get
     * @return value to key
     */
    fun getAttribute(key: String?): String?

    /**
     * returns attribute matching key
     *
     * @param key key to get
     * @param defaultValue return this value if key not exist
     * @return value to key
     */
    fun getAttribute(key: String?, defaultValue: String?): String?

    /**
     * return all sattribute keys
     *
     * @return all keys
     */
    val attributeList: Array<String?>?

    /**
     * returns attribute as int matching key
     *
     * @param key key to get
     * @return value to key
     * @throws NumberFormatException
     */
    @Throws(NumberFormatException::class)
    fun getIntAttribute(key: String?): Int

    /**
     * returns attribute as int matching key
     *
     * @param key key to get
     * @param defaultValue return this value if key not exist
     * @return value to key
     */
    fun getIntAttribute(key: String?, defaultValue: Int): Int

    /**
     * return given query
     *
     * @return return given query
     */
    val query: org.opencfml.cfx.Query?

    /**
     * returns all the settings
     *
     * @param key
     * @return settings
     */
    fun getSetting(key: String?): String?
}