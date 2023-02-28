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

import lucee.runtime.exp.PageException

/**
 * interface for the cookie scope
 */
interface Cookie : Scope, UserScope {
    /**
     * set a cookie value
     *
     * @param name name of the cookie
     * @param value value of the cookie
     * @param expires expirs of the cookie (Date, number in seconds or keyword as string )
     * @param secure set secure or not
     * @param path path of the cookie
     * @param domain domain of the cookie
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("")
    @Throws(PageException::class)
    fun setCookie(name: Collection.Key?, value: Object?, expires: Object?, secure: Boolean, path: String?, domain: String?)

    /**
     * set a cookie value
     *
     * @param name Name of the cookie
     * @param value value of the cookie
     * @param expires expires in seconds
     * @param secure secute or not
     * @param path path of the cookie
     * @param domain domain of the cookie
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("")
    @Throws(PageException::class)
    fun setCookie(name: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?)

    /**
     * set a cookie value
     *
     * @param name Name of the cookie
     * @param value value of the cookie
     * @param expires expires in seconds
     * @param secure secute or not
     * @param path path of the cookie
     * @param domain domain of the cookie
     */
    @Deprecated
    @Deprecated("")
    fun setCookieEL(name: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?)

    /**
     * set a cookie value
     *
     * @param name name of the cookie
     * @param value value of the cookie
     * @param expires expirs of the cookie (Date, number in seconds or keyword as string )
     * @param secure set secure or not
     * @param path path of the cookie
     * @param domain domain of the cookie
     * @param httpOnly if true, sets cookie as httponly so that it cannot be accessed using JavaScripts.
     * Note that the browser must have httponly compatibility.
     * @param preserveCase if true, keep the case of the name as it is
     * @param encode if true, url encode the name and the value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun setCookie(name: Collection.Key?, value: Object?, expires: Object?, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean,
                  encode: Boolean)

    /**
     * set a cookie value
     *
     * @param name Name of the cookie
     * @param value value of the cookie
     * @param expires expires in seconds
     * @param secure secute or not
     * @param path path of the cookie
     * @param domain domain of the cookie
     * @param httpOnly if true, sets cookie as httponly so that it cannot be accessed using JavaScripts.
     * Note that the browser must have httponly compatibility.
     * @param preserveCase if true, keep the case of the name as it is
     * @param encode if true, url encode the name and the value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun setCookie(name: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean,
                  encode: Boolean)

    /**
     * set a cookie value
     *
     * @param name Name of the cookie
     * @param value value of the cookie
     * @param expires expires in seconds
     * @param secure secute or not
     * @param path path of the cookie
     * @param domain domain of the cookie
     * @param httpOnly if true, sets cookie as httponly so that it cannot be accessed using JavaScripts.
     * Note that the browser must have httponly compatibility.
     * @param preserveCase if true, keep the case of the name as it is
     * @param encode if true, url encode the name and the value
     */
    fun setCookieEL(name: Collection.Key?, value: Object?, expires: Int, secure: Boolean, path: String?, domain: String?, httpOnly: Boolean, preserveCase: Boolean,
                    encode: Boolean)
}