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
package tachyon.runtime.net.mail

import kotlin.Throws
import tachyon.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 * DTO of a single Mailserver
 */
interface Server {
    /**
     * @return Returns the password.
     */
    val password: String?

    /**
     * @return Returns the port.
     */
    val port: Int

    /**
     * @return Returns the server.
     */
    val hostName: String?

    /**
     * @return Returns the username.
     */
    val username: String?

    /**
     * @return Returns if it has authentication or not
     */
    fun hasAuthentication(): Boolean

    /**
     * @return clone the DataSource as ReadOnly
     */
    fun cloneReadOnly(): Server?

    /**
     * @return Returns the readOnly.
     */
    val isReadOnly: Boolean

    /**
     * verify the server properties
     *
     * @return is ok
     * @throws SMTPException SMTP Exception
     */
    @Throws(SMTPException::class)
    fun verify(): Boolean

    /**
     * @return is tls
     */
    val isTLS: Boolean

    /**
     * @return is ssl
     */
    val isSSL: Boolean

    companion object {
        const val DEFAULT_PORT = 25
    }
}