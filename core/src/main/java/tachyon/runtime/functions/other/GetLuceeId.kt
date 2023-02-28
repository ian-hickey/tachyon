/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.functions.other

import tachyon.runtime.PageContext

/**
 * Implements the CFML Function createGuid
 */
object GetTachyonId : Function {
    private const val serialVersionUID = 105306626462365773L
    private val SECURITY_KEY: Collection.Key? = KeyImpl.getInstance("securityKey")
    private val API_KEY: Collection.Key? = KeyImpl.getInstance("apiKey")
    @Throws(PageException::class)
    fun call(pc: PageContext?): Struct? {
        val sct: Struct = StructImpl()
        val web: Struct = StructImpl()
        val server: Struct = StructImpl()
        val idw: IdentificationWeb = pc.getConfig().getIdentification()
        val ids: IdentificationServer = idw.getServerIdentification()

        // Web
        web.set(SECURITY_KEY, idw.getSecurityKey())
        web.set(KeyConstants._id, idw.getId())
        web.set(API_KEY, idw.getApiKey())
        sct.set(KeyConstants._web, web)

        // Server
        server.set(SECURITY_KEY, ids.getSecurityKey())
        server.set(KeyConstants._id, ids.getId())
        server.set(API_KEY, ids.getApiKey())
        sct.set(KeyConstants._server, server)
        sct.set(KeyConstants._request, Caster.toString(pc.getId()))
        return sct
    }
}