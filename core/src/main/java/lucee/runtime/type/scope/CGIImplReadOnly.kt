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
package lucee.runtime.type.scope

import java.io.Externalizable

/**
 *
 *
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
class CGIImplReadOnly : ReadOnlyStruct(), CGI, ScriptProtected, Externalizable {
    companion object {
        private const val serialVersionUID = 5219795840777155232L
        private val keys: Array<Collection.Key?>? = arrayOf<Collection.Key?>(KeyConstants._auth_password, KeyConstants._auth_type, KeyConstants._auth_user, KeyConstants._cert_cookie,
                KeyConstants._cert_flags, KeyConstants._cert_issuer, KeyConstants._cert_keysize, KeyConstants._cert_secretkeysize, KeyConstants._cert_serialnumber,
                KeyConstants._cert_server_issuer, KeyConstants._cert_server_subject, KeyConstants._cert_subject, KeyConstants._cf_template_path, KeyConstants._content_length,
                KeyConstants._content_type, KeyConstants._gateway_interface, KeyConstants._http_accept, KeyConstants._http_accept_encoding, KeyConstants._http_accept_language,
                KeyConstants._http_connection, KeyConstants._http_cookie, KeyConstants._http_host, KeyConstants._http_user_agent, KeyConstants._http_referer, KeyConstants._https,
                KeyConstants._https_keysize, KeyConstants._https_secretkeysize, KeyConstants._https_server_issuer, KeyConstants._https_server_subject, KeyConstants._path_info,
                KeyConstants._path_translated, KeyConstants._query_string, KeyConstants._remote_addr, KeyConstants._remote_host, KeyConstants._remote_user,
                KeyConstants._request_method, KeyConstants._request_url, KeyConstants._script_name, KeyConstants._server_name, KeyConstants._server_port,
                KeyConstants._server_port_secure, KeyConstants._server_protocol, KeyConstants._server_software, KeyConstants._web_server_api, KeyConstants._context_path,
                KeyConstants._local_addr, KeyConstants._local_host)
        private val staticKeys: Struct? = StructImpl()
        private var localAddress: String? = ""
        private var localHost: String? = ""
        fun getDomain(req: HttpServletRequest?): String? { // DIFF 23
            val sb = StringBuffer()
            sb.append(if (req.isSecure()) "https://" else "http://")
            sb.append(req.getServerName())
            sb.append(':')
            sb.append(req.getServerPort())
            if (!StringUtil.isEmpty(req.getContextPath())) sb.append(req.getContextPath())
            return sb.toString()
        }

        init {
            for (i in keys.indices) {
                staticKeys.setEL(keys!![i], "")
            }
        }

        init {
            try {
                val addr: InetAddress = InetAddress.getLocalHost()
                localAddress = lucee.runtime.type.scope.addr.getHostAddress()
                localHost = lucee.runtime.type.scope.addr.getHostName()
            } catch (uhe: UnknownHostException) {
            }
        }
    }

    @Transient
    private var req: HttpServletRequest? = null
    private var isInit = false

    @Transient
    private var https: Struct? = null

    @Transient
    private var headers: Struct? = null
    private var scriptProtected = 0
    private var disconnected = false
    private var disconnectedData: Map<Key?, Object?>? = null
    fun disconnect() {
        if (disconnected) return
        _disconnect()
        disconnected = true
        req = null
    }

    private fun _disconnect() {
        disconnectedData = HashMap<Key?, Object?>()
        for (i in keys.indices) {
            disconnectedData.put(keys!![i], get(keys[i], ""))
        }
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return staticKeys.containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return staticKeys.containsKey(key)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        // TODO Auto-generated method stub
        return super.containsValue(value)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val sct: Struct = StructImpl()
        copy(this, sct, deepCopy)
        return sct
    }

    @Override
    fun size(): Int {
        return keys!!.size
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return keys
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(null, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        if (disconnected) {
            return disconnectedData.getOrDefault(key, defaultValue)
        }
        if (https == null) {
            https = StructImpl()
            headers = StructImpl()
            var k: String?
            var v: String
            try {
                val e: Enumeration = req.getHeaderNames()
                while (e.hasMoreElements()) {
                    k = e.nextElement()
                    v = req.getHeader(k)
                    // print.err(k.length()+":"+k);
                    headers.setEL(KeyImpl.init(k), v)
                    headers.setEL(KeyImpl.init(k.replace('-', '_').also { k = it }), v)
                    https.setEL(KeyImpl.init("http_$k"), v)
                }
            } catch (e: Exception) {
            }
        }
        val lkey: String = key.getLowerString()
        val first: Char = lkey.charAt(0)
        try {
            if (first == 'a') {
                if (key.equals(KeyConstants._auth_type)) return toString(req.getAuthType())
            } else if (first == 'c') {
                if (key.equals(KeyConstants._context_path)) return toString(req.getContextPath())
                if (key.equals(KeyConstants._cf_template_path)) return getPathTranslated()
            } else if (first == 'h') {
                if (lkey.startsWith("http_")) {
                    val _null: Object = NullSupportHelper.NULL()
                    var o: Object = https.get(key, _null)
                    if (o === _null && key.equals(KeyConstants._http_if_modified_since)) o = https.get(KeyConstants._last_modified, _null)
                    if (o !== _null) return doScriptProtect(o as String)
                } else if (key.equals(KeyConstants._https)) return if (req.isSecure()) "on" else "off"
            } else if (first == 'r') {
                if (key.equals(KeyConstants._remote_user)) return toString(req.getRemoteUser())
                if (key.equals(KeyConstants._remote_addr)) {
                    return toString(req.getRemoteAddr())
                }
                if (key.equals(KeyConstants._remote_host)) return toString(req.getRemoteHost())
                if (key.equals(KeyConstants._request_method)) return req.getMethod()
                if (key.equals(KeyConstants._request_url)) {
                    try {
                        return ReqRspUtil.getRequestURL(req, true)
                    } catch (e: Exception) {
                    }
                }
                if (key.equals(KeyConstants._request_uri)) return toString(req.getAttribute("javax.servlet.include.request_uri"))
                if (key.getUpperString().startsWith("REDIRECT_")) {
                    // from attributes (key sensitive)
                    val value: Object = req.getAttribute(key.getString())
                    if (!StringUtil.isEmpty(value)) return toString(value)

                    // from attributes (key insensitive)
                    val names: Enumeration<String?> = req.getAttributeNames()
                    var k: String
                    while (names.hasMoreElements()) {
                        k = names.nextElement()
                        if (k.equalsIgnoreCase(key.getString())) {
                            return toString(req.getAttribute(k))
                        }
                    }
                }
            } else if (first == 'l') {
                if (key.equals(KeyConstants._local_addr)) return toString(localAddress)
                if (key.equals(KeyConstants._local_host)) return toString(localHost)
            } else if (first == 's') {
                if (key.equals(KeyConstants._script_name)) return ReqRspUtil.getScriptName(null, req)
                // return StringUtil.emptyIfNull(req.getContextPath())+StringUtil.emptyIfNull(req.getServletPath());
                if (key.equals(KeyConstants._server_name)) return toString(req.getServerName())
                if (key.equals(KeyConstants._server_protocol)) return toString(req.getProtocol())
                if (key.equals(KeyConstants._server_port)) return Caster.toString(req.getServerPort())
                if (key.equals(KeyConstants._server_port_secure)) return if (req.isSecure()) "1" else "0"
            } else if (first == 'p') {
                if (key.equals(KeyConstants._path_info)) {
                    var pathInfo: String? = Caster.toString(req.getAttribute("javax.servlet.include.path_info"), null)
                    if (StringUtil.isEmpty(pathInfo)) pathInfo = Caster.toString(req.getHeader("xajp-path-info"), null)
                    if (StringUtil.isEmpty(pathInfo)) pathInfo = req.getPathInfo()
                    if (StringUtil.isEmpty(pathInfo)) {
                        pathInfo = Caster.toString(req.getAttribute("requestedPath"), null)
                        if (!StringUtil.isEmpty(pathInfo, true)) {
                            val scriptName: String = ReqRspUtil.getScriptName(null, req)
                            if (pathInfo.startsWith(scriptName)) pathInfo = pathInfo.substring(scriptName.length())
                        }
                    }
                    return if (!StringUtil.isEmpty(pathInfo, true)) pathInfo else ""
                }
                if (key.equals(KeyConstants._path_translated)) return getPathTranslated()
            } else if (first == 'q') {
                if (key.equals(KeyConstants._query_string)) return doScriptProtect(toString(ReqRspUtil.getQueryString(req)))
            }
        } catch (e: Exception) {
            return other(key, defaultValue)
        }

        // check header
        val headerValue = headers.get(key, null) as String // req.getHeader(key.getString());
        return headerValue?.let { doScriptProtect(it) } ?: other(key, defaultValue)
    }

    private fun getPathTranslated(): Object? {
        try {
            val pc: PageContext = ThreadLocalPageContext.get()
            return pc.getBasePageSource().getResourceTranslated(pc).toString()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return ""
    }

    private fun other(key: Collection.Key?, defaultValue: Object?): Object? {
        return if (staticKeys.containsKey(key)) "" else defaultValue
    }

    private fun doScriptProtect(value: String?): String? {
        return if (isScriptProtected()) ScriptProtect.translate(value) else value
    }

    private fun toString(str: Object?): String? {
        return StringUtil.toStringEmptyIfNull(str)
    }

    @Override
    operator fun get(key: Collection.Key?): Object? {
        var value: Object = get(key, "")
        if (value == null) value = ""
        return value
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        var value: Object? = get(pc, key, "")
        if (value == null) value = ""
        return value
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return KeyIterator(keys())
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun isInitalized(): Boolean {
        return isInit
    }

    @Override
    fun initialize(pc: PageContext?) {
        isInit = true
        req = pc.getHttpServletRequest()
        if (scriptProtected == ScriptProtected.UNDEFINED) {
            scriptProtected = if (pc.getApplicationContext().getScriptProtect() and ApplicationContext.SCRIPT_PROTECT_CGI > 0) ScriptProtected.YES else ScriptProtected.NO
        }
    }

    @Override
    fun release(pc: PageContext?) {
        isInit = false
        scriptProtected = ScriptProtected.UNDEFINED
        req = null
        https = null
        headers = null
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return StructUtil.toDumpTable(this, "CGI Scope", pageContext, maxlevel, dp)
    }

    @Override
    fun getType(): Int {
        return SCOPE_CGI
    }

    @Override
    fun getTypeAsString(): String? {
        return "cgi"
    }

    @Override
    override fun isScriptProtected(): Boolean {
        return scriptProtected == ScriptProtected.YES
    }

    @Override
    override fun setScriptProtecting(ac: ApplicationContext?, scriptProtecting: Boolean) {
        scriptProtected = if (scriptProtecting) ScriptProtected.YES else ScriptProtected.NO
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        _disconnect()
        out.writeBoolean(isInit)
        out.writeObject(disconnectedData)
        out.writeInt(scriptProtected)
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        isInit = `in`.readBoolean()
        disconnectedData = `in`.readObject()
        scriptProtected = `in`.readInt()
        disconnected = true
    }

    init {
        this.setReadOnly(true)
    }
}