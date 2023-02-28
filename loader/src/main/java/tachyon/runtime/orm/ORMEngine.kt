/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.orm

import tachyon.runtime.PageContext

interface ORMEngine {
    /**
     * @return returns the label of the ORM Engine
     */
    val label: String?
    val mode: Int

    @Throws(PageException::class)
    fun createSession(pc: PageContext?): ORMSession?

    // public Object getSessionFactory(PageContext pc) throws PageException;
    @Throws(PageException::class)
    fun init(pc: PageContext?)
    fun getConfiguration(pc: PageContext?): ORMConfiguration?

    @Throws(PageException::class)
    fun reload(pc: PageContext?, force: Boolean): Boolean

    companion object {
        // (CFML Compatibility Mode) is not so strict in input interpretation
        const val MODE_LAZY = 0

        // more strict in input interpretation
        const val MODE_STRICT = 1
    }
}