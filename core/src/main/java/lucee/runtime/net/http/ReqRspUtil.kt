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
package lucee.runtime.net.http

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD

object ReqRspUtil {
    private val EMPTY: Array<Cookie?>? = arrayOfNulls<Cookie?>(0)
    private val rootPathes: Map<String?, String?>? = ReferenceMap<String?, String?>(HARD, SOFT)
    operator fun get(items: Array<Pair<String?, Object?>?>?, name: String?): String? {
        for (i in items.indices) {
            if (items!![i].getName().equalsIgnoreCase(name)) return Caster.toString(items[i].getValue(), null)
        }
        return null
    }

    fun add(items: Array<Pair<String?, Object?>?>?, name: String?, value: Object?): Array<Pair<String?, Object?>?>? {
        val tmp: Array<Pair<String?, Object?>?> = arrayOfNulls<Pair?>(items!!.size + 1)
        for (i in items.indices) {
            tmp[i] = items!![i]
        }
        tmp[items!!.size] = Pair<String?, Object?>(name, value)
        return tmp
    }

    operator fun set(items: Array<Pair<String?, Object?>?>?, name: String?, value: Object?): Array<Pair<String?, Object?>?>? {
        for (i in items.indices) {
            if (items!![i].getName().equalsIgnoreCase(name)) {
                items[i] = Pair<String?, Object?>(name, value)
                return items
            }
        }
        return add(items, name, value)
    }

    /**
     * return path to itself
     *
     * @param req
     */
    fun self(req: HttpServletRequest?): String? {
        val sb = StringBuffer(req.getServletPath())
        val qs: String = req.getQueryString()
        if (!StringUtil.isEmpty(qs)) sb.append('?').append(qs)
        return sb.toString()
    }

    fun setContentLength(rsp: HttpServletResponse?, length: Int) {
        rsp.setContentLength(length)
    }

    fun setContentLength(rsp: HttpServletResponse?, length: Long) {
        if (length <= Integer.MAX_VALUE) {
            setContentLength(rsp, length.toInt())
        } else {
            rsp.addHeader("Content-Length", Caster.toString(length))
        }
    }

    fun setContentType(rsp: HttpServletResponse?, contentType: String?) {
        rsp.setContentType(contentType)
    }

