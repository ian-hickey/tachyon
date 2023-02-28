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
package coldfusion.server

import java.util.Map

interface XmlRpcService : Service {
    fun getMappings(): Map?
    fun getUsernames(): Map?
    fun getPasswords(): Map?
    fun unregisterWebService(arg0: String?)
    fun refreshWebService(arg0: String?)
    fun registerWebService(arg0: String?, arg1: String?, arg2: String?, arg3: String?, arg4: Int, arg5: String?, arg6: String?, arg7: String?, arg8: String?)
    fun getWebService(arg0: String?, arg1: String?, arg2: String?, arg3: Int, arg4: String?, arg5: String?, arg6: String?, arg7: String?, arg8: String?): Object?

    // public abstract ServiceProxy getWebServiceProxy(String arg0, String arg1,String arg2, String
    // arg3);
    // public abstract ServiceProxy getWebServiceProxy(String arg0, String arg1,String arg2, int arg3,
    // String arg4, String arg5, String arg6,String arg7, String arg8);
    fun getClassPath(): String?
    fun setClassPath(arg0: String?)
}