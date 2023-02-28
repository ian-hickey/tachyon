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
package tachyon.runtime.cache.tag.udf

import java.io.Serializable

class UDFCacheItem(val output: String?, returnValue: Object?, udfName: String?, meta: String?, executionTimeNS: Long) : CacheItem, Serializable, Dumpable, Duplicable {
    val returnValue: Object?
    private val udfName: String?
    private val meta: String?
    private val executionTimeNS: Long
    private val payload: Long
    private var hash: String? = null
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        val table = DumpTable("#669999", "#ccffff", "#000000")
        table.setTitle("UDFCacheEntry")
        table.appendRow(1, SimpleDumpData("Return Value"), DumpUtil.toDumpData(returnValue, pageContext, maxlevel, properties))
        table.appendRow(1, SimpleDumpData("Output"), DumpUtil.toDumpData(SimpleDumpData(output), pageContext, maxlevel, properties))
        return table
    }

    @Override
    override fun toString(): String {
        return output!!
    }

    @Override
    fun getHashFromValue(): String? {
        if (hash == null) hash = Long.toString(HashUtil.create64BitHash(output.toString() + ":" + UDFArgConverter.serialize(returnValue)))
        return hash
    }

    @Override
    fun getName(): String? {
        return udfName
    }

    @Override
    fun getPayload(): Long {
        return payload
    }

    @Override
    fun getMeta(): String? {
        return meta
    }

    @Override
    fun getExecutionTime(): Long {
        return executionTimeNS
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return UDFCacheItem(output, Duplicator.duplicate(returnValue, deepCopy), udfName, meta, executionTimeNS)
    }

    companion object {
        private const val serialVersionUID = -3616023500492159529L
    }

    init {
        this.returnValue = returnValue
        this.udfName = udfName
        this.meta = meta
        this.executionTimeNS = executionTimeNS
        payload = output?.length()?.toLong() ?: 0
    }
}