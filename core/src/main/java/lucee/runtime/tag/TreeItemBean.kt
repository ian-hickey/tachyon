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
package lucee.runtime.tag

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