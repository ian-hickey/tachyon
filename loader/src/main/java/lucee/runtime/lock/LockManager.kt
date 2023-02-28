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
 * Manager to open and close locks
 */
interface LockManager {
    /**
     * locks a thread if already another thread is come until other thread notify him by unlock method
     *
     * @param type Local Type
     * @param name Lock Name (not case sensitive)
     * @param timeout timeout to for waiting in this method, if timeout occurs "lockTimeoutException"
     * will be thrown
     * @param pageContextId page context id
     * @return lock data object key for unlocking this lock
     * @throws LockTimeoutException Lock Timeout Exception
     * @throws InterruptedException Interrupted Exception
     */
    @Throws(LockTimeoutException::class, InterruptedException::class)
    fun lock(type: Int, name: String?, timeout: Int, pageContextId: Int): LockData?

    /**
     * unlocks a locked thread in lock method
     *
     * @param data data
     */
    fun unlock(data: LockData?)
    val openLockNames: Array<String?>?
    fun clean()

    companion object {
        /**
         * Field `TYPE_READONLY`
         */
        const val TYPE_READONLY = 0

        /**
         * Field `TYPE_EXCLUSIVE`
         */
        const val TYPE_EXCLUSIVE = 1
    }
}