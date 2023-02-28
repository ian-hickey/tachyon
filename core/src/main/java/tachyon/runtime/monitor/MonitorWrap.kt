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

import tachyon.runtime.config.ConfigServer

abstract class MonitorWrap(monitor: Object?, type: Short) : Monitor {
    private var configServer: ConfigServer? = null
    protected var monitor: Object?
    private var name: String? = null
    private val type: Short
    private var logEnabled = false
    @Override
    fun init(configServer: ConfigServer?, name: String?, logEnabled: Boolean) {
        this.configServer = configServer
        this.name = name
        this.logEnabled = logEnabled
    }

    @Override
    fun getType(): Short {
        return type
    }

    fun getMonitor(): Object? {
        return monitor
    }

    @Override
    fun getName(): String? {
        return name
    }

    @Override
    fun isLogEnabled(): Boolean {
        return logEnabled
    }

    @Override
    fun getClazz(): Class? {
        return monitor.getClass()
    }

    companion object {
        private val PARAMS_LOG: Array<Object?>? = arrayOfNulls<Object?>(0)
    }

    init {
        this.monitor = monitor
        this.type = type
    }
}