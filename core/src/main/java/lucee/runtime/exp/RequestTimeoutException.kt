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
package lucee.runtime.exp

import lucee.commons.lang.ExceptionUtil

class RequestTimeoutException(pc: PageContext?, stacktrace: Array<StackTraceElement?>?) : Abort(SCOPE_REQUEST, "Request [" + getPath(pc) + "] has run into a timeout (timeout: " + pc.getRequestTimeout() / 1000
        + " seconds) and has been stopped. The thread started " + (System.currentTimeMillis() - pc.getStartTime()) + "ms ago." + locks(pc)), Stop {
    private val stacktrace: Array<StackTraceElement?>?
    private var threadDeath: ThreadDeath? = null

    constructor(pc: PageContextImpl?, td: ThreadDeath?) : this(pc, pc.getTimeoutStackTrace()) {
        threadDeath = td
    }

    @get:Override
    override val stackTrace: Array<Any?>?
        get() = stacktrace

    fun getThreadDeath(): ThreadDeath? {
        return threadDeath
    }

    companion object {
        private const val serialVersionUID = -37886162001453270L
        fun locks(pc: PageContext?): String? {
            var strLocks = ""
            try {
                val manager: LockManager = pc.getConfig().getLockManager()
                val locks: Array<String?> = manager.getOpenLockNames()
                if (!ArrayUtil.isEmpty(locks)) strLocks = " Open locks at this time [" + ListUtil.arrayToList(locks, ", ").toString() + "]."
                // LockManagerImpl.unlockAll(pc.getId());
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            return strLocks
        }

        private fun getPath(pc: PageContext?): String? {
            return try {
                val ps: PageSource = pc.getBasePageSource()
                ps.getRealpathWithVirtual().toString() + " (" + pc.getBasePageSource().getDisplayPath() + ")"
            } catch (npe: NullPointerException) {
                "(no path available)"
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                "(fail to retrieve path:" + t.getClass().getName().toString() + ":" + t.getMessage().toString() + ")"
            }
        }
    }

    init {
        this.stacktrace = stacktrace
        setStackTrace(stacktrace)
        // TODO Auto-generated constructor stub
    }
}