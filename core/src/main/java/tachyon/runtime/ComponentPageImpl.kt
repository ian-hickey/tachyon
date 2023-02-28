/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime

import java.io.ByteArrayInputStream

/**
 * A Page that can produce Components
 */
abstract class ComponentPageImpl : ComponentPage(), PagePro {
    private var lastCheck: Long = -1
    private var staticScope: StaticScope? = null
    private var index: Long = 0
    @Throws(tachyon.runtime.exp.PageException::class)
    abstract fun newInstance(pc: PageContext?, callPath: String?, isRealPath: Boolean, isExtendedComponent: Boolean, executeConstr: Boolean): ComponentImpl?

    @Override
    override fun getHash(): Int {
        return 0
    }

    @Override
    override fun getSourceLength(): Long {
        return 0
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?): Object? {
        // remote persistent (only type server is supported)
        var strRemotePersisId: String? = Caster.toString(getURLorForm(pc, REMOTE_PERSISTENT_ID, null), null) // Caster.toString(pc.urlFormScope().get(REMOTE_PERSISTENT_ID,null),null);
        if (!StringUtil.isEmpty(strRemotePersisId, true)) {
            strRemotePersisId = strRemotePersisId.trim()
        } else strRemotePersisId = null
        val req: HttpServletRequest = pc.getHttpServletRequest()
        // client
        val client: String = Caster.toString(req.getAttribute("client"), null)
        // call type (invocation, store-only)
        val callType: String = Caster.toString(req.getAttribute("call-type"), null)
        val internalCall = "tachyon-gateway-1-0".equals(client) || "tachyon-listener-1-0".equals(client)
        val fromRest = "tachyon-rest-1-0".equals(client)
        var component: Component?
        try {
            pc.setSilent()
            // load the cfc
            try {
                if (internalCall && strRemotePersisId != null) {
                    val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
                    val engine: GatewayEngineImpl = config.getGatewayEngine() as GatewayEngineImpl
                    component = engine.getPersistentRemoteCFC(strRemotePersisId)
                    if (component == null) {
                        component = newInstance(pc, getComponentName(), false, false, true)
                        if (!internalCall) component = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_REMOTE, component)
                        engine.setPersistentRemoteCFC(strRemotePersisId, component)
                    }
                } else {
                    component = newInstance(pc, getComponentName(), false, false, true)
                    if (!internalCall) component = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_REMOTE, component)
                }
            } finally {
                pc.unsetSilent()
            }

            // Only get the Component, no invocation
            if ("store-only".equals(callType)) {
                req.setAttribute("component", component)
                return null
            }

            // METHOD INVOCATION
            val qs: String = ReqRspUtil.getQueryString(pc.getHttpServletRequest())
            if (pc.getBasePageSource() === this.getPageSource() && pc.getConfig().debug()) pc.getDebugger().setOutput(false)
            val isPost: Boolean = pc.getHttpServletRequest().getMethod().equalsIgnoreCase("POST")
            val suppressContent = pc.getRequestDialect() === CFMLEngine.DIALECT_LUCEE || (pc as PageContextImpl?)!!.getSuppressContent()
            if (suppressContent) pc.clear()
            if (fromRest) {
                callRest(pc, component, Caster.toString(req.getAttribute("rest-path"), ""), req.getAttribute("rest-result") as Result, suppressContent)
                return null
            }
            var method: Object?

            // POST
            if (isPost) {
                // Soap
                if (isSoap(pc)) {
                    callWebservice(pc, component)
                    // close(pc);
                    return null
                } else if (getURLorForm(pc, KeyConstants._method, null).also { method = it } != null) {
                    callWDDX(pc, component, KeyImpl.toKey(method), suppressContent)
                    // close(pc);
                    return null
                }
            } else {
                // WSDL
                if (qs != null && (qs.trim().equalsIgnoreCase("wsdl") || qs.trim().startsWith("wsdl&"))) {
                    callWSDL(pc, component)
                    // close(pc);
                    return null
                } else if (getURLorForm(pc, KeyConstants._method, null).also { method = it } != null) {
                    callWDDX(pc, component, KeyImpl.toKey(method), suppressContent)
                    // close(pc);
                    return null
                }
                if (qs != null) {
                    val rf: Int = UDFUtil.toReturnFormat(qs.trim(), -1)
                    if (rf != -1) callCFCMetaData(pc, component, rf)
                    // close(pc);
                    return null
                }
            }

