/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.tag.util

import java.nio.charset.Charset

object QueryParamConverter {
    @Throws(PageException::class)
    fun convert(sql: String?, params: Argument?): SQL? {
        // All items of arguments will be key-based or position-based so proxy appropriate arrays
        val it: Iterator<Entry<Key?, Object?>?> = params.entryIterator()
        if (it.hasNext()) {
            val e: Entry<Key?, Object?>? = it.next()
            if (e.getKey().getString() === String("1")) {
                // This indicates the first item has key == 1 therefore treat as array
                return convert(sql, Caster.toArray(params))
            }
        }
        return convert(sql, Caster.toStruct(params))
    }

    @Throws(PageException::class)
    fun convert(sql: String?, params: Struct?): SQL? {
        val it: Iterator<Entry<Key?, Object?>?> = params.entryIterator()
        val namedItems: List<SQLItems<NamedSQLItem?>?> = ArrayList<SQLItems<NamedSQLItem?>?>()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            val namedSqlItem = toNamedSQLItem(e.getKey().getString(), e.getValue())
            namedItems.add(namedSqlItem)
        }
        return convert(sql, ArrayList<SQLItems<SQLItem?>?>(), namedItems)
    }

    @Throws(PageException::class)
    fun convert(sql: String?, params: Array?): SQL? {
        val it: Iterator<Object?> = params.valueIterator()
        val namedItems: List<SQLItems<NamedSQLItem?>?> = ArrayList<SQLItems<NamedSQLItem?>?>()
        val items: List<SQLItems<SQLItem?>?> = ArrayList<SQLItems<SQLItem?>?>()
        var value: Object?
        var paramValue: Object
        while (it.hasNext()) {
            value = it.next()
            if (Decision.isStruct(value)) {
                val sct: Struct? = value as Struct?
                // name (optional)
                var name: String? = null
                val oName: Object = sct.get(KeyConstants._name, null)
                if (oName != null) name = Caster.toString(oName)

                // value (required)
                paramValue = sct.get(KeyConstants._value)
                val charset: Charset = CharsetUtil.toCharset(Caster.toString(sct.get(KeyConstants._charset, null), null), null)
                val maxlength: Int = Caster.toIntValue(sct.get("maxlength", null), -1)
                if (StringUtil.isEmpty(name)) {
                    items.add(SQLItems<SQLItem?>(SQLItemImpl(paramValue, Types.VARCHAR, maxlength, charset), sct))
                } else {
                    namedItems.add(SQLItems<NamedSQLItem?>(NamedSQLItem(name, paramValue, Types.VARCHAR, maxlength, charset), sct))
                }
            } else {
                items.add(SQLItems<SQLItem?>(SQLItemImpl(value)))
            }
        }
        return convert(sql, items, namedItems)
    }

    fun toStruct(item: SQLItem?): Struct? {
        val sct: Struct = StructImpl()
        if (item is NamedSQLItem) {
            val nsi = item as NamedSQLItem?
            sct.setEL(KeyConstants._name, nsi!!.name)
        }
        sct.setEL(KeyConstants._value, item.getValue())
        sct.setEL(KeyConstants._type, SQLCaster.toStringType(item.getType(), null))
        sct.setEL(KeyConstants._scale, item.getScale())
        return sct
    }

    @Throws(PageException::class)
    private fun toNamedSQLItem(name: String?, value: Object?): SQLItems<NamedSQLItem?>? {
        var value: Object? = value
        if (Decision.isStruct(value)) {
            val sct: Struct? = value as Struct?
            // value (required if not null)
            value = if (isParamNull(sct)) "" else sct.get(KeyConstants._value)
            val charset: Charset? = if (isParamNull(sct)) null else CharsetUtil.toCharset(Caster.toString(sct.get(KeyConstants._charset, null), null), null)
            val maxlength = if (isParamNull(sct)) -1 else Caster.toIntValue(sct.get("maxlength", null), -1)
            return SQLItems(NamedSQLItem(name, value, Types.VARCHAR, maxlength, charset), sct) // extracting the type is not necessary, that will happen
            // inside SQLItems
        }
        return SQLItems(NamedSQLItem(name, value, Types.VARCHAR, -1, null))
    }

    @Throws(ApplicationException::class, PageException::class)
    private fun convert(sql: String?, items: List<SQLItems<SQLItem?>?>?, namedItems: List<SQLItems<NamedSQLItem?>?>?): SQL? {
        // if(namedParams.size()==0) return new Pair<String, List<Param>>(sql,params);
        val sb = StringBuilder()
        val sqlLen: Int = sql!!.length()
        val initialParamSize: Int = items!!.size()
        var c: Char
        var quoteType = 0.toChar()
        var p = 0.toChar()
        var pp = 0.toChar()
        var inQuotes = false
        var qm = 0
        var _qm = 0
        var i = 0
        while (i < sqlLen) {
            c = sql.charAt(i)
            if (!inQuotes && sqlLen + 1 > i) {
                // read multi line
                if (c == '/' && sql.charAt(i + 1) === '*') {
                    val end: Int = sql.indexOf("*/", i + 2)
                    if (end != -1) {
                        i = end + 2
                        if (i == sqlLen) break
                        c = sql.charAt(i)
                    }
                }

                // read single line
                if (c == '-' && sql.charAt(i + 1) === '-') {
                    val end: Int = sql.indexOf('\n', i + 1)
                    if (end != -1) {
                        i = end + 1
                        if (i == sqlLen) break
                        c = sql.charAt(i)
                    } else break
                }
            }
            if (c == '"' || c == '\'') {
                if (inQuotes) {
                    if (c == quoteType) {
                        inQuotes = false
                    }
                } else {
                    quoteType = c
                    inQuotes = true
                }
            } else if (!inQuotes) {
                if (c == '?') {
                    if (i < sqlLen - 1 && sql.charAt(i + 1) === '?') {
                        sb.append(c).append(c) // '?' is escaped, add both characters so that it's handled later
                        i++
                        i++
                        continue
                    }
                    if (++_qm > initialParamSize) throw ApplicationException("there are more question marks in the SQL than params defined")
                } else if (c == ':') {
                    if (i < sqlLen - 1 && sql.charAt(i + 1) === ':') {
                        sb.append(c) // ':' is escaped, append it and skip parameter resolution
                        i++
                        i++
                        continue
                    }
                    val name = StringBuilder()
                    var cc: Char
                    var y = i + 1
                    while (y < sqlLen) {
                        cc = sql.charAt(y)
                        if (!isVariableName(cc, true)) break
                        name.append(cc)
                        y++
                    }
                    if (name.length() > 0) {
                        i = y - 1
                        c = '?'
                        items.add(qm, QueryParamConverter[name.toString(), namedItems])
                    }
                }
            }
            if (c == '?' && !inQuotes) {
                val len: Int = items[qm].size()
                for (j in 1..len) {
                    if (j > 1) sb.append(',')
                    sb.append('?')
                }
                qm++
            } else {
                sb.append(c)
            }
            pp = p
            p = c
            i++
        }
        val finalItems: SQLItems<SQLItem?>? = flattenItems(items)
        return SQLImpl(sb.toString(), finalItems.toArray(arrayOfNulls<SQLItem?>(finalItems.size())))
    }

    private fun flattenItems(items: List<SQLItems<SQLItem?>?>?): SQLItems<SQLItem?>? {
        val finalItems: SQLItems<SQLItem?> = SQLItems<SQLItem?>()
        val listsToFlatten: Iterator<SQLItems<SQLItem?>?> = items!!.iterator()
        while (listsToFlatten.hasNext()) {
            val sqlItems: List<SQLItem?>? = listsToFlatten.next()
            finalItems.addAll(sqlItems)
        }
        return finalItems
    }

    fun isVariableName(c: Char, alsoNumber: Boolean): Boolean {
        if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_') return true
        return if (alsoNumber && c >= '0' && c <= '9') true else false
    }

    @Throws(ApplicationException::class)
    private operator fun get(name: String?, items: List<SQLItems<NamedSQLItem?>?>?): SQLItems<SQLItem?>? {
        val it = items!!.iterator()
        var item: SQLItems<NamedSQLItem?>?
        while (it.hasNext()) {
            item = it.next()
            if (item.isEmpty()) {
                throw ApplicationException("param [$name] may not be empty")
            }
            if (item.get(0).name.equalsIgnoreCase(name)) {
                return item!!.convertToSQLItems()
            }
        }
        throw ApplicationException("param [$name] not found")
    }

    @Throws(PageException::class)
    private fun isParamNull(param: Struct?): Boolean {
        var oNulls: Object = param.get(KeyConstants._null, null)

        // "nulls" seems to be a typo that is currently left for backward compatibility; deprecate?
        if (oNulls == null) oNulls = param.get(KeyConstants._nulls, null)
        return if (oNulls != null) Caster.toBooleanValue(oNulls) else false
    }

    class NamedSQLItem(val name: String?, value: Object?, type: Int, maxlength: Int, charset: Charset?) : SQLItemImpl(value, type, maxlength, charset) {
        @Override
        override fun toString(): String {
            return "{name:" + name + ";" + super.toString() + "}"
        }

        @Override
        fun clone(`object`: Object?): NamedSQLItem? {
            val item = NamedSQLItem(name, `object`, getType(), getMaxlength(), getCharset())
            item.setNulls(isNulls())
            item.setScale(getScale())
            return item
        }
    }

    private class SQLItems<T : SQLItem?> : ArrayList<T?> {
        constructor() {}
        constructor(item: T?) {
            add(item)
        }

        constructor(item: T?, sct: Struct?) {
            val filledItem = fillSQLItem(item, sct)
            val oList: Object = sct.get(KeyConstants._list, null)
            val value: Object = filledItem.getValue()
            val isList = Decision.isArray(value) && value !is ByteArray || oList != null && Caster.toBooleanValue(oList)
            if (isList) {
                val values: Array
                if (Decision.isArray(value)) {
                    values = Caster.toArray(value)
                } else {
                    val oSeparator: Object = sct.get(KeyConstants._separator, null)
                    var separator = ","
                    if (oSeparator != null) separator = Caster.toString(oSeparator)
                    val v: String = Caster.toString(filledItem.getValue())
                    values = ListUtil.listToArrayRemoveEmpty(v, separator)
                }
                val len: Int = values.size()
                for (i in 1..len) {
                    val clonedItem = filledItem.clone(values.getE(i)) as T?
                    add(clonedItem)
                }
            } else {
                add(filledItem)
            }
        }

        fun convertToSQLItems(): SQLItems<SQLItem?>? {
            val it: Iterator<T?> = iterator()
            val p: SQLItems<SQLItem?> = SQLItems<SQLItem?>()
            while (it.hasNext()) {
                p.add(it.next())
            }
            return p
        }

        @Throws(PageException::class, DatabaseException::class)
        private fun fillSQLItem(item: T?, sct: Struct?): T? {

            // type (optional)
            var oType: Object = sct.get(KeyConstants._cfsqltype, null)
            if (oType == null) oType = sct.get(KeyConstants._sqltype, null)
            if (oType == null) oType = sct.get(KeyConstants._type, null)
            if (oType != null) {
                item.setType(SQLCaster.toSQLType(Caster.toString(oType)))
            }
            item.setNulls(isParamNull(sct))

            // scale (optional)
            val oScale: Object = sct.get(KeyConstants._scale, null)
            if (oScale != null) {
                item.setScale(Caster.toIntValue(oScale))
            }
            return item
        }
    } /*
	 *
	 * public static void main(String[] args) throws PageException { List<SQLItem> one=new
	 * ArrayList<SQLItem>(); one.add(new SQLItemImpl("aaa",1)); one.add(new SQLItemImpl("bbb",1));
	 *
	 * List<NamedSQLItem> two=new ArrayList<NamedSQLItem>(); two.add(new
	 * NamedSQLItem("susi","sorglos",1)); two.add(new NamedSQLItem("peter","Petrus",1));
	 *
	 * SQL sql = convert(
	 * "select ? as x, 'aa:a' as x from test where a=:susi and b=:peter and c=? and d=:susi", one, two);
	 *
	 * print.e(sql);
	 *
	 * // array with simple values Array arr=new ArrayImpl(); arr.appendEL("aaa"); arr.appendEL("bbb");
	 * sql = convert( "select * from test where a=? and b=?", arr); print.e(sql);
	 *
	 * // array with complex values arr=new ArrayImpl(); Struct val1=new StructImpl(); val1.set("value",
	 * "Susi Sorglos"); Struct val2=new StructImpl(); val2.set("value", "123"); val2.set("type",
	 * "integer"); arr.append(val1); arr.append(val2); sql = convert(
	 * "select * from test where a=? and b=?", arr); print.e(sql);
	 *
	 * // array with mixed values arr.appendEL("ccc"); arr.appendEL("ddd"); sql = convert(
	 * "select * from test where a=? and b=? and c=? and d=?", arr); print.e(sql);
	 *
	 * // array mixed with named values Struct val3=new StructImpl(); val3.set("value", "456");
	 * val3.set("type", "integer"); val3.set("name", "susi"); arr.append(val3); sql = convert(
	 * "select :susi as name from test where a=? and b=? and c=? and d=?", arr); print.e(sql);
	 *
	 *
	 * // struct with simple values Struct sct=new StructImpl(); sct.set("abc", "Sorglos"); sql =
	 * convert( "select * from test where a=:abc", sct); print.e(sql);
	 *
	 * // struct with mixed values sct.set("peter", val1); sct.set("susi", val3); sql = convert(
	 * "select :peter as p, :susi as s from test where a=:abc", sct); print.e(sql);
	 *
	 *
	 * }
	 */
}