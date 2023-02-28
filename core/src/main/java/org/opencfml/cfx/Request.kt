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