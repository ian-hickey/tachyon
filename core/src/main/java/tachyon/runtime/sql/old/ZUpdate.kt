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

import java.util.Enumeration

class ZUpdate(s: String?) : ZStatement {
    fun addSet(hashtable: Hashtable?) {
        set_ = hashtable
    }

    val set: Hashtable?
        get() = set_

    fun addColumnUpdate(s: String?, zexp: ZExp?) {
        if (set_ == null) set_ = Hashtable()
        set_.put(s, zexp)
        if (columns_ == null) columns_ = Vector()
        columns_.addElement(s)
    }

    fun getColumnUpdate(s: String?): ZExp? {
        return set_.get(s)
    }

    fun getColumnUpdate(i: Int): ZExp? {
        var i = i
        if (--i < 0) return null
        if (columns_ == null || i >= columns_.size()) {
            return null
        }
        val s = columns_.elementAt(i) as String
        return set_.get(s)
    }

    fun getColumnUpdateName(i: Int): String? {
        var i = i
        if (--i < 0) return null
        return if (columns_ == null || i >= columns_.size()) null else columns_.elementAt(i)
    }

    val columnUpdateCount: Int
        get() = if (set_ == null) 0 else set_.size()

    fun addWhere(zexp: ZExp?) {
        where_ = zexp
    }

    val where: tachyon.runtime.sql.old.ZExp?
        get() = where_

    @Override
    override fun toString(): String {
        val stringbuffer = StringBuffer("update " + table)
        stringbuffer.append(" set ")
        val enumeration: Enumeration
        enumeration = if (columns_ != null) columns_.elements() else set_.keys()
        var flag = true
        while (enumeration.hasMoreElements()) {
            val s: String = enumeration.nextElement().toString()
            if (!flag) stringbuffer.append(", ")
            stringbuffer.append(s + "=" + set_.get(s).toString())
            flag = false
        }
        if (where_ != null) stringbuffer.append(" where " + where_.toString())
        return stringbuffer.toString()
    }

    var table: String?
    var set_: Hashtable? = null
    var where_: ZExp? = null
    var columns_: Vector? = null

    init {
        table = String(s)
    }
}