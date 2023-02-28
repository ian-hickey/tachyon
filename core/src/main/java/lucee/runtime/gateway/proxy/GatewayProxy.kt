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
package lucee.runtime.gateway.proxy

import java.io.IOException

class GatewayProxy(gateway: Gateway?) : Gateway {
    private val gateway: Gateway?
    @Override
    @Throws(IOException::class)
    fun init(engine: GatewayEngine?, id: String?, cfcPath: String?, config: Map?) {
        gateway.init(engine, id, cfcPath, config)
    }

    @Override
    fun getId(): String? {
        return gateway.getId()
    }

    @Override
    @Throws(IOException::class)
    fun sendMessage(data: Map?): String? {
        return gateway.sendMessage(data)
    }

    @Override
    fun getHelper(): Object? {
        return gateway.getHelper()
    }

    @Override
    @Throws(IOException::class)
    fun doStart() {
        gateway.doStart()
    }

    @Override
    @Throws(IOException::class)
    fun doStop() {
        gateway.doStop()
    }

    @Override
    @Throws(IOException::class)
    fun doRestart() {
        gateway.doRestart()
    }

    @Override
    fun getState(): Int {
        return gateway.getState()
    }

    fun getGateway(): Gateway? {
        return gateway
    }

    init {
        this.gateway = gateway
    }
}