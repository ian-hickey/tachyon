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
package lucee.runtime.sql.old

import java.util.Vector

class ZQuery : ZStatement, ZExp {
    fun addSelect(vector: Vector?) {
        select_ = vector
    }

    fun addFrom(vector: Vector?) {
        from_ = vector
    }

    fun addWhere(zexp: ZExp?) {
        where_ = zexp
    }

    fun addGroupBy(zgroupby: ZGroupBy?) {
        groupby_ = zgroupby
    }

    fun addSet(zexpression: ZExpression?) {
        setclause_ = zexpression
    }

    fun addOrderBy(vector: Vector?) {
        orderby_ = vector
    }

    val select: Vector?
        get() = select_
    val from: Vector?
        get() = from_
    val where: lucee.runtime.sql.old.ZExp?
        get() = where_
    val groupBy: lucee.runtime.sql.old.ZGroupBy?
        get() = groupby_
    val set: lucee.runtime.sql.old.ZExpression?
        get() = setclause_
    val orderBy: Vector?
        get() = orderby_

    @Override
    override fun toString(): String {
        val stringbuffer = StringBuffer("select ")
        if (isDistinct) stringbuffer.append("distinct ")
        stringbuffer.append(select_.elementAt(0).toString())
        for (i in 1 until select_.size()) stringbuffer.append(", " + select_.elementAt(i).toString())
        stringbuffer.append(" from ")
        stringbuffer.append(from_.elementAt(0).toString())
        for (j in 1 until from_.size()) stringbuffer.append(", " + from_.elementAt(j).toString())
        if (where_ != null) stringbuffer.append(" where " + where_.toString())
        if (groupby_ != null) stringbuffer.append(" " + groupby_.toString())
        if (setclause_ != null) stringbuffer.append(" " + setclause_.toString())
        if (orderby_ != null) {
            stringbuffer.append(" order by ")
            stringbuffer.append(orderby_.elementAt(0).toString())
            for (k in 1 until orderby_.size()) stringbuffer.append(", " + orderby_.elementAt(k).toString())
        }
        if (isForUpdate) stringbuffer.append(" for update")
        return stringbuffer.toString()
    }

    var select_: Vector? = null
    var isDistinct = false
    var from_: Vector? = null
    var where_: ZExp? = null
    var groupby_: ZGroupBy? = null
    var setclause_: ZExpression? = null
    var orderby_: Vector? = null
    var isForUpdate = false

}