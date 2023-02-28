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

import tachyon.runtime.ext.tag.AppendixTag

abstract class CustomTag : BodyTagTryCatchFinallyImpl(), DynamicAttributes, AppendixTag {
    protected var attributesScope: StructImpl? = null
    protected var callerScope: Caller? = null
    @Override
    fun doInitBody() {
    }

    @Override
    fun setDynamicAttribute(uri: String?, name: String?, value: Object?) {
        TagUtil.setDynamicAttribute(attributesScope, KeyImpl.init(name), value, TagUtil.UPPER_CASE)
    }

    /**
     * @return return thistag scope
     */
    abstract val thisTagScope: Struct?

    /**
     * @return return the caller scope
     */
    abstract fun getCallerScope(): Struct?

    /**
     * @return return attributes scope
     */
    abstract fun getAttributesScope(): Struct?

    /**
     * @return the variables scope
     */
    abstract val variablesScope: Scope?

    companion object {
        protected val ON_ERROR: Collection.Key? = KeyConstants._onError
        protected val ON_FINALLY: Collection.Key? = KeyConstants._onFinally
        protected val ON_START_TAG: Collection.Key? = KeyConstants._onStartTag
        protected val ON_END_TAG: Collection.Key? = KeyConstants._onEndTag
        protected val INIT: Collection.Key? = KeyConstants._init
        protected val GENERATED_CONTENT: Collection.Key? = KeyConstants._GENERATEDCONTENT
        protected val EXECUTION_MODE: Collection.Key? = KeyConstants._EXECUTIONMODE
        protected val EXECUTE_BODY: Collection.Key? = KeyConstants._EXECUTEBODY
        protected val HAS_END_TAG: Collection.Key? = KeyConstants._HASENDTAG
        protected val ATTRIBUTES: Collection.Key? = KeyConstants._ATTRIBUTES
        protected val CALLER: Collection.Key? = KeyConstants._CALLER
        protected val THIS_TAG: Collection.Key? = KeyConstants._THISTAG
    }
}