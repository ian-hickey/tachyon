/**
 * Copyright (c) 2014, the Railo Company Ltd.
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
package lucee.runtime.converter

import java.io.File

/**
 * class to serialize and desirilize WDDX Packes
 */
class JSONConverter(private val ignoreRemotingFetch: Boolean, charset: Charset?, pattern: String?, preserveCase: Boolean?, multiline: Boolean, commentName: String?) : ConverterSupport() {
    private val charsetEncoder: CharsetEncoder?
    private val pattern: String?
    private val _preserveCase: Boolean?
    private val multiline: Boolean
    private var indent = 0
    private val commentName: Key?

    /**
     * @param ignoreRemotingFetch
     * @param charset if set, characters not supported by the charset are escaped.
     * @param patternCf
     */
    constructor(ignoreRemotingFetch: Boolean, charset: Charset?) : this(ignoreRemotingFetch, charset, JSONDateFormat.PATTERN_CF, null, false, null) {}
    constructor(ignoreRemotingFetch: Boolean, charset: Charset?, pattern: String?) : this(ignoreRemotingFetch, charset, pattern, null, false, null) {}
    constructor(ignoreRemotingFetch: Boolean, charset: Charset?, pattern: String?, preserveCase: Boolean?) : this(ignoreRemotingFetch, charset, pattern, preserveCase, false, null) {}
    constructor(ignoreRemotingFetch: Boolean, charset: Charset?, pattern: String?, preserveCase: Boolean?, multiline: Boolean) : this(ignoreRemotingFetch, charset, pattern, preserveCase, multiline, null) {}

    /**
     * serialize Serializable class
     *
     * @param serializable
     * @param sb
     * @param serializeQueryByColumns
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeClass(pc: PageContext?, test: Set?, clazz: Class?, obj: Object?, sb: StringBuilder?, queryFormat: Int, done: ObjectIdentityHashSet?) {
        var test: Set? = test
        val sct: Struct = StructImpl(Struct.TYPE_LINKED)
        if (test == null) test = HashSet()

        // Fields
        val fields: Array<Field?> = clazz.getFields()
        var field: Field?
        for (i in fields.indices) {
            field = fields[i]
            if (obj != null || field.getModifiers() and Modifier.STATIC > 0) try {
                sct.setEL(field.getName(), testRecursion(test, field.get(obj)))
            } catch (e: Exception) {
                LogUtil.log(pc, Controler::class.java.getName(), e)
            }
        }
        if (obj != null) {
            // setters
            val setters: Array<Method?> = Reflector.getSetters(clazz)
            for (i in setters.indices) {
                sct.setEL(setters[i].getName().substring(3), CollectionUtil.NULL)
            }
            // getters
            val getters: Array<Method?> = Reflector.getGetters(clazz)
            for (i in getters.indices) {
                try {
                    sct.setEL(getters[i].getName().substring(3), testRecursion(test, getters[i].invoke(obj, ArrayUtil.OBJECT_EMPTY)))
                } catch (e: Exception) {
                }
            }
        }
        test.add(clazz)
        _serializeStruct(pc, test, sct, sb, queryFormat, true, done)
    }

    private fun testRecursion(test: Set?, obj: Object?): Object? {
        return if (test.contains(obj.getClass())) obj.getClass().getName() else obj
    }

    /**
     * serialize a Date
     *
     * @param date Date to serialize
     * @param sb
     * @throws ConverterException
     */
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
    private fun _serializeDateTime(dateTime: DateTime?, sb: StringBuilder?) {
        sb.append(StringUtil.escapeJS(JSONDateFormat.format(dateTime, null, pattern), '"', charsetEncoder))

        /*
		 * try { sb.append("createDateTime("); sb.append(DateFormat.call(null,dateTime,"yyyy,m,d"));
		 * sb.append(' '); sb.append(TimeFormat.call(null,dateTime,"HH:mm:ss")); sb.append(')'); } catch
		 * (PageException e) { throw new ConverterException(e); }
		 */
        // Januar, 01 2000 01:01:01
    }

