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
class WDDXConverter(timeZone: TimeZone?, private val xmlConform: Boolean, ignoreRemotingFetch: Boolean) : ConverterSupport() {
    private var deep = 1
    private val del: Char
    private var timeZone: TimeZone?
    private val ignoreRemotingFetch = true

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
        // try {
        val strDate: String = DateFormat(Locale.US).format(dateTime, "yyyy-m-d", TimeZoneConstants.UTC)
        val strTime: String = TimeFormat(Locale.US).format(dateTime, "H:m:s", TimeZoneConstants.UTC)
        return goIn().toString() + "<dateTime>" + strDate + "T" + strTime + "+0:0" + "</dateTime>"

        /*
		 * } catch (PageException e) { throw new ConverterException(e); }
		 */
    }

    private fun _serializeBinary(binary: ByteArray?): String? {
        return StringBuilder("<binary length='").append(binary!!.size).append("'>").append(Base64Coder.encode(binary)).append("</binary>").toString()
    }

    /**
     * @param dateTime
     * @return returns the time zone info
     */
    private fun getTimeZoneInfo(dateTime: DateTime?): String? {
        timeZone = ThreadLocalPageContext.getTimeZone(timeZone)
        // if(timeZone==null) return "";
        var minutes: Int = timeZone.getOffset(dateTime.getTime()) / 1000 / 60
        val operator = if (minutes >= 0) "+" else "-"
        if (operator.equals("-")) minutes = minutes - (minutes + minutes)
        val hours = minutes / 60
        minutes = minutes % 60
        return "$operator$hours:$minutes"
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
    private fun _serializeArray(array: Array?, done: Set<Object?>?): String? {
        return _serializeList(array.toList(), done)
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
    private fun _serializeList(list: List?, done: Set<Object?>?): String? {
        val sb = StringBuilder(goIn().toString() + "<array length=" + del + list.size() + del + ">")
        val it: ListIterator = list.listIterator()
        while (it.hasNext()) {
            sb.append(_serialize(it.next(), done))
        }
        sb.append(goIn().toString() + "</array>")
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
    private fun _serializeComponent(component: Component?, done: Set<Object?>?): String? {
        var component: Component? = component
        val sb = StringBuilder()
        var ca: Component?
        component = ComponentSpecificAccess(Component.ACCESS_PRIVATE, component.also { ca = it })
        val isPeristent: Boolean = ca.isPersistent()
        deep++
        var member: Object
        var it: Iterator<Key?> = component.keyIterator()
        var key: Collection.Key
        while (it.hasNext()) {
            key = Caster.toKey(it.next(), null)
            member = component.get(key, null)
            if (member is UDF) continue
            sb.append(goIn().toString() + "<var scope=\"this\" name=" + del + XMLUtil.escapeXMLString(key.toString()) + del + ">")
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
            sb.append(goIn().toString() + "<var scope=\"variables\" name=" + del + XMLUtil.escapeXMLString(key.toString()) + del + ">")
            sb.append(_serialize(member, done))
            sb.append(goIn().toString() + "</var>")
        }
        deep--
        return try {
            // return goIn()+"<struct>"+sb+"</struct>";
            goIn().toString() + "<component md5=\"" + ComponentUtil.md5(component) + "\" name=\"" + XMLUtil.escapeXMLString(component.getAbsName()) + "\">" + sb + "</component>"
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
    private fun _serializeStruct(struct: Struct?, done: Set<Object?>?): String? {
        val sb = StringBuilder(goIn().toString() + "<struct>")
        val it: Iterator<Key?> = struct.keyIterator()
        deep++
        while (it.hasNext()) {
            val key: Key? = it.next()
            sb.append(goIn().toString() + "<var name=" + del + XMLUtil.escapeXMLString(key.toString()) + del + ">")
            sb.append(_serialize(struct.get(key, null), done))
            sb.append(goIn().toString() + "</var>")
        }
        deep--
        sb.append(goIn().toString() + "</struct>")
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
    private fun _serializeMap(map: Map?, done: Set<Object?>?): String? {
        val sb = StringBuilder(goIn().toString() + "<struct>")
        val it: Iterator = map.keySet().iterator()
        deep++
        while (it.hasNext()) {
            val key: Object = it.next()
            sb.append(goIn().toString() + "<var name=" + del + XMLUtil.escapeXMLString(key.toString()) + del + ">")
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
    private fun _serializeQuery(query: Query?, done: Set<Object?>?): String? {

        // fieldnames
        val pc: PageContext = ThreadLocalPageContext.get()
        var upperCase = false
        if (pc != null) upperCase = pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML && !(pc.getConfig() as ConfigWebPro).preserveCase()
        val fn = StringBuilder()
        val keys: Array<Collection.Key?> = CollectionUtil.keys(query)
        for (i in keys.indices) {
            if (i > 0) fn.append(',')
            fn.append(XMLUtil.escapeXMLString(if (upperCase) keys[i].getUpperString() else keys[i].getString()))
        }
        val sb = StringBuilder(
                goIn().toString() + "<recordset rowCount=" + del + query.getRecordcount() + del + " fieldNames=" + del + fn + del + " type=" + del + "coldfusion.sql.QueryTable" + del + ">")
        deep++
        val len: Int = query.getRecordcount()
        for (i in keys.indices) {
            sb.append(goIn().toString() + "<field name=" + del + XMLUtil.escapeXMLString(keys[i].getString()) + del + ">")
            for (y in 1..len) {
                try {
                    sb.append(_serialize(query.getAt(keys[i], y), done))
                } catch (e: PageException) {
                    sb.append(_serialize(e.getMessage(), done))
                }
            }
            sb.append(goIn().toString() + "</field>")
        }
        deep--
        sb.append(goIn().toString() + "</recordset>")
        return sb.toString()
    }

    /**
     * serialize an Object to his xml Format represenation
     *
     * @param object Object to serialize
     * @param done
     * @return serialized Object
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serialize(`object`: Object?, done: Set<Object?>?): String? {
        val rtn: String?
        deep++
        // NULL
        if (`object` == null) {
            rtn = goIn().toString() + "<null/>"
            deep--
            return rtn
        }
        // String
        if (`object` is String) {
            rtn = goIn().toString() + "<string>" + XMLUtil.escapeXMLString(`object`.toString()) + "</string>"
            deep--
            return rtn
        }
        // Number
        if (`object` is Number) {
            rtn = goIn().toString() + "<number>" + Caster.toString(`object` as Number?) + "</number>"
            deep--
            return rtn
        }
        // Boolean
        if (`object` is Boolean) {
            rtn = goIn().toString() + "<boolean value=" + del + (`object` as Boolean?).booleanValue() + del + "/>"
            deep--
            return rtn
        }
        // DateTime
        if (`object` is DateTime) {
            rtn = _serializeDateTime(`object` as DateTime?)
            deep--
            return rtn
        }
        // Date
        if (`object` is Date) {
            rtn = _serializeDate(`object` as Date?)
            deep--
            return rtn
        }
        // Date
        if (Decision.isCastableToBinary(`object`, false)) {
            rtn = _serializeBinary(Caster.toBinary(`object`, null))
            deep--
            return rtn
        }
        val raw: Object = LazyConverter.toRaw(`object`)
        if (done!!.contains(raw)) {
            rtn = goIn().toString() + "<null/>"
            deep--
            return rtn
        }
        done.add(raw)
        try {
            // Component
            if (`object` is Component) {
                rtn = _serializeComponent(`object` as Component?, done)
                deep--
                return rtn
            }
            // Struct
            if (`object` is Struct) {
                rtn = _serializeStruct(`object` as Struct?, done)
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
                rtn = _serializeArray(`object` as Array?, done)
                deep--
                return rtn
            }
            // List
            if (`object` is List) {
                rtn = _serializeList(`object` as List?, done)
                deep--
                return rtn
            }
            // Query
            if (`object` is Query) {
                rtn = _serializeQuery(`object` as Query?, done)
                deep--
                return rtn
            }
        } finally {
            done.remove(raw)
        }
        // Others
        rtn = "<struct type=" + del + "L" + `object`.getClass().getName() + ";" + del + "></struct>"
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
        if (xmlConform) sb.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
        sb.append("<wddxPacket version=" + del + "1.0" + del + ">")
        deep++
        sb.append(goIn().toString() + "<header/>")
        sb.append(goIn().toString() + "<data>")
        sb.append(_serialize(`object`, HashSet<Object?>()))
        sb.append(goIn().toString() + "</data>")
        deep--
        sb.append("</wddxPacket>")
        return sb.toString()
    }

    /**
     * deserialize a WDDX Package (XML String Representation) to a runtime object
     *
     * @param strWddx
     * @param validate
     * @return Object represent WDDX Package
     * @throws ConverterException
     * @throws IOException
     * @throws FactoryConfigurationError
     */
    @Throws(ConverterException::class, IOException::class, FactoryConfigurationError::class)
    fun deserialize(strWddx: String?, validate: Boolean): Object? {
        try {
            val doc: Document = XMLUtil.parse(XMLUtil.toInputSource(strWddx), null, if (validate) WDDXEntityResolver() else null, false)

            // WDDX Package
            val docChldren: NodeList = doc.getChildNodes()
            var wddxPacket: Node? = doc
            var len: Int = docChldren.getLength()
            for (i in 0 until len) {
                val node: Node = docChldren.item(i)
                if (node.getNodeName().equalsIgnoreCase("wddxPacket")) {
                    wddxPacket = node
                    break
                }
            }
            val nl: NodeList = wddxPacket.getChildNodes()
            val n: Int = nl.getLength()
            for (i in 0 until n) {
                val data: Node = nl.item(i)
                if (data.getNodeName().equals("data")) {
                    val list: NodeList = data.getChildNodes()
                    len = list.getLength()
                    for (y in 0 until len) {
                        val node: Node = list.item(y)
                        if (node is Element) return _deserialize(node as Element)
                    }
                }
            }
            throw IllegalArgumentException("Invalid WDDX Format: node 'data' not found in WDD packet")
        } catch (sxe: org.xml.sax.SAXException) {
            throw IllegalArgumentException("XML Error: " + sxe.toString())
        }
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
            /*
			 * Node data=element.getFirstChild(); if(data==null) return "";
			 * 
			 * String value=data.getNodeValue();
			 * 
			 * if(value==null) return ""; return XMLUtil.unescapeXMLString(value);
			 */
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
        } else if (nodeName.equals("binary")) {
            _deserializeBinary(element)
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

    @Throws(ConverterException::class)
    private fun _deserializeBinary(el: Element?): Object? {
        val node: Node = el.getFirstChild()
        if (node is CharacterData) {
            val data: String = (node as CharacterData).getData()
            return try {
                Base64Coder.decode(data, true)
            } catch (e: CoderException) {
                throw ConverterException(e.getMessage())
            }
        }
        throw ConverterException("cannot convert serialized binary back to binary data")
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
                query.setAt(KeyImpl.init(name), ++count, _deserialize(node as Element))
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
        /**
         * java objects are serialized as empty struct with a type that denotes the class, so if it's not a
         * known struct-type it is not a struct
         */
        return if (struct.isEmpty() && !StringUtil.isEmpty(type) && !KNOWN_STRUCT_TYPES!!.contains(type)) "" else struct
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
        private val KNOWN_STRUCT_TYPES: List<String?>? = Arrays.asList(arrayOf<String?>("coldfusion.server.ConfigMap"))
    }
    // private PageContext pcx;
    /**
     * constructor of the class
     *
     * @param timeZone
     * @param xmlConform define if generated xml conform output or wddx conform output (wddx is not xml
     * conform)
     */
    init {
        del = if (xmlConform) '"' else '\''
        this.timeZone = timeZone
        this.ignoreRemotingFetch = ignoreRemotingFetch
    }
}