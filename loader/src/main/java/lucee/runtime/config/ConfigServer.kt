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
package lucee.runtime.config

import java.net.MalformedURLException

/**
 * Config for the server
 */
interface ConfigServer : Config {
    /**
     * @return returns all config webs
     */
    fun getConfigWebs(): Array<ConfigWeb?>?

    /**
     * @param realpath path
     * @return returns config web matching given realpath
     */
    fun getConfigWeb(realpath: String?): ConfigWeb?

    /**
     * @return Returns the contextes.
     */
    fun getJSPFactoriesAsMap(): Map<String?, CFMLFactory?>?

    /**
     * @param id for the security manager
     * @return returns SecurityManager matching config
     */
    fun getSecurityManager(id: String?): SecurityManager?

    /**
     * is there an individual security manager for given id
     *
     * @param id for the security manager
     * @return returns SecurityManager matching config
     */
    fun hasIndividualSecurityManager(id: String?): Boolean

    /**
     * @return Returns the securityManager.
     */
    fun getDefaultSecurityManager(): SecurityManager?

    /**
     * @param updateType The updateType to set.
     */
    fun setUpdateType(updateType: String?)

    /**
     * @param updateLocation The updateLocation to set.
     */
    fun setUpdateLocation(updateLocation: URL?)

    /**
     * @param strUpdateLocation The updateLocation to set.
     * @throws MalformedURLException Malformed URL Exception
     */
    @Throws(MalformedURLException::class)
    fun setUpdateLocation(strUpdateLocation: String?)

    /**
     * @param strUpdateLocation The updateLocation to set.
     * @param defaultValue default value
     */
    fun setUpdateLocation(strUpdateLocation: String?, defaultValue: URL?)

    /**
     * @return the configListener
     */
    fun getConfigListener(): ConfigListener?

    /**
     * @param configListener the configListener to set
     */
    fun setConfigListener(configListener: ConfigListener?)

    @Override
    override fun getRemoteClients(): Array<RemoteClient?>?

    /**
     * @return returns the CFML Engine.
     */
    @Deprecated
    @Deprecated("""use instead getEngine
	  """)
    fun getCFMLEngine(): CFMLEngine?
    fun getEngine(): CFMLEngine?

    @Override
    override fun getIdentification(): IdentificationServer?
}