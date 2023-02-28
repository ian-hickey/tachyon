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
package lucee.commons.net

import java.io.IOException

class IPRange : Serializable {
    private var ranges = arrayOfNulls<Range>(SIZE)
    var max = 0

    private class Range(from: InetAddress, to: InetAddress) {
        private val from: InetAddress
        private val to: InetAddress
        private val bgFrom: BigInteger
        private val bgTo: BigInteger

        constructor(ip: InetAddress) : this(ip, ip) {}

        fun inRange(ia: InetAddress): Boolean {
            val bgIA = BigInteger(1, ia.getAddress())
            return bgIA.compareTo(bgFrom) >= 0 && bgIA.compareTo(bgTo) <= 0
        }

        @Override
        override fun toString(): String {
            return if (bgTo.compareTo(bgFrom) === 0) from.getHostAddress() else from.getHostAddress().toString() + "-" + to.getHostAddress() // toString(from);
        }

        private fun toString(sarr: ShortArray): String {
            return if (sarr.size == 4) StringBuilder().append(sarr[0]).append(".").append(sarr[1]).append(".").append(sarr[2]).append(".").append(sarr[3]).toString() else StringBuilder().append(toHex(sarr[0].toInt(), sarr[1].toInt(), false)).append(":").append(toHex(sarr[2].toInt(), sarr[3].toInt(), true)).append(":").append(toHex(sarr[4].toInt(), sarr[5].toInt(), true))
                    .append(":").append(toHex(sarr[6].toInt(), sarr[7].toInt(), true)).append(":").append(toHex(sarr[8].toInt(), sarr[9].toInt(), true)).append(":").append(toHex(sarr[10].toInt(), sarr[11].toInt(), true))
                    .append(":").append(toHex(sarr[12].toInt(), sarr[13].toInt(), true)).append(":").append(toHex(sarr[14].toInt(), sarr[15].toInt(), false)).toString()
        }

        private fun toHex(first: Int, second: Int, allowEmpty: Boolean): String {
            var str1: String = Integer.toString(first, 16)
            while (str1.length() < 2) str1 = "0$str1"
            var str2: String = Integer.toString(second, 16)
            while (str2.length() < 2) str2 = "0$str2"
            str1 += str2
            if (allowEmpty && str1.equals("0000")) return ""
            while (str1.length() > 1 && str1.charAt(0) === '0') str1 = str1.substring(1)
            return str1
        }

        private fun equal(left: ShortArray, right: ShortArray): Boolean {
            for (i in left.indices) {
                if (left[i] != right[i]) return false
            }
            return true
        }

        init {
            this.from = from
            this.to = to
            bgFrom = BigInteger(1, from.getAddress())
            bgTo = if (from.equals(to)) bgFrom else BigInteger(1, to.getAddress())
        }
    }

    @Throws(IOException::class)
    private fun add(ip: String) {
        var ip = ip
        ip = ip.trim()
        // no wildcard defined
        if (ip.indexOf('*') === -1) {
            add(Range(toInetAddress(ip)))
            return
        }
        if ("*".equals(ip)) {
            add("*.*.*.*")
            add("*:*:*:*:*:*:*:*")
            return
        }
        val from: String = ip.replace('*', '0')
        val to: String
        val addr1: InetAddress = toInetAddress(from)
        to = if (addr1 is Inet6Address) StringUtil.replace(ip, "*", "ffff", false) else StringUtil.replace(ip, "*", "255", false)
        add(Range(addr1, toInetAddress(to)))
    }

    @Throws(IOException::class)
    private fun add(ip1: String, ip2: String) {
        add(Range(toInetAddress(ip1), toInetAddress(ip2)))
    }

    @Synchronized
    private fun add(range: Range) {
        if (max >= ranges.size) {
            val tmp = arrayOfNulls<Range>(ranges.size + SIZE)
            for (i in ranges.indices) {
                tmp[i] = ranges[i]
            }
            ranges = tmp
        }
        ranges[max++] = range
    }

    @Throws(IOException::class)
    fun inRange(ip: String): Boolean {
        return inRange(toInetAddress(ip))
    }

    fun inRange(ip: InetAddress): Boolean {
        for (i in 0 until max) {
            if (ranges[i]!!.inRange(ip)) return true
        }
        return false
    }

    @Override
    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 0 until max) {
            if (i > 0) sb.append(",")
            sb.append(ranges[i].toString())
        }
        return sb.toString()
    }

    companion object {
        /**
         *
         */
        private const val serialVersionUID = 4427999443422764L
        private const val N256: Short = 256
        private const val SIZE = 4
        @Throws(IOException::class)
        fun getInstance(raw: String?): IPRange {
            return getInstance(ListUtil.listToStringArray(raw, ','))
        }

        @Throws(IOException::class)
        fun getInstance(raw: Array<String?>?): IPRange {
            val range = IPRange()
            val arr: Array<String> = ListUtil.trimItems(ListUtil.trim(raw))
            var str: String
            var index: Int
            for (i in arr.indices) {
                str = arr[i]
                if (str.length() > 0) {
                    index = str.indexOf('-')
                    if (index != -1) {
                        range.add(str.substring(0, index), str.substring(index + 1))
                    } else {
                        range.add(str)
                    }
                }
            }
            return range
        }

        @Throws(IOException::class)
        fun toShortArray(ip: String): ShortArray {
            return toShortArray(toInetAddress(ip))
        }

        @Throws(IOException::class)
        fun toInetAddress(ip: String): InetAddress {
            // TODO Auto-generated method stub
            return try {
                InetAddress.getByName(ip)
            } catch (e: UnknownHostException) {
                throw IOException("cannot parse the ip [$ip]")
            }
        }

        private fun toShortArray(ia: InetAddress): ShortArray {
            val addr: ByteArray = ia.getAddress()
            val sarr = ShortArray(addr.size)
            for (i in addr.indices) {
                sarr[i] = byte2short(addr[i])
            }
            return sarr
        }

        private fun byte2short(b: Byte): Short {
            return if (b < 0) (b + N256).toShort() else b.toShort()
        }
    }
}