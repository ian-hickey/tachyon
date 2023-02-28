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
package lucee.runtime.functions.other

import java.lang.ref.SoftReference

object DatasourceFlushMetaCache {
    fun call(pc: PageContext?): Boolean {
        return call(pc, null)
    }

    @Synchronized
    fun call(pc: PageContext?, datasource: String?): Boolean {
        val sources: Array<DataSource?> = pc.getConfig().getDataSources()
        var ds: DataSourceSupport?
        var has = false
        for (i in sources.indices) {
            ds = sources[i] as DataSourceSupport?
            if (StringUtil.isEmpty(datasource) || ds.getName().equalsIgnoreCase(datasource.trim())) {
                val cache: Map<String?, SoftReference<ProcMetaCollection?>?> = ds.getProcedureColumnCache()
                if (cache != null) cache.clear()
                if (!StringUtil.isEmpty(datasource)) return true
                has = true
            }
        }
        return has
    }
}