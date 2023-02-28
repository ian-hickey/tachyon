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
package lucee.runtime.cache.tag.query

import lucee.commons.io.res.util.UDFFilterSupport

class QueryCacheHandlerFilterUDF(udf: UDF?) : UDFFilterSupport(udf), CacheHandlerFilter {
    private override val udf: UDF?
    @Override
    fun accept(obj: Object?): Boolean {
        if (obj !is Query) return false
        args.get(0) = (obj as Query?).getSql()
        return try {
            Caster.toBooleanValue(udf.call(ThreadLocalPageContext.get(), args, true))
        } catch (e: PageException) {
            throw PageRuntimeException(e)
        }
    }

    init {
        this.udf = udf
    }
}