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

import tachyon.commons.io.SystemUtil

class NTAuthenticate : TagImpl() {
    private var username: String? = null
    private var password: String? = null
    private var domain: String? = null
    private var result: String? = "cfntauthenticate"

    // private String _action="auth";
    private var listGroups = false
    private var throwOnError = false
    @Override
    fun release() {
        super.release()
        username = null
        password = null
        domain = null
        result = "cfntauthenticate"
        listGroups = false
        throwOnError = false

        // _action = "auth";
    }
    /*
	 * public void setListGroups(boolean b) { if(b) { listGroups = true; _action = "authAndGroups"; }
	 * else { listGroups = false; _action = "auth"; } }
	 */
    /**
     * @param username the username to set
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * @param password the password to set
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * @param domain the domain to set
     */
    fun setDomain(domain: String?) {
        this.domain = domain
    }

    /**
     * @param result the result to set
     */
    fun setResult(result: String?) {
        this.result = result
    }

    /**
     * @param listGroups the listGroups to set
     */
    fun setListgroups(listGroups: Boolean) {
        this.listGroups = listGroups
    }

    /**
     * @param throwOnError the throwOnError to set
     */
    fun setThrowonerror(throwOnError: Boolean) {
        this.throwOnError = throwOnError
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (true) throw TagNotSupported("ntauthenticate")
        val os: String = System.getProperty("os.name")
        val resultSt: Struct = StructImpl()
        pageContext.setVariable(result, resultSt)
        if (SystemUtil.isWindows()) {
            /*
			 * 
			 * NTAuthentication ntauth = new NTAuthentication(domain); if(username != null)
			 * resultSt.set("username", username); try { boolean isAuth = false;
			 * 
			 * if(ntauth.IsUserInDirectory(username) && password != null && !StringUtil.isEmpty(domain)) isAuth
			 * = ntauth.AuthenticateUser(username, password);
			 * 
			 * resultSt.set(AUTH, Caster.toBoolean(isAuth)); resultSt.set(STATUS,
			 * isAuth?"success":"AuthenticationFailure");
			 * 
			 * if(listGroups && isAuth) { String groups =
			 * tachyon.runtime.type.List.arrayToList(ntauth.GetUserGroups(username), ","); resultSt.set(GROUPS,
			 * groups); } } catch(Exception e) { resultSt.set(AUTH, Boolean.FALSE); if(e instanceof
			 * UserNotInDirException) resultSt.set(STATUS, "UserNotInDirFailure"); else if(e instanceof
			 * AuthenticationFailureException) resultSt.set(STATUS, "AuthenticationFailure");
			 * 
			 * if(throwOnError) throw new JspException(e); }
			 */
        }
        return 0
    }
}