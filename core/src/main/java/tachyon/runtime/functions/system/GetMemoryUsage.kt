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
package tachyon.runtime.functions.system

import tachyon.commons.io.SystemUtil

object GetMemoryUsage : Function {
    private const val serialVersionUID = -7937791531186794443L
    @Throws(PageException::class)
    fun call(pc: PageContext?): Query? {
        return call(pc, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?): Query? {
        var type = type
        if (StringUtil.isEmpty(type)) return SystemUtil.getMemoryUsageAsQuery(SystemUtil.MEMORY_TYPE_ALL)
        type = type.trim().toLowerCase()
        if ("heap".equalsIgnoreCase(type)) return SystemUtil.getMemoryUsageAsQuery(SystemUtil.MEMORY_TYPE_HEAP)
        if ("non_heap".equalsIgnoreCase(type) || "nonheap".equalsIgnoreCase(type) || "non-heap".equalsIgnoreCase(type) || "none_heap".equalsIgnoreCase(type)
                || "noneheap".equalsIgnoreCase(type) || "none-heap".equalsIgnoreCase(type)) return SystemUtil.getMemoryUsageAsQuery(SystemUtil.MEMORY_TYPE_NON_HEAP)
        throw FunctionException(pc, "GetMemoryUsage", 1, "type", "invalid value [$type], valid values are [heap,non_heap]")
    }
}