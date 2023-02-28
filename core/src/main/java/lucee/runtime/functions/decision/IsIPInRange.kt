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
package lucee.runtime.functions.decision

import java.io.IOException

object IsIPInRange {
    @Throws(PageException::class)
    fun call(pc: PageContext?, ips: Object?, ip: String?): Boolean {
        return try {
            if (ips is String) return IPRange.getInstance(ips as String?).inRange(ip)
            val arr: Array = Caster.toArray(ips, null)
                    ?: throw FunctionException(pc, "IsIpRange", 1, "ips", "ips must be a string list or a string array")
            val _ips = arrayOfNulls<String?>(arr.size())
            for (i in _ips.indices) {
                _ips[i] = Caster.toString(arr.getE(i + 1), null)
                if (_ips[i] == null) throw FunctionException(pc, "IsIpRange", 1, "ips", "element number " + (i + 1) + " in ips array is not a string")
            }
            IPRange.getInstance(_ips).inRange(ip)
        } catch (e: IOException) {
            e.printStackTrace()
            throw Caster.toPageException(e)
        }
    }
}