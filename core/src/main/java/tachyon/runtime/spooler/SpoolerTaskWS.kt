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
package tachyon.runtime.spooler

import tachyon.commons.lang.ExceptionUtil

abstract class SpoolerTaskWS(plans: Array<ExecutionPlan?>?, client: RemoteClient?) : SpoolerTaskSupport(plans) {
    private val client: RemoteClient?
    @Override
    @Throws(PageException::class)
    fun execute(config: Config?): Object? {
        return try {
            val rpc: WSClient = (ThreadLocalPageContext.getConfig(config) as ConfigWebPro).getWSHandler().getWSClient(client.getUrl(), client.getServerUsername(),
                    client.getServerPassword(), client.getProxyData())
            rpc.callWithNamedValues(config, KeyImpl.init(getMethodName()), getArguments())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    @Override
    fun subject(): String? {
        return client.getLabel()
    }

    @Override
    fun detail(): Struct? {
        val sct: Struct = StructImpl()
        sct.setEL("label", client.getLabel())
        sct.setEL("url", client.getUrl())
        return sct
    }

    protected abstract fun getMethodName(): String?
    protected abstract fun getArguments(): Struct?

    init {
        this.client = client
    }
}