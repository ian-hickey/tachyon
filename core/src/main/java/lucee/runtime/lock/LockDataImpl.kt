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
package lucee.runtime.lock

import lucee.commons.lock.Lock

internal class LockDataImpl(lock: Lock?, name: String?, id: Int, readOnly: Boolean) : LockData {
    private val lock: Lock?

    @get:Override
    val name: String?

    @get:Override
    val id: Int

    @get:Override
    val isReadOnly: Boolean

    /**
     * @return the lock
     */
    @Override
    fun getLock(): Lock? {
        return lock
    }

    /**
     * constructor of the class
     *
     * @param token
     * @param name name of the token
     * @param id id of the token
     * @param readOnly
     */
    init {
        this.lock = lock
        this.name = name.toLowerCase()
        this.id = id
        isReadOnly = readOnly
    }
}