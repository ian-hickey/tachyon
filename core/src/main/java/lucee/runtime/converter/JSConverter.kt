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
package lucee.runtime.converter

import java.io.IOException

/**
 * class to serialize to Convert CFML Objects (query,array,struct usw) to a JavaScript
 * representation
 */
class JSConverter : ConverterSupport() {
    private var useShortcuts = false
    private var useWDDX = true

    /**
     * serialize a CFML object to a JavaScript Object
     *
     * @param object object to serialize
     * @param clientVariableName name of the variable to create
     * @return vonverte Javascript Code as String
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    fun serialize(`object`: Object?, clientVariableName: String?): String? {
        val sb = StringBuilder()
        _serialize(clientVariableName, `object`, sb, HashSet<Object?>())
        val str: String = sb.toString().trim()
        return clientVariableName.toString() + "=" + str + if (StringUtil.endsWith(str, ';')) "" else ";"
        // return sb.toString();
    }

    @Override
    @Throws(ConverterException::class, IOException::class)
    fun writeOut(pc: PageContext?, source: Object?, writer: Writer?) {
        writer.write(_serialize(source))
        writer.flush()
    }

    @Throws(ConverterException::class)
    private fun _serialize(`object`: Object?): String? {
        val sb = StringBuilder()
        _serialize("tmp", `object`, sb, HashSet<Object?>())
        val str: String = sb.toString().trim()
        return str + if (StringUtil.endsWith(str, ';')) "" else ";"
        // return sb.toString();
    }

    @Throws(ConverterException::class)
    private fun _serialize(name: String?, `object`: Object?, sb: StringBuilder?, done: Set<Object?>?) {
        // NULL
        if (`object` == null) {
            sb.append(goIn())
            sb.append(NULL.toString() + ";")
            return
        }
        // CharSequence (String, StringBuilder ...)
        if (`object` is CharSequence) {
            sb.append(goIn())
            sb.append(StringUtil.escapeJS(`object`.toString(), '"'))
            sb.append(";")
            return
        }
        // Number
        if (`object` is Number) {
            sb.append(goIn())
            sb.append(Caster.toString(`object` as Number?))
            sb.append(';')
            return
        }
        // Date
        if (Decision.isDateSimple(`object`, false)) {
            _serializeDateTime(Caster.toDate(`object`, false, null, null), sb)
            return
        }
        // Boolean
        if (`object` is Boolean) {
            sb.append(goIn())
            sb.append("\"")
            sb.append(if ((`object` as Boolean?).booleanValue()) "true" else "false")
            sb.append("\";")
            return
        }
        val raw: Object = LazyConverter.toRaw(`object`)
        if (done!!.contains(raw)) {
            sb.append(NULL.toString() + ";")
            return
        }
        done.add(raw)
        try {
            // Struct
            if (`object` is Struct) {
                _serializeStruct(name, `object` as Struct?, sb, done)
                return
            }
            // Map
            if (`object` is Map) {
                _serializeMap(name, `object` as Map?, sb, done)
                return
            }
            // List
            if (`object` is List) {
                _serializeList(name, `object` as List?, sb, done)
                return
            }
            // Array
            if (Decision.isArray(`object`)) {
                _serializeArray(name, Caster.toArray(`object`, null), sb, done)
                return
            }
            // Query
            if (`object` is Query) {
                _serializeQuery(name, `object` as Query?, sb, done)
                return
            }
        } finally {
            done.remove(raw)
        }
        sb.append(goIn())
        sb.append(NULL.toString() + ";")
        return
        // throw new ConverterException("can't serialize Object of type ["+Caster.toClassName(object)+"] to
        // a js representation");
    }

    /**
     * serialize an Array
     *
     * @param name
     * @param array Array to serialize
     * @param sb
     * @param done
     * @return serialized array
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeArray(name: String?, array: Array?, sb: StringBuilder?, done: Set<Object?>?) {
        _serializeList(name, array.toList(), sb, done)
    }

    /**
     * serialize a List (as Array)
     *
     * @param name
     * @param list List to serialize
     * @param sb
     * @param done
     * @return serialized list
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeList(name: String?, list: List?, sb: StringBuilder?, done: Set<Object?>?) {
        if (useShortcuts) sb.append("[];") else sb.append("new Array();")
        val it: ListIterator = list.listIterator()
        var index = -1
        while (it.hasNext()) {
            // if(index!=-1)sb.append(",");
            index = it.nextIndex()
            sb.append(name.toString() + "[" + index + "]=")
            _serialize(name.toString() + "[" + index + "]", it.next(), sb, done)
            // sb.append(";");
        }
    }

    /**
     * serialize a Struct
     *
     * @param name
     * @param struct Struct to serialize
     * @param done
     * @param sb2
     * @return serialized struct
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeStruct(name: String?, struct: Struct?, sb: StringBuilder?, done: Set<Object?>?): String? {
        if (useShortcuts) sb.append("{};") else sb.append("new Object();")
        val it: Iterator<Entry<Key?, Object?>?> = struct.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            // lower case ist ok!
            val key: String = StringUtil.escapeJS(Caster.toString(e.getKey().getLowerString(), ""), '"')
            sb.append(name.toString() + "[" + key + "]=")
            _serialize(name.toString() + "[" + key + "]", e.getValue(), sb, done)
        }
        return sb.toString()
    }

    /**
     * serialize a Map (as Struct)
     *
     * @param name
     * @param map Map to serialize
     * @param done
     * @param sb2
     * @return serialized map
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeMap(name: String?, map: Map?, sb: StringBuilder?, done: Set<Object?>?): String? {
        if (useShortcuts) sb.append("{}") else sb.append("new Object();")
        val it: Iterator = map.keySet().iterator()
        while (it.hasNext()) {
            val key: Object = it.next()
            val skey: String = StringUtil.toLowerCase(StringUtil.escapeJS(key.toString(), '"'))
            sb.append(name.toString() + "[" + skey + "]=")
            _serialize(name.toString() + "[" + skey + "]", map.get(key), sb, done)
            // sb.append(";");
        }
        return sb.toString()
    }

    /**
     * serialize a Query
     *
     * @param query Query to serialize
     * @param done
     * @return serialized query
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeQuery(name: String?, query: Query?, sb: StringBuilder?, done: Set<Object?>?) {
        if (useWDDX) _serializeWDDXQuery(name, query, sb, done) else _serializeASQuery(name, query, sb, done)
    }

    @Throws(ConverterException::class)
    private fun _serializeWDDXQuery(name: String?, query: Query?, sb: StringBuilder?, done: Set<Object?>?) {
        val it: Iterator<Key?> = query.keyIterator()
        var k: Key?
        sb.append("new WddxRecordset();")
        val recordcount: Int = query.getRecordcount()
        var i = -1
        while (it.hasNext()) {
            i++
            k = it.next()
            if (useShortcuts) sb.append("col$i=[];") else sb.append("col$i=new Array();")
            // lower case ist ok!
            val skey: String = StringUtil.escapeJS(k.getLowerString(), '"')
            for (y in 0 until recordcount) {
                sb.append("col$i[$y]=")
                _serialize("col$i[$y]", query.getAt(k, y + 1, null), sb, done)
            }
            sb.append(name.toString() + "[" + skey + "]=col" + i + ";col" + i + "=null;")
        }
    }

    @Throws(ConverterException::class)
    private fun _serializeASQuery(name: String?, query: Query?, sb: StringBuilder?, done: Set<Object?>?) {
        val keys: Array<Collection.Key?> = CollectionUtil.keys(query)
        val strKeys = arrayOfNulls<String?>(keys.size)
        for (i in strKeys.indices) {
            strKeys[i] = StringUtil.escapeJS(keys[i].getString(), '"')
        }
        if (useShortcuts) sb.append("[];") else sb.append("new Array();")
        val recordcount: Int = query.getRecordcount()
        for (i in 0 until recordcount) {
            if (useShortcuts) sb.append(name.toString() + "[" + i + "]={};") else sb.append(name.toString() + "[" + i + "]=new Object();")
            for (y in strKeys.indices) {
                sb.append(name.toString() + "[" + i + "][" + strKeys[y] + "]=")
                _serialize(name.toString() + "[" + i + "][" + strKeys[y] + "]", query.getAt(keys[y], i + 1, null), sb, done)
            }
        }
    }

    /**
     * serialize a DateTime
     *
     * @param dateTime DateTime to serialize
     * @param sb
     * @param sb
     * @throws ConverterException
     */
    private fun _serializeDateTime(dateTime: DateTime?, sb: StringBuilder?) {
        val c: Calendar = JREDateTimeUtil.getThreadCalendar(ThreadLocalPageContext.getTimeZone())
        c.setTime(dateTime)
        sb.append(goIn())
        sb.append("new Date(")
        sb.append(c.get(Calendar.YEAR))
        sb.append(",")
        sb.append(c.get(Calendar.MONTH))
        sb.append(",")
        sb.append(c.get(Calendar.DAY_OF_MONTH))
        sb.append(",")
        sb.append(c.get(Calendar.HOUR_OF_DAY))
        sb.append(",")
        sb.append(c.get(Calendar.MINUTE))
        sb.append(",")
        sb.append(c.get(Calendar.SECOND))
        sb.append(");")
    }

