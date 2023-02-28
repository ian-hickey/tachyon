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

import lucee.runtime.config.ConfigServer

interface Monitor {
    fun init(configServer: ConfigServer?, name: String?, logEnabled: Boolean)
    fun getType(): Short
    fun getName(): String?

    @SuppressWarnings("rawtypes")
    fun getClazz(): Class?
    fun isLogEnabled(): Boolean

    companion object {
        const val TYPE_INTERVAL: Short = 1
        const val TYPE_REQUEST: Short = 2
        const val TYPE_ACTION: Short = 4

        @Deprecated
        val TYPE_INTERVALL = TYPE_INTERVAL
    }
}