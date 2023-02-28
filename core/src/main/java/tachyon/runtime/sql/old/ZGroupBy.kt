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

import java.io.Serializable

// Referenced classes of package Zql:
//            ZExp
class ZGroupBy(vector: Vector?) : Serializable {
    val groupBy: Vector?
        get() = groupby_
    var having: tachyon.runtime.sql.old.ZExp?
        get() = having_
        set(zexp) {
            having_ = zexp
        }

    @Override
    override fun toString(): String {
        val stringbuffer = StringBuffer("group by ")
        stringbuffer.append(groupby_.elementAt(0).toString())
        for (i in 1 until groupby_.size()) stringbuffer.append(", " + groupby_.elementAt(i).toString())
        if (having_ != null) stringbuffer.append(" having " + having_.toString())
        return stringbuffer.toString()
    }

    var groupby_: Vector?
    var having_: ZExp? = null

    init {
        groupby_ = vector
    }
}