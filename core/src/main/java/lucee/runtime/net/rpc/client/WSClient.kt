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
package lucee.runtime.net.rpc.client

import org.w3c.dom.Node

interface WSClient : Objects, Iteratorable {
    @Throws(PageException::class)
    fun addHeader(header: Object?) // Object instead of header because Java 11 no longer support javax.xml.soap.SOAPHeaderElement

    @Throws(PageException::class)
    fun callWithNamedValues(config: Config?, methodName: Collection.Key?, arguments: Struct?): Object?

    @Throws(PageException::class)
    fun addSOAPRequestHeader(namespace: String?, name: String?, value: Object?, mustUnderstand: Boolean)

    @get:Throws(PageException::class)
    val sOAPRequest: Node?

    @get:Throws(PageException::class)
    val sOAPResponse: Node?

    @Throws(PageException::class)
    fun getSOAPResponseHeader(pc: PageContext?, namespace: String?, name: String?, asXML: Boolean): Object?
    val wSHandler: WSHandler?
}