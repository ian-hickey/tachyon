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
/**
 * Implements the CFML Function gettemplatepath
 */
package lucee.runtime.functions.system

import lucee.commons.lang.StringUtil

object PagePoolList : Function {
    private const val serialVersionUID = 7743072823224800862L
    @Throws(PageException::class)
    fun call(pc: PageContext?): Array? {
        val arr = ArrayImpl()
        fill(arr, ConfigWebUtil.getAllMappings(pc))
        return arr
    }

    @Throws(PageException::class)
    private fun fill(arr: Array?, mappings: Array<Mapping?>?) {
        if (mappings == null) return
        var mapping: MappingImpl?
        for (i in mappings.indices) {
            mapping = mappings[i] as MappingImpl?
            mapping.getDisplayPathes(arr)
        }
    }

    fun removeStartingSlash(virtual: String?): String? {
        var virtual = virtual
        virtual = virtual.trim()
        if (StringUtil.startsWith(virtual, '/')) virtual = virtual.substring(1)
        return if (StringUtil.isEmpty(virtual)) "root" else virtual
    }
}