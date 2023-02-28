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

import tachyon.commons.io.cache.CacheEntry

class ComponentCacheEventListener(component: Component?) : CacheEventListener {
    private val component: Component?
    @Override
    fun onRemove(entry: CacheEntry?) {
        call(ON_REMOVE, entry)
    }

    @Override
    fun onPut(entry: CacheEntry?) {
        call(ON_PUT, entry)
    }

    @Override
    fun onExpires(entry: CacheEntry?) {
        call(ON_EXPIRES, entry)
    }

    private fun call(methodName: Key?, entry: CacheEntry?) {
        // Struct data = entry.getCustomInfo();
        // cfc.callWithNamedValues(pc, methodName, data);
    }

    @Override
    fun duplicate(): CacheEventListener? {
        return ComponentCacheEventListener(component.duplicate(false) as Component)
    }

    companion object {
        private const val serialVersionUID = 6271280246677734153L
        private val ON_EXPIRES: Collection.Key? = KeyImpl.getInstance("onExpires")
        private val ON_PUT: Collection.Key? = KeyImpl.getInstance("onPut")
        private val ON_REMOVE: Collection.Key? = KeyImpl.getInstance("onRemove")
    }

    init {
        this.component = component
    }
}