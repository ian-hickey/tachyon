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
//Source File Name:   ZInsert.java
package tachyon.runtime.sql.old

import java.util.Vector

//         ZExpression, ZQuery, ZStatement, ZExp
class ZInsert(s: String?) : ZStatement {
    val columns: Vector?
        get() = columns_

    fun addColumns(vector: Vector?) {
        columns_ = vector
    }

    fun addValueSpec(zexp: ZExp?) {
        valueSpec_ = zexp
    }

    val values: Vector?
        get() = if (valueSpec_ !is ZExpression) null else (valueSpec_ as ZExpression?).getOperands()
    val query: tachyon.runtime.sql.old.ZQuery?
        get() = if (valueSpec_ !is ZQuery) null else valueSpec_ as ZQuery?

    @Override
    override fun toString(): String {
        val stringbuffer = StringBuffer("insert into " + table)
        if (columns_ != null && columns_.size() > 0) {
            stringbuffer.append("(" + columns_.elementAt(0))
            for (i in 1 until columns_.size()) stringbuffer.append("," + columns_.elementAt(i))
            stringbuffer.append(")")
        }
        val s: String = valueSpec_.toString()
        stringbuffer.append(" ")
        if (values != null) stringbuffer.append("values ")
        if (s.startsWith("(")) stringbuffer.append(s) else stringbuffer.append(" ($s)")
        return stringbuffer.toString()
    }

    var table: String?
    var columns_: Vector? = null
    var valueSpec_: ZExp? = null

    init {
        table = String(s)
    }
}