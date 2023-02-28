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
package lucee.commons.lock.rw

import java.util.ArrayList

class RWKeyLock<K> {
    private val locks: Map<K, RWLock<K>> = ConcurrentHashMap<K, RWLock<K>>()
    @Throws(LockException::class, LockInterruptedException::class)
    fun lock(token: K, timeout: Long, readOnly: Boolean): Lock {
        if (timeout <= 0) throw LockException("timeout must be a positive number")
        var wrap: RWWrap<K>
        // K token=key;
        synchronized(locks) {
            var lock: RWLock<K>?
            lock = locks[token]
            if (lock == null) {
                locks.put(token, RWLock<K>(token).also { lock = it })
            }
            lock!!.inc()
            wrap = RWWrap(lock, readOnly)
        }
        try {
            wrap.lock(timeout)
        } catch (e: LockException) {
            synchronized(locks) { wrap.getLock()!!.dec() }
            throw e
        } catch (e: LockInterruptedException) {
            synchronized(locks) { wrap.getLock()!!.dec() }
            throw e
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            synchronized(locks) { wrap.getLock()!!.dec() }
            throw PageRuntimeException(Caster.toPageException(t))
        }
        return wrap
    }

    fun unlock(lock: Lock) {
        if (lock !is RWWrap<*>) {
            return
        }
        lock.unlock()
        synchronized(locks) {
            (lock as RWWrap<*>).getLock()!!.dec()
            if (lock.getQueueLength() === 0) {
                locks.remove((lock as RWWrap<*>).label)
            }
        }
    }

    val openLockNames: List<K>
        get() {
            val it: Iterator<Entry<K, RWLock<K>>> = locks.entrySet().iterator()
            var entry: Entry<K, RWLock<K>>
            val list: List<K> = ArrayList<K>()
            while (it.hasNext()) {
                entry = it.next()
                if (entry.getValue().getQueueLength() > 0) list.add(entry.getKey())
            }
            return list
        }

    /**
     * Queries if the write lock is held by any thread on given lock token, returns null when lock with
     * this token does not exists
     *
     * @param token name of the lock to check
     * @return
     */
    fun isWriteLocked(token: K): Boolean? {
        val lock: RWLock<K> = locks[token] ?: return null
        return lock.isWriteLocked()
    }

    /**
     * Queries if one or more read lock is held by any thread on given lock token, returns null when
     * lock with this token does not exists
     *
     * @param token name of the lock to check
     * @return
     */
    fun isReadLocked(token: K): Boolean? {
        val lock: RWLock<K> = locks[token] ?: return null
        return lock.isReadLocked()
    }

    fun clean() {
        val it: Iterator<Entry<K, RWLock<K>>> = locks.entrySet().iterator()
        var entry: Entry<K, RWLock<K>>
        while (it.hasNext()) {
            entry = it.next()
            if (entry.getValue().getQueueLength() === 0) {
                synchronized(locks) {
                    if (entry.getValue().getQueueLength() === 0) {
                        locks.remove(entry.getKey())
                    }
                }
            }
        }
    }
}

internal class RWWrap<L>(lock: RWLock<L>?, readOnly: Boolean) : Lock {
    private val lock: RWLock<L>?
    val isReadOnly: Boolean
    @Override
    @Throws(LockException::class, LockInterruptedException::class)
    fun lock(timeout: Long) {
        lock!!.lock(timeout, isReadOnly)
    }

    @Override
    fun unlock() {
        lock!!.unlock(isReadOnly)
    }

    @get:Override
    val queueLength: Int
        get() = lock.getQueueLength()
    val label: L
        get() = lock.getLabel()

    fun getLock(): RWLock<L>? {
        return lock
    }

    init {
        this.lock = lock
        isReadOnly = readOnly
    }
}