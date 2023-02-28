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
package lucee.runtime.cache.tag.include

import java.io.Serializable

class IncludeCacheItem : CacheItem, Serializable, Dumpable, Duplicable {
    val output: String?
    private val executionTimeNS: Long
    private val path: String?
    private val name: String?
    private val payload: Int

    constructor(output: String?, ps: PageSource?, executionTimeNS: Long) {
        this.output = output
        path = ps.getDisplayPath()
        name = ps.getFileName()
        this.executionTimeNS = executionTimeNS
        payload = output?.length() ?: 0
    }

    constructor(output: String?, path: String?, name: String?, executionTimeNS: Long) {
        this.output = output
        this.path = path
        this.name = name
        this.executionTimeNS = executionTimeNS
        payload = output?.length() ?: 0
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        val table = DumpTable("#669999", "#ccffff", "#000000")
        table.setTitle("IncludeCacheEntry")
        table.appendRow(1, SimpleDumpData("Output"), DumpUtil.toDumpData(SimpleDumpData(output), pageContext, maxlevel, properties))
        if (path != null) table.appendRow(1, SimpleDumpData("Path"), DumpUtil.toDumpData(SimpleDumpData(path), pageContext, maxlevel, properties))
        return table
    }

    @Override
    override fun toString(): String {
        return output!!
    }

    @Override
    fun getHashFromValue(): String? {
        return Long.toString(HashUtil.create64BitHash(output))
    }

    fun getOutput(): String? {
        return output
    }

    @Override
    fun getName(): String? {
        return name
    }

    @Override
    fun getPayload(): Long {
        return payload.toLong()
    }

    @Override
    fun getMeta(): String? {
        return path
    }

    @Override
    fun getExecutionTime(): Long {
        return executionTimeNS
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return IncludeCacheItem(output, path, name, executionTimeNS)
    }

    companion object {
        private const val serialVersionUID = -3616023500492159529L
    }
}