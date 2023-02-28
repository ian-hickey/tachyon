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
package lucee.runtime.monitor

import java.io.IOException

class AsyncRequestMonitor(monitor: RequestMonitorPro?) : RequestMonitorPro {
    private val monitor: RequestMonitorPro?
    private var logEnabled = false
    @Override
    fun init(configServer: ConfigServer?, name: String?, logEnabled: Boolean) {
        monitor!!.init(configServer, name, logEnabled)
        this.logEnabled = logEnabled
    }

    @Override
    fun getType(): Short {
        return monitor.getType()
    }

    @Override
    fun getName(): String? {
        return monitor.getName()
    }

    @Override
    fun getClazz(): Class? {
        return monitor.getClazz()
    }

    @Override
    fun isLogEnabled(): Boolean {
        return monitor.isLogEnabled()
    }

    @Override
    @Throws(PageException::class)
    fun getData(config: ConfigWeb?, arguments: Map<String?, Object?>?): Query? {
        return monitor.getData(config, arguments)
    }

    @Override
    @Throws(IOException::class)
    override fun init(pc: PageContext?) {
        _Log(monitor, pc, false, logEnabled, true).start()
    }

    @Override
    @Throws(IOException::class)
    fun log(pc: PageContext?, error: Boolean) {
        _Log(monitor, pc, error, logEnabled, false).start()
    }

    internal class _Log(monitor: RequestMonitorPro?, pc: PageContext?, error: Boolean, logEnabled: Boolean, init: Boolean) : PageContextThread(pc) {
        private val monitor: RequestMonitorPro?
        private val error: Boolean
        private val logEnabled: Boolean
        private val init: Boolean
        @Override
        fun run(pc: PageContext?) {
            try {
                ThreadLocalPageContext.register(pc)
                try {
                    if (init) monitor.log(pc, error) else monitor!!.init(pc)
                } catch (e: IOException) {
                    if (logEnabled) {
                        val log: Log = ThreadLocalPageContext.getLog(pc, "io")
                        if (log != null) {
                            addParentStacktrace(e)
                            log.log(Log.LEVEL_ERROR, "io", e)
                        }
                    }
                }
            } finally {
                ThreadLocalPageContext.release()
            }
        }

        init {
            this.monitor = monitor
            this.error = error
            this.logEnabled = logEnabled
            this.init = init
        }
    }

    init {
        this.monitor = monitor
    }
}