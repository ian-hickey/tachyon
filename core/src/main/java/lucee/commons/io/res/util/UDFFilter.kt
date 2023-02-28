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
package lucee.commons.io.res.util

import java.io.File

class UDFFilter(udf: UDF) : UDFFilterSupport(udf), ResourceAndResourceNameFilter {
    fun accept(path: String?): Boolean {
        args.get(0) = path
        return try {
            Caster.toBooleanValue(udf.call(ThreadLocalPageContext.get(), args, true))
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    fun accept(file: Resource): Boolean {
        return accept(file.getAbsolutePath())
    }

    @Override
    fun accept(parent: Resource, name: String): Boolean {
        var path: String = parent.getAbsolutePath()
        if (path.endsWith(File.separator)) path += name else path += File.separator + name
        return accept(path)
    }

    @Override
    override fun toString(): String {
        return "UDFFilter:$udf"
    }

    companion object {
        @Throws(PageException::class)
        fun createResourceAndResourceNameFilter(filter: Object?): ResourceAndResourceNameFilter {
            return if (filter is UDF) createResourceAndResourceNameFilter(filter as UDF?) else createResourceAndResourceNameFilter(Caster.toString(filter))
        }

        @Throws(PageException::class)
        fun createResourceAndResourceNameFilter(filter: UDF): ResourceAndResourceNameFilter {
            return UDFFilter(filter)
        }

        fun createResourceAndResourceNameFilter(pattern: String): ResourceAndResourceNameFilter? {
            return if (!StringUtil.isEmpty(pattern, true)) WildcardPatternFilter(pattern, "|") else null
        }
    }
}