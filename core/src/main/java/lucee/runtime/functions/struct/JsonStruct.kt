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
package lucee.runtime.functions.struct

import lucee.runtime.PageContext

object JsonStruct : Function {
    private const val serialVersionUID = 3030769464899375329L
    @Throws(PageException::class)
    fun call(pc: PageContext?, objArr: Array<Object?>?): Struct? {
        return Struct_._call(objArr, "invalid argument for JSON struct, only named arguments are allowed like {name:\"value\",name2:\"value2\"}", Struct.TYPE_LINKED)
    }
}