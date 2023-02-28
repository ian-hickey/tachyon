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
package lucee.runtime.net.ftp

import java.io.IOException

/**
 * represent a ftp path
 */
class FTPPath(client: AFTPClient?, relpath: String?) : Dumpable {
    /**
     * @return Returns the path.
     */
    var path: String? = null
        private set

    /**
     * @return Returns the name.
     */
    var name: String? = null
        private set

    @Throws(PageException::class)
    private fun init(arr: Array?) {
        if (arr.size() > 0) {
            name = arr.get(arr.size(), "")
            arr.removeEL(arr.size())
            path = '/' + ListUtil.arrayToList(arr, "/") + '/'
        } else {
            path = "/"
            name = ""
        }
        // this.arrPath=arr;
    }

    @Override
    override fun toString(): String {
        return path + name // +" - "+"path("+getPath()+");"+"name("+getName()+");"+"parent("+getParentPath()+");";
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val table = DumpTable("string", "#ff6600", "#ffcc99", "#000000")
        table.appendRow(1, SimpleDumpData("FTPPath"), SimpleDumpData(toString()))
        return table
    }
    // private Array arrPath;
    /**
     * @param current
     * @param relpath
     * @throws PageException
     * @throws IOException
     */
    init {
        var relpath = relpath
        relpath = relpath.replace('\\', '/')
        val relpathArr: Array = ListUtil.listToArrayTrim(relpath, '/')

        // relpath is absolute
        if (relpath.startsWith("/")) {
            init(relpathArr)
            return
        }
        val current: String
        current = if (client == null) "" else client.printWorkingDirectory().replace('\\', '/')
        val parentArr: Array = ListUtil.listToArrayTrim(current, '/')

        // Single Dot .
        if (relpathArr.size() > 0 && relpathArr.get(1, "").equals(".")) {
            relpathArr.removeEL(1)
        }

        // Double Dot ..
        while (relpathArr.size() > 0 && relpathArr.get(1, "").equals("..")) {
            relpathArr.removeEL(1)
            if (parentArr.size() > 0) {
                parentArr.removeEL(parentArr.size())
            } else {
                parentArr.prepend("..")
            }
        }
        ArrayMerge.append(parentArr, relpathArr)
        init(parentArr)
    }
}