    private fun goIn(): String? {
        // StringBuilder rtn=new StringBuilder(deep);
        // for(int i=0;i<deep;i++) rtn.append('\t');
        return "" // rtn.toString();
    }

    fun useShortcuts(useShortcuts: Boolean) {
        this.useShortcuts = useShortcuts
    }

    fun useWDDX(useWDDX: Boolean) {
        this.useWDDX = useWDDX
    } /*
	 * @param args
	 * 
	 * @throws Exception
	 * 
	 * public static void main(String[] args) throws Exception { JSConverter js=new JSConverter(); Query
	 * query=QueryNew.call(null,"aaa,bbb,ccc"); QueryAddRow.call(null,query);
	 * QuerySetCell.call(null,query,"aaa","1.1"); QuerySetCell.call(null,query,"bbb","1.2");
	 * QuerySetCell.call(null,query,"ccc","1.3"); QueryAddRow.call(null,query);
	 * QuerySetCell.call(null,query,"aaa","2.1"); QuerySetCell.call(null,query,"bbb","2.2");
	 * QuerySetCell.call(null,query,"ccc","2.3"); QueryAddRow.call(null,query);
	 * QuerySetCell.call(null,query,"aaa","3.1"); QuerySetCell.call(null,query,"bbb","3.2");
	 * QuerySetCell.call(null,query,"ccc","3.3<hello>"); Array arr2=List ToArray.call(null,"111,222");
	 * Array arr=List ToArray.call(null,"aaaa,bbb,ccc,dddd,eee");
	 * 
	 * arr.set(10,arr2);
	 * 
	 * Struct sct= new Struct(); sct.set("aaa","val1"); sct.set("bbb","val2"); sct.set("ccc","val3");
	 * sct.set("ddd",arr2);
	 * 
	 * / * }
	 */

    companion object {
        private val NULL: String? = "null"
    }
}