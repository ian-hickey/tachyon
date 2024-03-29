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
package tachyon.commons.lock

import tachyon.runtime.exp.PageException

interface Lock {
    @Throws(PageException::class)
    fun lock(timeout: Long)
    fun unlock()

    /**
     * Returns an estimate of the number of threads waiting to acquire this lock. The value is only an
     * estimate because the number of threads may change dynamically while this method traverses
     * internal data structures. This method is designed for use in monitoring of the system state, not
     * for synchronization control.
     *
     * @return the estimated number of threads waiting for this lock
     */
    val queueLength: Int
}