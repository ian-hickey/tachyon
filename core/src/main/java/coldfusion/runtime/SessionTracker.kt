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
package coldfusion.runtime

import lucee.runtime.CFMLFactoryImpl

object SessionTracker {
    fun getSessionCount(): Int {
        val pc: PageContext = ThreadLocalPageContext.get()
        val sc: ScopeContext = (pc.getCFMLFactory() as CFMLFactoryImpl)!!.getScopeContext()
        return sc.getSessionCount(pc)
    }

    fun getSessionCollection(appName: String?): Struct? {
        val pc: PageContext = ThreadLocalPageContext.get()
        val sc: ScopeContext = (pc.getCFMLFactory() as CFMLFactoryImpl)!!.getScopeContext()
        return sc.getAllSessionScopes(appName)
    } /*
	 * public static coldfusion.runtime.SessionScope getSession(java.lang.String,java.lang.String)
	 * public static coldfusion.runtime.SessionScope getSession(java.lang.String) public static
	 * coldfusion.runtime.SessionScope getSession(javax.servlet.http.HttpSession,java.lang.String)
	 * public static coldfusion.runtime.SessionScope
	 * getSession(java.lang.String,java.lang.String,java.lang.String) public static
	 * coldfusion.runtime.SessionScope createSession(java.lang.String,java.lang.String) public static
	 * coldfusion.runtime.SessionScope createSession(java.lang.String,java.lang.String,java.lang.String)
	 * public static coldfusion.runtime.SessionScope
	 * createSession(javax.servlet.http.HttpSession,java.lang.String) public static void
	 * cleanUp(java.lang.String,java.lang.String,java.lang.String) public static void
	 * cleanUp(javax.servlet.http.HttpSession,java.lang.String) public static void
	 * cleanUp(java.lang.String,java.lang.String) public static java.util.Enumeration getSessionKeys()
	 * public static java.util.Hashtable getMSessionPool() public static
	 * coldfusion.runtime.AppSessionCollection getSessionCollection(java.lang.String)
	 */
}