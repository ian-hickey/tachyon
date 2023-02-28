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
package tachyon.runtime.functions

import tachyon.runtime.PageContext

class BIFProxy(clazz: Class?) : BIF() {
    private val clazz: Class?

    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        val _args: Array<Object?> = arrayOfNulls<Object?>(args!!.size + 1)
        _args[0] = pc
        for (i in args.indices) {
            _args[i + 1] = args!![i]
        }
        return Reflector.callStaticMethod(clazz, KeyConstants._call, _args)
    }

    init {
        this.clazz = clazz
    }
}