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

import java.net.InetAddress

class IPRangeCollection {
    private var list: List<IPRangeNode?>? = Collections.EMPTY_LIST
    fun add(child: IPRangeNode?, doCheck: Boolean) {
        if (list === Collections.EMPTY_LIST) list = ArrayList() else if (doCheck) {

            // scan for previous children in parent that should be moved under the newly added child after this
            // addition
            var listSize: Int = list!!.size()
            var i = 0
            while (i < listSize) {
                val sibling: IPRangeNode? = list!![i]
                if (child.containsRange(sibling)) { // move sibling under new child
                    list.remove(i--) // adjust i and numChildren due to removal
                    listSize--
                    child.addChild(sibling)
                }
                i++
            }
        }
        list.add(child)
    }

    fun add(child: IPRangeNode?) {
        this.add(child, true)
    }

    fun findFast(iaddr: InetAddress?, parents: List<IPRangeNode?>?): IPRangeNode? {
        val needle: IPRangeNode?
        val parent: IPRangeNode?
        needle = IPRangeNode(iaddr, iaddr)
        var pos: Int = Collections.binarySearch(list, needle, IPRangeNode.comparerRange)
        if (pos > -1) {
            parent = list!![pos]
            return parent.findFast(iaddr, parents)
        }
        val tests = 2
        pos = Math.abs(pos)
        pos = Math.max(0, pos - tests)
        val max: Int = Math.min(pos + tests, list!!.size())
        while (pos < max) {
            if (list!![pos].isInRange(iaddr)) {
                parent = list!![pos]
                return parent.findFast(iaddr, parents)
            }
            pos++
        }
        return null
    }

    /**
     * performs a binary search over a sorted list
     *
     * @param iaddr
     * @return
     */
    fun findFast(iaddr: InetAddress?): IPRangeNode? {
        val needle: IPRangeNode?
        val parent: IPRangeNode?
        needle = IPRangeNode(iaddr, iaddr)
        var pos: Int = Collections.binarySearch(list, needle, IPRangeNode.comparerRange)
        if (pos > -1) {
            parent = list!![pos]
            return parent.findFast(iaddr)
        }
        val tests = 2
        pos = Math.abs(pos)
        pos = Math.max(0, pos - tests)
        val max: Int = Math.min(pos + tests, list!!.size())
        while (pos < max) {
            if (list!![pos].isInRange(iaddr)) {
                parent = list!![pos]
                return parent.findFast(iaddr)
            }
            pos++
        }
        return null
    }

    /**
     * performs a binary search over sorted list
     *
     * @param addr
     * @return
     */
    fun findFast(addr: String?): IPRangeNode? {
        val iaddr: InetAddress
        iaddr = try {
            InetAddress.getByName(addr)
        } catch (ex: UnknownHostException) {
            return null
        }
        return findFast(iaddr)
    }

    /**
     * performs a linear scan for unsorted lists
     *
     * @param addr
     * @return
     */
    fun findAddr(addr: InetAddress?): IPRangeNode? {
        for (c in list!!) {
            val result: IPRangeNode = c.findAddr(addr)
            if (result != null) return result
        }
        return null
    }

    /**
     * performs a linear scan for unsorted lists
     *
     * @param child
     * @return
     */
    fun findRange(child: IPRangeNode?): IPRangeNode? {
        for (c in list!!) {
            val result: IPRangeNode = c.findRange(child)
            if (result != null) return result
        }
        return null
    }

    fun size(): Int {
        return list!!.size()
    }

    private fun sort() {
        Collections.sort(list)
    }

    fun sortChildren() {
        for (node in list!!) {
            node.getChildren().sort()
        }
    }
}