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
package tachyon.commons.lock.rw

import java.util.concurrent.TimeUnit

class RWLock<L>(label: L) {
    private val rwl: ReentrantReadWriteLock
    private val rl: Lock
    private val wl: Lock
    val label: L
    private var count = 0
    @Throws(LockException::class, LockInterruptedException::class)
    fun lock(timeout: Long, readOnly: Boolean) {
        if (timeout <= 0) throw LockException("timeout must be a positive number")
        try {
            if (!getLock(readOnly).tryLock(timeout, TimeUnit.MILLISECONDS)) {
                throw LockException(timeout)
            }
        } catch (e: InterruptedException) {
            throw LockInterruptedException(e)
        }
    }

    fun inc() {
        synchronized(rwl) { count++ }
    }

    fun dec() {
        synchronized(rwl) { count-- }
    }

    fun unlock(readOnly: Boolean) {
        // print.e("unlock:"+readOnly);
        getLock(readOnly).unlock()
    }

    private fun getLock(readOnly: Boolean): java.util.concurrent.locks.Lock {
        return if (readOnly) rl else wl
    }

    /**
     * Returns an estimate of the number of threads waiting to acquire this lock. The value is only an
     * estimate because the number of threads may change dynamically while this method traverses
     * internal data structures. This method is designed for use in monitoring of the system state, not
     * for synchronization control.
     *
     * @return the estimated number of threads waiting for this lock
     */
    val queueLength: Int
        get() {
            synchronized(rwl) { return count }
        }

    /**
     * Queries if the write lock is held by any thread.
     */
    val isWriteLocked: Boolean
        get() = rwl.isWriteLocked()

    /**
     * Queries if one or more write lock is held by any thread.
     */
    val isReadLocked: Boolean
        get() = rwl.getReadLockCount() > 0

    init {
        rwl = ReentrantReadWriteLock(true)
        rl = rwl.readLock()
        wl = rwl.writeLock()
        this.label = label
    }
}