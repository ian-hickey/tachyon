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
package tachyon.runtime.config

import java.io.Serializable

interface RemoteClient : Serializable {
    /**
     * @return the url
     */
    fun getUrl(): String?

    /**
     * @return the serverUsername
     */
    fun getServerUsername(): String?

    /**
     * @return the serverPassword
     */
    fun getServerPassword(): String?

    /**
     * @return the proxyData
     */
    fun getProxyData(): ProxyData?

    /**
     * @return the type
     */
    fun getType(): String?

    /**
     * @return the adminPassword
     */
    fun getAdminPassword(): String?

    /**
     * @return the securityKey
     */
    fun getSecurityKey(): String?
    fun getAdminPasswordEncrypted(): String?
    fun getLabel(): String?
    fun getUsage(): String?
    fun hasUsage(usage: String?): Boolean
    fun getId(config: Config?): String? // TODO doc
}