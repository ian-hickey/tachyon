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
class CGIImpl : StructSupport(), CGI, ScriptProtected, Externalizable {
    companion object {
        private const val serialVersionUID = 5219795840777155232L
        private val STATIC_KEYS: Array<Collection.Key?>? = arrayOf<Collection.Key?>(KeyConstants._auth_password, KeyConstants._auth_type, KeyConstants._auth_user, KeyConstants._cert_cookie,
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

        init {
            for (i in STATIC_KEYS.indices) {
                staticKeys.setEL(STATIC_KEYS!![i], "")
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
    private var internal: Struct? = null
    private var aliases: Map<Collection.Key?, Collection.Key?>? = null
    private var scriptProtected = 0
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

        // if(internal==null) {
        internal = StructImpl()
        aliases = HashMap<Collection.Key?, Collection.Key?>()
        var k: String
        var v: String?
        var key: Collection.Key
        var alias: Collection.Key?
        var httpKey: Collection.Key
        try {
            val e: Enumeration<String?> = req.getHeaderNames()
            while (e.hasMoreElements()) {
                // keys
                k = e.nextElement()
                key = KeyImpl.init(k)
                alias = if (k.contains("-")) KeyImpl.init(k.replace('-', '_')) else null
                httpKey = KeyImpl.init("http_" + (if (alias == null) key else alias).getString().toLowerCase())

                // set value
                v = doScriptProtect(req.getHeader(k))
                internal.setEL(httpKey, v)

                // set alias keys
                aliases.put(key, httpKey)
                if (alias != null) aliases.put(alias, httpKey)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        // }
    }

    @Override
    fun release(pc: PageContext?) {
        isInit = false
        scriptProtected = ScriptProtected.UNDEFINED
        req = null
        internal = null
        aliases = null
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return internal.containsKey(key) || staticKeys.containsKey(key) || aliases!!.containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return internal.containsKey(key) || staticKeys.containsKey(key) || aliases!!.containsKey(key)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        val it: Iterator<Object?> = internal.valueIterator()
        while (it.hasNext()) {
            if (it.next().equals(value)) return true
        }
        return false
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val sct: Struct = StructImpl()
        StructImpl.copy(this, sct, deepCopy)
        return sct
    }

    @Override
    fun size(): Int {
        return keys()!!.size
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        val set: Set<Collection.Key?> = HashSet<Collection.Key?>()
        var it: Iterator<Key?> = internal.keyIterator()
        while (it.hasNext()) set.add(it.next())
        it = staticKeys.keyIterator()
        while (it.hasNext()) set.add(it.next())
        return set.toArray(arrayOfNulls<Collection.Key?>(set.size()))
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(null, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {

        // do we have internal?
        val _null: Object = NullSupportHelper.NULL(pc)
        var res: Object = internal.get(pc, key, _null)
        if (res !== _null) return res

        // do we have an alias
        run {
            val k: Key? = aliases!![key]
            if (k != null) {
                res = internal.get(pc, k, _null)
                if (res !== _null) return res
            }
        }
        if (req != null) {
            val lkey: String = key.getLowerString()
            val first: Char = lkey.charAt(0)
            try {
                if (first == 'a') {
                    if (key.equals(KeyConstants._auth_type)) return store(key, toString(req.getAuthType()))
                } else if (first == 'c') {
                    if (key.equals(KeyConstants._context_path)) return store(key, toString(req.getContextPath()))
                    if (key.equals(KeyConstants._cf_template_path)) return store(key, getPathTranslated())
                } else if (first == 'h') {

                    // _http_if_modified_since
                    if (key.equals(KeyConstants._http_if_modified_since)) {
                        val o: Object = internal.get(KeyConstants._last_modified, _null)
                        if (o !== _null) return store(key, o as String)
                    } else if (key.equals(KeyConstants._https)) return store(key, if (req.isSecure()) "on" else "off")
                } else if (first == 'r') {
                    if (key.equals(KeyConstants._remote_user)) return store(key, toString(req.getRemoteUser()))
                    if (key.equals(KeyConstants._remote_addr)) return store(key, toString(req.getRemoteAddr()))
                    if (key.equals(KeyConstants._remote_host)) return store(key, toString(req.getRemoteHost()))
                    if (key.equals(KeyConstants._request_method)) return store(key, req.getMethod())
                    if (key.equals(KeyConstants._request_url)) return store(key, ReqRspUtil.getRequestURL(req, true))
                    if (key.equals(KeyConstants._request_uri)) return store(key, toString(req.getAttribute("javax.servlet.include.request_uri")))
                    // we do not store this, to be as backward compatible as possible.
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
                    if (key.equals(KeyConstants._local_addr)) return store(key, toString(localAddress))
                    if (key.equals(KeyConstants._local_host)) return store(key, toString(localHost))
                } else if (first == 's') {
                    if (key.equals(KeyConstants._script_name)) return store(key, ReqRspUtil.getScriptName(null, req))
                    if (key.equals(KeyConstants._server_name)) return store(key, toString(req.getServerName()))
                    if (key.equals(KeyConstants._server_protocol)) return store(key, toString(req.getProtocol()))
                    if (key.equals(KeyConstants._server_port)) return store(key, Caster.toString(req.getServerPort()))
                    if (key.equals(KeyConstants._server_port_secure)) return store(key, if (req.isSecure()) "1" else "0")
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
                        return if (!StringUtil.isEmpty(pathInfo, true)) store(key, pathInfo) else ""
                    }
                    if (key.equals(KeyConstants._path_translated)) return store(key, getPathTranslated())
                } else if (first == 'q') {
                    if (key.equals(KeyConstants._query_string)) return store(key, doScriptProtect(toString(ReqRspUtil.getQueryString(req))))
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        return other(key, defaultValue)
    }

    private fun store(key: Key?, value: String?): Object? {
        internal.setEL(key, value)
        return value
    }

    private fun other(key: Collection.Key?, defaultValue: Object?): Object? {
        return if (staticKeys.containsKey(key)) "" else defaultValue
    }

    private fun getPathTranslated(): String? {
        try {
            val pc: PageContext = ThreadLocalPageContext.get()
            return pc.getBasePageSource().getResourceTranslated(pc).toString()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return ""
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
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return StructUtil.toDumpTable(this, "CGI Scope (writable)", pageContext, maxlevel, dp)
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
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        var key: Key? = key
        val k: Key = aliases.remove(key)
        if (k != null) key = k
        val rtn: Object = internal.remove(key)
        if (staticKeys.containsKey(key)) internal.set(key, "") // we do this to avoid to this get reinit again
        return rtn
    }

    @Override
    fun removeEL(key: Key?): Object? {
        var key: Key? = key
        val k: Key = aliases.remove(key)
        if (k != null) key = k
        val rtn: Object = internal.removeEL(key)
        if (staticKeys.containsKey(key)) internal.setEL(key, "") // we do this to avoid to this get reinit again
        return rtn
    }

    @Override
    fun clear() {
        val keys: Array<Key?>? = keys()
        for (i in keys.indices) {
            removeEL(keys!![i])
        }
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        var key: Key? = key
        val k: Key? = aliases!![key]
        if (k != null) key = k
        return internal.set(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        var key: Key? = key
        val k: Key? = aliases!![key]
        if (k != null) key = k
        return internal.setEL(key, value)
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return internal.valueIterator()
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        duplicate(false) // we make this to store everything into internal
        out.writeBoolean(isInit)
        out.writeObject(internal)
        out.writeObject(aliases)
        out.writeInt(scriptProtected)
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        isInit = `in`.readBoolean()
        internal = `in`.readObject() as Struct
        aliases = `in`.readObject()
        scriptProtected = `in`.readInt()
    }
}