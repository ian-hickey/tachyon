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
package tachyon.runtime.cache.legacy

import java.io.ByteArrayInputStream

class FileCacheEntry : CacheEntry {
    private val res: Resource? = null

    // private Resource directory;
    // private String name,raw;
    private fun isOK(timeSpan: TimeSpan?): Boolean {
        return res.exists() && res.lastModified() + timeSpan.getMillis() >= System.currentTimeMillis()
    }

    @Override
    @Throws(IOException::class)
    override fun readEntry(timeSpan: TimeSpan?, defaultValue: String?): String? {
        return if (isOK(timeSpan)) IOUtil.toString(res, ENC) else defaultValue
    }

    @Override
    @Throws(IOException::class)
    override fun writeEntry(entry: String?, append: Boolean) {
        IOUtil.copy(ByteArrayInputStream(entry.getBytes(ENC)), res.getOutputStream(append), true, true)
    }

    companion object {
        private val ENC: String? = "utf-8"
    }
}