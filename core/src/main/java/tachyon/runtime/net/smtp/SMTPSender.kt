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
package tachyon.runtime.net.smtp

import javax.mail.Address

class SMTPSender(lock: Object?, mmas: MimeMessageAndSession?, host: String?, port: Int, user: String?, pass: String?, reuseConnection: Boolean) : Thread() {
    /**
     * @return was message sent
     */
    var isSent = false
        private set
    private var throwable: Exception? = null
    private val lock: Object?
    private val host: String?
    private val port: Int
    private val user: String?
    private var pass: String?
    private val mmas: MimeMessageAndSession?
    private val recyleConnection: Boolean
    @Override
    fun run() {
        var transport: Transport? = null
        try {
            transport = mmas.session.transport // SMTPConnectionPool.getTransport(session,host,port,user,pass);
            if (user == null) pass = null
            // connect
            if (!transport.isConnected()) transport.connect(host, port, user, pass)
            mmas.message.saveChanges()
            if (!StringUtil.isEmpty(mmas.messageId)) mmas.message.setHeader("Message-ID", mmas.messageId) // must be set after message.saveChanges()
            transport.sendMessage(mmas.message, mmas.message.getAllRecipients())
            isSent = true
        } catch (sfe: SendFailedException) {
            val valid: Array<Address?> = sfe.getValidSentAddresses()
            // a soon the mail was send to one reciever we do no longer block it
            if (valid != null && valid.size > 0) isSent = true
            throwable = sfe
        } catch (e: Exception) {
            throwable = e
        } finally {
            try {
                if (recyleConnection) SMTPConnectionPool.releaseSessionAndTransport(mmas.session) else SMTPConnectionPool.disconnect(mmas.session.transport)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            SystemUtil.notify(lock)
        }
    }

    /**
     * @return the messageExpection
     */
    fun getThrowable(): Throwable? {
        return throwable
    }

    init {
        this.lock = lock
        this.mmas = mmas
        this.host = host
        this.port = port
        this.user = user
        this.pass = pass
        recyleConnection = reuseConnection
    }
}