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
package tachyon.runtime.functions.system

import tachyon.runtime.PageContext

/**
 * returns the root of this current Page Context
 */
object GetFunctionCalledName : Function {
    private const val serialVersionUID = -3345605395096765821L
    fun call(pc: PageContext?): String? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val name: Key = pci.getActiveUDFCalledName()
        if (name != null) return name.getString()
        val udfs: Array<UDF?> = (pc as PageContextImpl?).getUDFs()
        return if (udfs.size == 0) "" else udfs[udfs.size - 1].getFunctionName()
    }
}