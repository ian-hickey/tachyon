package tachyon.runtime.functions.system

import java.io.ByteArrayOutputStream

object InternalRequest : Function {
    private const val serialVersionUID = -8163856691035353577L
    var cookieAsQuery = false
    val FILECONTENT_BYNARY: Key? = KeyImpl.getInstance("filecontent_binary")
    val STATUS_CODE: Key? = KeyImpl.getInstance("status_code")
    private val CONTENT_TYPE: Key? = KeyImpl.getInstance("content-type")
    private val CONTENT_LENGTH: Key? = KeyImpl.getInstance("content-length")
    @Throws(PageException::class)
    fun call(pc: PageContext?, template: String?, method: String?, oUrls: Object?, oForms: Object?, cookies: Struct?, headers: Struct?, body: Object?, strCharset: String?,
             addToken: Boolean, throwonerror: Boolean): Struct? {
        var cookies: Struct? = cookies
        var headers: Struct? = headers
        val urls: Struct? = toStruct(oUrls)
        val forms: Struct? = toStruct(oForms)

        // add token
        if (addToken) {
            // if(true) throw new ApplicationException("addtoken==true");
            if (cookies == null) cookies = StructImpl()
            cookies.set(KeyConstants._cfid, pc.getCFID())
            cookies.set(KeyConstants._cftoken, pc.getCFToken())
            val jsessionid: String = pc.getJSessionId()
            if (jsessionid != null) cookies.set("jsessionid", jsessionid)
        }

        // charset
        val reqCharset: Charset = if (StringUtil.isEmpty(strCharset)) pc.getWebCharset() else CharsetUtil.toCharset(strCharset)
        val ext: String = ResourceUtil.getExtension(template, null)
        // welcome files
        if (StringUtil.isEmpty(ext)) {
            throw FunctionException(pc, "Invoke", 1, "url", "welcome file listing not supported, please define the template name.")
        }

        // dialect
        var dialect: Int = (pc.getConfig().getFactory() as CFMLFactoryImpl).toDialect(ext, -1)
        if (dialect == -1) dialect = pc.getCurrentTemplateDialect()
        // CFMLEngine.DIALECT_LUCEE
        val baos = ByteArrayOutputStream()
        var _barr: ByteArray? = null
        if (Decision.isBinary(body)) _barr = Caster.toBinary(body) else if (body != null) {
            var cs: Charset? = null
            // get charset
            if (headers != null) {
                val strCT: String = Caster.toString(headers.get(CONTENT_TYPE, null), null)
                if (strCT != null) {
                    val ct: ContentType = HTTPUtil.toContentType(strCT, null)
                    if (ct != null) {
                        val strCS: String = ct.getCharset()
                        if (!StringUtil.isEmpty(strCS)) cs = CharsetUtil.toCharSet(strCS, CharSet.UTF8).toCharset()
                    }
                }
            }
            if (cs == null) cs = CharsetUtil.UTF8
            val str: String = Caster.toString(body)
            _barr = str.getBytes(cs)
        }
        val _pc: PageContextImpl? = createPageContext(pc, template, urls, cookies, headers, _barr, reqCharset, baos)
        fillForm(_pc, forms, reqCharset)
        val request: Collection
        var session: Collection? = null
        val status: Int
        val exeTime: Long
        var isText = false
        var _charset: Charset? = null
        var pe: PageException? = null
        val rspCookies: Object = if (cookieAsQuery) QueryImpl(arrayOf<String?>("name", "value", "path", "domain", "expires", "secure", "httpOnly", "samesite"), 0, "cookies") else StructImpl(Struct.TYPE_LINKED)
        try {
            if (CFMLEngine.DIALECT_LUCEE === dialect) _pc.execute(template, true, false) else _pc.executeCFML(template, true, false)
            var s: HttpSession?
            if (_pc.getSessionType() === Config.SESSION_TYPE_JEE && _pc.getSession().also { s = it } != null) _pc.cookieScope().set(KeyConstants._JSESSIONID, s.getId())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            if (t !is Abort) {
                if (throwonerror) throw Caster.toPageException(t)
                pe = Caster.toPageException(t)
            }
        } finally {
            _pc.flush()
            // cookie = _pc.cookieScope().duplicate(false);
            request = _pc.requestScope().duplicate(false)
            session = if (sessionEnabled(_pc)) _pc.sessionScope().duplicate(false) else null
            exeTime = System.currentTimeMillis() - pc.getStartTime()
            // debugging=_pc.getDebugger().getDebuggingData(_pc).duplicate(false);
            val rsp: HttpServletResponseDummy = _pc.getHttpServletResponse() as HttpServletResponseDummy

            // headers
            var name: Collection.Key
            headers = StructImpl()
            val it: Iterator<String?> = rsp.getHeaderNames().iterator()
            var values: Collection<String?>
            while (it.hasNext()) {
                name = KeyImpl.init(it.next())
                values = rsp.getHeaders(name.getString())
                if (values == null || values.size() === 0) continue
                if (name.equals("Set-Cookie")) {
                    val cs: String = _pc.getWebCharset().name()
                    for (v in values) {
                        if (cookieAsQuery) Http.parseCookie(rspCookies as Query, v, cs) else Http.parseCookie(rspCookies as Struct, v, cs)
                    }
                }
                if (values.size() > 1) headers.set(name, Caster.toArray(values)) else headers.set(name, values.iterator().next())
            }

            // content type and length
            headers.set(CONTENT_TYPE, rsp.getContentType())
            if (rsp.getContentLength() !== -1) headers.set(CONTENT_LENGTH, rsp.getContentLength())

            // status
            status = rsp.getStatus()
            val ct: ContentType = HTTPUtil.toContentType(rsp.getContentType(), null)
            if (ct != null) {
                isText = HTTPUtil.isTextMimeType(ct.getMimeType()) === Boolean.TRUE
                if (ct.getCharset() != null) _charset = CharsetUtil.toCharset(ct.getCharset(), null)
            }
            releasePageContext(_pc, pc)
        }
        val rst: Struct = StructImpl()
        val barr: ByteArray = baos.toByteArray()
        if (isText) rst.set(KeyConstants._filecontent, String(barr, if (_charset == null) reqCharset else _charset)) else rst.set(FILECONTENT_BYNARY, barr)
        rst.set(KeyConstants._cookies, rspCookies)
        rst.set(KeyConstants._request, request)
        if (session != null) rst.set(KeyConstants._session, session)
        rst.set(KeyConstants._headers, headers)
        // rst.put(KeyConstants._debugging, debugging);
        rst.set(KeyConstants._executionTime, Double.valueOf(exeTime))
        rst.set(KeyConstants._status, Double.valueOf(status))
        rst.set(STATUS_CODE, Double.valueOf(status))
        if (pe != null) rst.set(KeyConstants._error, pe.getCatchBlock(pc.getConfig()))
        return rst
    }

