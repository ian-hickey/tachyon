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
package lucee.runtime.net.mail

import java.util.Properties

/**
 * SMTP Server verifier
 */
object SMTPVerifier {
    /**
     * verify mail server
     *
     * @param host
     * @param username
     * @param password
     * @param port
     * @return are the setting ok
     * @throws SMTPException
     */
    @Throws(SMTPException::class)
    fun verify(host: String?, username: String?, password: String?, port: Int): Boolean {
        return try {
            _verify(host, username, password, port)
        } catch (e: MessagingException) {

            // check user
            if (!StringUtil.isEmpty(username)) {
                try {
                    _verify(host, null, null, port)
                    throw SMTPExceptionImpl("Cannot connect to mail server, authentication settings are invalid")
                } catch (e1: MessagingException) {
                }
            }
            // check port
            if (port > 0 && port != 25) {
                try {
                    _verify(host, null, null, 25)
                    throw SMTPExceptionImpl("Cannot connect to mail server, port definition is invalid")
                } catch (e1: MessagingException) {
                }
            }
            throw SMTPExceptionImpl("can't connect to mail server")
        }
    }

    @Throws(MessagingException::class)
    private fun _verify(host: String?, username: String?, password: String?, port: Int): Boolean {
        val hasAuth: Boolean = !StringUtil.isEmpty(username)
        val props = Properties()
        props.put("mail.smtp.host", host)
        if (hasAuth) props.put("mail.smtp.auth", "true")
        if (hasAuth) props.put("mail.smtp.user", username)
        if (hasAuth) props.put("mail.transport.connect-timeout", "30")
        if (port > 0) props.put("mail.smtp.port", String.valueOf(port))
        var auth: Authenticator? = null
        if (hasAuth) auth = DefaultAuthenticator(username, password)
        val session: Session = Session.getInstance(props, auth)
        session.setDebug(true) // enable logging mail debugging output to console
        val transport: Transport = session.getTransport("smtp")
        if (hasAuth) transport.connect(host, username, password) else transport.connect()
        val rtn: Boolean = transport.isConnected()
        transport.close()
        return rtn
    }
}