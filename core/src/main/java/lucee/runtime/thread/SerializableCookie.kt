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
package lucee.runtime.thread

import java.io.Serializable

class SerializableCookie : Serializable {
    private var comment: String?
    private var domain: String?
    private var maxAge: Int
    private var name: String?
    private var path: String?
    private var secure: Boolean
    private var value: String?
    private var version: Int
    private var httpOnly: Boolean

    constructor(comment: String?, domain: String?, maxAge: Int, name: String?, path: String?, secure: Boolean, value: String?, version: Int, httpOnly: Boolean) {
        this.comment = comment
        this.domain = domain
        this.maxAge = maxAge
        this.name = name
        this.path = path
        this.secure = secure
        this.value = value
        this.version = version
        this.httpOnly = httpOnly
    }

    constructor(cookie: Cookie?) {
        comment = cookie.getComment()
        domain = cookie.getDomain()
        maxAge = cookie.getMaxAge()
        name = cookie.getName()
        path = cookie.getPath()
        secure = cookie.getSecure()
        value = cookie.getValue()
        version = cookie.getVersion()
        httpOnly = CookieImpl.isHTTPOnly(cookie)
    }

    fun getComment(): String? {
        return comment
    }

    fun getDomain(): String? {
        return domain
    }

    fun getMaxAge(): Int {
        return maxAge
    }

    fun getName(): String? {
        return name
    }

    fun getPath(): String? {
        return path
    }

    fun getSecure(): Boolean {
        return secure
    }

    fun getValue(): String? {
        return value
    }

    fun getVersion(): Int {
        return version
    }

    fun isHttpOnly(): Boolean {
        return httpOnly
    }

    fun setComment(purpose: String?) {
        comment = purpose
    }

    fun setDomain(pattern: String?) {
        domain = pattern
    }

    fun setMaxAge(expiry: Int) {
        maxAge = expiry
    }

    fun setPath(uri: String?) {
        path = uri
    }

    fun setSecure(secure: Boolean) {
        this.secure = secure
    }

    fun setValue(value: String?) {
        this.value = value
    }

    fun setVersion(version: Int) {
        this.version = version
    }

    fun setHttpOnly(httpOnly: Boolean) {
        this.httpOnly = httpOnly
    }

    fun toCookie(): Cookie? {
        val c = Cookie(name, value)
        if (comment != null) c.setComment(comment)
        if (domain != null) c.setDomain(domain)
        c.setMaxAge(maxAge)
        if (path != null) c.setPath(path)
        c.setSecure(secure)
        c.setVersion(version)
        if (httpOnly) CookieImpl.setHTTPOnly(c)
        return c
    }

    companion object {
        private const val serialVersionUID = -7167614871212402517L
        fun toCookies(src: Array<SerializableCookie?>?): Array<Cookie?>? {
            if (src == null) return arrayOfNulls<Cookie?>(0)
            val dest: Array<Cookie?> = arrayOfNulls<Cookie?>(src.size)
            for (i in src.indices) {
                dest[i] = src[i]!!.toCookie()
            }
            return dest
        }

        fun toSerializableCookie(src: Array<Cookie?>?): Array<SerializableCookie?>? {
            if (src == null) return arrayOfNulls<SerializableCookie?>(0)
            val dest = arrayOfNulls<SerializableCookie?>(src.size)
            for (i in src.indices) {
                dest[i] = SerializableCookie(src[i])
            }
            return dest
        }
    }
}