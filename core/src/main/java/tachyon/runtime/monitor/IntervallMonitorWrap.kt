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
package tachyon.runtime.monitor

import java.io.IOException

class IntervallMonitorWrap(monitor: Object?) : MonitorWrap(monitor, TYPE_INTERVAL), IntervallMonitor {
    private var log: Method? = null
    private var getData: Method? = null
    @Override
    @Throws(IOException::class)
    fun log() {
        try {
            if (log == null) {
                log = monitor.getClass().getMethod("log", arrayOfNulls<Class?>(0))
            }
            log.invoke(monitor, PARAMS_LOG)
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
        private val PARAMS_LOG: Array<Object?>? = arrayOfNulls<Object?>(0)
    }
}