    /**
     * serialize an Array
     *
     * @param array Array to serialize
     * @param sb
     * @param serializeQueryByColumns
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeArray(pc: PageContext?, test: Set?, array: Array?, sb: StringBuilder?, queryFormat: Int, done: ObjectIdentityHashSet?) {
        _serializeList(pc, test, array.toList(), sb, queryFormat, done)
    }

    /**
     * serialize a List (as Array)
     *
     * @param list List to serialize
     * @param sb
     * @param serializeQueryByColumns
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeList(pc: PageContext?, test: Set?, list: List?, sb: StringBuilder?, queryFormat: Int, done: ObjectIdentityHashSet?) {
        sb.append("[")
        indentPlus(sb)
        var doIt = false
        val it: ListIterator = list.listIterator()
        while (it.hasNext()) {
            if (doIt) {
                nl(sb)
                sb.append(',')
            }
            doIt = true
            _serialize(pc, test, it.next(), sb, queryFormat, done)
        }
        indentMinus(sb)
        sb.append(']')
    }

    @Throws(ConverterException::class)
    private fun _serializeArray(pc: PageContext?, test: Set?, arr: Array<Object?>?, sb: StringBuilder?, queryFormat: Int, done: ObjectIdentityHashSet?) {
        sb.append("[")
        indentPlus(sb)
        for (i in arr.indices) {
            if (i > 0) {
                nl(sb)
                sb.append(',')
            }
            _serialize(pc, test, arr!![i], sb, queryFormat, done)
        }
        indentMinus(sb)
        sb.append(']')
    }

    /**
     * serialize a Struct
     *
     * @param struct Struct to serialize
     * @param sb
     * @param serializeQueryByColumns
     * @param addUDFs
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    fun _serializeStruct(pc: PageContext?, test: Set?, struct: Struct?, sb: StringBuilder?, queryFormat: Int, addUDFs: Boolean, done: ObjectIdentityHashSet?) {

        // preserve case by default for Struct
        val preserveCase = getPreserveCase(pc, false)

        // Component
        if (struct is Component) {
            val res = castToJson(pc, struct as Component?, NULL_STRING)
            if (res !== NULL_STRING) {
                sb.append(res)
                return
            }
        }
        if (commentName != null) {
            val comment: String = Caster.toString(struct.get(commentName, null), null)
            if (!StringUtil.isEmpty(comment, true)) {
                if (sb.length() > 0) nl(sb)
                sb.append("/*")
                nl(sb)
                sb.append(comment.trim())
                nl(sb)
                sb.append("*/")
                nl(sb)
            }
        }
        sb.append("{")
        indentPlus(sb)
        val it: Iterator<Entry<Key?, Object?>?> = struct.entryIterator()
        var e: Entry<Key?, Object?>?
        var k: String?
        var value: Object
        var doIt = false
        while (it.hasNext()) {
            e = it.next()
            k = e.getKey().getString()
            if (!preserveCase) k = k.toUpperCase()
            value = e.getValue()
            if (!addUDFs && (value is UDF || value == null)) continue
            if (doIt) {
                nl(sb)
                sb.append(',')
            }
            doIt = true
            sb.append(StringUtil.escapeJS(k, '"', charsetEncoder))
            sb.append(':')
            _serialize(pc, test, value, sb, queryFormat, done)
        }
        if (struct is Component) {
            var remotingFetch: Boolean
            val comp: Component? = struct as Component?
            var isPeristent = false
            isPeristent = comp.isPersistent()
            val props: Array<Property?> = comp.getProperties(false, true, false, false)
            val scope: ComponentScope = comp.getComponentScope()
            for (i in props.indices) {
                if (!ignoreRemotingFetch) {
                    remotingFetch = Caster.toBoolean(props[i].getDynamicAttributes().get(REMOTING_FETCH, null), null)
                    if (remotingFetch == null) {
                        if (isPeristent && ORMUtil.isRelated(props[i])) continue
                    } else if (!remotingFetch.booleanValue()) continue
                }
                val key: Key = KeyImpl.getInstance(props[i].getName())
                value = scope.get(key, null)
                if (!addUDFs && (value is UDF || value == null)) continue
                if (doIt) {
                    nl(sb)
                    sb.append(',')
                }
                doIt = true
                sb.append(StringUtil.escapeJS(key.getString(), '"', charsetEncoder))
                sb.append(':')
                _serialize(pc, test, value, sb, queryFormat, done)
            }
        }
        indentMinus(sb)
        sb.append('}')
    }

    private fun getPreserveCase(pc: PageContext?, forQuery: Boolean): Boolean {
        if (_preserveCase != null) {
            return _preserveCase.booleanValue()
        }
        val acs: ApplicationContextSupport? = if (pc == null) null else pc.getApplicationContext() as ApplicationContextSupport
        return if (acs != null) {
            if (forQuery) acs.getSerializationSettings().getPreserveCaseForQueryColumn() else acs.getSerializationSettings().getPreserveCaseForStructKey()
        } else true
    }

    /**
     * serialize a Map (as Struct)
     *
     * @param map Map to serialize
     * @param sb
     * @param serializeQueryByColumns
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeMap(pc: PageContext?, test: Set?, map: Map?, sb: StringBuilder?, queryFormat: Int, done: ObjectIdentityHashSet?) {
        sb.append("{")
        indentPlus(sb)
        val it: Iterator = map.keySet().iterator()
        var doIt = false
        while (it.hasNext()) {
            val key: Object = it.next()
            if (doIt) {
                nl(sb)
                sb.append(',')
            }
            doIt = true
            sb.append(StringUtil.escapeJS(key.toString(), '"', charsetEncoder))
            sb.append(':')
            _serialize(pc, test, map.get(key), sb, queryFormat, done)
        }
        indentMinus(sb)
        sb.append('}')
    }

    /**
     * serialize a Component
     *
     * @param component Component to serialize
     * @param sb
     * @param serializeQueryByColumns
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeComponent(pc: PageContext?, test: Set?, component: Component?, sb: StringBuilder?, queryFormat: Int, done: ObjectIdentityHashSet?) {
        val cw: ComponentSpecificAccess = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, component)
        _serializeStruct(pc, test, cw, sb, queryFormat, false, done)
    }

    @Throws(ConverterException::class)
    private fun _serializeUDF(pc: PageContext?, test: Set?, udf: UDF?, sb: StringBuilder?, queryFormat: Int, done: ObjectIdentityHashSet?) {
        val sct: Struct = StructImpl()
        try {
            // Meta
            val meta: Struct = udf.getMetaData(pc)
            sct.setEL("Metadata", meta)

            // Parameters
            sct.setEL("MethodAttributes", meta.get("PARAMETERS"))
        } catch (e: PageException) {
            throw toConverterException(e)
        }
        sct.setEL("Access", ComponentUtil.toStringAccess(udf.getAccess(), "public"))
        sct.setEL("Output", Caster.toBoolean(udf.getOutput()))
        sct.setEL("ReturnType", udf.getReturnTypeAsString())
        try {
            sct.setEL("PagePath", udf.getSource())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        _serializeStruct(pc, test, sct, sb, queryFormat, true, done)
        // TODO key SuperScope and next?
    }

    /**
     * serialize a Query
     *
     * @param query Query to serialize
     * @param sb
     * @param serializeQueryByColumns
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serializeQuery(pc: PageContext?, test: Set?, query: Query?, sb: StringBuilder?, queryFormat: Int, done: ObjectIdentityHashSet?) {
        var pc: PageContext? = pc
        val preserveCase = getPreserveCase(pc, true) // UPPERCASE column keys by default for Query
        val _keys: Array<Collection.Key?> = CollectionUtil.keys(query)
        if (queryFormat == SerializationSettings.SERIALIZE_AS_STRUCT) {
            sb.append("[")
            indentPlus(sb)
            val rc: Int = query.getRecordcount()
            for (row in 1..rc) {
                if (row > 1) {
                    nl(sb)
                    sb.append(',')
                }
                sb.append("{")
                indentPlus(sb)
                for (col in _keys.indices) {
                    if (col > 0) {
                        nl(sb)
                        sb.append(',')
                    }
                    sb.append(StringUtil.escapeJS(if (preserveCase) _keys[col].getString() else _keys[col].getUpperString(), '"', charsetEncoder))
                    sb.append(':')
                    try {
                        _serialize(pc, test, query.getAt(_keys[col], row), sb, queryFormat, done)
                    } catch (e: PageException) {
                        _serialize(pc, test, e.getMessage(), sb, queryFormat, done)
                    }
                }
                indentMinus(sb)
                sb.append("}")
            }
            indentMinus(sb)
            sb.append("]")
            return
        }
        sb.append("{")
        indentPlus(sb)
        /*
		 * 
		 * {"DATA":[["a","b"],["c","d"]]} {"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}
		 */
        // Rowcount
        if (queryFormat == SerializationSettings.SERIALIZE_AS_COLUMN) {
            sb.append("\"ROWCOUNT\":")
            sb.append(Caster.toString(query.getRecordcount()))
            nl(sb)
            sb.append(',')
        }

        // Columns
        sb.append("\"COLUMNS\":[")
        indentPlus(sb)
        val cols: Array<String?> = query.getColumns()
        for (i in cols.indices) {
            if (i > 0) {
                nl(sb)
                sb.append(",")
            }
            sb.append(StringUtil.escapeJS(if (preserveCase) cols[i] else cols[i].toUpperCase(), '"', charsetEncoder))
        }
        indentMinus(sb)
        sb.append("],")

        // Data
        sb.append("\"DATA\":")
        if (queryFormat == SerializationSettings.SERIALIZE_AS_COLUMN) {
            sb.append('{')
            indentPlus(sb)
            var oDoIt = false
            val len: Int = query.getRecordcount()
            pc = ThreadLocalPageContext.get(pc)
            var upperCase = false
            if (pc != null) upperCase = pc.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML && (pc.getConfig() as ConfigWebPro).getDotNotationUpperCase()
            for (i in _keys.indices) {
                if (oDoIt) {
                    nl(sb)
                    sb.append(',')
                }
                oDoIt = true
                sb.append(StringUtil.escapeJS(if (upperCase) _keys[i].getUpperString() else _keys[i].getString(), '"', charsetEncoder))
                sb.append(":[")
                indentPlus(sb)
                var doIt = false
                for (y in 1..len) {
                    if (doIt) {
                        nl(sb)
                        sb.append(',')
                    }
                    doIt = true
                    try {
                        _serialize(pc, test, query.getAt(_keys[i], y), sb, queryFormat, done)
                    } catch (e: PageException) {
                        _serialize(pc, test, e.getMessage(), sb, queryFormat, done)
                    }
                }
                indentMinus(sb)
                sb.append(']')
            }
            indentMinus(sb)
            sb.append('}')
        } else {
            sb.append('[')
            indentPlus(sb)
            var oDoIt = false
            val len: Int = query.getRecordcount()
            for (row in 1..len) {
                if (oDoIt) {
                    nl(sb)
                    sb.append(',')
                }
                oDoIt = true
                sb.append("[")
                indentPlus(sb)
                var doIt = false
                for (col in _keys.indices) {
                    if (doIt) {
                        nl(sb)
                        sb.append(',')
                    }
                    doIt = true
                    try {
                        _serialize(pc, test, query.getAt(_keys[col], row), sb, queryFormat, done)
                    } catch (e: PageException) {
                        _serialize(pc, test, e.getMessage(), sb, queryFormat, done)
                    }
                }
                indentMinus(sb)
                sb.append(']')
            }
            indentMinus(sb)
            sb.append(']')
        }
        indentMinus(sb)
        sb.append('}')
    }

    /**
     * serialize an Object to his xml Format represenation
     *
     * @param object Object to serialize
     * @param sb StringBuilder to write data
     * @param serializeQueryByColumns
     * @param done
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    private fun _serialize(pc: PageContext?, test: Set?, `object`: Object?, sb: StringBuilder?, queryFormat: Int, done: ObjectIdentityHashSet?) {

        // NULL
        if (`object` == null || `object` === CollectionUtil.NULL) {
            sb.append("null")
            return
        }
        // String
        if (`object` is String || `object` is StringBuilder) {
            sb.append(StringUtil.escapeJS(`object`.toString(), '"', charsetEncoder))
            return
        }
        // TimeZone
        if (`object` is TimeZone) {
            sb.append(StringUtil.escapeJS((`object` as TimeZone?).getID(), '"', charsetEncoder))
            return
        }
        // Locale
        if (`object` is Locale) {
            sb.append(StringUtil.escapeJS(LocaleFactory.toString(`object` as Locale?), '"', charsetEncoder))
            return
        }
        // Character
        if (`object` is Character) {
            sb.append(StringUtil.escapeJS(String.valueOf((`object` as Character?).charValue()), '"', charsetEncoder))
            return
        }
        // Number
        if (`object` is Number) {
            sb.append(Caster.toString(`object` as Number?))
            return
        }
        // Boolean
        if (`object` is Boolean) {
            sb.append(Caster.toString((`object` as Boolean?).booleanValue()))
            return
        }
        // DateTime
        if (`object` is DateTime) {
            _serializeDateTime(`object` as DateTime?, sb)
            return
        }
        // Date
        if (`object` is Date) {
            _serializeDate(`object` as Date?, sb)
            return
        }
        // XML
        if (`object` is Node) {
            _serializeXML(`object` as Node?, sb)
            return
        }
        // Timespan
        if (`object` is TimeSpan) {
            _serializeTimeSpan(`object` as TimeSpan?, sb)
            return
        }
        // File
        if (`object` is File) {
            _serialize(pc, test, (`object` as File?).getAbsolutePath(), sb, queryFormat, done)
            return
        }
        // String Converter
        if (`object` is ScriptConvertable) {
            sb.append((`object` as ScriptConvertable?).serialize())
            return
        }
        // byte[]
        if (`object` is ByteArray) {
            sb.append("\"" + Base64Coder.encode(`object` as ByteArray?).toString() + "\"")
            return
        }
        val raw: Object = LazyConverter.toRaw(`object`)
        if (done.contains(raw)) {
            sb.append("null")
            return
        }
        done.add(raw)
        try {
            // Component
            if (`object` is Component) {
                _serializeComponent(pc, test, `object` as Component?, sb, queryFormat, done)
                return
            }
            // UDF
            if (`object` is UDF) {
                _serializeUDF(pc, test, `object` as UDF?, sb, queryFormat, done)
                return
            }
            // Struct
            if (`object` is Struct) {
                _serializeStruct(pc, test, `object` as Struct?, sb, queryFormat, true, done)
                return
            }
            // Map
            if (`object` is Map) {
                _serializeMap(pc, test, `object` as Map?, sb, queryFormat, done)
                return
            }
            // Array
            if (`object` is Array) {
                _serializeArray(pc, test, `object` as Array?, sb, queryFormat, done)
                return
            }
            // List
            if (`object` is List) {
                _serializeList(pc, test, `object` as List?, sb, queryFormat, done)
                return
            }
            // Query
            if (`object` is Query) {
                _serializeQuery(pc, test, `object` as Query?, sb, queryFormat, done)
                return
            }
            // Native Array
            if (Decision.isNativeArray(`object`)) {
                if (`object` is CharArray) _serialize(pc, test, String(`object` as CharArray?), sb, queryFormat, done) else {
                    _serializeArray(pc, test, ArrayUtil.toReferenceType(`object`, ArrayUtil.OBJECT_EMPTY), sb, queryFormat, done)
                }
                return
            }
            // ObjectWrap
            if (`object` is ObjectWrap) {
                try {
                    _serialize(pc, test, (`object` as ObjectWrap?).getEmbededObject(), sb, queryFormat, done)
                } catch (e: PageException) {
                    if (`object` is JavaObject) {
                        _serializeClass(pc, test, (`object` as JavaObject?).getClazz(), null, sb, queryFormat, done)
                    } else throw ConverterException("can't serialize Object of type [ " + Caster.toClassName(`object`).toString() + " ]")
                }
                return
            }
            _serializeClass(pc, test, `object`.getClass(), `object`, sb, queryFormat, done)
        } finally {
            done.remove(raw)
        }
    }

    private fun _serializeXML(node: Node?, sb: StringBuilder?) {
        var node: Node? = node
        node = XMLCaster.toRawNode(node)
        sb.append(StringUtil.escapeJS(XMLCaster.toString(node, ""), '"', charsetEncoder))
    }

    @Throws(ConverterException::class)
    private fun _serializeTimeSpan(ts: TimeSpan?, sb: StringBuilder?) {
        try {
            sb.append(ts.castToDoubleValue())
        } catch (e: PageException) { // should never happen because TimeSpanImpl does not throw an exception
            throw ConverterException(e.getMessage())
        }
    }

    /**
     * serialize an Object to his literal Format
     *
     * @param object Object to serialize
     * @param serializeQueryByColumns
     * @return serialized wddx package
     * @throws ConverterException
     */
    @Throws(ConverterException::class)
    fun serialize(pc: PageContext?, `object`: Object?, queryFormat: Int): String? {
        val sb = StringBuilder(256)
        _serialize(pc, null, `object`, sb, queryFormat, ObjectIdentityHashSet())
        return sb.toString()
    }

    @Override
    @Throws(ConverterException::class, IOException::class)
    fun writeOut(pc: PageContext?, source: Object?, writer: Writer?) {
        writer.write(serialize(pc, source, SerializationSettings.SERIALIZE_AS_ROW))
        writer.flush()
    }

    private fun indentPlus(sb: StringBuilder?) {
        if (!multiline) return
        indent++
        nl(sb)
    }

    private fun indentMinus(sb: StringBuilder?) {
        if (!multiline) return
        indent--
        nl(sb)
    }

    private fun nl(sb: StringBuilder?) {
        if (!multiline) return
        sb.append(NL)
        for (i in 0 until indent) {
            sb.append('	')
        }
    }

    companion object {
        private val REMOTING_FETCH: Collection.Key? = KeyImpl.getInstance("remotingFetch")
        private val TO_JSON: Key? = KeyImpl.getInstance("_toJson")
        private val NULL_STRING: String? = ""
        private val NL: String? = "\n"
        @Throws(ConverterException::class)
        private fun castToJson(pc: PageContext?, c: Component?, defaultValue: String?): String? {
            val o: Object = c.get(TO_JSON, null) as? UDF ?: return defaultValue
            val udf: UDF = o as UDF
            return if (udf.getReturnType() !== CFTypes.TYPE_VOID && udf.getFunctionArguments().length === 0) {
                try {
                    Caster.toString(c.call(pc, TO_JSON, arrayOfNulls<Object?>(0)))
                } catch (e: PageException) {
                    throw toConverterException(e)
                }
            } else defaultValue
        }

        @Throws(ConverterException::class)
        fun serialize(pc: PageContext?, o: Object?): String? {
            val converter = JSONConverter(false, null)
            return converter.serialize(pc, o, SerializationSettings.SERIALIZE_AS_ROW)
        }

        fun toQueryFormat(options: Object?, defaultValue: Int): Int {
            val b: Boolean = Caster.toBoolean(options, null)
            if (Boolean.TRUE.equals(b)) return SerializationSettings.SERIALIZE_AS_COLUMN
            if (Boolean.FALSE.equals(b)) return SerializationSettings.SERIALIZE_AS_ROW
            val str: String = Caster.toString(options, null)
            if ("row".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_ROW
            if ("col".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_COLUMN
            if ("column".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_COLUMN
            return if ("struct".equalsIgnoreCase(str)) SerializationSettings.SERIALIZE_AS_STRUCT else defaultValue
        }
    }

    init {
        charsetEncoder = if (charset != null) charset.newEncoder() else null // .canEncode("string");
        this.pattern = pattern
        _preserveCase = preserveCase
        this.multiline = multiline
        this.commentName = if (StringUtil.isEmpty(commentName)) null else KeyImpl.init(commentName)
    }
}