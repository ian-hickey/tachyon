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
package lucee.runtime.engine

import java.io.IOException

class ThreadQueueImpl : ThreadQueue {
    private val token: SerializableObject? = SerializableObject()
    val list: List<PageContext?>? = ArrayList<PageContext?>()
    private var waiting = 0
    @Override
    @Throws(IOException::class)
    fun enter(pc: PageContext?) {
        try {
            synchronized(token) { waiting++ }
            _enter(pc)
        } finally {
            synchronized(token) { waiting-- }
        }
    }

    @Throws(IOException::class)
    private fun _enter(pc: PageContext?) {
        val ci: ConfigPro = pc.getConfig() as ConfigPro
        // print.e("enter("+Thread.currentThread().getName()+"):"+list.size());
        val start: Long = System.currentTimeMillis()
        var timeout: Long = ci.getQueueTimeout()
        if (timeout <= 0) timeout = pc.getRequestTimeout()
        while (true) {
            synchronized(token) {
                if (list!!.size() < ci.getQueueMax()) {
                    // print.e("- ok("+Thread.currentThread().getName()+"):"+list.size());
                    list.add(pc)
                    return
                }
            }
            if (timeout > 0) SystemUtil.wait(token, timeout) else SystemUtil.wait(token)
            if (timeout > 0 && System.currentTimeMillis() - start >= timeout) throw IOException("Concurrent request timeout (" + (System.currentTimeMillis() - start).toString() + ") ["
                    + timeout.toString() + " ms] has occurred, server is too busy handling other requests. This timeout setting can be changed in the server administrator.")
        }
    }

    @Override
    fun exit(pc: PageContext?) {
        // print.e("exist("+Thread.currentThread().getName()+")");
        synchronized(token) {
            list.remove(pc)
            token.notify()
        }
    }

    @Override
    fun size(): Int {
        return waiting
    }

    @Override
    fun clear() {
        list.clear()
        token.notifyAll()
    }
}