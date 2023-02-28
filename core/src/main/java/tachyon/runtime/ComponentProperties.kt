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
package tachyon.runtime

import java.io.Serializable

class ComponentProperties(val name: String?, val dspName: String?, val extend: String?, val implement: String?, val hint: String?, val output: Boolean?, val callPath: String?, val realPath: Boolean, val subName: String?,
                          val _synchronized: Boolean, javaAccessClass: Class?, persistent: Boolean, accessors: Boolean, modifier: Int, meta: Struct?) : Serializable {
    var javaAccessClass: Class?
    var properties: Map<String?, Property?>? = null
    var meta: Struct?
    val persistent: Boolean
    val accessors: Boolean
    val modifier: Int
    var inline = false
    fun duplicate(): ComponentProperties? {
        val cp = ComponentProperties(name, dspName, extend, implement, hint, output, callPath, realPath, subName, _synchronized, javaAccessClass, persistent,
                accessors, modifier, meta)
        cp.properties = properties
        cp.inline = inline
        return cp
    }

    /**
     * returns null if there is no wsdlFile defined
     *
     * @return the wsdlFile
     * @throws ExpressionException
     */
    fun getWsdlFile(): String? {
        return if (meta == null) null else meta.get(WSDL_FILE, null)
    }

    companion object {
        private val WSDL_FILE: Collection.Key? = KeyImpl.getInstance("wsdlfile")
    }

    init {
        this.javaAccessClass = javaAccessClass
        this.meta = meta
        this.persistent = persistent
        this.accessors = accessors
        this.modifier = modifier
    }
}