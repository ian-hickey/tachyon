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
package tachyon.runtime.interpreter.ref

import tachyon.runtime.PageContext

interface Set : Ref {
    /**
     * @return sets a value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setValue(pc: PageContext?, obj: Object?): Object?

    @Throws(PageException::class)
    fun getParent(pc: PageContext?): Ref?

    @Throws(PageException::class)
    fun getKey(pc: PageContext?): Ref?

    @Throws(PageException::class)
    fun getKeyAsString(pc: PageContext?): String?
}