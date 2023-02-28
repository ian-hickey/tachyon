/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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

import java.util.ArrayList

class KeyLockImpl<K> : KeyLock<K> {
    private val locks: Map<Token<K>, SimpleLock<Token<K>>> = HashMap<Token<K>, SimpleLock<Token<K>>>()
    @Override
    @Throws(LockException::class)
    fun lock(key: K, timeout: Long): Lock? {
        if (timeout <= 0) throw LockException("timeout must be a positive number")
        var lock: SimpleLock<Token<K>>?
        val token = Token(key)
        synchronized(locks) {
            lock = locks[token]
            if (lock == null) {
                locks.put(token, SimpleLock<Token<K>>(token).also { lock = it })
            } else if (lock.getLabel().getThreadId() === token.threadId) {
                return null
            }
        }
        lock!!.lock(timeout)
        return lock
    }

    @Override
    fun unlock(lock: Lock?) {
        if (lock == null) return
        synchronized(locks) {
            if (lock.getQueueLength() === 0) {
                locks.remove((lock as SimpleLock<Token<K>?>).getLabel())
            }
        }
        lock.unlock()
    }

    @get:Override
    val openLockNames: List<K>
        get() {
            val it: Iterator<Entry<Token<K>, SimpleLock<Token<K>>>> = locks.entrySet().iterator()
            var entry: Entry<Token<K>, SimpleLock<Token<K>>>
            val list: List<K> = ArrayList<K>()
            while (it.hasNext()) {
                entry = it.next()
                if (entry.getValue().getQueueLength() > 0) list.add(entry.getKey().getKey())
            }
            return list
        }

    @Override
    fun clean() {
        val it: Iterator<Entry<Token<K>, SimpleLock<Token<K>>>> = locks.entrySet().iterator()
        var entry: Entry<Token<K>, SimpleLock<Token<K>>>
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

internal class Token<K>(val key: K) {

    /**
     * @return the id
     */
    val threadId: Long

    @Override
    override fun toString(): String {
        return key.toString()
    }

    @Override
    override fun equals(obj: Object): Boolean {
        var obj: Object = obj
        if (obj is Token<*>) {
            val other = obj as Token<*>
            obj = other.key
        }
        return key!!.equals(obj)
    }

    @Override
    override fun hashCode(): Int {
        return key!!.hashCode()
    }

    /**
     * @param key
     */
    init {
        threadId = Thread.currentThread().getId()
    }
}