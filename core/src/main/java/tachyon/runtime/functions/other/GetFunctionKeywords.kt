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
package tachyon.runtime.functions.other

import java.util.HashSet

object GetFunctionKeywords {
    private val token: Object? = Object()
    private var keywords: Array? = null
    @Throws(PageException::class)
    fun call(pc: PageContext?): Array? {
        synchronized(token) {
            if (keywords == null) {
                val set: Set<String?> = HashSet<String?>()
                val flds: Array<FunctionLib?>
                flds = (pc.getConfig() as ConfigPro).getFLDs(pc.getCurrentTemplateDialect())
                var functions: Map<String?, FunctionLibFunction?>
                var it: Iterator<FunctionLibFunction?>
                var flf: FunctionLibFunction?
                var arr: Array<String?>
                for (i in flds.indices) {
                    functions = flds[i].getFunctions()
                    it = functions.values().iterator()
                    while (it.hasNext()) {
                        flf = it.next()
                        if (flf.getStatus() !== TagLib.STATUS_HIDDEN && flf.getStatus() !== TagLib.STATUS_UNIMPLEMENTED && !ArrayUtil.isEmpty(flf.getKeywords())) {
                            arr = flf.getKeywords()
                            if (arr != null) for (y in arr.indices) {
                                set.add(arr[y].toLowerCase())
                            }
                        }
                    }
                }
                keywords = Caster.toArray(set)
                ArraySort.call(pc, keywords, "textnocase")
                // }
            }
        }
        return keywords
    }
}