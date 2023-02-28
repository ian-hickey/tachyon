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
package lucee.commons.lock

import lucee.runtime.exp.ApplicationException

/**
 * Lock Timeout
 */
class LockException : ApplicationException {
    /**
     * @param type type of the log
     * @param name name of the Lock
     * @param timeout
     */
    constructor(type: Int, name: String, timeout: Long) : super("a timeout occurred on a " + toString(type) + " lock with name [" + name + "] after " + timeout / 1000 + " seconds") {
        // A timeout occurred while attempting to lock lockname
    }

    constructor(timeout: Long) : super("a timeout occurred after " + toTime(timeout)) {}
    constructor(text: String?) : super(text) {}

    companion object {
        private const val serialVersionUID = 9132132031478280069L
        private fun toTime(timeout: Long): String {
            return if (timeout >= 1000 && timeout / 1000 * 1000 == timeout) (timeout / 1000).toString() + " seconds" else "$timeout milliseconds"
        }

        private fun toString(type: Int): String {
            return if (LockManager.TYPE_EXCLUSIVE === type) "exclusive" else "read-only"
        }
    }
}