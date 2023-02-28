/**
 * Copyright (c) 2023, TachyonCFML.org
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

import tachyon.commons.lang.ExceptionUtil

class RemoteClientImpl(private val label: String?, type: String?, private val url: String?, private val serverUsername: String?, private val serverPassword: String?, adminPassword: String?, proxyData: ProxyData?, securityKey: String?,
                       usage: String?) : RemoteClient {
    private val proxyData: ProxyData?
    private val type: String?
    private val adminPassword: String?
    private val securityKey: String?
    private val usage: String?
    private var id: String? = null

    /**
     * @return the url
     */
    @Override
    fun getUrl(): String? {
        return url
    }

    /**
     * @return the serverUsername
     */
    @Override
    fun getServerUsername(): String? {
        return serverUsername
    }

    /**
     * @return the serverPassword
     */
    @Override
    fun getServerPassword(): String? {
        return serverPassword
    }

    /**
     * @return the proxyData
     */
    @Override
    fun getProxyData(): ProxyData? {
        return proxyData
    }

    /**
     * @return the type
     */
    @Override
    fun getType(): String? {
        return type
    }

    /**
     * @return the adminPassword
     */
    @Override
    fun getAdminPassword(): String? {
        return adminPassword
    }

    /**
     * @return the securityKey
     */
    @Override
    fun getSecurityKey(): String? {
        return securityKey
    }

    @Override
    fun getAdminPasswordEncrypted(): String? {
        return try {
            Encrypt.invoke(getAdminPassword(), getSecurityKey(), CFMXCompat.ALGORITHM_NAME, "uu", null, 0, true)
        } catch (e: PageException) {
            null
        }
    }

    @Override
    fun getLabel(): String? {
        return label
    }

    @Override
    fun getUsage(): String? {
        return usage
    }

    @Override
    fun hasUsage(usage: String?): Boolean {
        return ListUtil.listFindNoCaseIgnoreEmpty(this.usage, usage, ',') !== -1
    }

    @Override
    fun getId(config: Config?): String? {
        if (id != null) return id
        val attrColl: Struct = StructImpl()
        attrColl.setEL(KeyConstants._action, "getToken")
        val args: Struct = StructImpl()
        args.setEL(KeyConstants._type, getType())
        args.setEL(RemoteClientTask.PASSWORD, getAdminPasswordEncrypted())
        args.setEL(RemoteClientTask.CALLER_ID, "undefined")
        args.setEL(RemoteClientTask.ATTRIBUTE_COLLECTION, attrColl)
        return try {
            val rpc: WSClient = (ThreadLocalPageContext.getConfig(config) as ConfigWebPro)!!.getWSHandler().getWSClient(getUrl(), getServerUsername(), getServerPassword(), getProxyData())
            val result: Object = rpc.callWithNamedValues(config, KeyConstants._invoke, args)
            IdentificationImpl.createId(securityKey, Caster.toString(result, null), false, null).also { id = it }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            null
        }
    }

    init {
        this.proxyData = proxyData
        this.type = type
        this.adminPassword = adminPassword
        this.securityKey = securityKey
        this.usage = usage
    }
}