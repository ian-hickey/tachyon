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
package lucee.runtime.tag

import java.io.IOException

/**
 *
 */
class Login : BodyTagImpl() {
    private var idletimeout = 1800
    private var applicationtoken: String? = null
    private var cookiedomain: String? = null
    @Override
    fun release() {
        super.release()
        idletimeout = 1800
        applicationtoken = null
        cookiedomain = null
    }

    /**
     * @param applicationtoken The applicationtoken to set.
     */
    fun setApplicationtoken(applicationtoken: String?) {
        this.applicationtoken = applicationtoken
    }

    /**
     * @param cookiedomain The cookiedomain to set.
     */
    fun setCookiedomain(cookiedomain: String?) {
        this.cookiedomain = cookiedomain
    }

    /**
     * @param idletimeout The idletimout to set.
     */
    fun setIdletimeout(idletimeout: Double) {
        this.idletimeout = idletimeout.toInt()
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        val ac: ApplicationContext = pageContext.getApplicationContext()
        ac.setSecuritySettings(applicationtoken, cookiedomain, idletimeout)
        val remoteUser: Credential = pageContext.getRemoteUser()
        if (remoteUser == null) {

            // Form
            val name: Object = pageContext.formScope().get("j_username", null)
            val password: Object = pageContext.formScope().get("j_password", null)
            if (name != null) {
                setCFLogin(name, password)
                return EVAL_BODY_INCLUDE
            }
            // Header
            val strAuth: String = pageContext.getHttpServletRequest().getHeader("authorization")
            if (strAuth != null) {
                val pos: Int = strAuth.indexOf(' ')
                if (pos != -1) {
                    val format: String = strAuth.substring(0, pos).toLowerCase()
                    if (format.equals("basic")) {
                        val encoded: String = strAuth.substring(pos + 1)
                        val dec: String
                        dec = try {
                            Base64Coder.decodeToString(encoded, "UTF-8", true)
                        } catch (e: IOException) {
                            throw Caster.toPageException(e)
                        }

                        // print.ln("encoded:"+encoded);
                        // print.ln("decoded:"+Base64Util.decodeBase64(encoded));
                        val arr: Array = ListUtil.listToArray(dec, ":")
                        if (arr.size() < 3) {
                            if (arr.size() === 1) setCFLogin(arr.get(1, null), "") else setCFLogin(arr.get(1, null), arr.get(2, null))
                        }
                    }
                }
            }
            return EVAL_BODY_INCLUDE
        }
        return SKIP_BODY
    }

    /**
     * @param username
     * @param password
     */
    private fun setCFLogin(username: Object?, password: Object?) {
        var password: Object? = password
        if (username == null) return
        if (password == null) password = ""
        val sct: Struct = StructImpl()
        sct.setEL(KeyConstants._name, username)
        sct.setEL(KeyConstants._password, password)
        pageContext.undefinedScope().setEL(CFLOGIN, sct)
    }

    @Override
    fun doEndTag(): Int {
        pageContext.undefinedScope().removeEL(CFLOGIN)
        return EVAL_PAGE
    }

    companion object {
        private val CFLOGIN: Key? = KeyImpl.getInstance("cflogin")
        fun getApplicationName(appContext: ApplicationContext?): String? {
            return "cfauthorization_" + appContext.getSecurityApplicationToken()
        }

        fun getCookieDomain(appContext: ApplicationContext?): String? {
            return appContext.getSecurityCookieDomain()
        }

        fun getIdleTimeout(appContext: ApplicationContext?): Int {
            return appContext.getSecurityIdleTimeout()
        }
    }
}