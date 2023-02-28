/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.type

import java.util.ArrayList

/**
 * CFML array object implements Array,List,Objects
 */
class ArrayImpl @JvmOverloads constructor(initalCap: Int = DEFAULT_CAP, sync: Boolean = true) : ListAsArray(if (sync) Collections.synchronizedList(ArrayList(initalCap)) else ArrayList(initalCap)) {
    constructor(objects: Array<Object?>?) : this(if (ArrayUtil.isEmpty(objects)) 32 else objects!!.size) {
        for (i in objects.indices) {
            setEL(i + 1, objects!![i])
        }
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return duplicate(ArrayImpl(), deepCopy)
    }

    protected fun duplicate(arr: ArrayImpl?, deepCopy: Boolean): Collection? {
        val it: Iterator<Entry<Key?, Object?>?> = entryIterator()
        val inside = if (deepCopy) ThreadLocalDuplication.set(this, arr) else true
        var e: Entry<Key?, Object?>?
        try {
            while (it.hasNext()) {
                e = it.next()
                if (deepCopy) arr.set(e.getKey(), Duplicator.duplicate(e.getValue(), deepCopy)) else arr.set(e.getKey(), e.getValue())
            }
        } catch (ee: PageException) {
        } // MUST habdle this
        finally {
            if (!inside) ThreadLocalDuplication.reset()
        }
        return arr
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val table = DumpTable("array", "#99cc33", "#ccff33", "#000000")
        table.setTitle("Array")
        val top: Int = dp.getMaxlevel()
        if (size() > top) table.setComment("Rows: " + size().toString() + " (showing top " + top.toString() + ")") else if (size() > 10 && dp.getMetainfo()) table.setComment("Rows: " + size())
        val length: Int = size()
        for (i in 1..length) {
            var o: Object? = null
            try {
                o = getE(i)
            } catch (e: Exception) {
            }
            table.appendRow(1, SimpleDumpData(i), DumpUtil.toDumpData(o, pageContext, maxlevel, dp))
            if (i == top) break
        }
        return table
    }

    fun sync(): Boolean {
        return list.getClass().getName().indexOf("SynchronizedList") !== -1
    }

    companion object {
        private const val serialVersionUID = -6187994169003839005L
        const val DEFAULT_CAP = 32
    }
}