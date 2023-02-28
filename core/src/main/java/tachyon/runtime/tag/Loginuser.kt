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

import javax.servlet.jsp.tagext.Tag

/**
 *
 */
class Loginuser : TagImpl() {
    private var name: String? = null
    private var password: String? = null
    private var roles: Array<String?>?
    @Override
    fun release() {
        super.release()
        name = null
        password = null
        roles = null
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @param password The password to set.
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * @param oRoles The roles to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setRoles(oRoles: Object?) {
        roles = CredentialImpl.toRole(oRoles)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        val rolesDir: Resource = pageContext.getConfig().getConfigDir().getRealResource("roles")
        val login = CredentialImpl(name, password, roles, rolesDir)
        pageContext.setRemoteUser(login)
        var parent: Tag = getParent()
        while (parent != null && parent !is Login) {
            parent = parent.getParent()
        }
        val appContext: ApplicationContext = pageContext.getApplicationContext()
        if (parent != null) {
            val loginStorage: Int = appContext.getLoginStorage()
            val name: String = Login.getApplicationName(appContext)
            if (loginStorage == Scope.SCOPE_SESSION && pageContext.getApplicationContext().isSetSessionManagement()) pageContext.sessionScope().set(KeyImpl.init(name), login.encode()) else {
                val ac: ApplicationContext = pageContext.getApplicationContext()
                var tsExpires: TimeSpan? = AuthCookieDataImpl.DEFAULT.getTimeout()
                if (ac is ApplicationContextSupport) {
                    val acs: ApplicationContextSupport = ac as ApplicationContextSupport
                    val data: AuthCookieData = acs.getAuthCookie()
                    if (data != null) {
                        val tmp: TimeSpan = data.getTimeout()
                        if (tmp != null) tsExpires = tmp
                    }
                }
                val expires: Int
                val tmp: Long = tsExpires.getSeconds()
                expires = if (Integer.MAX_VALUE < tmp) Integer.MAX_VALUE else tmp.toInt()
                (pageContext.cookieScope() as CookieImpl).setCookie(KeyImpl.init(name), login.encode(), expires, false, "/", Login.getCookieDomain(appContext),
                        CookieData.SAMESITE_EMPTY)
            }
        }
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}