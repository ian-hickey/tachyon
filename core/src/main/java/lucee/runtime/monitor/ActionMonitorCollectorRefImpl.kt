/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

class ActionMonitorCollectorRefImpl : ActionMonitorCollector {
    private val monitors: List<ActionMonitor?>? = ArrayList<ActionMonitor?>()
    private var init: Method? = null
    private var logc: Method? = null
    private var getName: Method? = null
    private var logpc: Method? = null

    @Override
    @Throws(IOException::class)
    override fun addMonitor(cs: ConfigServer?, monitor: ActionMonitor?, name: String?, log: Boolean) {
        var monitor: ActionMonitor? = monitor
        monitor = init(monitor, cs, name, log)
        if (monitor != null) monitors.add(monitor)
    }

    @Override
    override fun log(pc: PageContext?, type: String?, label: String?, executionTime: Long, data: Object?) {
        val it: Iterator<ActionMonitor?> = monitors!!.iterator()
        while (it.hasNext()) {
            log(it.next(), pc, type, label, executionTime, data)
        }
    }

    @Override
    override fun log(config: ConfigWeb?, type: String?, label: String?, executionTime: Long, data: Object?) {
        val it: Iterator<ActionMonitor?> = monitors!!.iterator()
        while (it.hasNext()) {
            log(it.next(), config, type, label, executionTime, data)
        }
    }

    @Override
    override fun getActionMonitor(name: String?): ActionMonitor? {
        val it: Iterator<ActionMonitor?> = monitors!!.iterator()
        var am: ActionMonitor?
        while (it.hasNext()) {
            am = it.next()
            if (name.equalsIgnoreCase(getName(am))) return am
        }
        return null
    }

    private fun getName(am: Object?): String? {
        if (getName == null) {
            getName = try {
                am.getClass().getMethod("getName", arrayOf<Class?>())
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                return null
            }
        }
        try {
            return getName.invoke(am, arrayOf<Object?>())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return null
    }

    private fun log(monitor: Object?, pc: PageContext?, type: String?, label: String?, executionTime: Long, data: Object?) {
        if (logpc == null) {
            logpc = try {
                monitor.getClass().getMethod("log", arrayOf<Class?>(PageContext::class.java, String::class.java, String::class.java, Long::class.javaPrimitiveType, Object::class.java))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                return
            }
        }
        try {
            logpc.invoke(monitor, arrayOf(pc, type, label, executionTime, data))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    private fun log(monitor: Object?, config: ConfigWeb?, type: String?, label: String?, executionTime: Long, data: Object?) {
        if (logc == null) {
            logc = try {
                monitor.getClass().getMethod("log", arrayOf<Class?>(ConfigWeb::class.java, String::class.java, String::class.java, Long::class.javaPrimitiveType, Object::class.java))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                return
            }
        }
        try {
            logc.invoke(monitor, arrayOf(config, type, label, executionTime, data))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    private fun init(monitor: ActionMonitor?, cs: ConfigServer?, name: String?, log: Boolean): ActionMonitor? {
        if (init == null) {
            init = try {
                monitor.getClass().getMethod("init", arrayOf<Class?>(ConfigServer::class.java, String::class.java, Boolean::class.javaPrimitiveType))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                return null
            }
        }
        return try {
            init.invoke(monitor, arrayOf(cs, name, log)) as ActionMonitor
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            null
        }
    }
}