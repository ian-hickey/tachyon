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
package tachyon.runtime.tag

import tachyon.runtime.exp.ApplicationException

/**
 * Defines cookie variables, including expiration and security options.
 *
 *
 *
 */
class Cookie : TagImpl() {
    /**
     * Yes or No. Specifies that the variable must transmit securely. If the browser does not support
     * Secure Socket Layer (SSL) security, the cookie is not sent.
     */
    private var secure = false

    /** The value assigned to the cookie variable.  */
    private var value: String? = ""

    /**   */
    private var domain: String? = null

    /**   */
    private var path: String? = "/"

    /**
     * Schedules the expiration of a cookie variable. Can be specified as a date (as in, 10/09/97),
     * number of days (as in, 10, 100), "Now", or "Never". Using Now effectively deletes the cookie from
     * the client browser.
     */
    private var expires: Object? = null

    /** The name of the cookie variable.  */
    private var name: String? = null
    private var httponly = false
    private var preservecase = false
    private var encode: Boolean? = null
    private var samesite: Short = SessionCookieData.SAMESITE_EMPTY
    @Override
    fun release() {
        super.release()
        secure = false
        value = ""
        domain = null
        path = "/"
        expires = null
        name = null
        httponly = false
        preservecase = false
        encode = null
        samesite = SessionCookieData.SAMESITE_EMPTY
    }

    /**
     * set the value secure Yes or No. Specifies that the variable must transmit securely. If the
     * browser does not support Secure Socket Layer (SSL) security, the cookie is not sent.
     *
     * @param secure value to set
     */
    fun setSecure(secure: Boolean) {
        this.secure = secure
    }

    /**
     * set the value value The value assigned to the cookie variable.
     *
     * @param value value to set
     */
    fun setValue(value: String?) {
        this.value = value
    }

    /**
     * set the value domain
     *
     * @param domain value to set
     */
    fun setDomain(domain: String?) {
        this.domain = domain
    }

    /**
     * set the value path
     *
     * @param path value to set
     */
    fun setPath(path: String?) {
        this.path = path
    }

    /**
     * set the value expires Schedules the expiration of a cookie variable. Can be specified as a date
     * (as in, 10/09/97), number of days (as in, 10, 100), "Now", or "Never". Using Now effectively
     * deletes the cookie from the client browser.
     *
     * @param expires value to set
     */
    fun setExpires(expires: Object?) {
        this.expires = expires
    }

    /**
     * set the value expires Schedules the expiration of a cookie variable. Can be specified as a date
     * (as in, 10/09/97), number of days (as in, 10, 100), "Now", or "Never". Using Now effectively
     * deletes the cookie from the client browser.
     *
     * @param expires value to set
     */
    @Deprecated
    @Deprecated("replaced with setExpires(Object expires):void")
    fun setExpires(expires: String?) {
        this.expires = expires
    }

    /**
     * set the value name The name of the cookie variable.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    fun setHttponly(httponly: Boolean) {
        this.httponly = httponly
    }

    fun setPreservecase(preservecase: Boolean) {
        this.preservecase = preservecase
    }

    fun setEncodevalue(encode: Boolean) {
        this.encode = encode
    }

    fun setEncode(encode: Boolean) {
        this.encode = encode
    }

    @Throws(ApplicationException::class)
    fun setSamesite(samesite: String?) {
        this.samesite = SessionCookieDataImpl.toSamesite(samesite)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        val key: Key = KeyImpl.getInstance(name)
        val appName: String = Login.getApplicationName(pageContext.getApplicationContext())
        var isAppName = false
        if (KeyConstants._CFID.equalsIgnoreCase(key) || KeyConstants._CFTOKEN.equalsIgnoreCase(key) || key.equals(appName).also { isAppName = it }) {
            val ac: ApplicationContext = pageContext.getApplicationContext()
            if (ac is ApplicationContextSupport) {
                val acs: ApplicationContextSupport = ac as ApplicationContextSupport
                val data: CookieData = if (isAppName) acs.getAuthCookie() else acs.getSessionCookie()
                if (data != null && data.isDisableUpdate()) throw ExpressionException("customize $key is disabled!")
            }
        }
        (pageContext.cookieScope() as CookieImpl).setCookie(key, value, expires, secure, path, domain, httponly, preservecase, encode, samesite)
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}