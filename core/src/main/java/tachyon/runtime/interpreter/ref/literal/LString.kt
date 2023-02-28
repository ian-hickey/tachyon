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
package tachyon.runtime.interpreter.ref.literal

import tachyon.runtime.PageContext

/**
 * Literal String
 *
 */
class LString
/**
 * constructor of the class
 *
 * @param str
 */(private val str: String?) : RefSupport(), Literal {
    @Override
    fun getValue(pc: PageContext?): Object? {
        return str
    }

    @Override
    override fun toString(): String {
        return str!!
    }

    @Override
    fun getTypeName(): String? {
        return "literal"
    }

    @Override
    override fun getString(pc: PageContext?): String? {
        return toString()
    }

    @Override
    @Throws(PageException::class)
    fun eeq(pc: PageContext?, other: Ref?): Boolean {
        return RefUtil.eeq(pc, this, other)
    }
}