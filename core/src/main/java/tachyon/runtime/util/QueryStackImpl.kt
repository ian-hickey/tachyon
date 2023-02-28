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
package tachyon.runtime.util

import tachyon.runtime.PageContext

/**
 * Stack for Query Objects
 */
class QueryStackImpl : QueryStack {
    var queries: Array<Query?>? = arrayOfNulls<Query?>(20)
    var start = queries!!.size
    @Override
    fun duplicate(deepCopy: Boolean): QueryStack? {
        val qs = QueryStackImpl()
        if (deepCopy) {
            qs.queries = arrayOfNulls<Query?>(queries!!.size)
            for (i in queries.indices) {
                qs.queries!![i] = Duplicator.duplicate(queries!![i], deepCopy) as Query
            }
        } else qs.queries = queries
        qs.start = start
        return qs
    }

    @Override
    fun addQuery(query: Query?) {
        if (start < 1) grow()
        queries!![--start] = query
    }

    @Override
    fun removeQuery() {
        // print.ln("queries["+start+"]=null;");
        queries!![start++] = null
    }

    @Override
    fun isEmpty(): Boolean {
        return start == queries!!.size
    }

    @Override
    fun getDataFromACollection(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        // Object rtn;
        var col: QueryColumn
        // get data from queries
        for (i in start until queries!!.size) {
            col = queries!![i].getColumn(key, null)
            if (col != null) return col.get(queries!![i].getCurrentrow(pc.getId()), NullSupportHelper.empty(pc))
            // rtn=((Objects)queries[i]).get(pc,key,Null.NULL);
            // if(rtn!=Null.NULL) return rtn;
        }
        return defaultValue
    }

    @Override
    fun getColumnFromACollection(key: Key?): QueryColumn? {
        var rtn: QueryColumn? = null

        // get data from queries
        for (i in start until queries!!.size) {
            rtn = queries!![i].getColumn(key, null)
            if (rtn != null) {
                return rtn
            }
        }
        return null
    }

    @Override
    fun clear() {
        for (i in start until queries!!.size) {
            queries!![i] = null
        }
        start = queries!!.size
    }

    private fun grow() {
        val tmp: Array<Query?> = arrayOfNulls<Query?>(queries!!.size + 20)
        for (i in queries.indices) {
            tmp[i + 20] = queries!![i]
        }
        queries = tmp
        start += 20
    }

    @Override
    fun getQueries(): Array<Query?>? {
        val tmp: Array<Query?> = arrayOfNulls<Query?>(queries!!.size - start)
        var count = 0
        for (i in start until queries!!.size) {
            tmp[count++] = queries!![i]
        }
        return tmp
    }
}