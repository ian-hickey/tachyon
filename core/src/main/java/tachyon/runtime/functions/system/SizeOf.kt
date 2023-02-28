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

import tachyon.commons.lang.SizeAndCount

object SizeOf : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?): Object? {
        return call(pc, `object`, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, complex: Boolean): Object? {
        val size: Size = SizeAndCount.sizeOf(`object`)
        if (!complex) return Caster.toDouble(size.size)
        val sct: Struct = StructImpl()
        sct.set(KeyConstants._size, Caster.toDouble(size.size))
        sct.set(KeyConstants._count, Caster.toDouble(size.count))
        return sct
    }
}