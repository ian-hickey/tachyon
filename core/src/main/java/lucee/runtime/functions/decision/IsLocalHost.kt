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

import java.net.InetAddress

/**
 * Implements the CFML Function isleapyear
 */
object IsLocalHost : Function {
    private const val serialVersionUID = 5680807516948697186L
    fun call(pc: PageContext?, ip: String?): Boolean {
        return invoke(ip)
    }

    operator fun invoke(ip: String?): Boolean {
        var ip = ip
        if (StringUtil.isEmpty(ip, true)) return false
        ip = ip.trim().toLowerCase()
        if (ip.equalsIgnoreCase("localhost") || ip.equals("127.0.0.1") || ip.equalsIgnoreCase("0:0:0:0:0:0:0:1") || ip.equalsIgnoreCase("0:0:0:0:0:0:0:1%0")
                || ip.equalsIgnoreCase("::1")) return true
        try {
            val addr: InetAddress = InetAddress.getByName(ip)
            val localHost: InetAddress = InetAddress.getLocalHost()
            if (localHost.equals(addr)) return true
            val localHosts: Array<InetAddress?> = InetAddress.getAllByName(localHost.getHostName())
            for (i in localHosts.indices) {
                if (localHosts[i].equals(addr)) return true
            }
        } catch (e: UnknownHostException) {
        }
        return false
    }
}