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
/*
 * Created on Jan 20, 2005
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package lucee.runtime.cfx.customtag

import com.allaire.cfx.CustomTag

/**
 *
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
class CPPCFXTagClass : CFXTagClass {
    /**
     * @return the name
     */
    var name: String?
        private set

    @get:Override
    var isReadOnly = false
        private set

    /**
     * @return the serverLibrary
     */
    var sourceName: String?
        private set
        @Override get() = field
    set
    /**
     * @return the procedure
     */
    var procedure: String?
        private set

    /**
     * @return the keepAlive
     */
    var keepAlive: Boolean
        private set

    /**
     * @param name
     * @param readonly
     * @param serverLibrary
     * @param procedure
     * @param keepAlive
     */
    private constructor(name: String?, readonly: Boolean, serverLibrary: String?, procedure: String?, keepAlive: Boolean) : super() {
        this.name = name
        isReadOnly = readonly
        sourceName = serverLibrary
        this.procedure = procedure
        this.keepAlive = keepAlive
    }

    constructor(name: String?, serverLibrary: String?, procedure: String?, keepAlive: Boolean) {
        var name = name
        if (name.startsWith("cfx_")) name = name.substring(4)
        this.name = name
        sourceName = serverLibrary
        this.procedure = procedure
        this.keepAlive = keepAlive
    }

    @Override
    @Throws(CFXTagException::class)
    fun newInstance(): CustomTag? {
        return CPPCustomTag(sourceName, procedure, keepAlive)
    }

    @Override
    fun cloneReadOnly(): CFXTagClass? {
        return CPPCFXTagClass(name, true, sourceName, procedure, keepAlive)
    }

    @get:Override
    val displayType: String?
        get() = "cpp"

    @get:Override
    val isValid: Boolean
        get() = false
}