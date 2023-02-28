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

class ActionMonitorCollectorImpl : ActionMonitorCollector {
    private var monitors: List<ActionMonitor?>? = null

    @Override
    @Throws(IOException::class)
    override fun addMonitor(cs: ConfigServer?, monitor: ActionMonitor?, name: String?, log: Boolean) {
        monitor.init(cs, name, log)
        if (monitors == null) monitors = ArrayList<ActionMonitor?>()
        monitors.add(monitor)
    }

    /**
     * logs certain action within a Request
     *
     * @param pc
     * @param ar
     * @throws IOException
     */
    @Override
    override fun log(pc: PageContext?, type: String?, label: String?, executionTime: Long, data: Object?) {
        if (monitors == null) return
        val it: Iterator<ActionMonitor?> = monitors!!.iterator()
        while (it.hasNext()) {
            try {
                it.next().log(pc, type, label, executionTime, data)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
    }

    @Override
    override fun log(config: ConfigWeb?, type: String?, label: String?, executionTime: Long, data: Object?) {
        if (monitors == null) return
        val it: Iterator<ActionMonitor?> = monitors!!.iterator()
        while (it.hasNext()) {
            try {
                it.next().log(config, type, label, executionTime, data)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
    }

    @Override
    override fun getActionMonitor(name: String?): ActionMonitor? {
        if (monitors == null) return null
        val it: Iterator<ActionMonitor?> = monitors!!.iterator()
        var am: ActionMonitor?
        while (it.hasNext()) {
            am = it.next()
            if (name.equalsIgnoreCase(am.getName())) return am
        }
        return null
    }
}