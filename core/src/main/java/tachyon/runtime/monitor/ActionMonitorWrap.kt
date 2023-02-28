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
package tachyon.runtime.monitor

import java.io.IOException

class ActionMonitorWrap(monitor: Object?) : MonitorWrap(monitor, TYPE_ACTION), ActionMonitor {
    private var log: Method? = null
    private var getData: Method? = null
    @Override
    @Throws(IOException::class)
    fun log(pc: PageContext?, type: String?, label: String?, executionTime: Long, data: Object?) {
        try {
            if (log == null) {
                log = monitor.getClass().getMethod("log", PARAMS_LOG1)
            }
            log.invoke(monitor, arrayOf(pc, type, label, Caster.toLong(executionTime), data))
        } catch (e: Exception) {
            throw ExceptionUtil.toIOException(e)
        }
    }

    @Override
    @Throws(IOException::class)
    fun log(config: ConfigWeb?, type: String?, label: String?, executionTime: Long, data: Object?) {
        try {
            if (log == null) {
                log = monitor.getClass().getMethod("log", PARAMS_LOG2)
            }
            log.invoke(monitor, arrayOf(config, type, label, Caster.toLong(executionTime), data))
        } catch (e: Exception) {
            throw ExceptionUtil.toIOException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun getData(arguments: Map<String?, Object?>?): Query? {
        return try {
            if (getData == null) {
                getData = monitor.getClass().getMethod("getData", arrayOf<Class?>(Map::class.java))
            }
            getData.invoke(monitor, arrayOf(arguments)) as Query
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    companion object {
        private val PARAMS_LOG1: Array<Class?>? = arrayOf<Class?>(PageContext::class.java, String::class.java, String::class.java, Long::class.javaPrimitiveType, Object::class.java)
        private val PARAMS_LOG2: Array<Class?>? = arrayOf<Class?>(ConfigWeb::class.java, String::class.java, String::class.java, Long::class.javaPrimitiveType, Object::class.java)
    }
}