    @Throws(PageException::class)
    private fun toStruct(obj: Object?): Struct? {
        if (Decision.isCastableToStruct(obj)) return Caster.toStruct(obj)
        val str: String = Caster.toString(obj)
        var index: Int
        val data: Struct = StructImpl(Struct.TYPE_LINKED)
        // boolean asArray = pc.getApplicationContext().getSameFieldAsArray(scope);
        var n: Key
        var v: String
        var existing: Object
        for (el in ListUtil.listToList(str, '&', true)) {
            index = el.indexOf('=')
            if (index == -1) {
                n = KeyImpl.init(URLDecoder.decode(el, true))
                v = ""
            } else {
                n = KeyImpl.init(URLDecoder.decode(el.substring(0, index), true))
                v = URLDecoder.decode(el.substring(index + 1), true)
            }
            existing = data.get(n, null)
            if (existing != null) {
                if (existing is ArgumentImpl) {
                    (existing as ArgumentImpl).appendEL(v)
                } else {
                    val arr = ArgumentImpl()
                    arr.append(existing)
                    arr.append(v)
                    data.setEL(n, arr)
                }
            } else data.setEL(n, v)
        }
        return data
    }

    private fun sessionEnabled(pc: PageContextImpl?): Boolean {
        val ac: ApplicationContext = pc.getApplicationContext() ?: return false
        // this test properly is not necessary
        return ac.hasName() && ac.isSetSessionManagement()
    }

