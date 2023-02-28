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
package coldfusion.server

import java.util.Map

interface EventGatewayService : Service {
    // public abstract EventRequestDispatcher getEventRequestDispatcher();
    // public abstract EventRequestHandler getEventRequestHandler();
    // public abstract Logger getLogger();
    // public abstract Logger getLogger(String arg0);
    fun startEventGateway(arg0: String?)
    fun stopEventGateway(arg0: String?)
    fun restartEventGateway(arg0: String?)
    fun getEventGatewayStatus(arg0: String?): Int
    fun removeGateway(arg0: String?)
    fun registerGateway(arg0: String?, arg1: String?, arg2: String?, arg3: Array<String?>?, arg4: String?)
    fun getGateways(): Vector?
    fun getGatewayInfo(arg0: String?): Map?
    fun removeGatewayType(arg0: String?)
    fun registerGatewayType(arg0: String?, arg1: String?, arg2: String?, arg3: Int, arg4: Boolean)
    fun getGatewayTypes(): Vector?
    fun getGatewayTypeInfo(arg0: String?): Map?
    fun setCFCListeners(arg0: String?, arg1: Array<String?>?)
    fun getGatewayCFCListeners(arg0: String?): Array<String?>?

    // public abstract Gateway getGateway(String arg0);
    fun incrementEventsIn(arg0: String?)
    fun getEventsIn(arg0: String?): Long
    fun resetEventsIn(arg0: String?)
    fun incrementEventsOut(arg0: String?)
    fun getEventsOut(arg0: String?): Long
    fun resetEventsOut(arg0: String?) // public abstract GatewayInfo getGatewayStats(String arg0);
}