/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.net.http

import java.io.IOException

/**
 * Client to implement http based webservice
 */
class HTTPClient(httpUrl: String?, username: String?, password: String?, proxyData: ProxyData?) : Objects, Iteratorable {
    // private static final String USER_AGENT = ;
    private var metaURL: URL? = null
    private val username: String?
    private val password: String?
    private val proxyData: ProxyData?
    private var url: URL? = null
    private var meta: Struct? = null
    private var argumentsCollectionFormat = -1
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return try {
            var args: Array
            val sct: Struct? = getMetaData(pageContext)
            var `val`: Struct
            var a: Struct
            val cfc = DumpTable("udf", "#66ccff", "#ccffff", "#000000")
            var udf: DumpTable?
            var arg: DumpTable?
            cfc.setTitle("Web Service (HTTP)")
            if (dp.getMetainfo()) cfc.setComment(url.toExternalForm())
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            // Loop UDFs
            while (it.hasNext()) {
                e = it.next()
                `val` = Caster.toStruct(e.getValue())

                // udf name
                udf = DumpTable("udf", "#66ccff", "#ccffff", "#000000")
                arg = DumpTable("udf", "#66ccff", "#ccffff", "#000000")
                cfc.appendRow(1, SimpleDumpData(e.getKey().getString()), udf)

                // args
                args = Caster.toArray(`val`.get(KeyConstants._arguments))
                udf.appendRow(1, SimpleDumpData("arguments"), arg)
                arg.appendRow(7, SimpleDumpData("name"), SimpleDumpData("required"), SimpleDumpData("type"))
                val ait: Iterator<Object?> = args.valueIterator()
                while (ait.hasNext()) {
                    a = Caster.toStruct(ait.next())
                    arg.appendRow(0, SimpleDumpData(Caster.toString(a.get(KeyConstants._name))), SimpleDumpData(Caster.toString(a.get(KeyConstants._required))),
                            SimpleDumpData(Caster.toString(a.get(KeyConstants._type))))
                }

                // return type
                udf.appendRow(1, SimpleDumpData("return type"), SimpleDumpData(Caster.toString(`val`.get(KeyConstants._returntype))))

                /*
				 * cfc.appendRow(new DumpRow(0,new DumpData[]{ new SimpleDumpData(arg.getDisplayName()), new
				 * SimpleDumpData(e.getKey().getString()), new SimpleDumpData(arg.isRequired()), new
				 * SimpleDumpData(arg.getTypeAsString()), def, new SimpleDumpData(arg.getHint())}));
				 */
            }
            cfc
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw PageRuntimeException(Caster.toPageException(t))
        }
    }

    private fun getMetaData(pc: PageContext?): Struct? {
        var pc: PageContext? = pc
        if (meta == null) {
            pc = ThreadLocalPageContext.get(pc)
            var `is`: InputStream? = null
            var rsp: HTTPResponse? = null
            try {
                rsp = HTTPEngine.get(metaURL, username, password, 5000, false, "UTF-8", createUserAgent(pc), proxyData, null)
                val mt: MimeType = getMimeType(rsp, null)
                val format: Int = MimeType.toFormat(mt, -1)
                if (format == -1) throw ApplicationException("cannot convert response with mime type [$mt] to a CFML Object")
                `is` = rsp.getContentAsStream()
                val data: Struct = Caster.toStruct(ReqRspUtil.toObject(pc, IOUtil.toBytes(`is`, false), format, mt.getCharset(), null))
                val oUDF: Object = data.get(KeyConstants._functions, null)
                val oAACF: Object = data.get(ComponentPageImpl.ACCEPT_ARG_COLL_FORMATS, null)
                if (oUDF != null && oAACF != null) {
                    meta = Caster.toStruct(oUDF)
                    val strFormats: Array<String?> = ListUtil.listToStringArray(Caster.toString(oAACF), ',')
                    argumentsCollectionFormat = UDFUtil.toReturnFormat(strFormats, UDF.RETURN_FORMAT_JSON)
                } else {
                    meta = data
                }
            } catch (t: Throwable) {
                throw PageRuntimeException(Caster.toPageException(t))
            } finally {
                try {
                    IOUtil.close(`is`)
                } catch (e: IOException) {
                    throw PageRuntimeException(Caster.toPageException(e))
                }
                HTTPEngine.closeEL(rsp)
            }
        }
        return meta
    }

    private fun createUserAgent(pc: PageContext?): String? {
        val i: Info = CFMLEngineFactory.getInstance().getInfo()
        return Constants.NAME.toString() + " " + i.getVersion()
    }

    @Override
    fun keyIterator(): Iterator<Key?>? {
        return try {
            getMetaData(null).keyIterator()
        } catch (e: Exception) {
            KeyIterator(arrayOfNulls<Collection.Key?>(0))
        }
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, arguments: Array<Object?>?): Object? {
        checkFunctionExistence(pc, methodName, false)
        if (arguments!!.size == 0) return _callWithNamedValues(pc, methodName, StructImpl())
        val m: Struct? = checkFunctionExistence(pc, methodName, true)
        var args: Array? = Caster.toArray(m.get(KeyConstants._arguments, null), null)
        if (args == null) args = ArrayImpl()
        val sct: Struct = StructImpl()
        var el: Struct
        var name: String
        for (i in arguments.indices) {
            if (args.size() > i) {
                el = Caster.toStruct(args.get(i + 1, null), null)
                if (el != null) {
                    name = Caster.toString(el.get(KeyConstants._name, null), null)
                    if (!StringUtil.isEmpty(name)) {
                        sct.set(name, arguments[i])
                        continue
                    }
                }
            }
            sct.set("arg" + (i + 1), arguments[i])
        }
        return _callWithNamedValues(pc, methodName, sct)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        checkFunctionExistence(pc, methodName, false)
        return _callWithNamedValues(pc, methodName, args)
    }

    @Throws(PageException::class)
    private fun _callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {

        // prepare request
        val formfields: Map<String?, String?> = HashMap<String?, String?>()
        formfields.put("method", methodName.getString())
        formfields.put("returnformat", "cfml")
        val str: String
        try {
            if (UDF.RETURN_FORMAT_JSON === argumentsCollectionFormat) {
                val cs: Charset = pc.getWebCharset()
                str = JSONConverter(true, cs).serialize(pc, args, SerializationSettings.SERIALIZE_AS_ROW)
                formfields.put("argumentCollectionFormat", "json")
            } else if (UDF.RETURN_FORMAT_SERIALIZE === argumentsCollectionFormat) {
                str = ScriptConverter().serialize(args)
                formfields.put("argumentCollectionFormat", "cfml")
            } else {
                str = ScriptConverter().serialize(args) // Json interpreter also accepts cfscript
            }
        } catch (e: ConverterException) {
            throw Caster.toPageException(e)
        }

        // add aparams to request
        formfields.put("argumentCollection", str)
        /*
		 * Iterator<Entry<Key, Object>> it = args.entryIterator(); Entry<Key, Object> e;
		 * while(it.hasNext()){ e = it.next(); formfields.put(e.getKey().getString(),
		 * Caster.toString(e.getValue())); }
		 */
        val headers: Map<String?, String?> = HashMap<String?, String?>()
        headers.put("accept", "application/cfml,application/json") // application/java disabled for the moment, it is not working when we have different tachyon versions
        var rsp: HTTPResponse? = null
        var `is`: InputStream? = null
        return try {
            // call remote cfc
            rsp = HTTPEngine.post(url, username, password, -1, false, "UTF-8", createUserAgent(pc), proxyData, headers, formfields)

            // read result
            val rspHeaders: Array<Header?> = rsp.getAllHeaders()
            val mt: MimeType = getMimeType(rspHeaders, null)
            val format: Int = MimeType.toFormat(mt, -1)
            if (format == -1) {
                if (rsp.getStatusCode() !== 200) {
                    var hasMsg = false
                    var msg: String = rsp.getStatusText()
                    for (i in rspHeaders.indices) {
                        if (rspHeaders[i].getName().equalsIgnoreCase("exception-message")) {
                            msg = rspHeaders[i].getValue()
                            hasMsg = true
                        }
                    }
                    `is` = rsp.getContentAsStream()
                    val ae = ApplicationException("remote component throws the following error:$msg")
                    if (!hasMsg) ae.setAdditional(KeyImpl.getInstance("respone-body"), IOUtil.toString(`is`, mt.getCharset()))
                    throw ae
                }
                throw ApplicationException("cannot convert response with mime type [$mt] to a CFML Object")
            }
            `is` = rsp.getContentAsStream()
            ReqRspUtil.toObject(pc, IOUtil.toBytes(`is`, false), format, mt.getCharset(), null)
        } catch (ioe: IOException) {
            throw Caster.toPageException(ioe)
        } finally {
            try {
                IOUtil.close(`is`)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
            HTTPEngine.closeEL(rsp)
        }
    }

    @Throws(ApplicationException::class)
    private fun checkFunctionExistence(pc: PageContext?, methodName: Key?, getDataFromRemoteIfNecessary: Boolean): Struct? {
        if (getDataFromRemoteIfNecessary) getMetaData(pc)
        return if (meta == null) null else Caster.toStruct(meta.get(methodName, null), null)
                ?: throw ApplicationException("the remote component has no function with name [$methodName]",
                        ExceptionUtil.createSoundexDetail(methodName.getString(), meta.keysAsStringIterator(), "functions"))
    }

    private fun getMimeType(rsp: HTTPResponse?, defaultValue: MimeType?): MimeType? {
        return getMimeType(rsp.getAllHeaders(), defaultValue)
    }

    private fun getMimeType(headers: Array<Header?>?, defaultValue: MimeType?): MimeType? {
        var returnFormat: String? = null
        var contentType: String? = null
        for (i in headers.indices) {
            if (headers!![i].getName().equalsIgnoreCase("Return-Format")) returnFormat = headers[i].getValue() else if (headers[i].getName().equalsIgnoreCase("Content-Type")) contentType = headers[i].getValue()
        }
        var rf: MimeType? = null
        var ct: MimeType? = null

        // return format
        if (!StringUtil.isEmpty(returnFormat)) {
            val format: Int = UDFUtil.toReturnFormat(returnFormat, -1)
            rf = MimeType.toMimetype(format, null)
        }
        // ContentType
        if (!StringUtil.isEmpty(contentType)) {
            ct = MimeType.getInstance(contentType)
        }
        if (rf != null && ct != null) {
            return if (rf.same(ct)) ct else rf // because this has perhaps a charset definition
        }
        if (rf != null) return rf
        return if (ct != null) ct else defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return call(pc, KeyImpl.init("get" + key.getString()), ArrayUtil.OBJECT_EMPTY)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return try {
            call(pc, KeyImpl.init("get" + StringUtil.ucFirst(key.getString())), ArrayUtil.OBJECT_EMPTY)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        return call(pc, KeyImpl.init("set" + propertyName.getString()), arrayOf<Object?>(value))
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        return try {
            call(pc, KeyImpl.init("set" + propertyName.getString()), arrayOf<Object?>(value))
        } catch (e: PageException) {
            null
        }
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return KeyAsStringIterator(keyIterator())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return ObjectsIterator(keyIterator(), this)
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return ObjectsEntryIterator(keyIterator(), this)
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw RPCException("can't cast Webservice to a string")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw RPCException("can't cast Webservice to a boolean")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw RPCException("can't cast Webservice to a number")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(RPCException::class)
    fun castToDateTime(): DateTime? {
        throw RPCException("can't cast Webservice to a Date Object")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare Webservice Object with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Webservice Object with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Webservice Object with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Webservice Object with a String")
    }

    companion object {
        private const val serialVersionUID = -7920478535030737537L
    }

    init {
        try {
            url = HTTPUtil.toURL(httpUrl, HTTPUtil.ENCODED_AUTO)
            if (!StringUtil.isEmpty(url.getQuery())) throw ApplicationException("invalid url, query string is not allowed as part of the call")
            metaURL = HTTPUtil.toURL(url.toExternalForm().toString() + "?cfml", HTTPUtil.ENCODED_AUTO)
        } catch (e: MalformedURLException) {
            throw Caster.toPageException(e)
        }
        this.username = username
        this.password = password
        this.proxyData = proxyData
    }
}