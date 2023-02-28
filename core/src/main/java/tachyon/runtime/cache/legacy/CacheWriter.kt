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

import java.io.IOException

class CacheWriter(out: Writer?, cacheFile: Resource?) : ForkWriter(out, IOUtil.getWriter(cacheFile, null as Charset?)) {
    private var out: Writer?
    private var cacheFile: Resource?

    /**
     * @return the cacheFile
     */
    fun getCacheFile(): Resource? {
        return cacheFile
    }

    /**
     * @param cacheFile the cacheFile to set
     */
    fun setCacheFile(cacheFile: Resource?) {
        this.cacheFile = cacheFile
    }

    /**
     * @return the out
     */
    fun getOut(): Writer? {
        return out
    }

    /**
     * @param out the out to set
     */
    fun setOut(out: Writer?) {
        this.out = out
    }

    init {
        this.out = out
        this.cacheFile = cacheFile
    }
}