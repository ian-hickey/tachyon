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
package lucee.runtime.gateway

import java.util.Map

interface GatewayEngine {
    /**
     * invoke given method on cfc listener
     *
     * @param gateway gateway
     * @param method method to invoke
     * @param data arguments
     * @return returns if invocation was successfull
     */
    fun invokeListener(gateway: Gateway?, method: String?, data: Map<*, *>?): Boolean

    /**
     * logs message with defined logger for gateways
     *
     * @param gateway gateway
     * @param level level
     * @param message message
     */
    fun log(gateway: Gateway?, level: Int, message: String?)

    companion object {
        const val LOGLEVEL_INFO = 0
        const val LOGLEVEL_DEBUG = 1
        const val LOGLEVEL_WARN = 2
        const val LOGLEVEL_ERROR = 3
        const val LOGLEVEL_FATAL = 4
        const val LOGLEVEL_TRACE = 5
    }
}