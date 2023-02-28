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
package tachyon.runtime.type.it

import java.sql.SQLException

class ForEachQueryIterator(pc: PageContext?, qry: Query?, pid: Int) : Iterator, Resetable {
    private val qry: Query?
    private val pid: Int
    private val start: Int
    private var current = 0
    private val names: Array<Key?>?
    private val pcMayNull: PageContext?

    @Override
    operator fun hasNext(): Boolean {
        return current < qry.getRecordcount()
    }

    @Override
    operator fun next(): Object? {
        try {
            if (qry.go(++current, pid)) {
                val sct: Struct = StructImpl(Struct.TYPE_LINKED)
                val empty: Object? = if (NullSupportHelper.full(pcMayNull)) null else ""
                for (i in names.indices) {
                    sct.setEL(names!![i], qry.get(names[i], empty))
                }
                return sct
            }
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
        return null
    }

    @Override
    fun remove() {
        try {
            qry.removeRow(current)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    @Throws(PageException::class)
    fun reset() {
        qry.go(start, pid)
        if (qry is SimpleQuery) {
            val sq: SimpleQuery? = qry as SimpleQuery?
            try {
                if (!sq.isClosed()) {
                    sq.close()
                }
            } catch (e: SQLException) {
                throw DatabaseException(e, sq.getDc())
            }
        }
    }

    init {
        pcMayNull = pc
        this.qry = qry
        this.pid = pid
        start = qry.getCurrentrow(pid)
        names = qry.getColumnNames()
    }
}