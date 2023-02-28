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
package lucee.runtime.db

import java.util.HashSet

class HSQLUtil2 {
    private var selects: Selects

    constructor(sql: SQL) {
        selects = SelectParser().parse(sql.getSQLString())
    }

    constructor(selects: Selects) {
        this.selects = selects
    }

    val isUnion: Boolean
        get() = selects.getSelects().length > 1
    val invokedTables: Set<String>
        get() {
            val set: HashSet<String> = HashSet<String>()
            val tables: Array<Column> = selects.getTables()
            for (i in tables.indices) {
                set.add(tables[i].getFullName())
            }
            return set
        }
}