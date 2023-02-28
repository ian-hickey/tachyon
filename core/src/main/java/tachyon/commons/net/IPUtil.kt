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
package tachyon.commons.net

import java.net.InetAddress

class IPUtil {
    companion object {
        private var isCacheEnabled = false
        private var isCacheValid = false
        private var cachedLocalIPs: List<String>? = null
        fun isIPv4(ip: String?): Boolean {
            val arr: Array<String> = ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(ip, '.')))
            if (arr.size != 4) return false
            var tmp: Int
            for (i in arr.indices) {
                tmp = Caster.toIntValue(arr[i], -1)
                if (tmp < 0 || tmp > 255) return false
            }
            return true
        }

        fun isIPv62(ip: String): Boolean {
            if (ip.indexOf(':') === -1) return false
            val arr: Array<String> = ListUtil.trimItems(ListUtil.trim(ListUtil.listToStringArray(ip, ':')))
            if (arr.size != 8) return false
            var str: String
            var _int: Int
            for (i in arr.indices) {
                str = arr[i]
                if (!StringUtil.isEmpty(str)) {
                    _int = try {
                        Integer.parseInt(str, 16)
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        -1
                    }
                    if (_int < 0 || _int > 65535) return false
                }
            }
            return true
        }

        fun isIPv4(addr: InetAddress): Boolean {
            return addr.getAddress().length === 4
        }

        fun isIPv6(addr: InetAddress?): Boolean {
            return !isIPv4(addr)
        }

        fun getLocalIPs(refresh: Boolean): List<String> {
            if (isCacheEnabled && isCacheValid && !refresh) {
                return ArrayList<String>(cachedLocalIPs)
            }
            val result: List<String> = ArrayList()
            try {
                val eNics: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
                while (eNics.hasMoreElements()) {
                    val nic: NetworkInterface = eNics.nextElement()
                    if (nic.isUp()) {
                        val eAddr: Enumeration<InetAddress> = nic.getInetAddresses()
                        while (eAddr.hasMoreElements()) {
                            val inaddr: InetAddress = eAddr.nextElement()
                            var addr: String = inaddr.toString()
                            if (addr.startsWith("/")) addr = addr.substring(1)
                            if (addr.indexOf('%') > -1) addr = addr.substring(0, addr.indexOf('%')) // internal zone in some IPv6;
                            // http://en.wikipedia.org/wiki/IPv6_Addresses#Link-local%5Faddresses%5Fand%5Fzone%5Findices
                            result.add(addr)
                        }
                    }
                }
            } catch (e: SocketException) {
                result.add("127.0.0.1")
                result.add("0:0:0:0:0:0:0:1")
            }
            if (isCacheEnabled) {
                cachedLocalIPs = result
                isCacheValid = true
            }
            return result
        }

        init {
            val tc: Long = System.currentTimeMillis()
            val localIPs = getLocalIPs(true)
            isCacheEnabled = System.currentTimeMillis() > tachyon.commons.net.tc
            if (isCacheEnabled) {
                cachedLocalIPs = tachyon.commons.net.localIPs
                isCacheValid = true
            }
        }
    }

    /**
     * this method can be called from Controller periodically, or from Admin if user clicks to
     * invalidate the cache
     */
    fun invalidateCache() {
        isCacheValid = false
    }

    /** returns true if cache is used  */
    val isCacheEnabled: Boolean
        get() = Companion.isCacheEnabled
}