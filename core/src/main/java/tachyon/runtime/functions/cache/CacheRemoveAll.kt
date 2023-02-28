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
package tachyon.runtime.functions.cache

import tachyon.runtime.PageContext

class CacheRemoveAll : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc)
        if (args.size == 1) return call(pc, Caster.toString(args[0]))
        throw FunctionException(pc, "CacheRemoveAll", 0, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = -3444983104369826751L
        @Throws(PageException::class)
        fun call(pc: PageContext?): Double {
            return CacheClear.call(pc)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, cacheName: String?): Double {
            return CacheClear.call(pc, null, cacheName)
        }
    }
}