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
package lucee.runtime.cfx.customtag

import java.lang.reflect.Method

class CPPCustomTag(private val serverLibrary: String?, private val procedure: String?, private val keepAlive: Boolean) : CustomTag {
    @Override
    @Throws(Exception::class)
    fun processRequest(request: Request?, response: Response?) {
        processRequest.invoke(null, arrayOf(serverLibrary, procedure, request, response, keepAlive))
        // CFXNativeLib.processRequest(serverLibrary, procedure, request, response, keepAlive);
    }

    companion object {
        // this is loaded dynamic, because the lib is optional
        private var processRequest: Method? = null
    }

    init {
        if (processRequest == null) {
            var clazz: Class? = null
            clazz = try {
                ClassUtil.loadClass("com.naryx.tagfusion.cfx.CFXNativeLib")
            } catch (e: ClassException) {
                throw CFXTagException("cannot initialize C++ Custom tag library, make sure you have added all the required jar files. "
                        + "GO to the Lucee Server Administrator and on the page Services/Update, click on \"Update JARs\"")
            }
            try {
                processRequest = clazz.getMethod("processRequest", arrayOf<Class?>(String::class.java, String::class.java, Request::class.java, Response::class.java, Boolean::class.javaPrimitiveType))
            } catch (e: NoSuchMethodException) {
                throw CFXTagException(e)
            }
        }
    }
}