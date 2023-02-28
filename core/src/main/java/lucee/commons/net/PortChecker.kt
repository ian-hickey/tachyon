/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.commons.net

import java.io.IOException

object PortChecker {
    fun isActive(host: String?, port: Int): Boolean {
        var s: Socket? = null
        try {
            s = Socket()
            s.setReuseAddress(true)
            val sa: SocketAddress = InetSocketAddress(host, port)
            s.connect(sa, 3000)
            return true
        } catch (e: IOException) {
        } finally {
            IOUtil.closeEL(s)
        }
        return false
    }

    fun portsTaken(portFrom: Int, portTo: Int): Map<Integer, Boolean> {
        val result: Map<Integer, Boolean> = HashMap<Integer, Boolean>()
        for (i in portFrom..portTo) {
            result.put(i, portTaken(i))
        }
        return result
    }

    fun portTaken(port: Int): Boolean {
        var socket: ServerSocket? = null
        try {
            socket = ServerSocket(port)
        } catch (e: IOException) {
            return true
        } finally {
            IOUtil.closeEL(socket)
        }
        return false
    }
}