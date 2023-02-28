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
class XMLConverter(timeZone: TimeZone?, ignoreRemotingFetch: Boolean) : ConverterSupport() {
    private var deep = 1
    private val del = '"'
    private var timeZone: TimeZone?
    private val ignoreRemotingFetch = true

    // private PageContext pcx;
    private var type: String? = null
    private var id = 0

    /**
     * defines timezone info will
     *
     * @param timeZone
     */
    fun setTimeZone(timeZone: TimeZone?) {
        this.timeZone = timeZone
    }

    /**
     * serialize a Date
     *
     * @param date Date to serialize
     * @return serialized date
     * @throws ConverterException
     */
    private fun _serializeDate(date: Date?): String? {
        return _serializeDateTime(DateTimeImpl(date))
    }

    /**
     * serialize a DateTime
     *
     * @param dateTime DateTime to serialize
     * @return serialized dateTime
     * @throws ConverterException
     */
    private fun _serializeDateTime(dateTime: DateTime?): String? {
        /*
		 * ACF FORMAT String strDate = new
		 * tachyon.runtime.format.DateFormat(Locale.US).format(dateTime,"mmmm, dd yyyy"); String strTime = new
		 * tachyon.runtime.format.TimeFormat(Locale.US).format(dateTime,"HH:mm:ss"); return
		 * goIn()+strDate+" "+strTime;
		 */
        return goIn() + JSONDateFormat.format(dateTime, null, JSONDateFormat.PATTERN_CF)
    }