    fun getCookies(req: HttpServletRequest?, charset: Charset?): Array<Cookie?>? {
        var cookies: Array<Cookie?> = req.getCookies()
        if (cookies != null) {
            var tmp: String?
            for (cookie in cookies) {
                // value (is decoded by the servlet engine with iso-8859-1)
                if (cookie != null && !StringUtil.isAscii(cookie.getValue())) {
                    tmp = encode(cookie.getValue(), "iso-8859-1")
                    cookie.setValue(decode(tmp, charset.name(), false))
                }
            }
        }
        val values: Enumeration<String?> = req.getHeaders("Cookie")
        if (values != null) {
            val map: MutableMap<String?, Cookie?> = HashMap<String?, Cookie?>()
            if (cookies != null) {
                for (cookie in cookies) {
                    if (cookie != null) map.put(cookie.getName().toUpperCase(), cookie)
                }
            }
            try {
                var `val`: String
                while (values.hasMoreElements()) {
                    `val` = values.nextElement()
                    val arr: Array<String?> = lucee.runtime.type.util.ListUtil.listToStringArray(`val`, ';')
                    var tmp: Array<String?>
                    var c: Cookie?
                    for (i in arr.indices) {
                        tmp = lucee.runtime.type.util.ListUtil.listToStringArray(arr[i], '=')
                        if (tmp.size > 0) {
                            c = toCookie(dec(tmp[0], charset.name(), false), if (tmp.size > 1) dec(tmp[1], charset.name(), false) else "", null)
                            if (c != null) map.put(c.getName().toUpperCase(), c)
                        }
                    }
                }
                cookies = map.values().toArray(arrayOfNulls<Cookie?>(map.size()))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        return cookies ?: EMPTY
    }

    fun setCharacterEncoding(rsp: HttpServletResponse?, charset: String?) {
        try {
            val setCharacterEncoding: Method = rsp.getClass().getMethod("setCharacterEncoding", arrayOfNulls<Class?>(0))
            setCharacterEncoding.invoke(rsp, arrayOfNulls<Object?>(0))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw ExceptionUtil.toRuntimeException(t)
        }
    }

    fun getQueryString(req: HttpServletRequest?): String? {
        // String qs = req.getAttribute("javax.servlet.include.query_string");
        return req.getQueryString()
    }

    fun getHeader(request: HttpServletRequest?, name: String?, defaultValue: String?): String? {
        return try {
            request.getHeader(name)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun getHeaderIgnoreCase(pc: PageContext?, name: String?, defaultValue: String?): String? {
        val charset: String = pc.getWebCharset().name()
        val req: HttpServletRequest = pc.getHttpServletRequest()
        val e: Enumeration = req.getHeaderNames()
        var keyDecoded: String?
        var key: String
        while (e.hasMoreElements()) {
            key = e.nextElement().toString()
            keyDecoded = decode(key, charset, false)
            if (name.equalsIgnoreCase(key) || name.equalsIgnoreCase(keyDecoded)) return decode(req.getHeader(key), charset, false)
        }
        return defaultValue
    }

    fun getHeadersIgnoreCase(pc: PageContext?, name: String?): List<String?>? {
        val charset: String = pc.getWebCharset().name()
        val req: HttpServletRequest = pc.getHttpServletRequest()
        val e: Enumeration = req.getHeaderNames()
        val rtn: List<String?> = ArrayList<String?>()
        var keyDecoded: String?
        var key: String
        while (e.hasMoreElements()) {
            key = e.nextElement().toString()
            keyDecoded = decode(key, charset, false)
            if (name.equalsIgnoreCase(key) || name.equalsIgnoreCase(keyDecoded)) rtn.add(decode(req.getHeader(key), charset, false))
        }
        return rtn
    }

    fun getScriptName(pc: PageContext?, req: HttpServletRequest?): String? {
        var pc: PageContext? = pc
        var sn: String = StringUtil.emptyIfNull(req.getContextPath()) + StringUtil.emptyIfNull(req.getServletPath())
        if (pc == null) pc = ThreadLocalPageContext.get()
        if (pc != null && (pc.getApplicationContext().getScriptProtect() and ApplicationContext.SCRIPT_PROTECT_URL > 0
                        || pc.getApplicationContext().getScriptProtect() and ApplicationContext.SCRIPT_PROTECT_CGI > 0)) {
            sn = ScriptProtect.translate(sn)
        }
        return sn
    }

    private fun isHex(c: Char): Boolean {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F'
    }

    private fun dec(str: String?, charset: String?, force: Boolean): String? {
        var str = str
        str = str.trim()
        if (StringUtil.startsWith(str, '"') && StringUtil.endsWith(str, '"') && str!!.length() > 1) str = str.substring(1, str!!.length() - 1)
        return decode(str, charset, force) // java.net.URLDecoder.decode(str.trim(), charset);
    }

    fun decode(str: String?, charset: String?, force: Boolean): String? {
        return try {
            if (str == null) null else URLDecoder.decode(str, charset, force)
        } catch (e: UnsupportedEncodingException) {
            str
        }
    }

    fun encode(str: String?, charset: String?): String? {
        return try {
            URLEncoder.encode(str, charset)
        } catch (e: UnsupportedEncodingException) {
            str
        }
    }

    fun encode(str: String?, charset: Charset?): String? {
        return try {
            URLEncoder.encode(str, charset)
        } catch (e: UnsupportedEncodingException) {
            str
        }
    }

    fun needEncoding(str: String?, allowPlus: Boolean): Boolean {
        if (StringUtil.isEmpty(str, false)) return false
        val len: Int = str!!.length()
        var c: Char
        var i = 0
        while (i < len) {
            c = str.charAt(i)
            if (c >= '0' && c <= '9') {
                i++
                continue
            }
            if (c >= 'a' && c <= 'z') {
                i++
                continue
            }
            if (c >= 'A' && c <= 'Z') {
                i++
                continue
            }

            // _-.*
            if (c == '-') {
                i++
                continue
            }
            if (c == '_') {
                i++
                continue
            }
            if (c == '.') {
                i++
                continue
            }
            if (c == '*') {
                i++
                continue
            }
            if (c == '/') {
                i++
                continue
            }
            if (allowPlus && c == '+') {
                i++
                continue
            }
            if (c == '%') {
                if (i + 2 >= len) return true
                try {
                    val c1: Char = str.charAt(i + 1)
                    val c2: Char = str.charAt(i + 2)
                    if (!isHex(c1) || !isHex(c2)) return true
                    // Integer.parseInt(c1 + "" + c2, 16);
                } catch (nfe: NumberFormatException) {
                    return true
                }
                i += 2
                i++
                continue
            }
            return true
            i++
        }
        return false
    }

    fun needDecoding(str: String?): Boolean {
        if (StringUtil.isEmpty(str, false)) return false
        var need = false
        val len: Int = str!!.length()
        var c: Char
        var i = 0
        while (i < len) {
            c = str.charAt(i)
            if (c >= '0' && c <= '9') {
                i++
                continue
            }
            if (c >= 'a' && c <= 'z') {
                i++
                continue
            }
            if (c >= 'A' && c <= 'Z') {
                i++
                continue
            }

            // _-.*
            if (c == '-') {
                i++
                continue
            }
            if (c == '_') {
                i++
                continue
            }
            if (c == '.') {
                i++
                continue
            }
            if (c == '*') {
                i++
                continue
            }
            if (c == '+') {
                need = true
                i++
                continue
            }
            if (c == '%') {
                if (i + 2 >= len) return false
                try {
                    Integer.parseInt(str.substring(i + 1, i + 3), 16)
                } catch (nfe: NumberFormatException) {
                    return false
                }
                i += 2
                need = true
                i++
                continue
            }
            return false
            i++
        }
        return need
    }

    fun isThis(req: HttpServletRequest?, url: String?): Boolean {
        return try {
            isThis(req, HTTPUtil.toURL(url, HTTPUtil.ENCODED_AUTO))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            false
        }
    }

    fun isThis(req: HttpServletRequest?, url: URL?): Boolean {
        try {
            // Port
            var reqPort: Int = req.getServerPort()
            var urlPort: Int = url.getPort()
            if (urlPort <= 0) urlPort = if (HTTPUtil.isSecure(url)) 443 else 80
            if (reqPort <= 0) reqPort = if (req.isSecure()) 443 else 80
            if (reqPort != urlPort) return false

            // host
            val reqHost: String = req.getServerName()
            val urlHost: String = url.getHost()
            if (reqHost.equalsIgnoreCase(urlHost)) return true
            if (IsLocalHost.invoke(reqHost) && IsLocalHost.invoke(reqHost)) return true
            val urlAddr: InetAddress = InetAddress.getByName(urlHost)
            var reqAddr: InetAddress = InetAddress.getByName(reqHost)
            if (reqAddr.getHostName().equalsIgnoreCase(urlAddr.getHostName())) return true
            if (reqAddr.getHostAddress().equalsIgnoreCase(urlAddr.getHostAddress())) return true
            reqAddr = InetAddress.getByName(req.getRemoteAddr())
            if (reqAddr.getHostName().equalsIgnoreCase(urlAddr.getHostName())) return true
            if (reqAddr.getHostAddress().equalsIgnoreCase(urlAddr.getHostAddress())) return true
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return false
    }

    fun getAccept(pc: PageContext?): LinkedList<MimeType?>? {
        val accept: LinkedList<MimeType?> = LinkedList<MimeType?>()
        val it = getHeadersIgnoreCase(pc, "accept")!!.iterator()
        var value: String?
        while (it.hasNext()) {
            value = it.next()
            val mtes: Array<MimeType?> = MimeType.getInstances(value, ',')
            if (mtes != null) for (i in mtes.indices) {
                accept.add(mtes[i])
            }
        }
        return accept
    }

    fun getContentType(pc: PageContext?): MimeType? {
        val it = getHeadersIgnoreCase(pc, "content-type")!!.iterator()
        var value: String?
        var rtn: MimeType? = null
        while (it.hasNext()) {
            value = it.next()
            val mtes: Array<MimeType?> = MimeType.getInstances(value, ',')
            if (mtes != null) for (i in mtes.indices) {
                rtn = mtes[i]
            }
        }
        return if (rtn == null) MimeType.ALL else rtn
    }

    fun getContentTypeAsString(pc: PageContext?, defaultValue: String?): String? {
        val mt: MimeType? = getContentType(pc)
        return if (mt === MimeType.ALL) defaultValue else mt.toString()
    }

    /**
     * returns the body of the request
     *
     * @param pc
     * @param deserialized if true lucee tries to deserialize the body based on the content-type, for
     * example when the content type is "application/json"
     * @param defaultValue value returned if there is no body
     * @return
     */
    fun getRequestBody(pc: PageContext?, deserialized: Boolean, defaultValue: Object?): Object? {
        val req: HttpServletRequest = pc.getHttpServletRequest()
        val contentType: MimeType? = getContentType(pc)
        val strContentType: String? = if (contentType === MimeType.ALL) null else contentType.toString()
        val cs: Charset = getCharacterEncoding(pc, req)
        val isBinary = !(strContentType == null || HTTPUtil.isTextMimeType(contentType) === Boolean.TRUE || strContentType.toLowerCase().startsWith("application/x-www-form-urlencoded"))
        if (req.getContentLength() > -1) {
            var `is`: ServletInputStream? = null
            return try {
                val data: ByteArray = IOUtil.toBytes(req.getInputStream().also { `is` = it }) // new byte[req.getContentLength()];
                var obj: Object? = CollectionUtil.NULL
                if (deserialized) {
                    val format: Int = MimeType.toFormat(contentType, -1)
                    obj = toObject(pc, data, format, cs, obj)
                }
                if (obj === CollectionUtil.NULL) {
                    obj = if (isBinary) data else toString(data, cs)
                }
                obj
            } catch (e: Exception) {
                ThreadLocalPageContext.getLog(pc, "application").error("request", e)
                defaultValue
            } finally {
                try {
                    IOUtil.close(`is`)
                } catch (e: IOException) {
                    ThreadLocalPageContext.getLog(pc, "application").error("request", e)
                }
            }
        }
        return defaultValue
    }

    private fun toString(data: ByteArray?, cs: Charset?): String? {
        return if (cs != null) String(data, cs).trim() else String(data).trim()
    }

    /**
     * returns the full request URL
     *
     * @param req - the HttpServletRequest
     * @param includeQueryString - if true, the QueryString will be appended if one exists
     */
    fun getRequestURL(req: HttpServletRequest?, includeQueryString: Boolean): String? {
        val sb: StringBuffer = req.getRequestURL()
        val maxpos: Int = sb.indexOf("/", 8)
        if (maxpos > -1) {
            if (req.isSecure()) {
                if (sb.substring(maxpos - 4, maxpos).equals(":443")) sb.delete(maxpos - 4, maxpos)
            } else {
                if (sb.substring(maxpos - 3, maxpos).equals(":80")) sb.delete(maxpos - 3, maxpos)
            }
            if (includeQueryString && !StringUtil.isEmpty(req.getQueryString())) sb.append('?').append(req.getQueryString())
        }
        return sb.toString()
    }

    fun getRootPath(sc: ServletContext?): String? {
        if (sc == null) throw RuntimeException("cannot determinate webcontext root, because the ServletContext is null")
        val id: String = StringBuilder().append(sc.getContextPath()).append(':').append(sc.hashCode()).toString()
        var root = rootPathes!![id]
        if (!StringUtil.isEmpty(root, true)) return root
        root = sc.getRealPath("/")
        if (root == null) throw RuntimeException("cannot determinate webcontext root, the ServletContext from class [" + sc.getClass().getName()
                .toString() + "] is returning null for the method call sc.getRealPath(\"/\"), possibly due to configuration problem.")
        try {
            root = File(root).getCanonicalPath()
        } catch (e: IOException) {
        }
        rootPathes.put(id, root)
        return root
    }

    fun toObject(pc: PageContext?, data: ByteArray?, format: Int, charset: Charset?, defaultValue: Object?): Object? {
        when (format) {
            UDF.RETURN_FORMAT_JSON -> try {
                return JSONExpressionInterpreter().interpret(pc, toString(data, charset))
            } catch (pe: PageException) {
            }
            UDF.RETURN_FORMAT_SERIALIZE -> try {
                return CFMLExpressionInterpreter().interpret(pc, toString(data, charset))
            } catch (pe: PageException) {
            }
            UDF.RETURN_FORMAT_WDDX -> try {
                val converter = WDDXConverter(pc.getTimeZone(), false, true)
                converter.setTimeZone(pc.getTimeZone())
                return converter.deserialize(toString(data, charset), false)
            } catch (pe: Exception) {
            }
            UDF.RETURN_FORMAT_XML -> try {
                val xml: InputSource = XMLUtil.toInputSource(pc, toString(data, charset))
                val validator: InputSource? = null
                return XMLCaster.toXMLStruct(XMLUtil.parse(xml, validator, false), true)
            } catch (pe: Exception) {
            }
            UDF.RETURN_FORMAT_JAVA -> try {
                return JavaConverter.deserialize(ByteArrayInputStream(data))
            } catch (pe: Exception) {
            }
        }
        return defaultValue
    }

    fun identical(left: HttpServletRequest?, right: HttpServletRequest?): Boolean {
        var left: HttpServletRequest? = left
        var right: HttpServletRequest? = right
        if (left === right) return true
        if (left is HTTPServletRequestWrap) left = (left as HTTPServletRequestWrap?).getOriginalRequest()
        if (right is HTTPServletRequestWrap) right = (right as HTTPServletRequestWrap?).getOriginalRequest()
        return if (left === right) true else false
    }

    fun getCharacterEncoding(pc: PageContext?, req: ServletRequest?): Charset? {
        return _getCharacterEncoding(pc, req.getCharacterEncoding())
    }

    fun getCharacterEncoding(pc: PageContext?, rsp: ServletResponse?): Charset? {
        return _getCharacterEncoding(pc, rsp.getCharacterEncoding())
    }

    private fun _getCharacterEncoding(pc: PageContext?, ce: String?): Charset? {
        var pc: PageContext? = pc
        if (!StringUtil.isEmpty(ce, true)) {
            val c: Charset = CharsetUtil.toCharset(ce, null)
            if (c != null) return c
        }
        pc = ThreadLocalPageContext.get(pc)
        if (pc != null) return pc.getWebCharset()
        val config: Config = ThreadLocalPageContext.getConfig(pc)
        return config.getWebCharset()
    }

    fun removeCookie(rsp: HttpServletResponse?, name: String?) {
        val cookie: javax.servlet.http.Cookie = Cookie(name, "")
        cookie.setMaxAge(0)
        cookie.setSecure(false)
        cookie.setPath("/")
        rsp.addCookie(cookie)
    }

    /**
     * if encodings fails the given url is returned
     *
     * @param rsp
     * @param url
     * @return
     */
    fun encodeRedirectURLEL(rsp: HttpServletResponse?, url: String?): String? {
        return try {
            rsp.encodeRedirectURL(url)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            url
        }
    }

    fun getDomain(req: HttpServletRequest?): String? { // DIFF 23
        val sb = StringBuilder()
        sb.append(if (req.isSecure()) "https://" else "http://")
        sb.append(req.getServerName())
        sb.append(':')
        sb.append(req.getServerPort())
        if (!StringUtil.isEmpty(req.getContextPath())) sb.append(req.getContextPath())
        return sb.toString()
    }

    fun toCookie(name: String?, value: String?, defaultValue: Cookie?): Cookie? {
        return try {
            Cookie(name, value)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }
}