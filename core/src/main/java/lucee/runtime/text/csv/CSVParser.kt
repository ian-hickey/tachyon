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
package lucee.runtime.text.csv

import java.util.List

object CSVParser {
    @Throws(CSVParserException::class, PageException::class)
    fun toQuery(csv: String?, delimiter: Char, textQualifier: Char, headers: Array<String?>?, firstRowIsHeaders: Boolean): Query? {
        var headers = headers
        val allRows: List<List<String?>?> = CSVString(csv, delimiter).parse()
        val numRows: Int = allRows.size()

        // no records
        if (numRows == 0) {
            if (firstRowIsHeaders || headers == null) throw CSVParserException("No data found in CSV string")
            return QueryImpl(headers, 0, "query")
        }
        var row = allRows[0]
        val numCols: Int = row!!.size()
        var curRow = 0

        // set first line to header
        if (firstRowIsHeaders) {
            curRow++
            if (headers == null) headers = makeUnique(row.toArray(arrayOfNulls<String?>(numCols)))
        }

        // create first line for header
        if (headers == null) {
            headers = arrayOfNulls<String?>(numCols)
            for (i in 0 until numCols) headers[i] = "COLUMN_" + (i + 1)
        }
        val arrays: Array<Array?> = arrayOfNulls<Array?>(numCols) // create column Arrays
        for (i in 0 until numCols) arrays[i] = ArrayImpl()
        while (curRow < numRows) {
            row = allRows[curRow++]
            if (row!!.size() !== numCols) throw CSVParserException("Invalid CSV line size, expected " + numCols + " columns but found " + row!!.size() + " instead", row.toString())
            for (i in 0 until numCols) {
                arrays[i].append(row!![i])
            }
        }
        return QueryImpl(CollectionUtil.toKeys(headers, true), arrays, "query")
    }

    private fun makeUnique(headers: Array<String?>?): Array<String?>? {
        var c = 1
        val set: Set = TreeSet(String.CASE_INSENSITIVE_ORDER)
        var header: String?
        var orig: String?
        for (i in headers.indices) {
            header = headers!![i]
            orig = header
            while (set.contains(header)) header = orig.toString() + "_" + ++c
            set.add(header)
            if (header !== orig) // ref comparison for performance
                headers[i] = header
        }
        return headers
    }
}