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
package tachyon.commons.io.res.util

import java.util.Map

class ResourceSnippetsMap(maxSnippets: Int, maxSources: Int) {
    /* methods that access these Map objects should take care of synchronization */
    private val sources: Map<String, String>
    private val snippets: Map<String, ResourceSnippet>
    private val sync: Object = SerializableObject()

    /**
     * this method accesses the underlying Map(s) and is therefore synchronized
     *
     * @param ps
     * @param startPos
     * @param endPos
     * @param charset
     * @return
     */
    fun getSnippet(ps: PageSource, startPos: Int, endPos: Int, charset: String?): ResourceSnippet? {
        val keySnp = calcKey(ps, startPos, endPos)
        synchronized(sync) {
            var snippet: ResourceSnippet? = snippets[keySnp]
            if (snippet == null) {
                val res: Resource = ps.getResource()
                val keyRes = calcKey(res)
                var src = sources[keyRes]
                if (src == null) {
                    src = ResourceSnippet.getContents(res, charset)
                    sources.put(keyRes, src)
                }
                snippet = ResourceSnippet.createResourceSnippet(src, startPos, endPos)
                snippets.put(keySnp, snippet)
            }
            return snippet
        }
    }

    companion object {
        fun calcKey(res: Resource): String {
            return res.getAbsolutePath().toString() + "@" + res.lastModified()
        }

        fun calcKey(ps: PageSource, startPos: Int, endPos: Int): String {
            return ps.getDisplayPath().toString() + "@" + ps.getLastAccessTime() + ":" + startPos + "-" + endPos
        }
    }

    init {
        sources = LinkedHashMapMaxSize<String, String>(maxSources)
        snippets = LinkedHashMapMaxSize<String, ResourceSnippet>(maxSnippets)
    }
}