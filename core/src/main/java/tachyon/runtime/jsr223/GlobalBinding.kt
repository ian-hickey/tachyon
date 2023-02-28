/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.jsr223

import java.util.Collection

class GlobalBinding(pc: PageContext?) : Bindings {
    private val server: Server?
    @Override
    fun size(): Int {
        return server.size()
    }

    @Override
    fun isEmpty(): Boolean {
        return server.isEmpty()
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return server.containsValue(value)
    }

    @Override
    fun clear() {
        server.clear()
    }

    @Override
    fun keySet(): Set<String?>? {
        return server.keySet()
    }

    @Override
    fun values(): Collection<Object?>? {
        return server.values()
    }

    @Override
    fun entrySet(): Set<Map.Entry<String?, Object?>?>? {
        return server.entrySet()
    }

    @Override
    fun put(name: String?, value: Object?): Object? {
        return server.put(name, value)
    }

    @Override
    fun putAll(toMerge: Map<out String?, Object?>?) {
        server.putAll(toMerge)
    }

    @Override
    fun containsKey(key: Object?): Boolean {
        return server.containsKey(key)
    }

    @Override
    operator fun get(key: Object?): Object? {
        return server.get(key)
    }

    @Override
    fun remove(key: Object?): Object? {
        return server.remove(key)
    }

    init {
        server = ScopeContext.getServerScope(pc, true)
    }
}