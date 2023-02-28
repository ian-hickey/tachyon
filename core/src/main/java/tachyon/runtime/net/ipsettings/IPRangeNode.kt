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
package tachyon.runtime.net.ipsettings

import java.net.Inet4Address

class IPRangeNode<T>(lower: InetAddress?, upper: InetAddress?) : Comparable<IPRangeNode<*>?>, Comparator<IPRangeNode<*>?> {
    private val lower: InetAddress? = null
    private val upper: InetAddress? = null
    var isSingleAddress = false
    var data: T? = null
    private var children: IPRangeCollection? = null

    constructor(lower: String?, upper: String?) : this(InetAddress.getByName(lower), InetAddress.getByName(upper)) {}
    constructor(addr: String?) : this(addr, addr) {}

    fun isInRange(addr: InetAddress?): Boolean {
        return if (isV4 != IPSettings.isV4(addr)) false else comparerIAddr.compare(lower, addr) <= 0 && comparerIAddr.compare(upper, addr) >= 0
    }

    fun containsRange(other: IPRangeNode<*>?): Boolean {
        return if (isV4 != other!!.isV4) false else isInRange(other.lower) && isInRange(other.upper)
    }

    /**
     *
     * @param child
     * @param doCheck - passing false will avoid searching for a "better" parent, for a more efficient
     * insert in large data sets (e.g. Country Codes of all known ranges)
     * @return - true if the child was added
     */
    @Synchronized
    fun addChild(child: IPRangeNode<*>?, doCheck: Boolean): Boolean {
        if (!containsRange(child)) return false
        var parent: IPRangeNode<*>? = this
        if (doCheck) parent = findRange(child)

        // TODO: check for eqaulity of new child and found parent
        parent!!.children!!.add(child, doCheck)
        return true
    }

    /** calls addChild( child, true )  */
    fun addChild(child: IPRangeNode<*>?): Boolean {
        return addChild(child, true)
    }

    fun findRange(child: IPRangeNode<*>?): IPRangeNode<*>? {
        var result: IPRangeNode<*>? = null
        if (containsRange(child)) {
            result = this
            val temp: IPRangeNode<*> = children!!.findRange(child)
            if (temp != null) result = temp
        }
        return result
    }

    fun findAddr(iaddr: InetAddress?): IPRangeNode<*>? {
        var result: IPRangeNode<*>? = null
        if (isInRange(iaddr)) {
            result = this
            if (hasChildren()) {
                val temp: IPRangeNode<*> = children!!.findAddr(iaddr)
                if (temp != null) result = temp
            }
        }
        return result
    }

    fun findAddr(addr: String?): IPRangeNode<*>? {
        try {
            return findAddr(InetAddress.getByName(addr))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return null
    }

    fun findFast(iaddr: InetAddress?, parents: List<IPRangeNode<*>?>?): IPRangeNode<*>? {
        var result: IPRangeNode<*>? = null
        if (isInRange(iaddr)) {
            result = this
            parents?.add(result)
            if (hasChildren()) {
                val temp: IPRangeNode<*> = children!!.findFast(iaddr, parents)
                if (temp != null) result = temp
            }
        }
        return result
    }

    fun findFast(iaddr: InetAddress?): IPRangeNode<*>? {
        return findFast(iaddr, null)
    }

    /*
	 * / works public IPRangeNode findFast(InetAddress iaddr) {
	 * 
	 * IPRangeNode result = null;
	 * 
	 * if ( this.isInRange(iaddr) ) {
	 * 
	 * result = this;
	 * 
	 * if ( this.hasChildren() ) {
	 * 
	 * IPRangeNode temp = children.findFast( iaddr ); if ( temp != null ) result = temp; } }
	 * 
	 * return result; } //
	 */
    fun findFast(addr: String?): IPRangeNode<*>? {
        try {
            return findFast(InetAddress.getByName(addr))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return null
    }

    fun getChildren(): IPRangeCollection? {
        return children
    }

    fun hasChildren(): Boolean {
        return children!!.size() > 0
    }

    @Override
    override fun compareTo(other: IPRangeNode<*>?): Int {
        var c: Int = comparerIAddr.compare(lower, other!!.lower)
        if (c != 0) return c
        c = comparerIAddr.compare(upper, other.upper)
        return c
    }

    @Override
    fun compare(lhs: IPRangeNode<*>?, rhs: IPRangeNode<*>?): Int {
        return lhs!!.compareTo(rhs)
    }

    @Override
    override fun equals(o: Object?): Boolean {
        return if (o is IPRangeNode<*>) {
            compareTo(o as IPRangeNode<*>?) == 0
        } else false
    }

    @Override
    override fun hashCode(): Int {
        return lower.hashCode()
    }

    @Override
    override fun toString(): String {
        return if (isSingleAddress) lower.toString().substring(1) + String.format(" (%d)", children!!.size()) else lower.toString().substring(1).toString() + " - " + upper.toString().substring(1) + String.format(" (%d)", children!!.size())
    }

    val isV4: Boolean
        get() = IPSettings.isV4(lower)
    val isV6: Boolean
        get() = IPSettings.isV6(lower)

    companion object {
        val comparerRange: Comparator<IPRangeNode<*>?>? = object : Comparator<IPRangeNode<*>?>() {
            @Override
            fun compare(lhs: IPRangeNode<*>?, rhs: IPRangeNode<*>?): Int {
                return lhs!!.compareTo(rhs)
            }
        }
        val comparerIAddr: Comparator<InetAddress?>? = object : Comparator<InetAddress?>() {
            @Override
            fun compare(lhs: InetAddress?, rhs: InetAddress?): Int {
                if (lhs is Inet4Address != rhs is Inet4Address) throw IllegalArgumentException("Both arguments must be of the same IP Version")
                val barrLhs: ByteArray = lhs.getAddress()
                val barrRhs: ByteArray = rhs.getAddress()
                for (i in barrLhs.indices) {
                    val l: Int = barrLhs[i] and 0xff // fix signed bit in byte
                    val r: Int = barrRhs[i] and 0xff
                    if (l < r) return -1 else if (l > r) return 1
                }
                return 0 // equal
            }
        }
    }

    init {
        val c: Int = comparerIAddr.compare(lower, upper)
        if (c <= 0) {
            this.lower = lower
            this.upper = upper
        } else {
            this.lower = upper
            this.upper = lower
        }
        isSingleAddress = c == 0
        children = IPRangeCollection()
    }
}