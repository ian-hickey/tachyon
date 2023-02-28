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
 * Implements the CFML Function createdate
 */
package lucee.runtime.functions.component

import lucee.runtime.Component

@Deprecated
@Deprecated("use function GetMetaData instead")
object ComponentInfo : Function {
    fun call(pc: PageContext?, component: Component?): Struct? {
        DeprecatedUtil.function(pc, "ComponentInfo", "GetMetaData")
        val sct: Struct = StructImpl()
        sct.setEL(KeyConstants._name, component.getName())
        sct.setEL(KeyConstants._fullname, component.getCallName())
        var extend: String = component.getExtends()
        if (extend == null || extend.length() === 0) extend = "Component" // TODO Object instead?
        sct.setEL(KeyConstants._extends, extend)
        sct.setEL(KeyConstants._hint, component.getHint())
        return sct
    }
}