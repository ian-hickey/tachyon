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
package lucee.runtime.functions.system

import java.lang.management.ManagementFactory

object GetSystemInfo : Function {
    private const val serialVersionUID = 1L
    @Throws(PageException::class)
    fun call(pc: PageContext?): Struct? {
        val sct: Struct = StructImpl()
        val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
        val factory: CFMLFactoryImpl = config.getFactory() as CFMLFactoryImpl
        val sc: ScopeContext = factory.getScopeContext()
        val osBean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()

        // threads/requests
        sct.put("activeRequests", factory.getActiveRequests())
        sct.put("activeThreads", factory.getActiveThreads())
        sct.put("queueRequests", config.getThreadQueue().size())

        // Datasource connections
        run {

            // TODO provide more data
            var idle = 0
            var active = 0
            val waiters = 0
            for (pool in config.getDatasourceConnectionPools()) {
                idle += pool.getNumIdle()
                active += pool.getNumActive()
                idle += pool.getNumWaiters()
            }
            sct.put("activeDatasourceConnections", active)
            sct.put("idleDatasourceConnections", idle)
            sct.put("waitingForConn", waiters)
        }

        // tasks
        sct.put("tasksOpen", config.getSpoolerEngine().getOpenTaskCount())
        sct.put("tasksClosed", config.getSpoolerEngine().getClosedTaskCount())

        // scopes
        sct.put("sessionCount", sc.getSessionCount())
        sct.put("clientCount", sc.getClientCount())
        sct.put("applicationContextCount", sc.getAppContextCount())

        // cpu
        getCPU(sct)
        return sct
    }

    fun getCPU(data: Struct?) {
        var process: Object? = Double.valueOf(0)
        var system: Object? = Double.valueOf(0)
        try {
            val mbs: MBeanServer = ManagementFactory.getPlatformMBeanServer()
            val name: ObjectName = ObjectName.getInstance("java.lang:type=OperatingSystem")
            val list: AttributeList = mbs.getAttributes(name, arrayOf<String?>("ProcessCpuLoad", "SystemCpuLoad"))
            // Process
            if (list.size() >= 1) {
                val attr: Attribute = list.get(0) as Attribute
                val obj: Object = attr.getValue()
                if (obj is Double) process = obj
            }

            // System
            if (list.size() >= 2) {
                val attr: Attribute = list.get(1) as Attribute
                val obj: Object = attr.getValue()
                if (obj is Double) system = obj
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            data.setEL("cpuProcess", process)
            data.setEL("cpuSystem", system)
        }
    }
}