/**
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package lucee.runtime.functions.file

import lucee.commons.io.res.Resource

object DirectoryList {
    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?): Object? {
        return _call(pc, path, false, Directory.LIST_INFO_ARRAY_PATH, null, null, Directory.TYPE_ALL)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, recurse: Boolean): Object? {
        return _call(pc, path, recurse, Directory.LIST_INFO_ARRAY_PATH, null, null, Directory.TYPE_ALL)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, recurse: Boolean, strListInfo: String?): Object? {
        return _call(pc, path, recurse, toListInfo(strListInfo), null, null, Directory.TYPE_ALL)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, recurse: Boolean, strListInfo: String?, oFilter: Object?): Object? {
        return _call(pc, path, recurse, toListInfo(strListInfo), oFilter, null, Directory.TYPE_ALL)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, recurse: Boolean, strListInfo: String?, oFilter: Object?, sort: String?): Object? {
        return _call(pc, path, recurse, toListInfo(strListInfo), oFilter, sort, Directory.TYPE_ALL)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, recurse: Boolean, strListInfo: String?, oFilter: Object?, sort: String?, type: String?): Object? {
        return _call(pc, path, recurse, toListInfo(strListInfo), oFilter, sort, if (StringUtil.isEmpty(type)) Directory.TYPE_ALL else Directory.toType(type))
    }

    @Throws(PageException::class)
    fun _call(pc: PageContext?, path: String?, recurse: Boolean, listInfo: Int, oFilter: Object?, sort: String?, type: Int): Object? {
        val dir: Resource = ResourceUtil.toResourceNotExisting(pc, path)
        val filter: ResourceFilter = UDFFilter.createResourceAndResourceNameFilter(oFilter)
        return Directory.actionList(pc, dir, null, type, filter, listInfo, recurse, sort)
    }

    private fun toListInfo(strListInfo: String?): Int {
        var strListInfo = strListInfo
        var listInfo: Int = Directory.LIST_INFO_ARRAY_PATH
        if (!StringUtil.isEmpty(strListInfo, true)) {
            strListInfo = strListInfo.trim().toLowerCase()
            if ("name".equalsIgnoreCase(strListInfo)) {
                listInfo = Directory.LIST_INFO_ARRAY_NAME
            } else if ("query".equalsIgnoreCase(strListInfo)) {
                listInfo = Directory.LIST_INFO_QUERY_ALL
            }
        }
        return listInfo
    }
}