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

import lucee.commons.lang.ExceptionUtil

class GatewayThread(engine: GatewayEngine?, gateway: Gateway?, action: Int) : ParentThreasRefThread() {
    private val engine: GatewayEngine?
    private val gateway: Gateway?
    private val action: Int
    @Override
    fun run() {
        // MUST handle timeout
        try {
            if (action == START) gateway.doStart() else if (action == STOP) gateway.doStop() else if (action == RESTART) gateway.doRestart()
        } catch (ge: Throwable) {
            ExceptionUtil.rethrowIfNecessary(ge)
            addParentStacktrace(ge)
            engine.log(gateway, GatewayEngine.LOGLEVEL_ERROR, ge.getMessage())
        }
    }

    companion object {
        const val START = 0
        const val STOP = 1
        const val RESTART = 2
    }

    init {
        this.engine = engine
        this.gateway = gateway
        this.action = action
        this.setName("EventGateway-" + gateway.getId()) // name the thread
        if (gateway is GatewaySupport) (gateway as GatewaySupport?)!!.setThread(this)
    }
}