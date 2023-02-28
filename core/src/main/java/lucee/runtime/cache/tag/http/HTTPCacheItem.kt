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
package lucee.runtime.cache.tag.http

import java.io.Serializable

class HTTPCacheItem(data: Struct?, url: String?, executionTimeNS: Long) : CacheItem, Serializable, Dumpable, Duplicable {
    private val data: Struct?
    private val url: String?
    private val executionTimeNS: Long
    private val filecontent: Object?
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        val table = DumpTable("#669999", "#ccffff", "#000000")
        table.setTitle("HTTPCacheEntry")
        table.appendRow(1, SimpleDumpData("Output"), data.toDumpData(pageContext, maxlevel, properties))
        if (url != null) table.appendRow(1, SimpleDumpData("URL"), DumpUtil.toDumpData(SimpleDumpData(url), pageContext, maxlevel, properties))
        return table
    }

    @Override
    override fun toString(): String {
        // if(filecontent instanceof CharSequence)
        // return filecontent.toString();
        return if (filecontent is ByteArray) Caster.toB64(filecontent as ByteArray?) else filecontent.toString()
    }

    @Override
    fun getHashFromValue(): String? {
        return Long.toString(HashUtil.create64BitHash(toString()))
    }

    fun getData(): Struct? {
        return data
    }

    @Override
    fun getName(): String? {
        return url
    }

    @Override
    fun getPayload(): Long {
        if (filecontent is CharSequence) return (filecontent as CharSequence?)!!.length()
        if (filecontent is ByteArray) return (filecontent as ByteArray?)!!.size.toLong()
        return if (filecontent is Collection) (filecontent as Collection?)!!.size() else 0
    }

    @Override
    fun getMeta(): String? {
        return url
    }

    @Override
    fun getExecutionTime(): Long {
        return executionTimeNS
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return HTTPCacheItem(Duplicator.duplicate(data, deepCopy) as Struct, url, executionTimeNS)
    }

    companion object {
        private const val serialVersionUID = -8462614105941179140L
    }

    init {
        this.data = data
        filecontent = data.get(KeyConstants._filecontent, "")
        this.url = url
        this.executionTimeNS = executionTimeNS
    }
}