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
package tachyon.runtime.tag

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

class TreeItemBean {
    /**
     * @return the value
     */
    /**
     * @param value the value to set
     */
    var value: String? = null
    /**
     * @return the display
     */
    /**
     * @param display the display to set
     */
    var display: String? = null
    /**
     * @return the parent
     */
    /**
     * @param parent the parent to set
     */
    var parent: String? = null
    /**
     * @return the img
     */
    /**
     * @param img the img to set
     */
    var img = IMG_FOLDER
    /**
     * @return the imgCustom
     */
    /**
     * @param imgCustom the imgCustom to set
     */
    var imgCustom: String? = null
    /**
     * @return the imgOpen
     */
    /**
     * @param imgOpen the imgOpen to set
     */
    var imgOpen = IMG_FOLDER
    /**
     * @return the imgOpenCustom
     */
    /**
     * @param imgOpenCustom the imgOpenCustom to set
     */
    var imgOpenCustom: String? = null
    /**
     * @return the href
     */
    /**
     * @param href the href to set
     */
    var href: String? = null
    /**
     * @return the target
     */
    /**
     * @param target the target to set
     */
    var target: String? = null
    /**
     * @return the expand
     */
    /**
     * @param expand the expand to set
     */
    // private String query;
    // private int queryAsRoot=QUERY_AS_ROOT_YES;
    // private String queryAsRootCustom;
    var isExpand = true

    companion object {
        const val QUERY_AS_ROOT_YES = 1
        const val QUERY_AS_ROOT_NO = 0
        const val QUERY_AS_ROOT_CUSTOM = 2
        const val IMG_CD = 10
        const val IMG_COMPUTER = 11
        const val IMG_DOCUMENT = 12
        const val IMG_ELEMENT = 13
        const val IMG_FLOPPY = 14
        const val IMG_FOLDER = 15
        const val IMG_FIXED = 16
        const val IMG_REMOTE = 17
        const val IMG_CUSTOM = 18
    }
}