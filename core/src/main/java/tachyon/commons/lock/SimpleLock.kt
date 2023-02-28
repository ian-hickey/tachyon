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

import java.util.concurrent.TimeUnit

class SimpleLock<L>(label: L) : Lock {
    private val lock: ReentrantLock
    val label: L
    @Override
    @Throws(LockException::class)
    fun lock(timeout: Long) {
        var timeout = timeout
        if (timeout <= 0) throw LockException("timeout must be a positive number")
        val initialTimeout = timeout
        val start: Long = System.currentTimeMillis()
        do {
            timeout -= try {
                if (!lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    throw LockException(initialTimeout)
                }
                break // exit loop
            } catch (e: InterruptedException) {
                System.currentTimeMillis() - start
            }
            if (timeout <= 0) {
                // Tachyon was not able to aquire lock in time
                throw LockException(initialTimeout)
            }
        } while (true)
    }

    @Override
    fun unlock() {
        lock.unlock()
    }

    @get:Override
    val queueLength: Int
        get() = lock.getQueueLength()

    init {
        lock = ReentrantLock(true)
        this.label = label
    }
}