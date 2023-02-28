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
package tachyon.runtime.rest

import java.io.IOException

object RestUtil {
    fun splitPath(path: String?): Array<String?>? {
        return ListUtil.listToStringArray(path, '/')
    }

    /**
     * check if caller path match the cfc path
     *
     * @param variables
     * @param restPath
     * @param callerPath
     * @return match until which index of the given cfc path, returns -1 if there is no match
     */
    fun matchPath(variables: Struct?, restPath: Array<Path?>?, callerPath: Array<String?>?): Int {
        if (restPath!!.size > callerPath!!.size) return -1
        var index = 0
        while (index < restPath.size) {
            if (!restPath[index].match(variables, callerPath[index])) return -1
            index++
        }
        return index - 1
    }

    /**
     * clears the PageContext output buffer andsets the REST response's status code and message
     *
     * @param pc
     * @param status
     * @param msg
     */
    fun setStatus(pc: PageContext?, status: Int, msg: String?) {
        pc.clear()
        if (msg != null) {
            try {
                pc.forceWrite(msg)
            } catch (e: IOException) {
            }
        }
        val rsp: HttpServletResponse = pc.getHttpServletResponse()
        rsp.setHeader("Connection", "close") // IE unter IIS6, Win2K3 und Resin
        rsp.setStatus(status)
    }

    fun release(mappings: Array<Mapping?>?) {
        for (i in mappings.indices) {
            mappings!![i]!!.release()
        }
    }

    fun isMatch(pc: PageContext?, mapping: Mapping?, res: Resource?): Boolean {
        val p: Resource = mapping!!.getPhysical()
        return if (p != null) {
            p.equals(res)
        } else ResourceUtil.toResourceNotExisting(pc, mapping.getStrPhysical()).equals(res)
    }
}