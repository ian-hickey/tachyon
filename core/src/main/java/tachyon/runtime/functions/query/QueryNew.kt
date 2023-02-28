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
package tachyon.runtime.functions.query

import java.util.HashSet

/**
 * Implements the CFML Function querynew
 */
class QueryNew : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        return if (args.size == 2) call(pc, Caster.toString(args[0]), Caster.toString(args[1])) else call(pc, Caster.toString(args[0]), Caster.toString(args[1]), args[2])
    }

    companion object {
        private const val serialVersionUID = -4313766961671090938L
        @Throws(PageException::class)
        fun call(pc: PageContext?, columnNames: Object?): tachyon.runtime.type.Query? {
            val arr: Array? = toArray(pc, columnNames, 1)
            if (arr.size() > 0 && Decision.isStruct(arr.getE(1))) {
                val qry = QueryImpl(arrayOfNulls<Key?>(0), arr.size(), "")
                val it: Iterator<Object?> = arr.valueIterator()
                var rit: Iterator<Entry<Key?, Object?>?>
                var row = 0
                var e: Entry<Key?, Object?>?
                val containsCache: Set<Key?> = HashSet()
                while (it.hasNext()) {
                    rit = Caster.toStruct(it.next()).entryIterator()
                    row++
                    while (rit.hasNext()) {
                        e = rit.next()
                        // add column
                        if (!containsCache.contains(e.getKey())) {
                            qry.addColumn(e.getKey(), ArrayImpl())
                            containsCache.add(e.getKey())
                        }
                        qry.setAt(e.getKey(), row, e.getValue())
                    }
                }
                return qry
            }
            return QueryImpl(toArray(pc, columnNames, 1), 0, "query")
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, columnNames: Object?, columnTypes: Object?): tachyon.runtime.type.Query? {
            return if (StringUtil.isEmpty(columnTypes)) call(pc, columnNames) else QueryImpl(toArray(pc, columnNames, 1), toArray(pc, columnTypes, 2), 0, "query")
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, columnNames: Object?, columnTypes: Object?, data: Object?): tachyon.runtime.type.Query? {
            val cn: Array? = toArray(pc, columnNames, 1)
            val qry: tachyon.runtime.type.Query?
            if (StringUtil.isEmpty(columnTypes)) qry = QueryImpl(cn, 0, "query") else qry = QueryImpl(cn, toArray(pc, columnTypes, 2), 0, "query")
            return if (data == null) qry else populate(pc, qry, data, true)
        }

        @Throws(PageException::class)
        fun populate(pc: PageContext?, qry: Query?, data: Object?, arrayAsMultiRow: Boolean): Query? {
            return if (Decision.isArray(data)) _populate(pc, qry, Caster.toArray(data)) else if (Decision.isStruct(data)) _populate(pc, qry, Caster.toStruct(data), arrayAsMultiRow) else throw FunctionException(pc, "QueryNew", 3, "data", "the date must be defined as array of structs , array of arrays or struct of arrays")
        }

        @Throws(PageException::class)
        private fun _populate(pc: PageContext?, qry: Query?, data: Struct?, arrayAsMultiRow: Boolean): Query? {
            val it: Iterator<Entry<Key?, Object?>?> = data.entryIterator()
            var e: Entry<Key?, Object?>?
            var v: Object
            var arr: Array?
            val rows: Int = qry.getRecordcount()
            while (it.hasNext()) {
                e = it.next()
                if (qry.getColumn(e.getKey(), null) != null) {
                    v = e.getValue()
                    arr = if (arrayAsMultiRow) Caster.toArray(v, null) else null
                    if (arr == null) arr = ArrayImpl(arrayOf<Object?>(v))
                    populateColumn(qry, e.getKey(), arr, rows)
                }
            }
            return qry
        }

        @Throws(PageException::class)
        private fun populateColumn(qry: Query?, column: Key?, data: Array?, rows: Int) {
            val it: Iterator<*> = data.valueIterator()
            var row = rows
            while (it.hasNext()) {
                row++
                if (row > qry.getRecordcount()) qry.addRow()
                qry.setAt(column, row, it.next())
            }
        }

        @Throws(PageException::class)
        private fun _populate(pc: PageContext?, qry: Query?, data: Array?): Query? {
            // check if the array only contains simple values or mixed
            var it: Iterator<*> = data.valueIterator()
            var o: Object?
            var hasSimpleValues = false
            while (it.hasNext()) {
                o = it.next()
                if (!Decision.isStruct(o) && !Decision.isArray(o)) hasSimpleValues = true
            }
            if (hasSimpleValues) {
                qry.addRow()
                populateRow(qry, data)
            } else {
                it = data.valueIterator()
                while (it.hasNext()) {
                    o = it.next()
                    qry.addRow()
                    if (Decision.isStruct(o)) populateRow(qry, Caster.toStruct(o)) else if (Decision.isArray(o)) populateRow(qry, Caster.toArray(o)) else {
                        populateRow(qry, ArrayImpl(arrayOf<Object?>(o)))
                    }
                }
            }
            return qry
        }

        @Throws(PageException::class)
        private fun populateRow(qry: Query?, data: Struct?) {
            val columns: Array<Key?> = QueryUtil.getColumnNames(qry)
            val row: Int = qry.getRecordcount()
            var value: Object
            for (i in columns.indices) {
                value = data.get(columns[i], null)
                if (value != null) qry.setAt(columns[i], row, value)
            }
        }

        @Throws(PageException::class)
        private fun populateRow(qry: Query?, data: Array?) {
            val it: Iterator<*> = data.valueIterator()
            val columns: Array<Key?> = QueryUtil.getColumnNames(qry)
            val row: Int = qry.getRecordcount()
            var index = -1
            while (it.hasNext()) {
                index++
                if (index >= columns.size) break
                qry.setAt(columns[index], row, it.next())
            }
        }

        @Throws(PageException::class)
        private fun toArray(pc: PageContext?, columnNames: Object?, index: Int): Array? {
            if (Decision.isArray(columnNames)) return Caster.toArray(columnNames)
            val str: String = Caster.toString(columnNames, null)
                    ?: throw FunctionException(pc, "QueryNew", index, if (index == 1) "columnNames" else "columnTypes", "cannot cast to an array or a string list")
            return ListUtil.listToArrayTrim(str, ",")
        }
    }
}