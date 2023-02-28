/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.jsr223

import java.util.Collection

class EngineBinding(pc: PageContext?) : Bindings {
    private val pc: PageContext?
    @Override
    fun size(): Int {
        return pc.undefinedScope().size()
    }

    @Override
    fun isEmpty(): Boolean {
        return pc.undefinedScope().isEmpty()
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return pc.undefinedScope().containsValue(value)
    }

    @Override
    fun clear() {
        pc.undefinedScope().clear()
    }

    @Override
    fun keySet(): Set<String?>? {
        return pc.undefinedScope().keySet()
    }

    @Override
    fun values(): Collection<Object?>? {
        return pc.undefinedScope().values()
    }

    @Override
    fun entrySet(): Set<Map.Entry<String?, Object?>?>? {
        return pc.undefinedScope().entrySet()
    }

    @Override
    fun put(name: String?, value: Object?): Object? {
        return pc.undefinedScope().put(name, value)
    }

    @Override
    fun putAll(toMerge: Map<out String?, Object?>?) {
        pc.undefinedScope().putAll(toMerge)
    }

    @Override
    fun containsKey(key: Object?): Boolean {
        return pc.undefinedScope().containsKey(key)
    }

    @Override
    operator fun get(key: Object?): Object? {
        return pc.undefinedScope().get(key)
    }

    @Override
    fun remove(key: Object?): Object? {
        return pc.undefinedScope().remove(key)
    }

    init {
        this.pc = pc
    }
}