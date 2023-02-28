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
package lucee.intergral.fusiondebug.server.type.qry

import java.util.ArrayList

class FDQuery(frame: IFDStackFrame?, qry: Query?) : FDValueNotMutability() {
    private val children: ArrayList? = ArrayList()
    private val qry: Query?
    @Override
    fun getChildren(): List? {
        return children
    }

    @Override
    fun hasChildren(): Boolean {
        return true
    }

    @Override
    override fun toString(): String {
        return "Query(Columns:" + qry.getColumns().length.toString() + ", Rows:" + qry.getRecordcount().toString() + ")"
    }

    companion object {
        private const val INTERVAL = 10
        private fun fill(frame: IFDStackFrame?, qry: Query?, lstRows: List?, start: Int, len: Int, strColumns: Array<String?>?) {
            val to = start + len
            var interval = INTERVAL
            while (interval * interval < len) interval *= interval
            if (len > interval) {
                var max: Int
                var i = start
                while (i < to) {
                    max = if (i + interval < to) interval - 1 else to - i
                    val group = ArrayList()
                    lstRows.add(FDSimpleVariable(frame, "Rows", "[" + i + "-" + (i + max) + "]", group))
                    fill(frame, qry, group, i, max, strColumns)
                    i += interval
                }
            } else {
                var values: ArrayList?
                for (r in start..to) {
                    values = ArrayList()
                    for (c in strColumns.indices) {
                        values.add(FDVariable(frame, strColumns!![c], FDQueryNode(frame, qry, r, strColumns[c])))
                    }
                    lstRows.add(FDSimpleVariable(frame, "Row", "[$r]", values))
                }
            }
        }
    }

    init {
        this.qry = qry

        // columns
        val strColumns: Array<String?> = qry.getColumns()
        val lstColumns: List = ArrayList()
        var type: String
        for (i in strColumns.indices) {
            type = qry.getColumn(strColumns[i], null).getTypeAsString()
            // else type="";
            lstColumns.add(FDSimpleVariable(frame, strColumns[i], type, null))
        }
        children.add(FDSimpleVariable(frame, "Columns", Caster.toString(strColumns.size), lstColumns))

        // rows
        val rowcount: Int = qry.getRowCount()
        val lstRows: List = ArrayList() // ,values;
        fill(frame, qry, lstRows, 1, rowcount - 1, strColumns)
        children.add(FDSimpleVariable(frame, "Rows", Caster.toString(rowcount), lstRows))
    }
}