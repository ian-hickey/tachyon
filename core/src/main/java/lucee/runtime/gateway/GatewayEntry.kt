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
package lucee.runtime.gateway

import org.osgi.framework.BundleException

interface GatewayEntry {
    /**
     * @return the gateway
     * @throws ClassException
     * @throws PageException
     * @throws BundleException
     */
    @Throws(ClassException::class, PageException::class, BundleException::class)
    fun createGateway(config: Config?)
    fun getGateway(): Gateway?

    /**
     * @return the id
     */
    fun getId(): String?
    // public abstract Class getClazz();
    /**
     * @return the custom
     */
    fun getCustom(): Struct?

    /**
     * @return the readOnly
     */
    fun isReadOnly(): Boolean

    /**
     * @return the cfcPath
     */
    fun getListenerCfcPath(): String?
    fun getCfcPath(): String?

    /**
     * @return the startupMode
     */
    fun getStartupMode(): Int
    fun getClassDefinition(): ClassDefinition?

    companion object {
        const val STARTUP_MODE_AUTOMATIC = 1
        const val STARTUP_MODE_MANUAL = 2
        const val STARTUP_MODE_DISABLED = 4
    }
}