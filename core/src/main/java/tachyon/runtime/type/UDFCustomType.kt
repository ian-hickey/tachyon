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
package tachyon.runtime.type

import tachyon.commons.lang.ExceptionUtil

class UDFCustomType(udf: UDF?) : CustomType {
    private val udf: UDF?
    @Override
    @Throws(PageException::class)
    fun convert(pc: PageContext?, o: Object?): Object? {
        return udf.call(pc, arrayOf<Object?>(o), false)
    }

    @Override
    fun convert(pc: PageContext?, o: Object?, defaultValue: Object?): Object? {
        return try {
            udf.call(pc, arrayOf<Object?>(o), false)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    init {
        this.udf = udf
    }
}