    /**
     * serialize an Array
     *
     * @param array Array to serialize
     * @param done
     * @return serialized array
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeArray(array: Array?, done: Map<Object?, String?>?, id: String?): String? {
        return _serializeList(array.toList(), done, id)
    }

    /**
     * serialize a List (as Array)
     *
     * @param list List to serialize
     * @param done
     * @return serialized list
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeList(list: List?, done: Map<Object?, String?>?, id: String?): String? {
        // <ARRAY ID="1" SIZE="1"><ITEM INDEX="1" TYPE="STRING">hello world</ITEM></ARRAY>
        val sb = StringBuilder(goIn().toString() + "<ARRAY ID=\"" + id + "\" SIZE=" + del + list.size() + del + ">")
        var index: Int
        val it: ListIterator = list.listIterator()
        while (it.hasNext()) {
            // <ITEM INDEX="1" TYPE="STRING">hello world</ITEM>
            index = it.nextIndex()
            val value = _serialize(it.next(), done)
            sb.append(goIn().toString() + "<ITEM INDEX=\"" + (index + 1) + "\" TYPE=\"" + type + "\">")
            sb.append(value)
            sb.append(goIn().toString() + "</ITEM>")
        }
        sb.append(goIn().toString() + "</ARRAY>")
        type = "ARRAY"
        return sb.toString()
    }

    /**
     * serialize a Component
     *
     * @param component Component to serialize
     * @param done
     * @return serialized component
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeComponent(component: Component?, done: Map<Object?, String?>?): String? {
        var component: Component? = component
        val sb = StringBuilder()
        var ca: Component?
        component = ComponentSpecificAccess(Component.ACCESS_PRIVATE, component.also { ca = it })
        val isPeristent: Boolean = ca.isPersistent()
        deep++
        var member: Object
        var it: Iterator<Key?> = component.keyIterator()
        var key: Collection.Key?
        while (it.hasNext()) {
            key = it.next()
            member = component.get(key, null)
            if (member is UDF) continue
            sb.append(goIn().toString() + "<var scope=\"this\" name=" + del + key.toString() + del + ">")
            sb.append(_serialize(member, done))
            sb.append(goIn().toString() + "</var>")
        }
        var p: Property
        var remotingFetch: Boolean
        val props: Struct? = if (ignoreRemotingFetch) null else ComponentUtil.getPropertiesAsStruct(ca, false)
        val scope: ComponentScope = ca.getComponentScope()
        it = scope.keyIterator()
        while (it.hasNext()) {
            key = Caster.toKey(it.next(), null)
            if (!ignoreRemotingFetch) {
                p = props.get(key, null) as Property
                if (p != null) {
                    remotingFetch = Caster.toBoolean(p.getDynamicAttributes().get(REMOTING_FETCH, null), null)
                    if (remotingFetch == null) {
                        if (isPeristent && ORMUtil.isRelated(p)) continue
                    } else if (!remotingFetch.booleanValue()) continue
                }
            }
            member = scope.get(key, null)
            if (member is UDF || key.equals(KeyConstants._this)) continue
            sb.append(goIn().toString() + "<var scope=\"variables\" name=" + del + key.toString() + del + ">")
            sb.append(_serialize(member, done))
            sb.append(goIn().toString() + "</var>")
        }
        deep--
        return try {
            // return goIn()+"<struct>"+sb+"</struct>";
            goIn().toString() + "<component md5=\"" + ComponentUtil.md5(component) + "\" name=\"" + component.getAbsName() + "\">" + sb + "</component>"
        } catch (e: Exception) {
            throw toConverterException(e)
        }
    }

    /**
     * serialize a Struct
     *
     * @param struct Struct to serialize
     * @param done
     * @return serialized struct
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeStruct(struct: Struct?, done: Map<Object?, String?>?, id: String?): String? {
        val sb = StringBuilder(goIn().toString() + "<STRUCT ID=\"" + id + "\">")
        val it: Iterator<Key?> = struct.keyIterator()
        deep++
        while (it.hasNext()) {
            val key: Key? = it.next()
            // <ENTRY NAME="STRING" TYPE="STRING">hello</ENTRY>
            val value = _serialize(struct.get(key, null), done)
            sb.append(goIn().toString() + "<ENTRY NAME=\"" + key.toString() + "\" TYPE=\"" + type + "\">")
            sb.append(value)
            sb.append(goIn().toString() + "</ENTRY>")
        }
        deep--
        sb.append(goIn().toString() + "</STRUCT>")
        type = "STRUCT"
        return sb.toString()
    }

    /**
     * serialize a Map (as Struct)
     *
     * @param map Map to serialize
     * @param done
     * @return serialized map
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeMap(map: Map?, done: Map<Object?, String?>?): String? {
        val sb = StringBuilder(goIn().toString() + "<struct>")
        val it: Iterator = map.keySet().iterator()
        deep++
        while (it.hasNext()) {
            val key: Object = it.next()
            sb.append(goIn().toString() + "<var name=" + del + key.toString() + del + ">")
            sb.append(_serialize(map.get(key), done))
            sb.append(goIn().toString() + "</var>")
        }
        deep--
        sb.append(goIn().toString() + "</struct>")
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
    private fun _serializeQuery(query: Query?, done: Map<Object?, String?>?, id: String?): String? {

        /*
		 * <QUERY ID="1"> <COLUMNNAMES> <COLUMN NAME="a"></COLUMN> <COLUMN NAME="b"></COLUMN> </COLUMNNAMES>
		 * 
		 * <ROWS> <ROW> <COLUMN TYPE="STRING">a1</COLUMN> <COLUMN TYPE="STRING">b1</COLUMN> </ROW> <ROW>
		 * <COLUMN TYPE="STRING">a2</COLUMN> <COLUMN TYPE="STRING">b2</COLUMN> </ROW> </ROWS> </QUERY>
		 */
        val keys: Array<Collection.Key?> = CollectionUtil.keys(query)
        val sb = StringBuilder(goIn().toString() + "<QUERY ID=\"" + id + "\">")

