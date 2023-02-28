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
package tachyon.runtime.interpreter.ref.util

import tachyon.runtime.PageContext

object RefUtil {
    /**
     * transalte a Ref array to an Object array
     *
     * @param refs
     * @return objects
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getValue(pc: PageContext?, refs: Array<Ref?>?): Array<Object?>? {
        val objs: Array<Object?> = arrayOfNulls<Object?>(refs!!.size)
        for (i in refs.indices) {
            objs[i] = refs!![i].getValue(pc)
        }
        return objs
    }

    @Throws(PageException::class)
    fun eeq(pc: PageContext?, left: Ref?, right: Ref?): Boolean {
        // TODO Auto-generated method stub
        return left.getValue(pc) === right.getValue(pc)
    }
}