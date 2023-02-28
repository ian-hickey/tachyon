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
package tachyon.intergral.fusiondebug.server.type.coll

import java.util.ArrayList

class FDCollection(frame: IFDStackFrame?, private val name: String?, coll: Collection?, keys: Array<Key?>?) : FDValueSupport() {
    private val children: ArrayList?
    private val coll: Collection?
    private val keys: Array<Key?>?

    /**
     * Constructor of the class
     *
     * @param frame
     * @param name
     * @param name
     * @param coll
     */
    constructor(frame: IFDStackFrame?, name: String?, coll: Collection?) : this(frame, name, coll, keys(coll)) {}

    @Override
    fun getChildren(): List? {
        return children
    }

    fun getStackFrame(): IFDStackFrame? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun isMutable(): Boolean {
        return false
    }

    @Override
    @Throws(FDMutabilityException::class, FDLanguageException::class)
    fun set(arg0: String?) {
        throw FDMutabilityException()
    }

    @Override
    fun hasChildren(): Boolean {
        return true
    }

    @Override
    override fun toString(): String {
        if (coll is Array) return "[" + fromto() + "]"
        if (coll is Component) {
            val c: Component? = coll as Component?
            return "Component " + c.getName().toString() + "(" + c.getPageSource().getDisplayPath().toString() + ")"
        }
        return if (coll is Struct) "{" + fromto() + "}" else FDCaster.serialize(coll)
    }

    private fun fromto(): String? {
        val sb = StringBuffer()
        for (i in keys.indices) {
            if (i != 0) sb.append(",")
            sb.append(keys!![i].toString())
        }
        return keys!![0].toString() + " ... " + keys[keys.size - 1]
    }

    @Override
    fun getName(): String? {
        return name
    }

    companion object {
        private const val INTERVAL = 10
        private fun keys(coll: Collection?): Array<Key?>? {
            val keys: Array<Key?> = CollectionUtil.keys(coll)
            if (coll is Array) return keys
            val comp = TextComparator(true, true)
            Arrays.sort(keys, comp)
            return keys
        }
    }

    init {
        this.coll = coll
        this.keys = keys
        // Key[] keys = coll.keys();
        children = ArrayList()
        var interval = INTERVAL
        while (interval * interval < keys!!.size) interval *= interval
        if (keys.size > interval) {
            var node: FDCollection
            val len = keys.size
            var max: Int
            var i = 0
            while (i < len) {
                max = if (i + interval < len) interval else len - i
                val skeys: Array<Key?> = arrayOfNulls<Key?>(max)
                for (y in 0 until max) {
                    skeys[y] = keys[i + y]
                }
                node = FDCollection(frame, "Rows", coll, skeys)
                children.add(FDVariable(frame, node.getName(), node))
                i += interval
            }
        } else {
            var node: FDCollectionNode?
            for (i in keys.indices) {
                node = FDCollectionNode(frame, coll, keys[i])
                children.add(FDVariable(frame, node.getName(), node))
            }
        }
    }
}