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
package tachyon.runtime.converter

import java.io.IOException

/**
 * class to serialize and desirilize WDDX Packes
 */
class ScriptConverter : ConverterSupport {
    private var deep = 1
    private var ignoreRemotingFetch = true

    /**
     * constructor of the class
     */
    constructor() {}
    constructor(ignoreRemotingFetch: Boolean) {
        this.ignoreRemotingFetch = ignoreRemotingFetch
    }

    /**
     * serialize Serializable class
     *
     * @param serializable
     * @param sb
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeSerializable(serializable: Serializable?, sb: StringBuilder?) {
        sb.append(goIn())
        sb.append("evaluateJava(").append(QUOTE_CHR)
        try {
            sb.append(JavaConverter.serialize(serializable))
        } catch (e: IOException) {
            throw toConverterException(e)
        }
        sb.append(QUOTE_CHR).append(')')
    }

    /**
     * serialize a Date
     *
     * @param date Date to serialize
     * @param sb
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeDate(date: Date?, sb: StringBuilder?) {
        _serializeDateTime(DateTimeImpl(date), sb)
    }

    /**
     * serialize a DateTime
     *
     * @param dateTime DateTime to serialize
     * @param sb
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeDateTime(dateTime: DateTime?, sb: StringBuilder?) {
        try {
            val tz: TimeZone = ThreadLocalPageContext.getTimeZone()
            sb.append(goIn())
            sb.append("createDateTime(")
            sb.append(DateFormat.call(null, dateTime, "yyyy,m,d", tz))
            sb.append(',')
            sb.append(TimeFormat.call(null, dateTime, "H,m,s,l,", tz))
            sb.append('"').append(tz.getID()).append('"')
            sb.append(')')
        } catch (e: PageException) {
            throw toConverterException(e)
        }
    }

    /**
     * serialize an Array
     *
     * @param array Array to serialize
     * @param sb
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeArray(array: Array?, sb: StringBuilder?, done: Set<Object?>?) {
        _serializeList(array.toList(), sb, done)
    }

    /**
     * serialize a List (as Array)
     *
     * @param list List to serialize
     * @param sb
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeList(list: List?, sb: StringBuilder?, done: Set<Object?>?) {
        sb.append(goIn())
        sb.append("[")
        var doIt = false
        val it: ListIterator = list.listIterator()
        while (it.hasNext()) {
            if (doIt) sb.append(',')
            doIt = true
            _serialize(it.next(), sb, done)
        }
        sb.append(']')
    }

    /**
     * serialize a Struct
     *
     * @param struct Struct to serialize
     * @param sb
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    fun _serializeStruct(struct: Struct?, sb: StringBuilder?, done: Set<Object?>?) {
        sb.append(goIn())
        val ordered = struct is StructImpl && (struct as StructImpl?).getType() === Struct.TYPE_LINKED
        if (ordered) sb.append('[') else sb.append('{')
        val it: Iterator<Entry<Key?, Object?>?> = struct.entryIterator()
        var e: Entry<Key?, Object?>?
        var doIt = false
        deep++
        while (it.hasNext()) {
            e = it.next()
            val key: String = e.getKey().getString()
            if (doIt) sb.append(',')
            doIt = true
            sb.append(QUOTE_CHR)
            sb.append(escape(key))
            sb.append(QUOTE_CHR)
            sb.append(':')
            _serialize(e.getValue(), sb, done)
        }
        deep--
        if (ordered) sb.append(']') else sb.append('}')
    }

    @Throws(ConverterException::class)
    fun serializeStruct(struct: Struct?, ignoreSet: Set<Collection.Key?>?): String? {
        val sb = StringBuilder()
        sb.append(goIn())
        sb.append("{")
        val hasIgnores = ignoreSet != null
        val it: Iterator<Key?> = struct.keyIterator()
        var doIt = false
        deep++
        var key: Key?
        while (it.hasNext()) {
            key = it.next()
            if (hasIgnores && ignoreSet!!.contains(key)) continue
            if (doIt) sb.append(',')
            doIt = true
            sb.append(QUOTE_CHR)
            sb.append(escape(key.getString()))
            sb.append(QUOTE_CHR)
            sb.append(':')
            _serialize(struct.get(key, null), sb, HashSet<Object?>())
        }
        deep--
        return sb.append('}').toString()
    }

    /**
     * serialize a Map (as Struct)
     *
     * @param map Map to serialize
     * @param sb
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeMap(map: Map?, sb: StringBuilder?, done: Set<Object?>?) {
        if (map is Serializable) {
            _serializeSerializable(map as Serializable?, sb)
            return
        }
        sb.append(goIn())
        sb.append("{")
        val it: Iterator = map.keySet().iterator()
        var doIt = false
        deep++
        while (it.hasNext()) {
            val key: Object = it.next()
            if (doIt) sb.append(',')
            doIt = true
            sb.append(QUOTE_CHR)
            sb.append(escape(key.toString()))
            sb.append(QUOTE_CHR)
            sb.append(':')
            _serialize(map.get(key), sb, done)
        }
        deep--
        sb.append('}')
    }

    /**
     * serialize a Component
     *
     * @param c Component to serialize
     * @param sb
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeComponent(c: Component?, sb: StringBuilder?, done: Set<Object?>?) {
        val cw = ComponentSpecificAccess(Component.ACCESS_PRIVATE, c)
        sb.append(goIn())
        try {
            sb.append("evaluateComponent(").append(QUOTE_CHR).append(c.getAbsName()).append(QUOTE_CHR).append(',').append(QUOTE_CHR).append(ComponentUtil.md5(c)).append(QUOTE_CHR)
                    .append(",{")
        } catch (e: Exception) {
            throw toConverterException(e)
        }
        var doIt = false
        var member: Object
        run {
            val it: Iterator<Entry<Key?, Object?>?> = cw.entryIterator()
            deep++
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                member = e.getValue()
                if (member is UDF) continue
                if (doIt) sb.append(',')
                doIt = true
                sb.append(QUOTE_CHR)
                sb.append(escape(e.getKey().getString()))
                sb.append(QUOTE_CHR)
                sb.append(':')
                _serialize(member, sb, done)
            }
            sb.append("}")
            deep--
        }
        run {
            val isPeristent: Boolean = c.isPersistent()
            val scope: ComponentScope = c.getComponentScope()
            val it: Iterator<Entry<Key?, Object?>?> = scope.entryIterator()
            sb.append(",{")
            deep++
            doIt = false
            var p: Property
            var remotingFetch: Boolean
            val props: Struct? = if (ignoreRemotingFetch) null else ComponentUtil.getPropertiesAsStruct(c, false)
            var e: Entry<Key?, Object?>?
            var k: Key
            while (it.hasNext()) {
                e = it.next()
                k = e.getKey()
                // String key=Caster.toString(it.next(),"");
                if (KeyConstants._THIS.equalsIgnoreCase(k)) continue
                if (!ignoreRemotingFetch) {
                    p = props.get(k, null) as Property
                    if (p != null) {
                        remotingFetch = Caster.toBoolean(p.getDynamicAttributes().get(REMOTING_FETCH, null), null)
                        if (remotingFetch == null) {
                            if (isPeristent && ORMUtil.isRelated(p)) continue
                        } else if (!remotingFetch.booleanValue()) continue
                    }
                }
                member = e.getValue()
                if (member is UDF) continue
                if (doIt) sb.append(',')
                doIt = true
                sb.append(QUOTE_CHR)
                sb.append(escape(k.getString()))
                sb.append(QUOTE_CHR)
                sb.append(':')
                _serialize(member, sb, done)
            }
            sb.append("}")
            deep--
        }
        sb.append(")")
        // sb.append("");
        // throw new ConverterException("can't serialize a component "+component.getDisplayName());
    }

    /**
     * serialize a Query
     *
     * @param query Query to serialize
     * @param sb
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeQuery(query: Query?, sb: StringBuilder?, done: Set<Object?>?) {

        // Collection.Key[] keys = query.keys();
        val it: Iterator<Key?> = query.keyIterator()
        var k: Key?
        sb.append(goIn())
        sb.append("query(")
        deep++
        var oDoIt = false
        val len: Int = query.getRecordcount()
        while (it.hasNext()) {
            k = it.next()
            if (oDoIt) sb.append(',')
            oDoIt = true
            sb.append(goIn())
            sb.append(QUOTE_CHR)
            sb.append(escape(k.getString()))
            sb.append(QUOTE_CHR)
            sb.append(":[")
            var doIt = false
            for (y in 1..len) {
                if (doIt) sb.append(',')
                doIt = true
                try {
                    _serialize(query.getAt(k, y), sb, done)
                } catch (e: PageException) {
                    _serialize(e.getMessage(), sb, done)
                }
            }
            sb.append(']')
        }
        deep--
        sb.append(')')
    }

    /**
     * serialize an Object to his xml Format represenation
     *
     * @param object Object to serialize
     * @param sb StringBuilder to write data
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serialize(`object`: Object?, sb: StringBuilder?, done: Set<Object?>?) {
        // try {
        deep++
        // NULL
        if (`object` == null) {
            sb.append(goIn())
            sb.append("nullValue()")
            deep--
            return
        }
        // String
        if (`object` is String) {
            sb.append(goIn())
            sb.append(QUOTE_CHR)
            sb.append(escape(`object`.toString()))
            sb.append(QUOTE_CHR)
            deep--
            return
        }
        if (`object` is TimeZone) {
            sb.append(goIn())
            sb.append(QUOTE_CHR)
            sb.append(escape((`object` as TimeZone?).getID()))
            sb.append(QUOTE_CHR)
            deep--
            return
        }
        if (`object` is Locale) {
            sb.append(goIn())
            sb.append(QUOTE_CHR)
            sb.append(LocaleFactory.toString(`object` as Locale?))
            sb.append(QUOTE_CHR)
            deep--
            return
        }
        // Number
        if (`object` is Number) {
            sb.append(goIn())
            sb.append(Caster.toString(`object` as Number?))
            deep--
            return
        }
        // Boolean
        if (`object` is Boolean) {
            sb.append(goIn())
            sb.append(Caster.toString((`object` as Boolean?).booleanValue()))
            deep--
            return
        }
        // DateTime
        if (`object` is DateTime) {
            _serializeDateTime(`object` as DateTime?, sb)
            deep--
            return
        }
        // Date
        if (`object` is Date) {
            _serializeDate(`object` as Date?, sb)
            deep--
            return
        }
        // XML
        if (`object` is Node) {
            _serializeXML(`object` as Node?, sb)
            deep--
            return
        }
        if (`object` is ObjectWrap) {
            try {
                _serialize((`object` as ObjectWrap?).getEmbededObject(), sb, done)
            } catch (e: PageException) {
                throw toConverterException(e)
            }
            deep--
            return
        }
        // Timespan
        if (`object` is TimeSpan) {
            _serializeTimeSpan(`object` as TimeSpan?, sb)
            deep--
            return
        }
        val raw: Object = LazyConverter.toRaw(`object`)
        if (done!!.contains(raw)) {
            sb.append(goIn())
            sb.append("nullValue()")
            deep--
            return
        }
        done.add(raw)
        try {
            // Component
            if (`object` is Component) {
                _serializeComponent(`object` as Component?, sb, done)
                deep--
                return
            }

            // Struct
            if (`object` is Struct) {
                _serializeStruct(`object` as Struct?, sb, done)
                deep--
                return
            }
            // Map
            if (`object` is Map) {
                _serializeMap(`object` as Map?, sb, done)
                deep--
                return
            }
            // Array
            if (`object` is Array) {
                _serializeArray(`object` as Array?, sb, done)
                deep--
                return
            }
            // List
            if (`object` is List) {
                _serializeList(`object` as List?, sb, done)
                deep--
                return
            }
            // Query
            if (`object` is Query) {
                _serializeQuery(`object` as Query?, sb, done)
                deep--
                return
            }
            // String Converter
            if (`object` is ScriptConvertable) {
                sb.append((`object` as ScriptConvertable?).serialize())
                deep--
                return
            }
            if (`object` is Serializable) {
                _serializeSerializable(`object` as Serializable?, sb)
                deep--
                return
            }
        } finally {
            done.remove(raw)
        }
        throw ConverterException("can't serialize Object of type [ " + Caster.toClassName(`object`).toString() + " ]")
        // deep--;
        /*
		 * } catch(StackOverflowError soe){ throw soe; }
		 */
    }

    private fun _serializeXML(node: Node?, sb: StringBuilder?) {
        var node: Node? = node
        node = XMLCaster.toRawNode(node)
        sb.append(goIn())
        sb.append("xmlParse(").append(QUOTE_CHR)
        sb.append(escape(XMLCaster.toString(node, "")))
        sb.append(QUOTE_CHR).append(")")
    }

    private fun _serializeTimeSpan(span: TimeSpan?, sb: StringBuilder?) {
        sb.append(goIn())
        sb.append("createTimeSpan(")
        sb.append(span.getDay())
        sb.append(',')
        sb.append(span.getHour())
        sb.append(',')
        sb.append(span.getMinute())
        sb.append(',')
        sb.append(span.getSecond())
        sb.append(')')
    }

    private fun escape(str: String?): String? {
        return StringUtil.replace(StringUtil.replace(str, QUOTE_STR, QUOTE_STR + QUOTE_STR, false), "#", "##", false)
    }

    @Override
    @Throws(ConverterException::class, IOException::class)
    fun writeOut(pc: PageContext?, source: Object?, writer: Writer?) {
        writer.write(serialize(source))
        writer.flush()
    }

    /**
     * serialize an Object to his literal Format
     *
     * @param object Object to serialize
     * @return serialized wddx package
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    fun serialize(`object`: Object?): String? {
        deep = 0
        val sb = StringBuilder()
        _serialize(`object`, sb, HashSet<Object?>())
        return sb.toString()
    }

    /**
     * @return return current blockquote
     */
    private fun goIn(): String? {
        /*
		 * StringBuilder rtn=new StringBuilder('\n'); for(int i=0;i<deep;i++) rtn.append('\t'); return
		 * rtn.toString(); /
		 */
        return ""
    }

    companion object {
        private val REMOTING_FETCH: Collection.Key? = KeyImpl.getInstance("remotingFetch")
        private const val QUOTE_CHR = '"'
        private val QUOTE_STR: String? = String.valueOf(QUOTE_CHR)
    }
}