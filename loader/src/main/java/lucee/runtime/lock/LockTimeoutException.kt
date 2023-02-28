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

import kotlin.Throws
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 * Lock Timeout
 */
class LockTimeoutException
/**
 * @param type type of the log
 * @param name name of the Lock
 * @param timeout lock timeout
 */
(type: Int, name: String, timeout: Int) : Exception("a timeout occurred on a " + toString(type) + " lock with name [" + name + "] after " + getTime(timeout)) {
    companion object {
        private const val serialVersionUID = -2772267544602614500L
        private fun getTime(timeout: Int): String {
            if (timeout / 1000 * 1000 == timeout) {
                val s = timeout / 1000
                return s.toString() + if (s > 1) " seconds" else " second"
            }
            return timeout.toString() + if (timeout > 1) " milliseconds" else " millisecond"
        }

        private fun toString(type: Int): String {
            return if (LockManager.TYPE_EXCLUSIVE === type) "exclusive" else "read-only"
        }
    }
}