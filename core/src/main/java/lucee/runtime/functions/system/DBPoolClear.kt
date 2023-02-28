/**
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
/**
 * Implements the CFML Function gettemplatepath
 */
package lucee.runtime.functions.system

import java.util.Iterator

object DBPoolClear : Function {
    fun call(pc: PageContext?): Boolean {
        return call(pc, null)
    }

    fun call(pc: PageContext?, dataSourceName: String?): Boolean {
        val it: Iterator<DatasourceConnPool?> = (pc.getConfig() as ConfigPro).getDatasourceConnectionPools().iterator()
        while (it.hasNext()) {
            val dcp: DatasourceConnPool? = it.next()
            if (StringUtil.isEmpty(dataSourceName) || dataSourceName.equalsIgnoreCase(dcp.getFactory().getDatasource().getName())) clear(dcp)
        }
        return true
    }

    private fun clear(dcp: DatasourceConnPool?) {
        dcp.clear()
    }
}