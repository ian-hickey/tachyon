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
package tachyon.runtime.config

import java.io.Serializable

class IdentificationWebImpl(cw: ConfigWebPro?, securityKey: String?, apiKey: String?) : IdentificationImpl(cw, securityKey, apiKey), IdentificationWeb, Serializable {
    @Transient
    private val cw: ConfigWebPro?
    @Override
    fun getServerIdentification(): IdentificationServer? {
        return (ThreadLocalPageContext.getConfig(cw) as ConfigWebImpl)!!.getConfigServerImpl()!!.getIdentification()
    }

    @Override
    fun toQueryString(): String? {
        val qs = StringBuilder()
        append(qs, "webApiKey", getApiKey())
        append(qs, "webId", getId())
        append(qs, "webSecurityKey", getSecurityKey())
        val sid: IdentificationServer? = getServerIdentification()
        append(qs, "serverApiKey", sid.getApiKey())
        append(qs, "serverId", sid.getId())
        append(qs, "serverSecurityKey", sid.getSecurityKey())
        return qs.toString()
    }

    init {
        this.cw = cw
    }
}