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
package lucee.runtime.search

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

class AddionalAttrs(private val contextBytes: Int, private val contextPassages: Int, private val contextHighlightBegin: String?, private val contextHighlightEnd: String?) {
    private var startrow = 1
    private var maxrows = -1
    private var hasRowHandling = false

    /**
     * @return the contextBytes
     */
    fun getContextBytes(): Int {
        return contextBytes
    }

    /**
     * @return the contextHighlightBegin
     */
    fun getContextHighlightBegin(): String? {
        return contextHighlightBegin
    }

    /**
     * @return the contextPassages
     */
    fun getContextPassages(): Int {
        return contextPassages
    }

    /**
     * @return the contextHighlightEnd
     */
    fun getContextHighlightEnd(): String? {
        return contextHighlightEnd
    }

    /**
     * @return the startrow
     */
    fun getStartrow(): Int {
        return startrow
    }

    /**
     * @param startrow the startrow to set
     */
    fun setStartrow(startrow: Int) {
        this.startrow = startrow
    }

    /**
     * @return the maxrows
     */
    fun getMaxrows(): Int {
        return maxrows
    }

    /**
     * @param maxrows the maxrows to set
     */
    fun setMaxrows(maxrows: Int) {
        this.maxrows = maxrows
    }

    fun hasRowHandling(): Boolean {
        return hasRowHandling
    }

    fun setHasRowHandling(hasRowHandling: Boolean) {
        this.hasRowHandling = hasRowHandling
    }

    companion object {
        private val addAttrs: ThreadLocal? = ThreadLocal()
        fun getAddionlAttrs(): AddionalAttrs? {
            var aa = addAttrs.get() as AddionalAttrs
            if (aa == null) aa = AddionalAttrs(300, 0, "<b>", "</b>")
            return aa
        }

        fun setAddionalAttrs(aa: AddionalAttrs?) {
            addAttrs.set(aa)
        }

        fun setAddionalAttrs(contextBytes: Int, contextPassages: Int, contextHighlightBegin: String?, contextHighlightEnd: String?) {
            setAddionalAttrs(AddionalAttrs(contextBytes, contextPassages, contextHighlightBegin, contextHighlightEnd))
        }

        fun removeAddionalAttrs() {
            addAttrs.set(null)
        }
    }
}