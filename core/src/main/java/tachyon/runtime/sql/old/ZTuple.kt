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
package tachyon.runtime.sql.old

import java.util.Hashtable

class ZTuple() {
    constructor(s: String?) : this() {
        val stringtokenizer = StringTokenizer(s, ",")
        while (stringtokenizer.hasMoreTokens()) {
            setAtt(stringtokenizer.nextToken().trim(), null)
        }
    }

    fun setRow(s: String?) {
        val stringtokenizer = StringTokenizer(s, ",")
        var i = 0
        while (stringtokenizer.hasMoreTokens()) {
            val s1: String = stringtokenizer.nextToken().trim()
            try {
                val double1: Double = Double.valueOf(s1)
                setAtt(getAttName(i), double1)
            } catch (exception: Exception) {
                setAtt(getAttName(i), s1)
            }
            i++
        }
    }

    fun setRow(vector: Vector?) {
        for (i in 0 until vector.size()) setAtt(getAttName(i), vector.elementAt(i))
    }

    fun setAtt(s: String?, obj: Object?) {
        if (s != null) {
            val flag: Boolean = searchTable_.containsKey(s)
            if (flag) {
                val i: Int = (searchTable_.get(s) as Integer).intValue()
                values_.setElementAt(obj, i)
            } else {
                val j: Int = attributes_.size()
                attributes_.addElement(s)
                values_.addElement(obj)
                searchTable_.put(s, Integer.valueOf(j))
            }
        }
    }

    fun getAttName(i: Int): String? {
        return try {
            attributes_.elementAt(i)
        } catch (arrayindexoutofboundsexception: ArrayIndexOutOfBoundsException) {
            null
        }
    }

    fun getAttIndex(s: String?): Int {
        if (s == null) return -1
        val integer: Integer = searchTable_.get(s) as Integer
        return if (integer != null) integer.intValue() else -1
    }

    fun getAttValue(i: Int): Object? {
        return try {
            values_.elementAt(i)
        } catch (arrayindexoutofboundsexception: ArrayIndexOutOfBoundsException) {
            null
        }
    }

    fun getAttValue(s: String?): Object? {
        var flag = false
        if (s != null) flag = searchTable_.containsKey(s)
        if (flag) {
            val i: Int = (searchTable_.get(s) as Integer).intValue()
            return values_.elementAt(i)
        }
        return null
    }

    fun isAttribute(s: String?): Boolean {
        return if (s != null) searchTable_.containsKey(s) else false
    }

    val numAtt: Int
        get() = values_.size()

    @Override
    override fun toString(): String {
        val stringbuffer = StringBuffer()
        stringbuffer.append("[")
        if (attributes_.size() > 0) {
            val obj: Object = attributes_.elementAt(0)
            val s: String
            s = if (obj == null) "(null)" else obj.toString()
            val obj2: Object = values_.elementAt(0)
            val s2: String
            s2 = if (obj2 == null) "(null)" else obj2.toString()
            stringbuffer.append("$s = $s2")
        }
        for (i in 1 until attributes_.size()) {
            val obj1: Object = attributes_.elementAt(i)
            var s1: String
            s1 = if (obj1 == null) "(null)" else obj1.toString()
            val obj3: Object = values_.elementAt(i)
            var s3: String
            s3 = if (obj3 == null) "(null)" else obj3.toString()
            stringbuffer.append(", $s1 = $s3")
        }
        stringbuffer.append("]")
        return stringbuffer.toString()
    }

    private val attributes_: Vector?
    private val values_: Vector?
    private val searchTable_: Hashtable?

    init {
        attributes_ = Vector()
        values_ = Vector()
        searchTable_ = Hashtable()
    }
}