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
package tachyon.runtime.type.scope.storage

import tachyon.commons.io.cache.CacheEntry

class SessionEndCacheEvent : CacheEventListener {
    @Override
    fun onExpires(entry: CacheEntry?) {
        val key: String = entry.getKey()

        // type
        var index: Int = key.indexOf(':')
        var last: Int
        // String type=key.substring(0,index);

        // cfid
        last = index + 1
        index = key.indexOf(':', last)
        val cfid: String = key.substring(last, index)

        // appName
        last = index + 1
        index = key.indexOf(':', last)
        val appName: String = key.substring(last)
        val config: Config = ThreadLocalPageContext.getConfig()
        _doEnd((config as ConfigWeb).getFactory() as CFMLFactoryImpl, appName, cfid)
    }

    private fun _doEnd(factory: CFMLFactoryImpl?, appName: String?, cfid: String?) {
        val listener: ApplicationListener = factory.getConfig().getApplicationListener()
        try {
            factory.getScopeContext().info("call onSessionEnd for $appName/$cfid")
            listener.onSessionEnd(factory, appName, cfid)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            ExceptionHandler.log(factory.getConfig(), Caster.toPageException(t))
        }
    }

    @Override
    fun onRemove(entry: CacheEntry?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun onPut(entry: CacheEntry?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun duplicate(): CacheEventListener? {
        // TODO Auto-generated method stub
        return null
    }
}