        // columns
        sb.append(goIn().toString() + "<COLUMNNAMES>")
        for (i in keys.indices) {
            sb.append(goIn().toString() + "<COLUMN NAME=\"" + keys[i].getString() + "\"></COLUMN>")
        }
        sb.append(goIn().toString() + "</COLUMNNAMES>")
        var value: String?
        deep++
        sb.append(goIn().toString() + "<ROWS>")
        val len: Int = query.getRecordcount()
        for (row in 1..len) {
            sb.append(goIn().toString() + "<ROW>")
            for (col in keys.indices) {
                value = try {
                    _serialize(query.getAt(keys[col], row), done)
                } catch (e: PageException) {
                    _serialize(e.getMessage(), done)
                }
                sb.append("<COLUMN TYPE=\"$type\">$value</COLUMN>")
            }
            sb.append(goIn().toString() + "</ROW>")
        }
        sb.append(goIn().toString() + "</ROWS>")
        deep--
        sb.append(goIn().toString() + "</QUERY>")
        type = "QUERY"
        return sb.toString()
    }

    /**
     * serialize an Object to it's xml Format represenation
     *
     * @param object Object to serialize
     * @param done
     * @return serialized Object
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serialize(`object`: Object?, done: Map<Object?, String?>?): String? {
        type = "OBJECT"
        val rtn: String?
        deep++
        // NULL
        if (`object` == null) {
            rtn = goIn().toString() + ""
            deep--
            type = "NULL"
            return rtn
        }
        // String
        if (`object` is String) {
            rtn = goIn() + XMLUtil.escapeXMLString(`object`.toString())
            deep--
            type = "STRING"
            return rtn
        }
        // Number
        if (`object` is Number) {
            rtn = goIn() + Caster.toString(`object` as Number?)
            deep--
            type = "NUMBER"
            return rtn
        }
        // Boolean
        if (`object` is Boolean) {
            rtn = goIn() + (`object` as Boolean?).booleanValue()
            deep--
            type = "BOOLEAN"
            return rtn
        }
        // DateTime
        if (`object` is DateTime) {
            rtn = _serializeDateTime(`object` as DateTime?)
            deep--
            type = "DATE"
            return rtn
        }
        // Date
        if (`object` is Date) {
            rtn = _serializeDate(`object` as Date?)
            deep--
            type = "DATE"
            return rtn
        }
        val raw: Object = LazyConverter.toRaw(`object`)
        var strId = done!![raw]
        if (strId != null) {
            rtn = goIn().toString() + "<REF id=\"" + strId + "\"\\>"
            deep--
            type = "NULL"
            return rtn
        }
        strId = Caster.toString(++id)
        done.put(raw, strId)
        try {
            // Component
            if (`object` is Component) {
                rtn = _serializeComponent(`object` as Component?, done)
                deep--
                return rtn
            }
            // Struct
            if (`object` is Struct) {
                rtn = _serializeStruct(`object` as Struct?, done, strId)
                deep--
                return rtn
            }
            // Map
            if (`object` is Map) {
                rtn = _serializeMap(`object` as Map?, done)
                deep--
                return rtn
            }
            // Array
            if (`object` is Array) {
                rtn = _serializeArray(`object` as Array?, done, strId)
                deep--
                return rtn
            }
            // List
            if (`object` is List) {
                rtn = _serializeList(`object` as List?, done, strId)
                deep--
                return rtn
            }
            // Query
            if (`object` is Query) {
                rtn = _serializeQuery(`object` as Query?, done, strId)
                deep--
                return rtn
            }
        } finally {
            done.remove(raw)
        }
        // Others
        rtn = "<STRUCT ID=\"" + strId + "\" TYPE=\"" + Caster.toTypeName(`object`) + "\"></STRUCT>"
        deep--
        return rtn
    }

    @Override
    @Throws(ConverterException::class, IOException::class)
    fun writeOut(pc: PageContext?, source: Object?, writer: Writer?) {
        writer.write(serialize(source))
        writer.flush()
    }

    /**
     * serialize an Object to his xml Format represenation and create a valid wddx representation
     *
     * @param object Object to serialize
     * @return serialized wddx package
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    fun serialize(`object`: Object?): String? {
        deep = 0
        val sb = StringBuilder()
        // if(xmlConform)sb.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
        deep++
        sb.append(_serialize(`object`, HashMap<Object?, String?>()))
        deep--
        return sb.toString()
    }

    /**
     * deserialize a WDDX Package (XML Element) to a runtime object
     *
     * @param element
     * @return deserialized Element
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _deserialize(element: Element?): Object? {
        val nodeName: String = element.getNodeName().toLowerCase()

        // NULL
        return if (nodeName.equals("null")) {
            null
        } else if (nodeName.equals("string")) {
            _deserializeString(element)
        } else if (nodeName.equals("number")) {
            try {
                val data: Node = element.getFirstChild() ?: return Double.valueOf(0)
                Caster.toDouble(data.getNodeValue())
            } catch (e: Exception) {
                throw toConverterException(e)
            }
        } else if (nodeName.equals("boolean")) {
            try {
                Caster.toBoolean(element.getAttribute("value"))
            } catch (e: PageException) {
                throw toConverterException(e)
            }
        } else if (nodeName.equals("array")) {
            _deserializeArray(element)
        } else if (nodeName.equals("component") || nodeName.equals("class")) {
            _deserializeComponent(element)
        } else if (nodeName.equals("struct")) {
            _deserializeStruct(element)
        } else if (nodeName.equals("recordset")) {
            _deserializeQuery(element)
        } else if (nodeName.equalsIgnoreCase("dateTime")) {
            try {
                DateCaster.toDateAdvanced(element.getFirstChild().getNodeValue(), timeZone)
            } catch (e: Exception) {
                throw toConverterException(e)
            }
        } else throw ConverterException("can't deserialize Element of type [$nodeName] to an Object representation")
    }

    private fun _deserializeString(element: Element?): Object? {
        val childList: NodeList = element.getChildNodes()
        val len: Int = childList.getLength()
        val sb = StringBuilder()
        var data: Node
        var str: String?
        for (i in 0 until len) {
            data = childList.item(i)
            if (data == null) continue

            // <char code="0a"/>
            if ("char".equals(data.getNodeName())) {
                str = (data as Element).getAttribute("code")
                sb.append(NumberUtil.hexToInt(str, 10) as Char)
            } else {
                sb.append(data.getNodeValue().also { str = it })
            }
        }
        return sb.toString()
        // return XMLUtil.unescapeXMLString(sb.toString());
    }

    /**
     * Desirialize a Query Object
     *
     * @param recordset Query Object as XML Element
     * @return Query Object
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _deserializeQuery(recordset: Element?): Object? {
        return try {
            // create Query Object
            val query: Query = QueryImpl(tachyon.runtime.type.util.ListUtil.listToArray(recordset.getAttribute("fieldNames"), ','),
                    Caster.toIntValue(recordset.getAttribute("rowCount")), "query")
            val list: NodeList = recordset.getChildNodes()
            val len: Int = list.getLength()
            for (i in 0 until len) {
                val node: Node = list.item(i)
                if (node is Element) {
                    _deserializeQueryField(query, node as Element)
                }
            }
            query
        } catch (e: PageException) {
            throw toConverterException(e)
        }
    }

    /**
     * deserilize a single Field of a query WDDX Object
     *
     * @param query
     * @param field
     * @throws ConverterException
     * @throws PageException
     */
    @Throws(PageException::class, ConverterException::class)
    private fun _deserializeQueryField(query: Query?, field: Element?) {
        val name: String = field.getAttribute("name")
        val list: NodeList = field.getChildNodes()
        val len: Int = list.getLength()
        var count = 0
        for (i in 0 until len) {
            val node: Node = list.item(i)
            if (node is Element) {
                query.setAt(name, ++count, _deserialize(node as Element))
            }
        }
    }

    /**
     * Desirialize a Component Object
     *
     * @param elComp Component Object as XML Element
     * @return Component Object
     * @throws ConverterException
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _deserializeComponent(elComp: Element?): Object? {
        // String type=elStruct.getAttribute("type");
        val name: String = elComp.getAttribute("name")
        val md5: String = elComp.getAttribute("md5")

        // TLPC
        val pc: PageContext = ThreadLocalPageContext.get()

        // Load comp
        var comp: Component? = null
        try {
            comp = pc.loadComponent(name)
            if (!ComponentUtil.md5(comp).equals(md5)) {
                throw ConverterException("component [" + name
                        + "] in this environment has not the same interface as the component to load, it is possible that one off the components has Functions added dynamically.")
            }
        } catch (e: ConverterException) {
            throw e
        } catch (e: Exception) {
            throw ConverterException(e.getMessage())
        }
        val list: NodeList = elComp.getChildNodes()
        val scope: ComponentScope = comp.getComponentScope()
        val len: Int = list.getLength()
        var scopeName: String
        var `var`: Element
        var value: Element?
        var key: Collection.Key
        for (i in 0 until len) {
            val node: Node = list.item(i)
            if (node is Element) {
                `var` = node as Element
                value = getChildElement(node as Element)
                scopeName = `var`.getAttribute("scope")
                if (value != null) {
                    key = Caster.toKey(`var`.getAttribute("name"), null)
                    if (key == null) continue
                    if ("variables".equalsIgnoreCase(scopeName)) scope.setEL(key, _deserialize(value)) else comp.setEL(key, _deserialize(value))
                }
            }
        }
        return comp
    }

    /**
     * Desirialize a Struct Object
     *
     * @param elStruct Struct Object as XML Element
     * @return Struct Object
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _deserializeStruct(elStruct: Element?): Object? {
        val type: String = elStruct.getAttribute("type")
        val struct: Struct = StructImpl()
        val list: NodeList = elStruct.getChildNodes()
        val len: Int = list.getLength()
        for (i in 0 until len) {
            // print.ln(i);
            val node: Node = list.item(i)
            if (node is Element) {
                val `var`: Element = node as Element
                val value: Element? = getChildElement(node as Element)
                if (value != null) {
                    struct.setEL(`var`.getAttribute("name"), _deserialize(value))
                }
            }
        }
        return if (struct.size() === 0 && type != null && type.length() > 0) {
            ""
        } else struct
    }

    /**
     * Desirialize a Struct Object
     *
     * @param el Struct Object as XML Element
     * @return Struct Object
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _deserializeArray(el: Element?): Array? {
        val array: Array = ArrayImpl()
        val list: NodeList = el.getChildNodes()
        val len: Int = list.getLength()
        for (i in 0 until len) {
            val node: Node = list.item(i)
            if (node is Element) try {
                array.append(_deserialize(node as Element))
            } catch (e: PageException) {
                throw toConverterException(e)
            }
        }
        return array
    }

    /**
     * return fitst child Element of an Element, if there are no child Elements return null
     *
     * @param parent parent node
     * @return child Element
     */
    private fun getChildElement(parent: Element?): Element? {
        val list: NodeList = parent.getChildNodes()
        val len: Int = list.getLength()
        for (i in 0 until len) {
            val node: Node = list.item(i)
            if (node is Element) {
                return node as Element
            }
        }
        return null
    }

    /**
     * @return return current blockquote
     */
    private fun goIn(): String? {
        // StringBuilder rtn=new StringBuilder(deep);
        // for(int i=0;i<deep;i++) rtn.append('\t');
        // return rtn.toString();
        return ""
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return timeZone.equals(obj)
    }

    companion object {
        private val REMOTING_FETCH: Collection.Key? = KeyImpl.getInstance("remotingFetch")
    }

    /**
     * constructor of the class
     *
     * @param timeZone
     * @param xmlConform define if generated xml conform output or wddx conform output (wddx is not xml
     * conform)
     */
    init {
        this.timeZone = timeZone
        this.ignoreRemotingFetch = ignoreRemotingFetch
    }
}