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
package tachyon.commons.io.res.util

import java.util.HashMap

class ResourceLockImpl(
        /**
         * @param lockTimeout the lockTimeout to set
         */
        @set:Override
        @get:Override var lockTimeout: Long, private var caseSensitive: Boolean) : ResourceLock {
    private val token: Object = SerializableObject()
    private val resources: Map<String, Thread> = HashMap<String, Thread>()
    @Override
    fun lock(res: Resource) {
        val path = getPath(res)
        synchronized(token) {
            _read(path)
            resources.put(path, Thread.currentThread())
        }
    }

    private fun getPath(res: Resource): String {
        return if (caseSensitive) res.getPath() else res.getPath().toLowerCase()
    }

    @Override
    fun unlock(res: Resource) {
        val path = getPath(res)
        // if(path.endsWith(".dmg"))print.err("unlock:"+path);
        synchronized(token) {
            resources.remove(path)
            token.notifyAll()
        }
    }

    @Override
    fun read(res: Resource) {
        val path = getPath(res)
        synchronized(token) {
            // print.ln(".......");
            _read(path)
        }
    }

    private fun _read(path: String) {
        var start: Long = -1
        var now: Long
        var t: Thread
        do {
            if (resources[path].also { t = it } == null) {
                return
            }
            if (t === Thread.currentThread()) {
                val config: Config = ThreadLocalPageContext.getConfig()
                if (!LogUtil.isAlreadyInLog()) LogUtil.log(config, Log.LEVEL_ERROR, "file", "Conflict in same thread: on [$path]")
                return
            }
            // bugfix when lock from dead thread, it will be ignored
            if (!t.isAlive()) {
                resources.remove(path)
                return
            }
            if (start == -1L) start = System.currentTimeMillis()
            try {
                token.wait(lockTimeout)
                now = System.currentTimeMillis()
                if (start + lockTimeout <= now) {
                    val config: Config = ThreadLocalPageContext.getConfig()
                    if (config != null) {
                        var pc: PageContextImpl? = null
                        var add = ""
                        if (config is ConfigWeb) {
                            val factory: CFMLFactory = (config as ConfigWeb).getFactory()
                            if (factory is CFMLFactoryImpl) {
                                val pcs: Map<Integer, PageContextImpl> = (factory as CFMLFactoryImpl).getActivePageContexts()
                                val it: Iterator<PageContextImpl> = pcs.values().iterator()
                                var tmp: PageContextImpl
                                while (it.hasNext()) {
                                    tmp = it.next()
                                    if (t === tmp.getThread()) {
                                        pc = tmp
                                        break
                                    }
                                }
                            }
                        }
                        if (pc != null) {
                            add = " The file is locked by a request on the following URL [" + ReqRspUtil.getRequestURL(pc.getHttpServletRequest(), true)
                                    .toString() + "], that request started " + (System.currentTimeMillis() - pc.getStartTime()).toString() + "ms ago."
                        }
                        if (!LogUtil.isAlreadyInLog()) LogUtil.log(config, Log.LEVEL_ERROR, "file",
                                "Timeout after " + (now - start) + " ms (" + lockTimeout + " ms) occurred while accessing file [" + path + "]." + add)
                    } else {
                        if (!LogUtil.isAlreadyInLog()) LogUtil.log(config, Log.LEVEL_ERROR, "file", "Timeout ($lockTimeout ms) occurred while accessing file [$path].")
                    }
                    return
                }
            } catch (e: InterruptedException) {
            }
        } while (true)
    }

    /**
     * @param caseSensitive the caseSensitive to set
     */
    fun setCaseSensitive(caseSensitive: Boolean) {
        this.caseSensitive = caseSensitive
    }

    companion object {
        private const val serialVersionUID = 6888529579290798651L
    }
}