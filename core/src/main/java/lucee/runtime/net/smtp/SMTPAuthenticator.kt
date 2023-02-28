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
package lucee.runtime.net.smtp

import javax.mail.Authenticator

/**
 * This is a very simple authentication object that can be used for any transport needing basic
 * userName and password type authentication.
 *
 */
class SMTPAuthenticator(userName: String?, password: String?) : Authenticator() {
    /** Stores the login information for authentication  */
    private val authentication: PasswordAuthentication?

    /**
     * Gets the authentication object that will be used to login to the mail server.
     *
     * @return A `PasswordAuthentication` object containing the login information.
     */
    @get:Override
    protected val passwordAuthentication: PasswordAuthentication?
        protected get() = authentication

    /**
     * Default constructor
     *
     * @param userName user name to use when authentication is requested
     * @param password password to use when authentication is requested
     */
    init {
        authentication = PasswordAuthentication(userName, password)
    }
}