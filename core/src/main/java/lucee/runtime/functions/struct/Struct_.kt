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
 * Implements the CFML Function array
 */
package lucee.runtime.functions.struct

import lucee.runtime.PageContext

object Struct_ : Function {
    private const val serialVersionUID = 8708684598035273346L
    @Throws(PageException::class)
    fun call(pc: PageContext?, objArr: Array<Object?>?): Struct? {
        return _call(objArr, "invalid argument for function struct, only named arguments are allowed like struct(name:\"value\",name2:\"value2\")", StructImpl.TYPE_UNDEFINED)
    }

    @Throws(PageException::class)
    internal fun _call(objArr: Array<Object?>?, expMessage: String?, type: Int): Struct? {
        val sct: StructImpl = if (type < 0) StructImpl() else StructImpl(type)
        var fv: FunctionValueImpl?
        for (i in objArr.indices) {
            if (objArr!![i] is FunctionValue) {
                fv = objArr[i] as FunctionValueImpl?
                if (fv.getNames() == null) {
                    sct.set(fv.getNameAsKey(), fv.getValue())
                } else {
                    val arr: Array<String?> = fv.getNames()
                    var s: Struct? = sct
                    for (y in 0 until arr.size - 1) {
                        s = touch(s, arr[y])
                    }
                    s.set(KeyImpl.init(arr[arr.size - 1]), fv.getValue())
                }
            } else {
                throw ExpressionException(expMessage)
            }
        }
        return sct
    }

    private fun touch(parent: Struct?, name: String?): Struct? {
        val key: Key = KeyImpl.init(name.trim())
        val obj: Object = parent.get(key, null)
        if (obj is Struct) return obj as Struct
        val sct: Struct = StructImpl()
        parent.setEL(key, sct)
        return sct
    }
}