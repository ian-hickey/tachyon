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
package lucee.runtime.net.ipsettings

import java.net.Inet4Address

/**
 * an efficient data structure for IP-range based settings
 */
class IPSettings {
    private var root: IPRangeNode<Map?>? = null
    private var ipv4: IPRangeNode<Map?>? = null
    private var ipv6: IPRangeNode<Map?>? = null
    private var isSorted = false
    var version = 0
        private set

    /**
     * all added data should go through this method
     *
     * @param ipr
     * @param doCheck
     */
    @Synchronized
    fun put(ipr: IPRangeNode<Map?>?, doCheck: Boolean) {
        val parent: IPRangeNode = if (ipr!!.isV4()) ipv4 else ipv6
        parent.addChild(ipr, doCheck)
        version++
        isSorted = false
    }

    /** calls put( IPRangeNode ipr )  */
    fun put(ipr: IPRangeNode<Map?>?) {
        this.put(ipr, true)
    }

    /**
     * puts all the children at the IPv4 or IPv6 nodes for fast insertion. this method does not look for
     * a more accurate insertion point and is useful when adding many items at once, e.g. for Country
     * Codes of all known IP ranges
     *
     * @param children
     */
    fun putAll(children: List<IPRangeNode<Map?>?>?) {
        for (child in children!!) {
            this.put(child, false) // pass false for optimized insertion performance
        }
    }

    @Throws(UnknownHostException::class)
    fun putSettings(lower: String?, upper: String?, settings: Map?) {
        val ipr: IPRangeNode<Map?> = IPRangeNode(lower, upper)
        ipr.setData(settings)
        this.put(ipr)
    }

    @Throws(UnknownHostException::class)
    fun putSettings(addr: String?, settings: Map?) {
        if (addr!!.equals("*")) {
            root.setData(settings)
            return
        }
        val ipr: IPRangeNode<Map?> = IPRangeNode(addr)
        ipr.setData(settings)
        this.put(ipr)
    }

    /**
     * returns a single, best matching node for the given address
     *
     * @param addr
     * @return
     */
    operator fun get(addr: InetAddress?): IPRangeNode? {
        if (version == 0) // no data was added
            return null
        val node: IPRangeNode = if (isV4(addr)) ipv4 else ipv6
        if (!isSorted) optimize()
        return node.findFast(addr)
    }

    /**
     * returns a List of all the nodes (from root to best matching) for the given address
     *
     * @param iaddr
     * @return
     */
    fun getChain(iaddr: InetAddress?): List<IPRangeNode?>? {
        val result: List<IPRangeNode?> = ArrayList()
        result.add(root)
        val node: IPRangeNode = if (isV4(iaddr)) ipv4 else ipv6
        node.findFast(iaddr, result)
        return result
    }

    /**
     * returns the cumulative settings for a given address
     *
     * @param iaddr
     * @return
     */
    fun getSettings(iaddr: InetAddress?): Map? {
        val result: Map = TreeMap(String.CASE_INSENSITIVE_ORDER)
        val chain: List<IPRangeNode?>? = getChain(iaddr)
        for (ipr in chain!!) {
            val m: Map = ipr.getData()
            if (m != null) result.putAll(m)
        }
        return result
    }

    /**
     * returns the cumulative settings for a given address
     *
     * @param addr
     * @return
     */
    fun getSettings(addr: String?): Map? {
        try {
            return this.getSettings(InetAddress.getByName(addr))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return EMPTY
    }

    /**
     * returns the settings for a single (non-cumulative) node that best matches the given address
     *
     * @param addr
     * @return
     */
    fun getNodeSettings(addr: InetAddress?): Map? {
        val ipr: IPRangeNode<Map?>? = this[addr]
        if (ipr != null) {
            val result: Map = ipr.getData()
            if (result != null) return result
        }
        return EMPTY
    }

    /**
     * returns the settings for a single (non-cumulative) node that best matches the given address
     *
     * @param addr
     * @return
     */
    fun getNodeSettings(addr: String?): Map? {
        try {
            return this.getNodeSettings(InetAddress.getByName(addr))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return EMPTY
    }

    /** sorts the data for fast binary search  */
    private fun optimize() {
        root!!.getChildren()!!.sortChildren()
        isSorted = true
    }

    companion object {
        val EMPTY: Map? = Collections.EMPTY_MAP

        /** returns true if the value is an IPv4 address  */
        fun isV4(addr: InetAddress?): Boolean {
            return addr is Inet4Address
        }

        /** returns true if the value is an IPv6 address  */
        fun isV6(addr: InetAddress?): Boolean {
            return addr is Inet6Address
        }
    }

    init {
        try {
            root = IPRangeNodeRoot()
            root!!.addChild(IPRangeNode("0.0.0.0", "255.255.255.255").also { ipv4 = it })
            root!!.addChild(IPRangeNode("0:0:0:0:0:0:0:0", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").also { ipv6 = it })
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        } // all valid addresses, should never happen
    }
}