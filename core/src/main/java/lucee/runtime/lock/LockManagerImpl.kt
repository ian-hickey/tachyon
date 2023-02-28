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

import java.util.ArrayList

/**
 * Lock mnager to make a log by a string name
 */
class LockManagerImpl private constructor(private val caseSensitive: Boolean) : LockManager {
    private val locks: RWKeyLock<String?>? = RWKeyLock<String?>()
    @Override
    @Throws(LockTimeoutException::class, InterruptedException::class)
    fun lock(type: Int, name: String?, timeout: Int, pageContextId: Int): LockData? {
        var name = name
        var timeout = timeout
        if (!caseSensitive) name = name.toLowerCase()
        // if(type==LockManager.TYPE_READONLY) return new ReadLockData(name,pageContextId);
        if (timeout <= 0) timeout = 1
        val lock: Lock
        lock = try {
            locks.lock(name, timeout, type == LockManager.TYPE_READONLY)
        } catch (e: LockException) {
            throw LockTimeoutException(type, name, timeout)
        } catch (e: LockInterruptedException) {
            throw e.getLockInterruptedException()
        }
        return LockDataImpl(lock, name, pageContextId, type == LockManager.TYPE_READONLY)
    }

    @Override
    fun unlock(data: LockData?) {
        val l: Lock = data.getLock()
        locks.unlock(l)
    }

    @get:Override
    val openLockNames: Array<String?>?
        get() {
            val list: List<String?> = locks.getOpenLockNames()
            return list.toArray(arrayOfNulls<String?>(list.size()))
        }

    @Override
    fun clean() {
        locks.clean()
    }

    fun isReadLocked(name: String?): Boolean? {
        var name = name
        if (!caseSensitive) name = name.toLowerCase()
        return locks.isReadLocked(name)
    }

    fun isWriteLocked(name: String?): Boolean? {
        var name = name
        if (!caseSensitive) name = name.toLowerCase()
        return locks.isWriteLocked(name)
    }

    companion object {
        private val managers: List<LockManagerImpl?>? = ArrayList<LockManagerImpl?>()
        fun getInstance(caseSensitive: Boolean): LockManager? {
            val lmi = LockManagerImpl(caseSensitive)
            managers.add(lmi)
            return lmi
        }
    }
}