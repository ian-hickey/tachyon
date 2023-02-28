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
package lucee.runtime.type.scope

import java.lang.reflect.Method

/**
 * Implementation of the Cookie scope
 */
class CookieImpl
/**
 * constructor for the Cookie Scope
 */
    : ScopeSupport("cookie", SCOPE_COOKIE, Struct.TYPE_LINKED), Cookie, ScriptProtected {
    private var rsp: HttpServletResponse? = null
    private var scriptProtected: Int = ScriptProtected.UNDEFINED
    private val raw: Map<String?, String?>? = HashMap<String?, String?>()
    private var charset: String? = null
    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return try {
            set(key, value)
        } catch (e: PageException) {
            null
        }
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        raw.remove(key.getLowerString())
        if (Decision.isStruct(value)) {
            val sct: Struct = Caster.toStruct(value)
            val expires: Object = sct.get(KeyConstants._expires, null)
            val `val`: Object = sct.get(KeyConstants._value, null)
            val secure: Boolean = Caster.toBooleanValue(sct.get(KeyConstants._secure, null), false)
            val httpOnly: Boolean = Caster.toBooleanValue(sct.get(KeyConstants._httponly, null), false)
            val domain: String = Caster.toString(sct.get(KeyConstants._domain, null), null)
            val path: String = Caster.toString(sct.get(KeyConstants._path, null), null)
            val preserveCase: Boolean = Caster.toBooleanValue(sct.get(KeyConstants._preservecase, null), false)
            var encode: Boolean = Caster.toBoolean(sct.get(KeyConstants._encode, null), null)
            if (encode == null) encode = Caster.toBoolean(sct.get(KeyConstants._encodevalue, Boolean.TRUE), Boolean.TRUE)
            val samesite: Short = SessionCookieDataImpl.toSamesite(Caster.toString(sct.get(KeyConstants._SameSite, null), ""), CookieData.SAMESITE_EMPTY)
            setCookie(key, `val`, expires, secure, path, domain, httpOnly, preserveCase, encode.booleanValue(), samesite)
        } else setCookie(key, value, null, false, "/", null, false, false, true, CookieData.SAMESITE_EMPTY)
        return value
    }

    private operator fun set(config: Config?, cookie: javax.servlet.http.Cookie?) {
        val name: String = StringUtil.toLowerCase(ReqRspUtil.decode(cookie.getName(), charset, false))
        if (!raw!!.containsKey(name) || !StringUtil.isEmpty(cookie.getPath())) {
            // when there are multiple cookies with the same name let the cookies with a path overwrite a cookie
            // without a path.
            raw.put(name, cookie.getValue())
            if (isScriptProtected()) super.setEL(KeyImpl.init(name), ScriptProtect.translate(dec(cookie.getValue()))) else super.setEL(KeyImpl.init(name), dec(cookie.getValue()))
        }
    }

    @Override
    fun clear() {
        raw.clear()
        val keys: Array<Collection.Key?> = keys()
        for (i in keys.indices) {
            removeEL(keys[i], false)
        }
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        raw.remove(key.getLowerString())
        return remove(key, true)
    }

    @Throws(PageException::class)
    fun remove(key: Collection.Key?, alsoInResponse: Boolean): Object? {
        raw.remove(key.getLowerString())
        val obj: Object = super.remove(key)
        if (alsoInResponse) removeCookie(key)
        return obj
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return removeEL(key, true)
    }

    private fun removeEL(key: Collection.Key?, alsoInResponse: Boolean): Object? {
        raw.remove(key.getLowerString())
        val obj: Object = super.removeEL(key)
        if (obj != null && alsoInResponse) removeCookie(key)
        return obj
    }

    private fun removeCookie(key: Collection.Key?) {
        ReqRspUtil.removeCookie(rsp, key.getUpperString())
    }

    @Override
    @Throws(PageException::class)
    fun setCookie(key: Collection.Key?, value: Object?, expires: Object?, secure: Boolean, path: String?, domain: String?) {
        setCookie(key, value, expires, secure, path, domain, false, false, true, CookieData.SAMESITE_EMPTY)
    }

    // FUTURE add to interface
    @Throws(PageException::class)
    fun setCookie(key: Collection.Key?, value: Object?, expires: Object?, secure: Boolean, path: String?, domain: String?, samesite: Short) {
        setCookie(key, value, expires, secure, path, domain, false, false, true, samesite)
    }

    @Override
    @Throws(PageException::class)
    fun setCookie(key: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?) {
        setCookie(key, value, expires, secure, path, domain, false, false, true, CookieData.SAMESITE_EMPTY)
    }

    // FUTURE add to interface
    @Throws(PageException::class)
    fun setCookie(key: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?, samesite: Short) {
        setCookie(key, value, expires, secure, path, domain, false, false, true, samesite)
    }

    @Override
    fun setCookieEL(key: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?) {
        setCookieEL(key, value, expires, secure, path, domain, false, false, true, CookieData.SAMESITE_EMPTY)
    }

    // FUTURE add to interface
    fun setCookieEL(key: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?, samesite: Short) {
        setCookieEL(key, value, expires, secure, path, domain, false, false, true, samesite)
    }

    @Override
    @Throws(PageException::class)
    fun setCookie(key: Collection.Key?, value: Object?, expires: Object?, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean, encode: Boolean) {
        setCookie(key, value, expires, secure, path, domain, httpOnly, preserveCase, encode, CookieData.SAMESITE_EMPTY)
    }

    // FUTURE add to interface
    @Throws(PageException::class)
    fun setCookie(key: Collection.Key?, value: Object?, expires: Object?, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean, encode: Boolean?,
                  samesite: Short) {
        var exp = EXPIRES_NULL

        // expires
        if (expires == null) {
            exp = EXPIRES_NULL
        } else if (expires is Date) {
            exp = toExpires(expires as Date?)
        } else if (expires is TimeSpan) {
            exp = toExpires(expires as TimeSpan?)
        } else if (expires is String) {
            exp = toExpires(expires as String?)
        } else if (Decision.isNumber(expires)) {
            exp = toExpires(Caster.toDoubleValue(expires))
        } else {
            throw ExpressionException("invalid type [" + Caster.toClassName(expires).toString() + "] for expires")
        }
        _addCookie(key, Caster.toString(value), exp, secure, path, domain, httpOnly, preserveCase, encode, samesite)
        super.set(key, value)
    }

    @Override
    @Throws(PageException::class)
    fun setCookie(key: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean, encode: Boolean) {
        _addCookie(key, Caster.toString(value), expires, secure, path, domain, httpOnly, preserveCase, encode, CookieData.SAMESITE_EMPTY)
        super.set(key, value)
    }

    // FUTURE add to interface
    @Throws(PageException::class)
    fun setCookie(key: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean, encode: Boolean,
                  samesite: Short) {
        _addCookie(key, Caster.toString(value), expires, secure, path, domain, httpOnly, preserveCase, encode, samesite)
        super.set(key, value)
    }

    @Override
    fun setCookieEL(key: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean, encode: Boolean) {
        _addCookie(key, Caster.toString(value, ""), expires, secure, path, domain, httpOnly, preserveCase, encode, CookieData.SAMESITE_EMPTY)
        super.setEL(key, value)
    }

    // FUTURE add to interface
    fun setCookieEL(key: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean, encode: Boolean,
                    samesite: Short) {
        _addCookie(key, Caster.toString(value, ""), expires, secure, path, domain, httpOnly, preserveCase, encode, samesite)
        super.setEL(key, value)
    }

    private fun _addCookie(key: Key?, value: String?, expires: Int, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean, encode: Boolean?,
                           samesite: Short) {
        val name: String = if (preserveCase) key.getString() else key.getUpperString()

        // build the value
        val sb = StringBuilder()
        /* Name */sb.append(enc(name)).append('=').append(if (encode == null) enc(value) else enc(value, encode.booleanValue()))
        /* Path */sb.append(";Path=").append(enc(path))
        /* Domain */if (!StringUtil.isEmpty(domain)) sb.append(";Domain=").append(enc(domain))
        /* Expires */if (expires != EXPIRES_NULL) sb.append(";Expires=").append(DateTimeUtil.toHTTPTimeString(System.currentTimeMillis() + expires * 1000L, false))
        /* Secure */if (secure) sb.append(";Secure")
        /* HTTPOnly */if (httpOnly) sb.append(";HttpOnly")
        val tmpSameSite: String = SessionCookieDataImpl.toSamesite(samesite)
        /* Samesite */if (!StringUtil.isEmpty(tmpSameSite, true)) sb.append(";SameSite").append('=').append(tmpSameSite)
        rsp.addHeader("Set-Cookie", sb.toString())
    }

    /*
	 * private void _addCookieOld(Key key, String value, int expires, boolean secure, String path,
	 * String domain, boolean httpOnly, boolean preserveCase, boolean encode) { String
	 * name=preserveCase?key.getString():key.getUpperString(); if(encode) { name=enc(name);
	 * value=enc(value); }
	 * 
	 * javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(name,value);
	 * cookie.setMaxAge(expires); cookie.setSecure(secure); cookie.setPath(path);
	 * if(!StringUtil.isEmpty(domain,true))cookie.setDomain(domain); if(httpOnly) setHTTPOnly(cookie);
	 * rsp.addCookie(cookie);
	 * 
	 * }
	 */
    @Throws(ExpressionException::class)
    private fun toExpires(expires: String?): Int {
        val str: String = StringUtil.toLowerCase(expires.toString())
        return if (str.equals("now")) 0 else if (str.equals("never")) NEVER else {
            val dt: DateTime = DateCaster.toDateAdvanced(expires, DateCaster.CONVERTING_TYPE_NONE, null, null)
            if (dt != null) {
                toExpires(dt)
            } else toExpires(Caster.toDoubleValue(expires))
        }
    }

    private fun toExpires(expires: Double): Int {
        return Caster.toIntValue(expires * 24 * 60 * 60)
    }

    private fun toExpires(expires: Date?): Int {
        val diff: Double = expires.getTime() - System.currentTimeMillis()
        return Math.round(diff / 1000.0)
    }

    private fun toExpires(span: TimeSpan?): Int {
        return span.getSeconds()
    }

    @Override
    override fun initialize(pc: PageContext?) {
        val config: Config = ThreadLocalPageContext.getConfig(pc)
        charset = pc.getWebCharset().name()
        if (scriptProtected == ScriptProtected.UNDEFINED) {
            val ac: ApplicationContext = pc.getApplicationContext()
            val sp: Int = if (ac != null) ac.getScriptProtect() else config.getScriptProtect()
            scriptProtected = if (sp and ApplicationContext.SCRIPT_PROTECT_COOKIE > 0) ScriptProtected.YES else ScriptProtected.NO
        }
        super.initialize(pc)
        val req: HttpServletRequest = pc.getHttpServletRequest()
        rsp = pc.getHttpServletResponse()
        val cookies: Array<javax.servlet.http.Cookie?> = ReqRspUtil.getCookies(req, pc.getWebCharset())
        for (i in cookies.indices) {
            set(config, cookies[i])
        }
    }

    @Override
    override fun release(pc: PageContext?) {
        raw.clear()
        scriptProtected = ScriptProtected.UNDEFINED
        super.release(pc)
    }

    @Override
    override fun isScriptProtected(): Boolean {
        return scriptProtected == ScriptProtected.YES
    }

    @Override
    override fun setScriptProtecting(ac: ApplicationContext?, scriptProtected: Boolean) {
        val _scriptProtected: Int = if (scriptProtected) ScriptProtected.YES else ScriptProtected.NO
        if (isInitalized() && _scriptProtected != this.scriptProtected) {
            val it: Iterator<Entry<String?, String?>?> = raw.entrySet().iterator()
            var entry: Entry<String?, String?>?
            var key: String
            var value: String?
            while (it.hasNext()) {
                entry = it.next()
                key = entry.getKey().toString()
                value = dec(entry.getValue().toString())
                super.setEL(KeyImpl.init(key), if (scriptProtected) ScriptProtect.translate(value) else value)
            }
        }
        this.scriptProtected = _scriptProtected
    }

    fun dec(str: String?): String? {
        return ReqRspUtil.decode(str, charset, false)
    }

    fun enc(str: String?, encode: Boolean): String? {
        return if (encode) ReqRspUtil.encode(str, charset) else str
    }

    fun enc(str: String?): String? {
        return if (ReqRspUtil.needEncoding(str, false)) enc(str, true) else enc(str, false)
    }

    @Override
    fun resetEnv(pc: PageContext?) {
    }

    @Override
    fun touchBeforeRequest(pc: PageContext?) {
    }

    @Override
    fun touchAfterRequest(pc: PageContext?) {
    }

    companion object {
        private const val serialVersionUID = -2341079090783313736L
        const val NEVER = 946626690
        private val IS_HTTP_ONLY_ARGS_CLASSES: Array<Class<*>?>? = arrayOf<Class?>()
        private val IS_HTTP_ONLY_ARGS: Array<Object?>? = arrayOf<Object?>()
        private val SET_HTTP_ONLY_ARGS_CLASSES: Array<Class<*>?>? = arrayOf<Class?>(Boolean::class.javaPrimitiveType)
        private val SET_HTTP_ONLY_ARGS: Array<Object?>? = arrayOf<Object?>(Boolean.TRUE)
        private const val EXPIRES_NULL = -1
        private var isHttpOnly: Method? = null
        private var setHttpOnly: Method? = null
        fun setHTTPOnly(cookie: javax.servlet.http.Cookie?) {
            try {
                if (setHttpOnly == null) {
                    setHttpOnly = cookie.getClass().getMethod("setHttpOnly", SET_HTTP_ONLY_ARGS_CLASSES)
                }
                setHttpOnly.invoke(cookie, SET_HTTP_ONLY_ARGS)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        fun isHTTPOnly(cookie: javax.servlet.http.Cookie?): Boolean {
            return try {
                if (isHttpOnly == null) {
                    isHttpOnly = cookie.getClass().getMethod("isHttpOnly", IS_HTTP_ONLY_ARGS_CLASSES)
                }
                Caster.toBooleanValue(isHttpOnly.invoke(cookie, IS_HTTP_ONLY_ARGS))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                false
            }
        }
    }
}