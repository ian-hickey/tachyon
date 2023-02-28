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
package tachyon.runtime.functions.decision

import java.net.Inet6Address

object IsIPv6 {
    @Throws(PageException::class)
    fun call(pc: PageContext?): Boolean {
        return try {
            val ia: InetAddress = InetAddress.getLocalHost()
            val ias: Array<InetAddress?> = InetAddress.getAllByName(ia.getHostName())
            _call(ias)
        } catch (e: UnknownHostException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, hostName: String?): Boolean {
        return if (StringUtil.isEmpty(hostName)) call(pc) else try {
            val ias: Array<InetAddress?> = InetAddress.getAllByName(hostName)
            _call(ias)
        } catch (e: UnknownHostException) {
            if (hostName.equalsIgnoreCase("localhost") || hostName!!.equals("127.0.0.1") || hostName.equalsIgnoreCase("0:0:0:0:0:0:0:1") || hostName.equalsIgnoreCase("::1")) return call(pc)
            throw Caster.toPageException(e)
        }
    }

    private fun _call(ias: Array<InetAddress?>?): Boolean {
        for (i in ias.indices) {
            if (ias!![i] is Inet6Address) return true
        }
        return false
    }
}