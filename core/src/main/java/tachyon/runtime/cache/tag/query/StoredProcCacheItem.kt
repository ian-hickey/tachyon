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
package tachyon.runtime.cache.tag.query

import java.io.Serializable

class StoredProcCacheItem(sct: Struct?, procedure: String?, executionTime: Long) : CacheItem, Serializable, Duplicable {
    private val sct: Struct?
    private val procedure: String?
    private val executionTime: Long
    @Override
    fun getHashFromValue(): String? {
        return toString(HashUtil.create64BitHash(UDFArgConverter.serialize(sct)))
    }

    @Override
    fun getName(): String? {
        return procedure
    }

    @Override
    fun getPayload(): Long {
        return sct.size()
    }

    @Override
    fun getMeta(): String? {
        return ""
    }

    @Override
    fun getExecutionTime(): Long {
        return executionTime
    }

    fun getStruct(): Struct? {
        return sct
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return StoredProcCacheItem(sct.duplicate(true) as Struct, procedure, executionTime)
    }

    companion object {
        private const val serialVersionUID = 7327671003736543783L
    }

    init {
        this.sct = sct
        this.procedure = procedure
        this.executionTime = executionTime
    }
}