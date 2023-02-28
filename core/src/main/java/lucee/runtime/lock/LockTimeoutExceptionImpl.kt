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

import lucee.commons.lang.StringUtil

/**
 * Lock Timeout // FUTURE replace LockTimeoutException with this implementation
 */
class LockTimeoutExceptionImpl
/**
 * @param type type of the log
 * @param name name of the Lock
 * @param timeout
 * @param readLocked
 * @param writeLocked
 */(private val type: Int, private val name: String?, private val timeout: Int, private val readLocked: Boolean?, private val writeLocked: Boolean?) : Exception() {
    fun getMessage(scopeName: String?): String? {
        return createMessage(type, name, scopeName, timeout, readLocked, writeLocked)
    }

    @get:Override
    val message: String?
        get() = createMessage(type, name, null, timeout, readLocked, writeLocked)

    companion object {
        fun createMessage(type: Int, name: String?, scopeName: String?, timeout: Int, readLocked: Boolean?, writeLocked: Boolean?): String? {
            // if(LockManager.TYPE_EXCLUSIVE==type && readLocked==Boolean.TRUE && writeLocked==Boolean.FALSE)
            val sb: StringBuilder = StringBuilder().append("a timeout occurred after ").append(getTime(timeout)).append(" trying to acquire a ").append(toString(type))
            if (StringUtil.isEmpty(scopeName)) {
                sb.append(" lock with name [").append(name).append("]")
            } else {
                sb.append(" [").append(scopeName).append("] scope lock")
            }
            if (readLocked === Boolean.TRUE && writeLocked === Boolean.FALSE) {
                sb.append(" on an existing read lock.")
                if (LockManager.TYPE_EXCLUSIVE === type) sb.append(" You cannot upgrade an existing lock from \"read\" to \"exclusive\".")
            } else sb.append(".")
            return sb.toString()
        }

        private fun getTime(timeout: Int): String? {
            if (timeout / 1000 * 1000 == timeout) {
                val s = timeout / 1000
                return s.toString() + if (s > 1) " seconds" else " second"
            }
            return timeout.toString() + if (timeout > 1) " milliseconds" else " millisecond"
        }

        private fun toString(type: Int): String? {
            return if (LockManager.TYPE_EXCLUSIVE === type) "exclusive" else "read-only"
        }
    }
}