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
package tachyon.runtime.functions.conversion

import java.util.Iterator

/**
 * Decodes Binary Data that are encoded as String
 */
object DeserializeJSON : Function {
    private val ROWCOUNT: Key? = KeyImpl.getInstance("ROWCOUNT")
    @Throws(PageException::class)
    fun call(pc: PageContext?, JSONVar: String?): Object? {
        return call(pc, JSONVar, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, JSONVar: String?, strictMapping: Boolean): Object? {
        if (StringUtil.isEmpty(JSONVar, true)) throw FunctionException(pc, "DeserializeJSON", 1, "JSONVar", "input value cannot be empty string.", "Must be the valid JSON string")
        val result: Object = JSONExpressionInterpreter().interpret(pc, JSONVar)
        return if (!strictMapping) toQuery(result) else result
    }

    // {"COLUMNS":["AAA","BBB"],"DATA":[["a","b"],["c","d"]]}
    // {"ROWCOUNT":2,"COLUMNS":["AAA","BBB"],"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}
    @Throws(PageException::class)
    private fun toQuery(obj: Object?): Object? {
        if (obj is Struct) {
            val sct: Struct? = obj as Struct?
            val keys: Array<Key?> = CollectionUtil.keys(sct)

            // Columns
            var columns: Array<Key?>? = null
            if (contains(keys, KeyConstants._COLUMNS)) columns = toColumns(sct.get(KeyConstants._COLUMNS, null)) else if (contains(keys, KeyConstants._COLUMNLIST)) columns = toColumnlist(sct.get(KeyConstants._COLUMNLIST, null))

            // rowcount
            var rowcount = -1
            if (contains(keys, ROWCOUNT)) rowcount = toRowCount(sct.get(ROWCOUNT, null)) else if (contains(keys, KeyConstants._RECORDCOUNT)) rowcount = toRowCount(sct.get(KeyConstants._RECORDCOUNT, null))
            if (columns != null) {
                if (keys.size == 2 && contains(keys, KeyConstants._DATA)) {
                    val data: Array<Array?>? = toData(sct.get(KeyConstants._DATA, null), columns)
                    if (data != null) {
                        return QueryImpl(columns, data, "query")
                    }
                } else if (keys.size == 3 && rowcount != -1 && contains(keys, KeyConstants._DATA)) {
                    val data: Array<Array?>? = toData(sct.get(KeyConstants._DATA, null), columns, rowcount)
                    if (data != null) {
                        return QueryImpl(columns, data, "query")
                    }
                }
            }
            return toQuery(sct, keys)
        } else if (obj is Collection) {
            val coll: Collection? = obj
            return toQuery(coll, CollectionUtil.keys(coll))
        }
        return obj
    }

    /*
	 * private static Object toQuery(Query qry) throws DatabaseException { int
	 * rows=qry.getRecordcount(); String[] columns = qry.getColumns(); Object src,trg; for(int
	 * row=1;row<=rows;row++) { for(int col=0;col<columns.length;col++) {
	 * trg=toQuery(src=qry.getAt(columns[col], row, null)); if(src!=trg) qry.setAtEL(columns[col], row,
	 * trg); } } return qry; }
	 */
    @Throws(PageException::class)
    private fun toQuery(coll: Collection?, keys: Array<Key?>?): Collection? {
        var src: Object?
        var trg: Object?
        for (i in keys.indices) {
            trg = toQuery(coll.get(keys!![i], null).also { src = it })
            if (src !== trg) coll.setEL(keys[i], trg)
        }
        return coll
    }

    private fun toRowCount(obj: Object?): Int {
        return Caster.toIntValue(obj, -1)
    }

    @Throws(PageException::class)
    private fun toData(obj: Object?, columns: Array<Key?>?, rowcount: Int): Array<Array?>? {
        if (columns == null || rowcount == -1) return null
        val sct: Struct = Caster.toStruct(obj, null, false)
        if (sct != null && sct.size() === columns.size) {
            val datas: Array<Array?> = arrayOfNulls<Array?>(columns.size)
            var col: Array
            var colLen = -1
            for (i in columns.indices) {
                col = Caster.toArray(sct.get(columns[i], null), null)
                if (col == null || colLen != -1 && colLen != col.size()) return null
                datas[i] = toQuery(col, CollectionUtil.keys(col)) as Array?
                colLen = col.size()
            }
            return datas
        }
        return null
    }

    @Throws(PageException::class)
    private fun toData(obj: Object?, columns: Array<Key?>?): Array<Array?>? {
        if (columns == null) return null
        val arr: Array = Caster.toArray(obj, null)
        if (arr != null) {
            val datas: Array<Array?> = arrayOfNulls<Array?>(columns.size)
            for (i in datas.indices) {
                datas[i] = ArrayImpl()
            }
            var data: Array
            val it: Iterator<Object?> = arr.valueIterator()
            while (it.hasNext()) {
                data = Caster.toArray(it.next(), null)
                if (data == null || data.size() !== datas.size) return null
                for (i in datas.indices) {
                    datas[i].appendEL(toQuery(data.get(i + 1, null)))
                }
            }
            return datas
        }
        return null
    }

    private fun toColumns(obj: Object?): Array<Key?>? {
        val arr: Array = Caster.toArray(obj, null)
        if (arr != null) {
            val columns: Array<Key?> = arrayOfNulls<Key?>(arr.size())
            var column: String
            var index = 0
            val it: Iterator<Object?> = arr.valueIterator()
            while (it.hasNext()) {
                column = Caster.toString(it.next(), null)
                if (StringUtil.isEmpty(column)) return null
                columns[index++] = KeyImpl.getInstance(column)
            }
            return columns
        }
        return null
    }

    @Throws(PageException::class)
    private fun toColumnlist(obj: Object?): Array<Key?>? {
        val list: String = Caster.toString(obj, null)
        return if (StringUtil.isEmpty(list)) null else toColumns(ListUtil.trimItems(ListUtil.listToArrayRemoveEmpty(list, ',')))
    }

    /*
	 * private static boolean contains(Key[] haystack, Key[] needle) { Key h; outer:for(int
	 * i=0;i<haystack.length;i++) { h=haystack[i]; for(int y=0;y<needle.length;y++) {
	 * if(h.equalsIgnoreCase(needle[y])) continue outer; } return false; } return true; }
	 */
    private fun contains(haystack: Array<Key?>?, needle: Key?): Boolean {
        for (i in haystack.indices) {
            if (haystack!![i].equalsIgnoreCase(needle)) return true
        }
        return false
    }
}