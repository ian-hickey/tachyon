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

object ActionMonitorFatory {
    fun getActionMonitorCollector(): ActionMonitorCollector? {
        return if (SystemUtil.getLoaderVersion() > 4) ActionMonitorCollectorImpl() else ActionMonitorCollectorRefImpl()
    }

    @Throws(IOException::class)
    fun getActionMonitorCollector(cs: ConfigServer?, temps: Array<ConfigWebFactory.MonitorTemp?>?): ActionMonitorCollector? {
        // try to load with interface
        return try {
            val collector: ActionMonitorCollector = ActionMonitorCollectorImpl()
            addMonitors(collector, cs, temps)
            collector
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            val collector: ActionMonitorCollector = ActionMonitorCollectorRefImpl()
            addMonitors(collector, cs, temps)
            collector
        }
    }

    @Throws(IOException::class)
    private fun addMonitors(collector: ActionMonitorCollector?, cs: ConfigServer?, temps: Array<MonitorTemp?>?) {
        var temp: MonitorTemp?
        for (i in temps.indices) {
            temp = temps!![i]
            collector!!.addMonitor(cs, temp.am, temp.name, temp.log)
        }
    }
}