    @Throws(PageException::class)
    private fun fillForm(_pc: PageContextImpl?, src: Struct?, charset: Charset?) {
        if (src == null) return
        val it: Iterator<Entry<Key?, Object?>?> = src.entryIterator()
        val tmp: Form = _pc.formScope()
        val trg: FormImpl = if (tmp is UrlFormImpl) (tmp as UrlFormImpl).getForm() else tmp as FormImpl
        var e: Entry<Key?, Object?>?
        var n: Key
        var v: Object
        var vv: Object
        val list: MutableList<URLItem?> = ArrayList()
        while (it.hasNext()) {
            e = it.next()
            n = e.getKey()
            v = e.getValue()
            if (v is Array) {
                val itt: Iterator<Object?> = (v as Array).valueIterator()
                while (itt.hasNext()) {
                    vv = itt.next()
                    list.add(URLItem(n.getString(), Caster.toString(vv), false))
                }
            } else if (v is Struct) {
                val itt: Iterator<Entry<Key?, Object?>?> = (v as Struct).entryIterator()
                var ee: Entry<Key?, Object?>?
                while (itt.hasNext()) {
                    ee = itt.next()
                    list.add(URLItem(n.getString().toString() + "." + ee.getKey(), Caster.toString(ee.getValue()), false))
                }
            } else list.add(URLItem(n.getString(), Caster.toString(v), false))
        }
        trg.addRaw(null, list.toArray(arrayOfNulls<URLItem?>(list.size())))
    }

    @Throws(PageException::class)
    private fun createPageContext(pc: PageContext?, template: String?, urls: Struct?, cookies: Struct?, headers: Struct?, body: ByteArray?, charset: Charset?, os: OutputStream?): PageContextImpl? {
        val session: HttpSession? = if (pc.getSessionType() === Config.SESSION_TYPE_JEE) pc.getSession() else null
        return ThreadUtil.createPageContext(pc.getConfig(), os, pc.getHttpServletRequest().getServerName(), template, toQueryString(urls, charset),
                CreatePageContext.toCookies(cookies), CreatePageContext.toPair(headers, true), body, CreatePageContext.toPair(StructImpl(), true),
                CreatePageContext.castValuesToString(StructImpl()), true, -1, session)
    }

    @Throws(PageException::class)
    private fun toQueryString(urls: Struct?, charset: Charset?): String? {
        // query string | URL
        var e: Entry<Key?, Object?>
        val sbQS = StringBuilder()
        if (urls != null) {
            val it: Iterator<Entry<Key?, Object?>?> = urls.entryIterator()
            var v: Object
            var n: Key
            while (it.hasNext()) {
                e = it.next()
                n = e.getKey()
                v = e.getValue()
                if (v is Argument) {
                    val itt: Iterator<Entry<Key?, Object?>?> = (v as Argument).entryIterator()
                    var ee: Entry<Key?, Object?>?
                    while (itt.hasNext()) {
                        ee = itt.next()
                        if (sbQS.length() > 0) sbQS.append('&')
                        sbQS.append(urlenc(n.getString(), charset))
                        sbQS.append('=')
                        sbQS.append(urlenc(Caster.toString(ee.getValue()), charset))
                    }
                } else {
                    if (sbQS.length() > 0) sbQS.append('&')
                    sbQS.append(urlenc(e.getKey().getString(), charset))
                    sbQS.append('=')
                    sbQS.append(urlenc(Caster.toString(v), charset))
                }
            }
        }
        return sbQS.toString()
    }

    private fun releasePageContext(pc: PageContext?, oldPC: PageContext?) {
        pc.flush()
        oldPC.getConfig().getFactory().releaseTachyonPageContext(pc, false)
        ThreadLocalPageContext.release()
        if (oldPC != null) ThreadLocalPageContext.register(oldPC)
    }

    @Throws(PageException::class)
    private fun urlenc(str: String?, charset: Charset?): String? {
        return try {
            if (!ReqRspUtil.needEncoding(str, false)) str else URLEncoder.encode(str, charset)
        } catch (uee: UnsupportedEncodingException) {
            throw Caster.toPageException(uee)
        }
    }
}