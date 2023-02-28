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
package tachyon.runtime.functions.xml

import tachyon.runtime.PageContext

object AddSOAPRequestHeader : Function {
    private const val serialVersionUID = 4305004275924545217L
    @Throws(PageException::class)
    fun call(pc: PageContext?, client: Object?, nameSpace: String?, name: String?, value: Object?): Boolean {
        return call(pc, client, nameSpace, name, value, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, client: Object?, nameSpace: String?, name: String?, value: Object?, mustUnderstand: Boolean): Boolean {
        if (client !is WSClient) throw FunctionException(pc, "addSOAPRequestHeader", 1, "webservice", "value must be a webservice Object generated with createObject/<cfobject>")
        (client as WSClient?).addSOAPRequestHeader(nameSpace, name, value, mustUnderstand)
        return true
    }
}