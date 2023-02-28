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
package tachyon.runtime.cache

import java.io.IOException

class ServerCacheConnection(cs: ConfigServerImpl?, cc: CacheConnection?) : CacheConnectionPlus {
    private val cc: CacheConnection?
    private val cs: ConfigServerImpl?
    @Override
    @Throws(IOException::class)
    fun duplicate(config: Config?): CacheConnection? {
        return ServerCacheConnection(cs, cc.duplicate(config) as CacheConnectionPlus)
    }

    @Override
    fun getClassDefinition(): ClassDefinition? {
        return cc.getClassDefinition()
    }

    @Override
    fun getCustom(): Struct? {
        return cc.getCustom()
    }

    @Override
    @Throws(IOException::class)
    fun getInstance(config: Config?): Cache? {
        return cc.getInstance(cs)
    }

    @Override
    fun getName(): String? {
        return cc.getName()
    }

    @Override
    fun isReadOnly(): Boolean {
        return true
    }

    @Override
    fun isStorage(): Boolean {
        return cc.isStorage()
    }

    @Override
    override fun getLoadedInstance(): Cache? {
        if (cc is CacheConnectionPlus) return (cc as CacheConnectionPlus?)!!.getLoadedInstance()
        try {
            return cc.getInstance(null)
        } catch (e: Exception) {
        }
        return null
    }

    /**
     * Constructor of the class
     *
     * @param configServer
     * @param cc
     */
    init {
        this.cs = cs
        this.cc = cc
    }
}