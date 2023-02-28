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
package tachyon.runtime.engine

import tachyon.commons.io.log.LogUtil

/**
 * own thread how check the main thread and his data
 */
class Monitor(configServer: ConfigServer?, state: ControllerState?) : ParentThreasRefThread() {
    private val configServer: ConfigServerImpl?
    private val state: ControllerState?
    @Override
    fun run() {
        var tries: Short = 0
        while (state!!.active()) {
            try {
                sleep(INTERVALL)
            } catch (e: InterruptedException) {
                addParentStacktrace(e)
                LogUtil.log(configServer, Monitor::class.java.getName(), e)
            }
            if (!configServer.isMonitoringEnabled()) return
            val monitors: Array<tachyon.runtime.monitor.IntervallMonitor?> = configServer.getIntervallMonitors()
            var logCount = 0
            if (monitors != null) for (i in monitors.indices) {
                if (monitors[i].isLogEnabled()) {
                    logCount++
                    try {
                        monitors[i].log()
                    } catch (e: Exception) {
                        addParentStacktrace(e)
                        LogUtil.log(configServer, Monitor::class.java.getName(), e)
                    }
                }
            }
            if (logCount == 0) {
                tries++
                if (tries >= 10) return
            }
        }
    }

    companion object {
        private const val INTERVALL: Long = 5000
    }

    /**
     * @param contextes
     * @param interval
     * @param run
     */
    init {
        this.state = state
        this.configServer = configServer as ConfigServerImpl?
    }
}