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
/**
 * Implements the CFML Function expandpath
 */
package tachyon.runtime.functions.system

import tachyon.commons.io.SystemUtil

object ContractPath : Function {
    fun call(pc: PageContext?, absPath: String?): String? {
        return call(pc, absPath, false)
    }

    fun call(pc: PageContext?, absPath: String?, placeHolder: Boolean): String? {
        val res: Resource = ResourceUtil.toResourceNotExisting(pc, absPath)
        if (!res.exists()) return absPath
        if (placeHolder) {
            val cp: String = SystemUtil.addPlaceHolder(res, null)
            if (!StringUtil.isEmpty(cp)) return cp
        }

        // Config config=pc.getConfig();
        val ps: PageSource = pc.toPageSource(res, null) ?: return absPath
        var realPath: String? = ps.getRealpath()
        realPath = realPath.replace('\\', '/')
        if (StringUtil.endsWith(realPath, '/')) realPath = realPath.substring(0, realPath!!.length() - 1)
        var mapping: String? = ps.getMapping().getVirtual()
        mapping = mapping.replace('\\', '/')
        if (StringUtil.endsWith(mapping, '/')) mapping = mapping.substring(0, mapping!!.length() - 1)
        return mapping + realPath
    }
}