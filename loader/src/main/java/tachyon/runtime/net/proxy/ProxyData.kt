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
package tachyon.runtime.net.proxy

import java.io.Serializable

interface ProxyData : Serializable {
    fun release()
    /**
     * @return the password
     */
    /**
     * @param password the password to set
     */
    var password: String?
    /**
     * @return the port
     */
    /**
     * @param port the port to set
     */
    var port: Int
    /**
     * @return the server
     */
    /**
     * @param server the server to set
     */
    var server: String?
    /**
     * @return the username
     */
    /**
     * @param username the username to set
     */
    var username: String?
}