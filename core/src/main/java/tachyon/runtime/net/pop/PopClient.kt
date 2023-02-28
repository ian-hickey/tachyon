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
package tachyon.runtime.net.pop

import javax.mail.Folder

class PopClient(server: String?, port: Int, username: String?, password: String?, secure: Boolean) : MailClient(server, port, username, password, secure) {
    @Override
    @Throws(MessagingException::class)
    protected fun _getId(folder: Folder?, message: Message?): String? {
        return (folder as POP3Folder?).getUID(message)
    }

    @get:Override
    protected val typeAsString: String?
        protected get() = "pop3"

    @get:Override
    protected val type: Int
        protected get() = TYPE_POP3
}