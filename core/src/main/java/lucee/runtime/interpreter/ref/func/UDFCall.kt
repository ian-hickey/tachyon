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
package lucee.runtime.interpreter.ref.func

import lucee.runtime.PageContext

/**
 * call of a User defined Function
 */
class UDFCall : RefSupport, Ref {
    private var arguments: Array<Ref?>?
    private var name: String? = null
    private var parent: Ref?
    private var refName: Ref? = null

    /**
     * @param pc
     * @param parent
     * @param name
     * @param arguments
     */
    constructor(parent: Ref?, name: String?, arguments: Array<Ref?>?) {
        this.parent = parent
        this.name = name
        this.arguments = arguments
    }

    /**
     * @param pc
     * @param parent
     * @param refName
     * @param arguments
     */
    constructor(parent: Ref?, refName: Ref?, arguments: Array<Ref?>?) {
        this.parent = parent
        this.refName = refName
        this.arguments = arguments
    }

    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        return pc.getVariableUtil().callFunction(pc, parent.getValue(pc), getName(pc), RefUtil.getValue(pc, arguments))
    }

    @Throws(PageException::class)
    private fun getName(pc: PageContext?): String? {
        return if (name != null) name else Caster.toString(refName.getValue(pc))
    }

    @Override
    fun getTypeName(): String? {
        return "user defined function"
    }
}