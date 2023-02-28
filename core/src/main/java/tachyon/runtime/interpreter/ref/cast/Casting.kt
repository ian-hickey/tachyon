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
package tachyon.runtime.interpreter.ref.cast

import tachyon.runtime.PageContext

/**
 * cast
 */
class Casting : RefSupport, Ref {
    private val type: Short
    private val strType: String?
    private var ref: Ref? = null
    private var `val`: Object? = null

    /**
     * constructor of the class
     *
     * @param pc
     * @param strType
     * @param type
     * @param ref
     */
    constructor(strType: String?, type: Short, ref: Ref?) {
        this.type = type
        this.strType = strType
        this.ref = ref
    }

    constructor(strType: String?, type: Short, `val`: Object?) {
        this.type = type
        this.strType = strType
        this.`val` = `val`
    }

    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        // if ref == null, it is val based Casting
        if (ref == null) return Caster.castTo(pc, type, strType, `val`)
        if (ref is Variable && "queryColumn".equalsIgnoreCase(strType)) {
            val `var`: Variable? = ref as Variable?
            return Caster.castTo(pc, type, strType, `var`.getCollection(pc))
        }
        return Caster.castTo(pc, type, strType, ref.getValue(pc))
    }

    fun getRef(): Ref? {
        return ref
    }

    fun getStringType(): String? {
        return strType
    }

    fun getType(): Short {
        return type
    }

    @Override
    fun getTypeName(): String? {
        return "operation"
    }

    @Override
    override fun toString(): String {
        return strType.toString() + ":" + ref + ":" + `val`
    }
}