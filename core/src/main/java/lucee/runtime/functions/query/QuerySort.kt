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
/**
 * Implements the CFML Function querysetcell
 */
package lucee.runtime.functions.query

import java.util.Arrays

class QuerySort : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toQuery(args[0]), args[1]) else call(pc, Caster.toQuery(args[0]), args[1], Caster.toString(args[2]))
    }

    class QueryRow(query: Query?, rowNbr: Int, row: Struct?) {
        val query: Query?
        val rowNbr: Int
        val row: Struct?

        init {
            this.query = query
            this.rowNbr = rowNbr
            this.row = row
        }
    }

    class QueryRowComparator(pc: PageContext?, udf: UDF?) : Comparator<QueryRow?> {
        private val pc: PageContext?
        private val udf: UDF?
        @Override
        fun compare(left: QueryRow?, right: QueryRow?): Int {
            return try {
                Caster.toIntValue(udf.call(pc, arrayOf<Object?>(left!!.row, right!!.row), true))
            } catch (pe: PageException) {
                throw PageRuntimeException(pe)
            }
        }

        init {
            this.pc = pc
            this.udf = udf
        }
    }

    companion object {
        private const val serialVersionUID = -6566120440638749819L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnNameOrSortFunc: Object?): Boolean {
            return if (Decision.isSimpleValue(columnNameOrSortFunc)) _call(pc, query, Caster.toString(columnNameOrSortFunc), null) else _call(pc, query, Caster.toFunction(columnNameOrSortFunc))
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, columnNameOrSortFunc: Object?, directions: String?): Boolean {
            return if (Decision.isSimpleValue(columnNameOrSortFunc)) _call(pc, query, Caster.toString(columnNameOrSortFunc), directions) else _call(pc, query, Caster.toFunction(columnNameOrSortFunc))
        }

        @Throws(PageException::class)
        fun _call(pc: PageContext?, query: Query?, udf: UDF?): Boolean {
            val recordcount: Int = query.getRecordcount()
            val columns: Array<Key?> = query.getColumnNames()
            val rows = arrayOfNulls<QueryRow?>(recordcount)
            var sct: Struct?
            val empty: Object? = if (NullSupportHelper.full(pc)) null else ""
            for (row in 1..recordcount) {
                sct = StructImpl()
                for (col in columns.indices) {
                    sct.setEL(columns[col], query.getAt(columns[col], row, empty))
                }
                rows[row - 1] = QueryRow(query, row, sct)
            }
            Arrays.sort(rows, QueryRowComparator(pc, udf))
            (query as QueryImpl?).sort(toInt(rows))
            return true
        }

        private fun toInt(rows: Array<QueryRow?>?): IntArray? {
            val ints = IntArray(rows!!.size)
            for (i in rows.indices) {
                ints[i] = rows[i]!!.rowNbr
            }
            return ints
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, query: Query?, columnNames: String?, directions: String?): Boolean {
            // column names
            val arrColumnNames: Array<String?> = ListUtil.trimItems(ListUtil.listToStringArray(columnNames, ','))
            val dirs = IntArray(arrColumnNames.size)

            // directions
            if (!StringUtil.isEmpty(directions)) {
                val arrDirections: Array<String?> = ListUtil.trimItems(ListUtil.listToStringArray(directions, ','))
                if (arrColumnNames.size != arrDirections.size) throw DatabaseException("column names and directions has not the same count", null, null, null)
                var direction: String?
                for (i in dirs.indices) {
                    direction = arrDirections[i].toLowerCase()
                    dirs[i] = 0
                    if (direction!!.equals("asc")) dirs[i] = Query.ORDER_ASC else if (direction.equals("desc")) dirs[i] = Query.ORDER_DESC else {
                        throw DatabaseException("argument direction of function querySort must be \"asc\" or \"desc\", now \"$direction\"", null, null, null)
                    }
                }
            } else {
                for (i in dirs.indices) {
                    dirs[i] = Query.ORDER_ASC
                }
            }
            for (i in arrColumnNames.indices.reversed()) query.sort(KeyImpl.init(arrColumnNames[i]), dirs[i])
            return true
        }
    }
}