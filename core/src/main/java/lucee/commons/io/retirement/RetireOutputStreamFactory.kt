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
package lucee.commons.io.retirement

import java.util.ArrayList

object RetireOutputStreamFactory {
    var list: List<RetireOutputStream> = ArrayList<RetireOutputStream>()
    private var thread: RetireThread? = null
    var isClosed = false
        private set

    /**
     * close existing threads and stops opening new onces
     */
    fun close() {
        if (thread != null && thread.isAlive()) {
            thread!!.close = true
            isClosed = true
            SystemUtil.notify(thread)
            SystemUtil.stop(thread)
        }
    }

    fun startThread(timeout: Long) {
        var timeout = timeout
        if (timeout < 1000) timeout = 1000
        if (thread == null || !thread.isAlive()) {
            thread = RetireThread(timeout)
            thread.start()
        } else if (thread!!.sleepTime > timeout) {
            thread!!.sleepTime = timeout
            SystemUtil.notify(thread)
        }
    }

    internal class RetireThread(var sleepTime: Long) : ParentThreasRefThread() {
        var close = false
        @Override
        fun run() {
            while (true) {
                try {
                    if (list.size() === 0) break
                    synchronized(this) { this.wait(sleepTime) }
                    val arr: Array<RetireOutputStream?> = list.toArray(arrayOfNulls<RetireOutputStream>(list.size())) // not using iterator to avoid ConcurrentModificationException
                    for (i in arr.indices) {
                        if (arr[i] == null) continue
                        if (close) arr[i].retireNow() else arr[i].retire()
                    }
                    if (close) break
                } catch (ie: InterruptedException) {
                    addParentStacktrace(ie)
                    LogUtil.log("file", ie)
                    break
                } catch (e: Exception) {
                    addParentStacktrace(e)
                    LogUtil.log("file", e)
                }
            }
            thread = null
        }
    }
}