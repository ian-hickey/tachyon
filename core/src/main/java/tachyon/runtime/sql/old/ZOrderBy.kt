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
class ZOrderBy(zexp: ZExp?) : Serializable {
    val expression: tachyon.runtime.sql.old.ZExp?
        get() = exp_

    @Override
    override fun toString(): String {
        return exp_.toString().toString() + " " + if (ascOrder) "ASC" else "DESC"
    }

    var exp_: ZExp?
    var ascOrder = true

    init {
        exp_ = zexp
    }
}