            // Include MUST
            val path: Array = pc.getTemplatePath()
            // if(path.size()>1 ) {
            if (path.size() > 1 && !(path.size() === 3 && ListUtil.last(path.getE(2).toString(), "/\\", true)
                            .equalsIgnoreCase(if (pc.getRequestDialect() === CFMLEngine.DIALECT_CFML) tachyon.runtime.config.Constants.CFML_APPLICATION_EVENT_HANDLER else tachyon.runtime.config.Constants.LUCEE_APPLICATION_EVENT_HANDLER))) { // MUSTMUST
                // bad
                // impl
                // ->
                // check
                // with
                // and
                // without
                // application
                // .
                // cfc
                val c: ComponentSpecificAccess = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, component)
                val keys: Array<Key?> = c!!.keys()
                var el: Object
                val `var`: Scope = pc.variablesScope()
                for (i in keys.indices) {
                    el = c.get(keys[i], null)
                    if (el is UDF) `var`.set(keys[i], el)
                }
                return null
            }

            // DUMP
            if (!req.getServletPath().equalsIgnoreCase("/Web." + if (pc.getRequestDialect() === CFMLEngine.DIALECT_CFML) tachyon.runtime.config.Constants.getCFMLComponentExtension() else tachyon.runtime.config.Constants.getTachyonComponentExtension())) {
                val cdf: String = pc.getConfig().getComponentDumpTemplate()
                if (cdf != null && cdf.trim().length() > 0) {
                    pc.variablesScope().set(KeyConstants._component, component)
                    pc.doInclude(cdf, false)
                } else pc.write(pc.getConfig().getDefaultDumpWriter(DumpWriter.DEFAULT_RICH).toString(pc, component.toDumpData(pc, 9999, DumpUtil.toDumpProperties()), true))
            }
        } catch (t: Throwable) {
            throw Caster.toPageException(t) // Exception Handler.castAnd
            // Stack(t, this, pc);
        }
        return null
    }

    private fun getURLorForm(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        val res: Object = pc.formScope().get(key, null)
        return if (res != null) res else pc.urlScope().get(key, defaultValue)
    }

    @Throws(PageException::class, IOException::class, ConverterException::class)
    private fun callRest(pc: PageContext?, component: Component?, path: String?, result: Result?, suppressContent: Boolean) {
        val method: String = pc.getHttpServletRequest().getMethod()
        val subPath: Array<String?> = result.getPath()
        val cMeta: Struct
        cMeta = try {
            component.getMetaData(pc)
        } catch (pe: PageException) {
            throw ExceptionUtil.toIOException(pe)
        }

        // Consumes
        var cConsumes: Array<MimeType?>? = null
        var strMimeType: String = Caster.toString(cMeta.get(KeyConstants._consumes, null), null)
        if (!StringUtil.isEmpty(strMimeType, true)) {
            cConsumes = MimeType.getInstances(strMimeType, ',')
        }

        // Produces
        var cProduces: Array<MimeType?>? = null
        strMimeType = Caster.toString(cMeta.get(KeyConstants._produces, null), null)
        if (!StringUtil.isEmpty(strMimeType, true)) {
            cProduces = MimeType.getInstances(strMimeType, ',')
        }
        val it: Iterator<Entry<Key?, Object?>?> = component.entryIterator()
        var e: Entry<Key?, Object?>?
        var value: Object
        var udf: UDF
        var meta: Struct
        var status = 404
        var bestP: MimeType?
        var bestC: MimeType?
        while (it.hasNext()) {
            e = it.next()
            value = e.getValue()
            if (value is UDF) {
                udf = value as UDF
                try {
                    meta = udf.getMetaData(pc)

                    // check if http method match
                    val httpMethod: String = Caster.toString(meta.get(KeyConstants._httpmethod, null), null)
                    if (StringUtil.isEmpty(httpMethod) || !httpMethod.equalsIgnoreCase(method)) continue

                    // get consumes mimetype
                    var consumes: Array<MimeType?>?
                    strMimeType = Caster.toString(meta.get(KeyConstants._consumes, null), null)
                    consumes = if (!StringUtil.isEmpty(strMimeType, true)) {
                        MimeType.getInstances(strMimeType, ',')
                    } else cConsumes

                    // get produces mimetype
                    var produces: Array<MimeType?>?
                    strMimeType = Caster.toString(meta.get(KeyConstants._produces, null), null)
                    produces = if (!StringUtil.isEmpty(strMimeType, true)) {
                        MimeType.getInstances(strMimeType, ',')
                    } else cProduces
                    val restPath: String = Caster.toString(meta.get(KeyConstants._restPath, null), null)

                    // no rest path
                    if (StringUtil.isEmpty(restPath)) {
                        if (ArrayUtil.isEmpty(subPath)) {
                            bestC = best(consumes, result.getContentType())
                            bestP = best(produces, result.getAccept())
                            if (bestC == null) status = 405 else if (bestP == null) status = 406 else {
                                status = 200
                                _callRest(pc, component, udf, path, result.getVariables(), result, bestP, produces, suppressContent, e.getKey())
                                break
                            }
                        }
                    } else {
                        val `var`: Struct = result.getVariables()
                        val index: Int = RestUtil.matchPath(`var`, Path.init(restPath) /* TODO cache this */, result.getPath())
                        if (index >= 0 && index + 1 == result.getPath().length) {
                            bestC = best(consumes, result.getContentType())
                            bestP = best(produces, result.getAccept())
                            if (bestC == null) status = 405 else if (bestP == null) status = 406 else {
                                status = 200
                                _callRest(pc, component, udf, path, `var`, result, bestP, produces, suppressContent, e.getKey())
                                break
                            }
                        }
                    }
                } catch (pe: PageException) {
                    ThreadLocalPageContext.getLog(pc, "rest").error("REST", pe)
                    throw pe
                }
            }
        }
        if (status == 404) {
            RestUtil.setStatus(pc, 404, "no rest service for [" + HTMLEntities.escapeHTML(path).toString() + "] found")
            ThreadLocalPageContext.getLog(pc, "rest").error("REST", "404; no rest service for [$path] found")
        } else if (status == 405) {
            RestUtil.setStatus(pc, 405, "Unsupported Media Type")
            ThreadLocalPageContext.getLog(pc, "rest").error("REST", "405; Unsupported Media Type")
        } else if (status == 406) {
            RestUtil.setStatus(pc, 406, "Not Acceptable")
            ThreadLocalPageContext.getLog(pc, "rest").error("REST", "406; Not Acceptable")
        }
    }

    private fun best(produces: Array<MimeType?>?, vararg accept: MimeType?): MimeType? {
        if (ArrayUtil.isEmpty(produces)) {
            return if (accept.size > 0) accept[0] else MimeType.ALL
        }
        var best: MimeType? = null
        var tmp: MimeType?
        for (a in 0 until accept.size) {
            tmp = accept[a].bestMatch(produces)
            if (tmp != null && !accept[a].hasWildCards() && tmp.hasWildCards()) {
                tmp = accept[a]
            }
            if (tmp != null && (best == null || best.getQuality() < tmp.getQuality() || best.getQuality() === tmp.getQuality() && best.hasWildCards() && !tmp.hasWildCards())) best = tmp
        }
        return best
    }

    @Throws(PageException::class, IOException::class, ConverterException::class)
    private fun _callRest(pc: PageContext?, component: Component?, udf: UDF?, path: String?, variables: Struct?, result: Result?, best: MimeType?, produces: Array<MimeType?>?, suppressContent: Boolean,
                          methodName: Key?) {
        val fa: Array<FunctionArgument?> = udf.getFunctionArguments()
        val args: Struct = StructImpl()
        var meta: Struct
        var name: Key
        var restArgName: String
        var restArgSource: String
        var value: String
        for (i in fa.indices) {
            name = fa[i].getName()
            meta = fa[i].getMetaData()
            restArgSource = if (meta == null) "" else Caster.toString(meta.get(KeyConstants._restArgSource, ""), "")
            if ("path".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, variables.get(name, null))
            if ("query".equalsIgnoreCase(restArgSource) || "url".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, pc.urlScope().get(name, null))
            if ("form".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, pc.formScope().get(name, null))
            if ("cookie".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, pc.cookieScope().get(name, null))
            if ("header".equalsIgnoreCase(restArgSource) || "head".equalsIgnoreCase(restArgSource)) {
                restArgName = if (meta == null) "" else Caster.toString(meta.get(KeyConstants._restArgName, ""), "")
                if (StringUtil.isEmpty(restArgName)) restArgName = name.getString()
                value = ReqRspUtil.getHeaderIgnoreCase(pc, restArgName, null)
                setValue(fa[i], args, name, value)
            }
            if ("matrix".equalsIgnoreCase(restArgSource)) setValue(fa[i], args, name, result.getMatrix().get(name, null))
            if ("body".equalsIgnoreCase(restArgSource) || StringUtil.isEmpty(restArgSource, true)) {
                val isSimple: Boolean = CFTypes.isSimpleType(fa[i].getType())
                var body: Object = ReqRspUtil.getRequestBody(pc, true, null)
                if (isSimple && !Decision.isSimpleValue(body)) body = ReqRspUtil.getRequestBody(pc, false, null)
                setValue(fa[i], args, name, body)
            }
        }
        var rtn: Object? = null
        rtn = try {
            if (suppressContent) pc.setSilent()
            component.callWithNamedValues(pc, methodName, args)
        } catch (e: PageException) {
            RestUtil.setStatus(pc, 500, ExceptionUtil.getMessage(e))
            ThreadLocalPageContext.getLog(pc, "rest").error("REST", e)
            throw e
        } finally {
            if (suppressContent) pc.unsetSilent()
        }

        // custom response
        val sct: Struct = result.getCustomResponse()
        var hasContent = false
        if (sct != null) {
            val rsp: HttpServletResponse = pc.getHttpServletResponse()
            // status
            val status: Int = Caster.toIntValue(sct.get(KeyConstants._status, Constants.DOUBLE_ZERO), 0)
            if (status > 0) rsp.setStatus(status)

            // content
            val o: Object = sct.get(KeyConstants._content, null)
            if (o != null) {
                val content: String = Caster.toString(o, null)
                if (content != null) {
                    try {
                        pc.forceWrite(content)
                        hasContent = true
                    } catch (e: IOException) {
                    }
                }
            }

            // headers
            val headers: Struct = Caster.toStruct(sct.get(KeyConstants._headers, null), null)
            if (headers != null) {
                // Key[] keys = headers.keys();
                val it: Iterator<Entry<Key?, Object?>?> = headers.entryIterator()
                var e: Entry<Key?, Object?>?
                var n: String
                var v: String
                var tmp: Object
                while (it.hasNext()) {
                    e = it.next()
                    n = e.getKey().getString()
                    tmp = e.getValue()
                    v = Caster.toString(tmp, null)
                    if (tmp != null && v == null) v = tmp.toString()
                    rsp.setHeader(n, v)
                }
            }
        }

        // convert result
        if (rtn != null && !hasContent) {
            val props = Props()
            props.format = result.getFormat()
            val cs: Charset? = getCharset(pc)
            if (result.hasFormatExtension()) {
                // setFormat(pc.getHttpServletResponse(), props.format,cs);
                _writeOut(pc, props, null, rtn, cs, true)
            } else {
                if (best != null && !MimeType.ALL.same(best)) {
                    val f: Int = MimeType.toFormat(best, -1)
                    if (f != -1) {
                        props.format = f
                        // setFormat(pc.getHttpServletResponse(), f,cs);
                        _writeOut(pc, props, null, rtn, cs, true)
                    } else {
                        writeOut(pc, props, rtn, best)
                    }
                } else {
                    _writeOut(pc, props, null, rtn, cs, true)
                }
            }
        }
    }

    private operator fun setValue(fa: FunctionArgument?, args: Struct?, name: Key?, value: Object?) {
        var value: Object? = value
        if (value == null) {
            val meta: Struct = fa.getMetaData()
            if (meta != null) value = meta.get(KeyConstants._default, null)
        }
        args.setEL(name, value)
    }

    @Throws(PageException::class, IOException::class, ConverterException::class)
    private fun writeOut(pc: PageContext?, props: Props?, obj: Object?, mt: MimeType?) {
        // TODO miemtype mapping with converter defintion from external file
        // Images
        /*
		 * if (mt.same(MimeType.IMAGE_GIF)) writeOut(pc, obj, mt, new ImageConverter("gif")); else if
		 * (mt.same(MimeType.IMAGE_JPG)) writeOut(pc, obj, mt, new ImageConverter("jpeg")); else if
		 * (mt.same(MimeType.IMAGE_PNG)) writeOut(pc, obj, mt, new ImageConverter("png")); else if
		 * (mt.same(MimeType.IMAGE_TIFF)) writeOut(pc, obj, mt, new ImageConverter("tiff")); else if
		 * (mt.same(MimeType.IMAGE_BMP)) writeOut(pc, obj, mt, new ImageConverter("bmp")); else if
		 * (mt.same(MimeType.IMAGE_WBMP)) writeOut(pc, obj, mt, new ImageConverter("wbmp")); else if
		 * (mt.same(MimeType.IMAGE_FBX)) writeOut(pc, obj, mt, new ImageConverter("fbx")); else if
		 * (mt.same(MimeType.IMAGE_FBX)) writeOut(pc, obj, mt, new ImageConverter("fbx")); else if
		 * (mt.same(MimeType.IMAGE_PNM)) writeOut(pc, obj, mt, new ImageConverter("pnm")); else if
		 * (mt.same(MimeType.IMAGE_PGM)) writeOut(pc, obj, mt, new ImageConverter("pgm")); else if
		 * (mt.same(MimeType.IMAGE_PBM)) writeOut(pc, obj, mt, new ImageConverter("pbm")); else if
		 * (mt.same(MimeType.IMAGE_ICO)) writeOut(pc, obj, mt, new ImageConverter("ico")); else if
		 * (mt.same(MimeType.IMAGE_PSD)) writeOut(pc, obj, mt, new ImageConverter("psd")); else if
		 * (mt.same(MimeType.IMAGE_ASTERIX)) writeOut(pc, obj, MimeType.IMAGE_PNG, new
		 * ImageConverter("png"));
		 */
        // Application
        if (mt.same(MimeType.APPLICATION_JAVA)) writeOut(pc, obj, mt, JavaConverter()) else _writeOut(pc, props, null, obj, null, true)
    }

    @Throws(PageException::class)
    private fun callWDDX(pc: PageContext?, component: Component?, methodName: Collection.Key?, suppressContent: Boolean) {
        try {
            // Struct url = StructUtil.duplicate(pc.urlFormScope(),true);
            var url: Struct? = StructUtil.merge(arrayOf<Struct?>(pc.formScope(), pc.urlScope()))
            // define args
            url.removeEL(KeyConstants._fieldnames)
            url.removeEL(KeyConstants._method)
            var args: Object = url.get(KeyConstants._argumentCollection, null)
            var strArgCollFormat: String = Caster.toString(url.get("argumentCollectionFormat", null), null)

            // url.returnFormat
            var urlReturnFormat = -1
            val oReturnFormatFromURL: Object = url.get(KeyConstants._returnFormat, null)
            if (oReturnFormatFromURL != null) urlReturnFormat = UDFUtil.toReturnFormat(Caster.toString(oReturnFormatFromURL, null), -1)

            // request header "accept"
            val accept: List<MimeType?> = ReqRspUtil.getAccept(pc)
            val headerReturnFormat: Int = MimeType.toFormat(accept, UDF.RETURN_FORMAT_XML, -1)
            val queryFormat: Object = url.get(KeyConstants._queryFormat, null)
            if (args == null) {
                args = pc.getHttpServletRequest().getAttribute("argumentCollection")
            }
            if (StringUtil.isEmpty(strArgCollFormat)) {
                strArgCollFormat = Caster.toString(pc.getHttpServletRequest().getAttribute("argumentCollectionFormat"), null)
            }

            // content-type
            val cs: Charset? = getCharset(pc)
            var o: Object = component.get(pc, methodName, null)

            // onMissingMethod
            if (o == null) o = component.get(pc, KeyConstants._onmissingmethod, null)
            val props = getProps(pc, o, urlReturnFormat, headerReturnFormat)
            // if(!props.output)
            setFormat(pc.getHttpServletResponse(), props!!.format, cs)
            var rtn: Object? = null
            try {
                if (suppressContent) pc.setSilent()
                if (args == null) {
                    url = translate(component, methodName.getString(), url)
                    rtn = component.callWithNamedValues(pc, methodName, url)
                } else if (args is String) {
                    val str = args as String
                    val format: Int = UDFUtil.toReturnFormat(strArgCollFormat, -1)

                    // CFML
                    if (UDF.RETURN_FORMAT_SERIALIZE === format) {
                        // do not catch exception when format is defined
                        args = CFMLExpressionInterpreter().interpret(pc, str)
                    }
                    // JSON
                    if (UDF.RETURN_FORMAT_JSON === format) {
                        // do not catch exception when format is defined
                        args = JSONExpressionInterpreter(false).interpret(pc, str)
                    } else {
                        // catch exception when format is not defined, then in
                        // this case the string can also be a simple argument
                        try {
                            args = JSONExpressionInterpreter(false).interpret(pc, str)
                        } catch (pe: PageException) {
                            try {
                                args = CFMLExpressionInterpreter().interpret(pc, str)
                            } catch (_pe: PageException) {
                            }
                        }
                    }
                }

                // call
                if (args != null) {
                    if (Decision.isCastableToStruct(args)) {
                        rtn = component.callWithNamedValues(pc, methodName, Caster.toStruct(args, false))
                    } else if (Decision.isCastableToArray(args)) {
                        rtn = component.call(pc, methodName, Caster.toNativeArray(args))
                    } else {
                        val ac: Array<Object?> = arrayOfNulls<Object?>(1)
                        ac[0] = args
                        rtn = component.call(pc, methodName, ac)
                    }
                }
            } finally {
                if (suppressContent) pc.unsetSilent()
            }
            // convert result
            if (rtn != null) {
                if (pc.getHttpServletRequest().getHeader("AMF-Forward") != null) {
                    pc.variablesScope().setEL("AMF-Forward", rtn)
                } else {
                    _writeOut(pc, props, queryFormat, rtn, cs, false)
                }
            }
        } catch (t: Throwable) {
            val pe: PageException = Caster.toPageException(t)
            if (pc.getConfig().debug()) pe.setExposeMessage(true)
            throw pe
        }
    }

    @Throws(IOException::class, PageException::class, ConverterException::class)
    private fun callCFCMetaData(pc: PageContext?, cfc: Component?, format: Int) {
        val cw = ComponentSpecificAccess(Component.ACCESS_REMOTE, cfc)
        val scope: ComponentScope = cw!!.getComponentScope()
        val udfs: Struct = StructImpl()
        var sctUDF: Struct?
        var sctArg: Struct?
        var arrArg: Array?
        val it: Iterator<Object?> = scope.valueIterator()
        var v: Object?
        var udf: UDF?
        var args: Array<FunctionArgument?>
        while (it.hasNext()) {
            v = it.next()
            // UDF
            if (v is UDF) {
                udf = v as UDF?
                sctUDF = StructImpl()
                arrArg = ArrayImpl()
                udfs.setEL(udf.getFunctionName(), sctUDF)
                args = udf.getFunctionArguments()
                for (i in args.indices) {
                    sctArg = StructImpl()
                    arrArg.appendEL(sctArg)
                    sctArg.setEL(KeyConstants._name, args[i].getName().getString())
                    sctArg.setEL(KeyConstants._type, args[i].getTypeAsString())
                    sctArg.setEL(KeyConstants._required, args[i].isRequired())
                    if (!StringUtil.isEmpty(args[i].getHint())) sctArg.setEL(KeyConstants._hint, args[i].getHint())
                }
                sctUDF.set(KeyConstants._arguments, arrArg)
                sctUDF.set(KeyConstants._returntype, udf.getReturnTypeAsString())
            }
        }
        val rtn: Struct = StructImpl()
        rtn.set(KeyConstants._functions, udfs)
        rtn.set(ACCEPT_ARG_COLL_FORMATS, "cfml,json")
        val `is`: InputStream?
        var cs: Charset? = null
        // WDDX
        if (UDF.RETURN_FORMAT_WDDX === format) {
            val converter = WDDXConverter(pc.getTimeZone(), false, false)
            converter.setTimeZone(pc.getTimeZone())
            val str: String = converter.serialize(rtn)
            cs = getCharset(pc)
            `is` = ByteArrayInputStream(str.getBytes(cs))
        } else if (UDF.RETURN_FORMAT_JSON === format) {
            val qf: Int = SerializationSettings.SERIALIZE_AS_ROW
            cs = getCharset(pc)
            val converter = JSONConverter(false, cs)
            val str: String = converter.serialize(pc, rtn, qf)
            `is` = ByteArrayInputStream(str.getBytes(cs))
        } else if (UDF.RETURN_FORMAT_SERIALIZE === format) {
            val converter = ScriptConverter(false)
            val str: String = converter.serialize(rtn)
            cs = getCharset(pc)
            `is` = ByteArrayInputStream(str.getBytes(cs))
        } else if (UDF.RETURN_FORMAT_XML === format) {
            val converter = XMLConverter(pc.getTimeZone(), false)
            converter.setTimeZone(pc.getTimeZone())
            val str: String = converter.serialize(rtn)
            cs = getCharset(pc)
            `is` = ByteArrayInputStream(str.getBytes(cs))
        } else if (UDF.RETURN_FORMAT_PLAIN === format) {
            val str: String = Caster.toString(rtn)
            cs = getCharset(pc)
            `is` = ByteArrayInputStream(str.getBytes(cs))
        } else if (UDF.RETURN_FORMAT_JAVA === format) {
            val bytes: ByteArray = JavaConverter.serializeAsBinary(rtn)
            `is` = ByteArrayInputStream(bytes)
        } else throw IOException("invalid format defintion:$format")
        var os: OutputStream? = null
        try {
            os = pc.getResponseStream()
            setFormat(pc.getHttpServletResponse(), format, cs)
            IOUtil.copy(`is`, os, false, false)
        } finally {
            IOUtil.flushEL(os)
            IOUtil.close(os)
            (pc as PageContextImpl?)!!.getRootOut().setClosed(true)
        }
    }

    private fun getCharset(pc: PageContext?): Charset? {
        val rsp: HttpServletResponse = pc.getHttpServletResponse()
        var cs: Charset = ReqRspUtil.getCharacterEncoding(pc, rsp)
        if (cs == null) cs = pc.getWebCharset()
        return cs
    }

    @Throws(ServletException::class, IOException::class, PageException::class)
    private fun callWSDL(pc: PageContext?, component: Component?) {
        // take wsdl file defined by user
        val wsdl: String = component.getWSDLFile()
        if (!StringUtil.isEmpty(wsdl)) {
            var os: OutputStream? = null
            val input: Resource = ResourceUtil.toResourceExisting(pc, wsdl)
            try {
                os = pc.getResponseStream()
                ReqRspUtil.setContentType(pc.getHttpServletResponse(), "text/xml; charset=utf-8")
                IOUtil.copy(input, os, false)
            } finally {
                IOUtil.flushEL(os)
                IOUtil.close(os)
                (pc as PageContextImpl?)!!.getRootOut().setClosed(true)
            }
        } else {
            (ThreadLocalPageContext.getConfig(pc) as ConfigWebPro).getWSHandler().getWSServer(pc).doGet(pc, pc.getHttpServletRequest(), pc.getHttpServletResponse(), component)
        }
    }

    @Throws(IOException::class, ServletException::class, PageException::class)
    private fun callWebservice(pc: PageContext?, component: Component?) {
        (ThreadLocalPageContext.getConfig(pc) as ConfigWebPro).getWSHandler().getWSServer(pc).doPost(pc, pc.getHttpServletRequest(), pc.getHttpServletResponse(), component)
    }

    /**
     * default implementation of the static constructor, that does nothing
     */
    fun staticConstructor(pagecontext: PageContext?, cfc: ComponentImpl?) {
        // do nothing
    }

    // this method only exist that old classes from archives still work, not perfectly, but good enough
    fun getStaticStruct(): StaticStruct? {
        return StaticStruct()
    }

    @Throws(PageException::class)
    abstract fun initComponent(pc: PageContext?, c: ComponentImpl?, executeDefaultConstructor: Boolean)
    fun ckecked() {
        lastCheck = System.currentTimeMillis()
    }

    fun lastCheck(): Long {
        return lastCheck
    }

    fun getComponentName(): String? {
        return if (getSubname() != null) getPageSource().getComponentName().toString() + "$" + getSubname() else getPageSource().getComponentName()
    }

    fun getStaticScope(): StaticScope? {
        return staticScope
    }

    fun getIndex(): Long {
        return index
    }

    fun setStaticScope(staticScope: StaticScope?) {
        this.staticScope = staticScope
        index = staticScope!!.index()
    }

    companion object {
        val ACCEPT_ARG_COLL_FORMATS: Collection.Key? = KeyImpl.getInstance("acceptedArgumentCollectionFormats")
        private const val serialVersionUID = -3483642653131058030L
        val REMOTE_PERSISTENT_ID: tachyon.runtime.type.Collection.Key? = KeyImpl.getInstance("Id16hohohh")
        @Throws(ConverterException::class, IOException::class)
        private fun writeOut(pc: PageContext?, obj: Object?, mt: MimeType?, converter: BinaryConverter?) {
            ReqRspUtil.setContentType(pc.getHttpServletResponse(), mt.toString())
            var os: OutputStream? = null
            try {
                converter.writeOut(pc, obj, pc.getResponseStream().also { os = it })
            } finally {
                IOUtil.close(os)
            }
        }

        fun isSoap(pc: PageContext?): Boolean {
            val req: HttpServletRequest = pc.getHttpServletRequest()
            var `is`: InputStream? = null
            return try {
                `is` = req.getInputStream()
                val input: String = IOUtil.toString(`is`, CharsetUtil.ISO88591)
                StringUtil.indexOfIgnoreCase(input, ":Envelope>") !== -1
            } catch (e: IOException) {
                false
            } finally {
                IOUtil.closeEL(`is`)
            }
        }

        private fun setFormat(rsp: HttpServletResponse?, format: Int, charset: Charset?) {
            val strCS: String
            strCS = if (charset == null) "" else "; charset=" + charset.displayName()
            when (format) {
                UDF.RETURN_FORMAT_WDDX -> {
                    ReqRspUtil.setContentType(rsp, "text/xml$strCS")
                    rsp.setHeader("Return-Format", "wddx")
                }
                UDF.RETURN_FORMAT_JSON -> {
                    ReqRspUtil.setContentType(rsp, "application/json$strCS")
                    rsp.setHeader("Return-Format", "json")
                }
                UDF.RETURN_FORMAT_PLAIN -> {
                    ReqRspUtil.setContentType(rsp, "text/plain$strCS")
                    rsp.setHeader("Return-Format", "plain")
                }
                UDF.RETURN_FORMAT_XML -> {
                    ReqRspUtil.setContentType(rsp, "text/xml$strCS")
                    rsp.setHeader("Return-Format", "xml")
                }
                UDF.RETURN_FORMAT_SERIALIZE -> {
                    ReqRspUtil.setContentType(rsp, "application/cfml$strCS")
                    rsp.setHeader("Return-Format", "cfml")
                }
                UDF.RETURN_FORMAT_JAVA -> {
                    ReqRspUtil.setContentType(rsp, "application/java")
                    rsp.setHeader("Return-Format", "java")
                }
            }
        }

        private fun getProps(pc: PageContext?, o: Object?, urlReturnFormat: Int, headerReturnFormat: Int): Props? {
            val props = Props()
            props.strType = "any"
            props.secureJson = pc.getApplicationContext().getSecureJson()
            var udfReturnFormat = -1
            if (o is UDF) {
                val udf: UDF? = o as UDF?
                udfReturnFormat = udf.getReturnFormat(-1)
                props.type = udf.getReturnType()
                props.strType = udf.getReturnTypeAsString()
                props.output = udf.getOutput()
                if (udf.getSecureJson() != null) props.secureJson = udf.getSecureJson().booleanValue()
            }

            // format
            if (isValid(urlReturnFormat)) props.format = urlReturnFormat else if (isValid(udfReturnFormat)) props.format = udfReturnFormat else if (isValid(headerReturnFormat)) props.format = headerReturnFormat else props.format = UDF.RETURN_FORMAT_WDDX

            // return type XML ignore WDDX
            if (props.type == CFTypes.TYPE_XML) {
                if (UDF.RETURN_FORMAT_WDDX === props.format) props.format = UDF.RETURN_FORMAT_PLAIN
            }
            return props
        }

        private fun isValid(returnFormat: Int): Boolean {
            return returnFormat != -1 && returnFormat != UDF.RETURN_FORMAT_XML
        }

        @Throws(ConverterException::class, PageException::class, IOException::class)
        fun writeToResponseStream(pc: PageContext?, component: Component?, methodName: String?, urlReturnFormat: Int, headerReturnFormat: Int, queryFormat: Object?, rtn: Object?) {
            val o: Object = component.get(KeyImpl.init(methodName), null)
            val p = getProps(pc, o, urlReturnFormat, headerReturnFormat)
            _writeOut(pc, p, queryFormat, rtn, null, true)
        }

        @Throws(ConverterException::class, PageException::class, IOException::class)
        private fun _writeOut(pc: PageContext?, props: Props?, queryFormat: Object?, rtn: Object?, cs: Charset?, setFormat: Boolean) {
            // return type XML ignore WDDX
            var rtn: Object? = rtn
            rtn = if (props!!.type == CFTypes.TYPE_XML) {
                // if(UDF.RETURN_FORMAT_WDDX==format)
                // format=UDF.RETURN_FORMAT_PLAIN;
                Caster.toString(Caster.toXML(rtn))
            } else Caster.castTo(pc, props.type.toShort(), props.strType, rtn)
            if (setFormat) setFormat(pc.getHttpServletResponse(), props.format, cs)

            // WDDX
            if (UDF.RETURN_FORMAT_WDDX === props.format) {
                val converter = WDDXConverter(pc.getTimeZone(), false, false)
                converter.setTimeZone(pc.getTimeZone())
                pc.forceWrite(converter.serialize(rtn))
            } else if (UDF.RETURN_FORMAT_JSON === props.format) {
                var qf: Int = SerializationSettings.SERIALIZE_AS_ROW
                if (queryFormat != null) {
                    qf = JSONConverter.toQueryFormat(queryFormat, SerializationSettings.SERIALIZE_AS_UNDEFINED)
                    if (qf == SerializationSettings.SERIALIZE_AS_UNDEFINED) throw ApplicationException("invalid queryformat definition [$queryFormat], valid formats are [row,column,struct]")
                }
                val converter = JSONConverter(false, cs)
                var prefix = ""
                if (props.secureJson) {
                    prefix = pc.getApplicationContext().getSecureJsonPrefix()
                    if (prefix == null) prefix = ""
                }
                pc.forceWrite(prefix + converter.serialize(pc, rtn, qf))
            } else if (UDF.RETURN_FORMAT_SERIALIZE === props.format) {
                val converter = ScriptConverter(false)
                pc.forceWrite(converter.serialize(rtn))
            } else if (UDF.RETURN_FORMAT_XML === props.format) {
                val converter = XMLConverter(pc.getTimeZone(), false)
                converter.setTimeZone(pc.getTimeZone())
                pc.forceWrite(converter.serialize(rtn))
            } else if (UDF.RETURN_FORMAT_PLAIN === props.format) {
                pc.forceWrite(Caster.toString(rtn))
            } else if (UDF.RETURN_FORMAT_JAVA === props.format) {
                writeOut(pc, rtn, MimeType.APPLICATION_JAVA, JavaConverter())
            } else throw IOException("invalid return format defintion:" + props.format)
        }

        fun translate(c: Component?, strMethodName: String?, params: Struct?): Struct? {
            val methodName: Collection.Key = KeyImpl.init(strMethodName)
            val keys: Array<Key?> = CollectionUtil.keys(params)
            var args: Array<FunctionArgument?>? = null
            var index = -1
            var value: Object
            for (i in keys.indices) {
                index = Caster.toIntValue(keys[i].getString(), 0)
                if (index > 0) {
                    if (args == null) args = _getArgs(c, methodName)
                    if (args != null && index <= args.size) {
                        value = params.removeEL(keys[i])
                        if (value != null) params.setEL(args[index - 1].getName(), value)
                    }
                }
            }
            return params
        }

        private fun _getArgs(c: Component?, methodName: Collection.Key?): Array<FunctionArgument?>? {
            val o: Object = c.get(methodName, null)
            return if (o is UDF) (o as UDF).getFunctionArguments() else null
        }
    }
}

internal class Props {
    var strType: String? = "any"
    var secureJson = false
    var type: Int = CFTypes.TYPE_ANY
    var format: Int = UDF.RETURN_FORMAT_WDDX
    var output = true
}