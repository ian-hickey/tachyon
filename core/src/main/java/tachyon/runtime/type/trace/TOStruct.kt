/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.type.trace

import java.util.Map

class TOStruct(debugger: Debugger?, sct: Struct?, type: Int, category: String?, text: String?) : TOCollection(debugger, sct, type, category, text), Struct {
    private val sct: Struct?
    @Override
    fun isEmpty(): Boolean {
        log(null)
        return sct.isEmpty()
    }

    @Override
    override fun containsKey(key: Object?): Boolean {
        log(null)
        return sct.containsKey(key)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        log(null)
        return sct.containsValue(value)
    }

    @Override
    override operator fun get(key: Object?): Object? {
        log(null)
        return sct.get(key)
    }

    @Override
    fun put(key: Object?, value: Object?): Object? {
        log(null)
        return sct.put(key, value)
    }

    @Override
    override fun remove(key: Object?): Object? {
        log(null)
        return sct.remove(key)
    }

    @Override
    fun putAll(m: Map?) {
        log(null)
        sct.putAll(m)
    }

    @Override
    fun keySet(): Set? {
        log(null)
        return sct.keySet()
    }

    @Override
    fun values(): Collection<*>? {
        log(null)
        return sct.values()
    }

    @Override
    fun entrySet(): Set? {
        log(null)
        return sct.entrySet()
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        log(null)
        return TOStruct(debugger, Duplicator.duplicate(sct, deepCopy) as Struct, type, category, text)
    }

    @Override
    fun getIterator(): Iterator<String?>? {
        return keysAsStringIterator()
    }

    companion object {
        private const val serialVersionUID = 4868199372417392722L
    }

    init {
        this.sct = sct
    }
}