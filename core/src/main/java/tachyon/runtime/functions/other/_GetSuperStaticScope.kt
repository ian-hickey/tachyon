/**
 * Copyright (c) 2017, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.functions.other

import tachyon.runtime.Component

object _GetSuperStaticScope : Function {
    private const val serialVersionUID = -2676531632543576056L
    @Throws(PageException::class)
    fun call(pc: PageContext?): Struct? {
        val cfc: Component = pc.getActiveComponent()
                ?: throw ApplicationException("[static::] is not supported outside a component.")
        val base: Component = cfc.getBaseComponent()
                ?: throw ApplicationException("component [" + cfc.getCallName().toString() + "] does not have a base component.")
        return base.staticScope()
    }
}